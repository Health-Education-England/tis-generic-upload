package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.service.service.mapper.PersonMapper;
import com.transformuk.hee.tis.genericupload.service.service.mapper.TrainerApprovalMapper;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.TrainerApprovalDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class PersonUpdateTransformerService {

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

    // TODO: handle exclusion of duplicate ids.
    for (PersonUpdateXls xls : xlsList) {
      TrainerApprovalDTO trainerApprovalDto = trainerApprovalMapper.toDto(xls);
      PersonDTO personDto = personMapper.toDto(xls);
      personDto.setTrainerApprovals(Collections.singleton(trainerApprovalDto));

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
}
