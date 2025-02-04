package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.service.service.mapper.CurriculumMembershipMapper;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Component
public class CurriculumMembershipUpdateTransformerService {

  protected static final String PM_ID_NOT_UUID = "Programme membership ID is not a valid UUID.";
  protected static final String CM_ID_NOT_NUMBER = "Curriculum membership is not numerical";
  private final TcsServiceImpl tcsService;
  private final CurriculumMembershipMapper cmMapper;

  /**
   * Initialise CurriculumMembershipCreateTransformerService.
   *
   * @param tcsService the tcs service client
   * @param cmMapper   the mapper to map xls to dto
   */
  public CurriculumMembershipUpdateTransformerService(TcsServiceImpl tcsService,
      CurriculumMembershipMapper cmMapper) {
    this.tcsService = tcsService;
    this.cmMapper = cmMapper;
  }

  /**
   * Validate some data from Excel and send pass request to TCS, then handle the response.
   *
   * @param cmXlsList The Xls list from the Excel that user input
   */
  public void processCurriculumMembershipUpdateUpload(
      List<CurriculumMembershipUpdateXls> cmXlsList) {
    if (cmXlsList.isEmpty()) {
      return;
    }
    cmXlsList.forEach(
        xls -> {
          xls.initialiseSuccessfullyImported();
          CurriculumMembershipDTO cmDto;
          try {
            Long.parseLong(xls.getTisCurriculumMembershipId());
            cmDto = cmMapper.toDto(xls);
          } catch (NumberFormatException nfe) {
            xls.addErrorMessage(CM_ID_NOT_NUMBER);
            return;
          } catch (IllegalArgumentException e) {
            xls.addErrorMessage(PM_ID_NOT_UUID);
            return;
          }

          // Check if both startDate and endDate are empty
          if (xls.getCurriculumStartDate() == null && xls.getCurriculumEndDate() == null) {
            xls.addErrorMessage("Start date and end date cannot both be empty.");
            return;
          }

          if (!xls.hasErrors()) {
            try {
              tcsService.patchCurriculumMembership(cmDto);
              xls.setSuccessfullyImported(true);
            } catch (ResourceAccessException rae) {
              new ErrorHandler().recordErrorMessageOnTemplateOrLogUnknown(xls, rae);
            }
          }
        });
  }
}
