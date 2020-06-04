package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.service.service.mapper.PersonMapper;
import com.transformuk.hee.tis.genericupload.service.service.mapper.TrainerApprovalMapper;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.TrainerApprovalDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ApprovalStatus;
import com.transformuk.hee.tis.tcs.api.enumeration.PermitToWorkType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class PersonUpdateTransformerService {

  public static final String PERSON_ID_DUPLICATE = "Duplicate Tis_Person_ID: %s.";
  public static final String PERMIT_TO_WORK_NOT_EXISTS = "Permit to work '%s' does not exist.";
  public static final String TRAINER_APPROVAL_STATUS_NOT_EXISTS = "Trainer Approval Status '%s' does not exist.";
  public static final String ROLE_ERROR_SEPARATOR = "Role '%s' should not use ',' as a separator, please use ';' instead.";
  private static final Logger LOGGER =
      LoggerFactory.getLogger(PersonUpdateTransformerService.class);
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

    // Create a HashMap to store all the numbers of personIds
    HashMap<String, Integer> numberOfIds = new HashMap<String, Integer>();
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
      List<String> initialErrorMessages = new ArrayList<>();
      initialValidate(xls, initialErrorMessages);

      PersonDTO personDto = personMapper.toDto(xls);

      if (xls.getTrainerApprovalStartDate() != null || xls.getTrainerApprovalEndDate() != null
          || !StringUtils.isEmpty(xls.getTrainerApprovalStatus())) {
        TrainerApprovalDTO trainerApprovalDto = trainerApprovalMapper.toDto(xls);
        personDto.setTrainerApprovals(Collections.singleton(trainerApprovalDto));
      }

      if (initialErrorMessages.size() != 0) {
        personDto.getMessageList().addAll(initialErrorMessages);
      }
      personIdToXls.put(personDto.getId(), xls);
      personDtos.add(personDto);
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

  /**
   * Those validation can not be handled in TCS
   *
   * @param xls           the current row object of the xls
   * @param errorMessages list to add error messages
   */
  private void initialValidate(PersonUpdateXls xls, List<String> errorMessages) {

    String permitToWork = xls.getPermitToWork();
    if (!StringUtils.isEmpty(permitToWork)) {
      PermitToWorkType permitToWorkType = PermitToWorkType.fromString(permitToWork);
      if (permitToWorkType == null) {
        errorMessages.add(String.format(PERMIT_TO_WORK_NOT_EXISTS, permitToWork));
      }
    }
    String trainerApprovalStatus = xls.getTrainerApprovalStatus();
    if (!StringUtils.isEmpty(trainerApprovalStatus) && !EnumUtils.isValidEnum(
        ApprovalStatus.class, trainerApprovalStatus)) {
      errorMessages.add(String.format(TRAINER_APPROVAL_STATUS_NOT_EXISTS, trainerApprovalStatus));
    }
    String role = xls.getRole();
    if (StringUtils.contains(role, ',')) {
      errorMessages.add(String.format(ROLE_ERROR_SEPARATOR, role));
    }
  }
}
