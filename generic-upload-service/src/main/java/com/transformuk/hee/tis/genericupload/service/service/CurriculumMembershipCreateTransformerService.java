package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXLS;
import com.transformuk.hee.tis.genericupload.service.service.mapper.CurriculumMembershipMapper;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Component
public class CurriculumMembershipCreateTransformerService {

  protected static final String PM_ID_NOT_UUID = "Programme membership ID is not a valid UUID.";
  protected static final String CURRICULUM_NOT_FOUND = "Curriculum not found for name \"%s\".";
  protected static final String MULTIPLE_CURRICULA_FOUND =
      "Multiple curricula found for name \"%s\"";
  private final TcsServiceImpl tcsService;
  private final CurriculumMembershipMapper cmMapper;

  /**
   * Initialise CurriculumMembershipCreateTransformerService.
   *
   * @param tcsService the tcs service client
   * @param cmMapper   the mapper to map xls to dto
   */
  public CurriculumMembershipCreateTransformerService(TcsServiceImpl tcsService,
      CurriculumMembershipMapper cmMapper) {
    this.tcsService = tcsService;
    this.cmMapper = cmMapper;
  }

  /**
   * Validate some data from Excel and send pass request to TCS, then handle the response.
   *
   * @param cmXlsList The Xls list from the Excel that user input
   */
  public void processCurriculumMembershipCreateUpload(
      List<CurriculumMembershipCreateXLS> cmXlsList) {
    cmXlsList.forEach(
        xls -> {
          xls.initialiseSuccessfullyImported();
          CurriculumMembershipDTO cmDto;
          try {
            cmDto = cmMapper.toDto(xls);
          } catch (IllegalArgumentException e) {
            xls.addErrorMessage(PM_ID_NOT_UUID);
            return;
          }
          Long curriculumId = checkCurriculum(xls);
          if (curriculumId != null) {
            cmDto.setCurriculumId(curriculumId);
          }

          if (!xls.hasErrors()) {
            try {
              tcsService.createCurriculumMembership(cmDto);
              xls.setSuccessfullyImported(true);
            } catch (ResourceAccessException rae) {
              new ErrorHandler().recordErrorMessageOnTemplateOrLogUnknown(xls, rae);
            }
          }
        });
  }

  private Long checkCurriculum(CurriculumMembershipCreateXLS xls) {
    String curriculumName = xls.getCurriculumName();

    List<CurriculumDTO> curriculumDtos = tcsService.getCurriculaByName(curriculumName);
    if (curriculumDtos.isEmpty()) {
      xls.addErrorMessage(String.format(CURRICULUM_NOT_FOUND, curriculumName));
    } else if (curriculumDtos.size() > 1) {
      xls.addErrorMessage(String.format(MULTIPLE_CURRICULA_FOUND, curriculumName));
    } else {
      return curriculumDtos.get(0).getId();
    }
    return null;
  }
}
