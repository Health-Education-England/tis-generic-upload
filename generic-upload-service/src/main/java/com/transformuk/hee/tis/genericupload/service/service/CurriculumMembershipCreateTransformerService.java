package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXLS;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

@Component
public class CurriculumMembershipCreateTransformerService {

  private static final String DID_NOT_FIND_PROGRAMME_MEMBERSHIP_FOR_ID = "Did not find the programme membership for id \"%s\".";

  @Autowired
  private TcsServiceImpl tcsServiceImpl;

  void processCurriculumMembershipCreateUpload(
      List<CurriculumMembershipCreateXLS> curriculumMembershipCreateXLSS, String username) {
    curriculumMembershipCreateXLSS.forEach(
        CurriculumMembershipCreateXLS::initialiseSuccessfullyImported);

    for (CurriculumMembershipCreateXLS curriculumMembershipCreateXLS : curriculumMembershipCreateXLSS) {
      useMatchingCriteriaToCreateCurriculumMembership(curriculumMembershipCreateXLS, username);
    }
  }

  private void useMatchingCriteriaToCreateCurriculumMembership(
      CurriculumMembershipCreateXLS curriculumMembershipCreateXLS, String username) {
    String tisProgrammeMembershipId = curriculumMembershipCreateXLS.getTisProgrammeMembershipId();
    if (!StringUtils.isEmpty(tisProgrammeMembershipId)) {
      try {
        ProgrammeMembershipDTO programmeMembershipDTO = tcsServiceImpl.getProgrammeMembershipByUuid(
            UUID.fromString(tisProgrammeMembershipId));
        if (programmeMembershipDTO != null) {
          //need to call the create curriculum membership method here
        } else {
          curriculumMembershipCreateXLS.addErrorMessage(
              String.format(DID_NOT_FIND_PROGRAMME_MEMBERSHIP_FOR_ID, tisProgrammeMembershipId));
        }
      } catch (ResourceAccessException e) {
        new ErrorHandler().recordErrorMessageOnTemplateOrLogUnknown(curriculumMembershipCreateXLS,
            e);
      }
    }
  }
}
