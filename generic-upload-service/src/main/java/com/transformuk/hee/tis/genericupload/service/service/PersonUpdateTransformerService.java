package com.transformuk.hee.tis.genericupload.service.service;

import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.service.service.mapper.PersonMapper;
import com.transformuk.hee.tis.genericupload.service.service.mapper.TrainerApprovalMapper;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.TrainerApprovalDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ApprovalStatus;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class PersonUpdateTransformerService {

  private static final Logger logger = getLogger(PersonUpdateTransformerService.class);

  public static final String PERSON_ID_DUPLICATE = "Duplicate Tis_Person_ID: %s.";
  public static final String TRAINER_APPROVAL_STATUS_NOT_EXISTS = "Trainer Approval Status '%s' does not exist.";
  public static final String ROLE_ERROR_SEPARATOR = "Role '%s' should not use ',' as a separator, please use ';' instead.";
  public static final String PERSON_ID_MUST_BE_VALID =
      "Tis_Person_ID (%s) is invalid."
          + "It should be a number and not contain blank space or special characters.";
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
    xlsList.forEach(update -> update.setTisPersonId(update.getTisPersonId().trim()));

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
      }

      // Handle validation of id, enumerations and role and set errors
      initialValidate(xls);

      if (xls.hasErrors()) {
        // Do not send to TCS to process
        continue;
      }

      PersonDTO personDto = personMapper.toDto(xls);

      if (xls.getTrainerApprovalStartDate() != null || xls.getTrainerApprovalEndDate() != null
          || !StringUtils.isEmpty(xls.getTrainerApprovalStatus())) {
        TrainerApprovalDTO trainerApprovalDto = trainerApprovalMapper.toDto(xls);
        personDto.setTrainerApprovals(Collections.singleton(trainerApprovalDto));
      }

      personIdToXls.put(personDto.getId(), xls);
      personDtos.add(personDto);
    }

    if (personDtos.isEmpty()) {
      return;
    }

    try {
      logger.info("Sending bulk people update to TCS service: recordCount={}", personDtos.size());
      List<PersonDTO> patchedPersonDtos = tcsService.patchPeople(personDtos);
      logger.info("Bulk people update response received from TCS: recordCount={}", patchedPersonDtos.size());

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

  /**
   * Validate fields that can not be handled in TCS and record error messages.
   *
   * @param xls PersonUpdateXls to be validated
   */
  private void initialValidate(PersonUpdateXls xls) {

    List<String> errorMessages = new ArrayList<>();

    String trainerApprovalStatus = xls.getTrainerApprovalStatus();
    if (!StringUtils.isEmpty(trainerApprovalStatus) && !EnumUtils.isValidEnum(
        ApprovalStatus.class, trainerApprovalStatus)) {
      errorMessages.add(String.format(TRAINER_APPROVAL_STATUS_NOT_EXISTS, trainerApprovalStatus));
    }
    String role = xls.getRole();
    if (StringUtils.contains(role, ',')) {
      errorMessages.add(String.format(ROLE_ERROR_SEPARATOR, role));
    }

    try {
      Long.parseLong(xls.getTisPersonId());
    } catch (NumberFormatException e) {
      errorMessages.add(
          String.format(PERSON_ID_MUST_BE_VALID, xls.getTisPersonId()));
    }
    xls.addErrorMessages(errorMessages);
  }
}
