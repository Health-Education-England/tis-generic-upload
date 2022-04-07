package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.service.service.mapper.PersonMapper;
import com.transformuk.hee.tis.genericupload.service.service.mapper.TrainerApprovalMapper;
import com.transformuk.hee.tis.tcs.api.dto.*;
import com.transformuk.hee.tis.tcs.api.enumeration.ApprovalStatus;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

import java.util.*;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;

@Component
public class PersonUpdateTransformerService {

  public static final String PERSON_ID_DUPLICATE = "Duplicate Tis_Person_ID: %s.";
  public static final String TRAINER_APPROVAL_STATUS_NOT_EXISTS = "Trainer Approval Status '%s' does not exist.";
  public static final String ROLE_ERROR_SEPARATOR = "Role '%s' should not use ',' as a separator, please use ';' instead.";
  private static final String VISA_DATES_VALIDATION_ERROR  = "'Visa issued' date must be before " +
          "'Visa valid to' date.";
  private final TcsServiceImpl tcsService;
  private final PersonMapper personMapper;
  private final TrainerApprovalMapper trainerApprovalMapper;

  PersonUpdateTransformerService(TcsServiceImpl tcsService, PersonMapper personMapper,
      TrainerApprovalMapper trainerApprovalMapper) {
    this.tcsService = tcsService;
    this.personMapper = personMapper;
    this.trainerApprovalMapper = trainerApprovalMapper;
  }

  public void processUpload(List<PersonUpdateXls> xlsList) {
    xlsList.forEach(TemplateXLS::initialiseSuccessfullyImported);

    Map<Long, PersonUpdateXls> personIdToXls = new HashMap<>();
    List<PersonDTO> personDtos = new ArrayList<>();

    // Use a HashMap to store all the numbers of personIds
    HashMap<String, Integer> numberOfIds = new HashMap<>();
    for (PersonUpdateXls xls : xlsList) {
      String personId = xls.getTisPersonId();
      if (!numberOfIds.containsKey(personId)) {
        numberOfIds.put(personId, 1);
      } else {
        numberOfIds.put(personId, numberOfIds.get(personId) + 1);
      }
    }

    for (PersonUpdateXls xls : xlsList) {
      // Handle exclusion of duplicate ids
      if (numberOfIds.get(xls.getTisPersonId()) > 1) {
        xls.addErrorMessage(String.format(PERSON_ID_DUPLICATE, xls.getTisPersonId()));
        continue;
      }

      // Handle validation of enumerations and role.
      List<String> initialErrorMessages = initialValidate(xls);

      PersonDTO personDto = personMapper.toDto(xls);

      if (xls.getTrainerApprovalStartDate() != null || xls.getTrainerApprovalEndDate() != null
          || !StringUtils.isEmpty(xls.getTrainerApprovalStatus())) {
        TrainerApprovalDTO trainerApprovalDto = trainerApprovalMapper.toDto(xls);
        personDto.setTrainerApprovals(Collections.singleton(trainerApprovalDto));
      }

      if (!initialErrorMessages.isEmpty()) {
        personDto.getMessageList().addAll(initialErrorMessages);
      }
      personIdToXls.put(personDto.getId(), xls);
      personDtos.add(personDto);
    }

    if (personDtos.isEmpty()) {
      return;
    }

    try {
      List<PersonDTO> patchedPersonDtos = tcsService.patchPeople(personDtos);

      for (PersonDTO patchedPersonDto : patchedPersonDtos) {
        // Get the source XLS for the DTO and add error messages or success.
        PersonUpdateXls xls = personIdToXls.get(patchedPersonDto.getId());
        List<String> errorMessages = patchedPersonDto.getMessageList();

        if (errorMessages.isEmpty()) {
          xls.setSuccessfullyImported(true);
        } else {
          xls.addErrorMessages(errorMessages);
        }
      }
    } catch (RestClientException e) {
      for (PersonUpdateXls xls : personIdToXls.values()) {
        xls.addErrorMessage(e.getMessage());
      }
    }
  }

  public void validateDates(RightToWorkDTO dbRightToWorkDTO,
                            PersonUpdateXls personUpdateXls) {

    System.out.println("Old Date valid to: " + dbRightToWorkDTO.getVisaValidTo());
    System.out.println("Old Date Issued: " + dbRightToWorkDTO.getVisaIssued());

    System.out.println("Date valid to: " + personUpdateXls.getVisaValidTo());
    System.out.println("Date Issued: " + personUpdateXls.getVisaIssued());

    boolean dateError = true;
    personUpdateXls.addErrorMessage(VISA_DATES_VALIDATION_ERROR);
    if (personUpdateXls.getVisaIssued() != null && personUpdateXls.getVisaValidTo() != null) {
      System.out.println("first loop");
      if (personUpdateXls.getVisaIssued().isBefore(personUpdateXls.getVisaValidTo())) {
        System.out.println("both avaliable");
        dbRightToWorkDTO.setVisaIssued(personUpdateXls.getVisaIssued());
        dbRightToWorkDTO.setVisaValidTo(personUpdateXls.getVisaValidTo());
        dateError = false;
      }
    } else if (personUpdateXls.getVisaIssued() != null && personUpdateXls.getVisaValidTo() == null) {
      System.out.println("second loop");
      if (personUpdateXls.getVisaIssued().isBefore(dbRightToWorkDTO.getVisaValidTo())) {
        System.out.println("visa issued available");
        dbRightToWorkDTO.setVisaIssued(personUpdateXls.getVisaIssued());
        dateError = false;
      }
    } else if (personUpdateXls.getVisaValidTo() != null && personUpdateXls.getVisaIssued() == null) {
      System.out.println("third loop");
      if (personUpdateXls.getVisaValidTo().isAfter(dbRightToWorkDTO.getVisaIssued())) {
        System.out.println("visa valid to available");
        dbRightToWorkDTO.setVisaValidTo(personUpdateXls.getVisaValidTo());
        dateError = false;
      }
    } else if (personUpdateXls.getVisaIssued() == null && personUpdateXls.getVisaValidTo() == null) {
      System.out.println("both unavailable");
      dateError = false;
    }
    if (dateError) {
      personUpdateXls.addErrorMessage(VISA_DATES_VALIDATION_ERROR);
    }
  }

  /**
   * Those validation can not be handled in TCS.
   *
   * @param xls PersonUpdateXls to be validated
   * @return errorMessages list
   */
  List<String> initialValidate(PersonUpdateXls xls) {

    List<String> errorMessages = new ArrayList<>();

    RightToWorkDTO rightToWorkDTO = tcsService.getPerson(xls.getTisPersonId()).getRightToWork();

    validateDates(rightToWorkDTO, xls);

    String trainerApprovalStatus = xls.getTrainerApprovalStatus();
    if (!StringUtils.isEmpty(trainerApprovalStatus) && !EnumUtils.isValidEnum(
        ApprovalStatus.class, trainerApprovalStatus)) {
      errorMessages.add(String.format(TRAINER_APPROVAL_STATUS_NOT_EXISTS, trainerApprovalStatus));
    }
    String role = xls.getRole();
    if (StringUtils.contains(role, ',')) {
      errorMessages.add(String.format(ROLE_ERROR_SEPARATOR, role));
    }
    return errorMessages;
  }
}
