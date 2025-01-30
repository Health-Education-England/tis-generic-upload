package com.transformuk.hee.tis.genericupload.api.enumeration;

import com.transformuk.hee.tis.genericupload.api.dto.AssessmentDeleteXLS;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentXLS;
import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXls;
import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.FundingUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementDeleteXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PostCreateXls;
import com.transformuk.hee.tis.genericupload.api.dto.PostFundingUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.ProgrammeMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import lombok.Getter;

public enum FileType {
  ASSESSMENTS(AssessmentXLS.class),
  ASSESSMENTS_DELETE(AssessmentDeleteXLS.class),
  FUNDING_UPDATE(FundingUpdateXLS.class),
  PEOPLE(PersonXLS.class),
  PEOPLE_UPDATE(PersonUpdateXls.class),
  PLACEMENTS(PlacementXLS.class),
  PLACEMENTS_DELETE(PlacementDeleteXLS.class),
  PLACEMENTS_UPDATE(PlacementUpdateXLS.class),
  POSTS_CREATE(PostCreateXls.class),
  POSTS_UPDATE(PostUpdateXLS.class),
  POSTS_FUNDING_UPDATE(PostFundingUpdateXLS.class),
  ASSESSMENTS_UPDATE(AssessmentUpdateXLS.class),
  PROGRAMME_MEMBERSHIP_UPDATE(ProgrammeMembershipUpdateXls.class),
  CURRICULUM_MEMBERSHIP_CREATE(CurriculumMembershipCreateXls.class),
  CURRICULUM_MEMBERSHIP_UPDATE(CurriculumMembershipUpdateXls.class);

  /**
   * The class of the XLS DTO related to this FileType.
   *
   * @return The associated class.
   */
  @Getter
  private final Class<? extends TemplateXLS> dtoClass;

  FileType(Class<? extends TemplateXLS> dtoClass) {
    this.dtoClass = dtoClass;
  }
}
