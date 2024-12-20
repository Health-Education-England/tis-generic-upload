package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXLS;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

@Component
public class CurriculumMembershipCreateTransformerService {

  private static final String NO_PROGRAMME_MEMBERSHIP_FOR_ID = "Could not find the programme membership for id \"%s\".";

  private static final String EMPTY_PROGRAMME_MEMBERSHIP_ID_FIELD = "Programme Membership Id field cannot be null or empty";

  private static final String EMPTY_CURRICULUM_NAME_FIELD = "Curriculum Name field cannot be null or empty";

  private static final String EMPTY_CURRICULUM_START_DATE_FIELD = "Curriculum Start Date field cannot be null or empty";

  private static final String EMPTY_CURRICULUM_END_DATE_FIELD = "Curriculum End Date field cannot be null or empty";

  private TcsServiceImpl tcsService;

  private CurriculumMembershipCreateTransformerService(TcsServiceImpl tcsService) {
    this.tcsService = tcsService;
  }

  void processCurriculumMembershipCreateUpload(
      List<CurriculumMembershipCreateXLS> upload) {
    upload.forEach(
        cm -> {
          cm.initialiseSuccessfullyImported();
          boolean valid = validateUpload(cm);
          ProgrammeMembershipDTO pm = null;
          if (valid) {
            pm = checkAndValidateExistingPmId(cm);
          }
          if (pm != null) {
            cm.setSuccessfullyImported(false);
            return;
          }
          createcurriculumMembershipXls(cm);
          cm.setSuccessfullyImported(true);
        });
  }

  private boolean validateUpload(
      CurriculumMembershipCreateXLS curriculumMembershipXls) {
    if (curriculumMembershipXls.getTisProgrammeMembershipId() == null
        || curriculumMembershipXls.getTisProgrammeMembershipId().trim().isEmpty()) {
      curriculumMembershipXls.addErrorMessage(EMPTY_PROGRAMME_MEMBERSHIP_ID_FIELD);
      return false;
    }
    if (curriculumMembershipXls.getCurriculumName() == null
        || curriculumMembershipXls.getCurriculumName().trim().isEmpty()) {
      curriculumMembershipXls.addErrorMessage(EMPTY_CURRICULUM_NAME_FIELD);
      return false;
    }
    if (curriculumMembershipXls.getCurriculumStartDate() == null) {
      curriculumMembershipXls.addErrorMessage(EMPTY_CURRICULUM_START_DATE_FIELD);
      return false;
    }
    if (curriculumMembershipXls.getCurriculumEndDate() == null) {
      curriculumMembershipXls.addErrorMessage(EMPTY_CURRICULUM_END_DATE_FIELD);
      return false;
    }
    return true;
  }

  private ProgrammeMembershipDTO checkAndValidateExistingPmId(
      CurriculumMembershipCreateXLS curriculumMembershipXls) {
    try {
      ProgrammeMembershipDTO programmeMembership = tcsService.getProgrammeMembershipByUuid(
          UUID.fromString(curriculumMembershipXls.getTisProgrammeMembershipId()));
      if (programmeMembership == null) {
        curriculumMembershipXls.addErrorMessage(NO_PROGRAMME_MEMBERSHIP_FOR_ID);
      }
      return programmeMembership;
    } catch (Exception e) {
      curriculumMembershipXls.addErrorMessage(e.getMessage());
      return null;
    }
  }

  private void createcurriculumMembershipXls(
      CurriculumMembershipCreateXLS curriculumMembershipXls) {
    CurriculumMembershipDTO curriculumMembershipDTO = new CurriculumMembershipDTO();
    curriculumMembershipDTO.setCurriculumId();
    curriculumMembershipDTO.setCurriculumStartDate();
    curriculumMembershipDTO.setCurriculumEndDate();
  }
}
