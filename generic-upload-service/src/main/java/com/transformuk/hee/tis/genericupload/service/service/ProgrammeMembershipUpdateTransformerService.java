package com.transformuk.hee.tis.genericupload.service.service;

import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.ProgrammeMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.service.service.mapper.ProgrammeMembershipMapper;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

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

  /**
   * Validate some data from Excel and send path request to TCS, then handle the response.
   *
   * @param xlsList The Xls list from the Excel user input
   */
  public void processProgrammeMembershipsUpdateUpload(
      List<ProgrammeMembershipUpdateXls> xlsList) {

    xlsList.forEach(TemplateXLS::initialiseSuccessfullyImported);

    if (xlsList.isEmpty()) {
      return;
    }

    List<ProgrammeMembershipUpdateXls> filteredList = handleDuplicateIds(xlsList);

    for (ProgrammeMembershipUpdateXls xls : filteredList) {
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
        logger.error(UNEXPECTED_ERROR, rae);
        xls.addErrorMessage(UNEXPECTED_ERROR);
      }
    }
  }

  List<String> initialValidate(ProgrammeMembershipUpdateXls xls) {
    List<String> errMsg = new ArrayList<>();

    String programmeMembershipType = xls.getProgrammeMembershipType();
    if (!StringUtils.isEmpty(programmeMembershipType) && !EnumUtils.isValidEnum(
        ProgrammeMembershipType.class, programmeMembershipType)) {
      errMsg.add(String.format(PROGRAMME_MEMBERSHIP_TYPE_NOT_EXISTS, programmeMembershipType));
    }
    return errMsg;
  }

  List<ProgrammeMembershipUpdateXls> handleDuplicateIds(
      List<ProgrammeMembershipUpdateXls> xlsList) {

    List<ProgrammeMembershipUpdateXls> filteredList = new ArrayList<>();

    xlsList.stream()
        .collect(Collectors.groupingBy(ProgrammeMembershipUpdateXls::getProgrammeMembershipId))
        .forEach((id, pms) -> {
          // ProgrammeMembershipId is a mandatory field so pms.size <1 is left undefined.
          if (pms.size() == 1) {
            filteredList.add(pms.get(0));
          } else {
            pms.forEach(pm -> pm.addErrorMessage(String.format(PM_ID_IS_DUPLICATE, id)));
          }
        });
    return filteredList;
  }
}
