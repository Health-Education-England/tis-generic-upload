package com.transformuk.hee.tis.genericupload.service.service;

import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.ProgrammeMembershipUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.service.service.mapper.ProgrammeMembershipMapper;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;;

@Component
public class ProgrammeMembershipUpdateTransformerService {

  protected static final String PM_ID_IS_DUPLICATE = "Duplicate TIS_ProgrammeMembership_ID: %s.";
  protected static final String UNEXPECTED_ERROR = "Unexpected error occurred";
  public static final String PROGRAMME_MEMBERSHIP_TYPE_NOT_EXISTS =
      "Programme membership type with code %s does not exist.";
  private static final Logger logger = getLogger(ProgrammeMembershipUpdateTransformerService.class);

  private final TcsServiceImpl tcsService;
  private final ProgrammeMembershipMapper pmMapper;

  ProgrammeMembershipUpdateTransformerService(TcsServiceImpl tcsService,
      ProgrammeMembershipMapper pmMapper) {
    this.tcsService = tcsService;
    this.pmMapper = pmMapper;
  }

  public void processProgrammeMembershipsUpdateUpload(List<ProgrammeMembershipUpdateXLS> xlsList) {
    xlsList.forEach(TemplateXLS::initialiseSuccessfullyImported);

    if (xlsList.isEmpty()) {
      return;
    }

    List<ProgrammeMembershipUpdateXLS> filteredList = handleDuplicateIds(xlsList);

    for (ProgrammeMembershipUpdateXLS xls: filteredList) {
      List<String> initialErrMsgs = initialValidate(xls);
      ProgrammeMembershipDTO programmeMembershipDto = pmMapper.toDto(xls);
      programmeMembershipDto.getMessageList().addAll(initialErrMsgs);
      try {
        ProgrammeMembershipDTO patchedProgrammeMembershipDto =
            tcsService.patchProgrammeMembership(programmeMembershipDto);
        List<String> errMsgs = patchedProgrammeMembershipDto.getMessageList();
        if (errMsgs.isEmpty()) {
          xls.setSuccessfullyImported(true);
        } else {
          xls.addErrorMessages(errMsgs);
        }
      } catch (ResourceAccessException rae) {
        xls.addErrorMessage(UNEXPECTED_ERROR);
      }
    }
  }

  List<String> initialValidate(ProgrammeMembershipUpdateXLS xls) {
    List<String> errMsg = new ArrayList<>();

    String programmeMembershipType = xls.getProgrammeMembershipType();
    if (!StringUtils.isEmpty(programmeMembershipType) && !EnumUtils.isValidEnum(
        ProgrammeMembershipType.class, programmeMembershipType)) {
      errMsg.add(String.format(PROGRAMME_MEMBERSHIP_TYPE_NOT_EXISTS, programmeMembershipType));
    }
    return errMsg;
  }

  List<ProgrammeMembershipUpdateXLS> handleDuplicateIds(List<ProgrammeMembershipUpdateXLS> xlsList) {
    List<ProgrammeMembershipUpdateXLS> filteredList = new ArrayList<>();
    // Use a HashMap to store all the numbers of each ProgrammeMembershipUuid
    Map<String, Integer> countOfIds = new HashMap<>();

    for (ProgrammeMembershipUpdateXLS xls : xlsList) {
      String programmeMembershipId = xls.getProgrammeMembershipId();
      if (!countOfIds.containsKey(programmeMembershipId)) {
        countOfIds.put(programmeMembershipId, 1);
      } else {
        countOfIds.put(programmeMembershipId, countOfIds.get(programmeMembershipId) + 1);
      }
    }
    for (ProgrammeMembershipUpdateXLS xls : xlsList) {
      if (countOfIds.get(xls.getProgrammeMembershipId()) > 1) {
        xls.addErrorMessage(String.format(PM_ID_IS_DUPLICATE, xls.getProgrammeMembershipId()));
      } else {
        filteredList.add(xls);
      }
    }
    return filteredList;
  }
}
