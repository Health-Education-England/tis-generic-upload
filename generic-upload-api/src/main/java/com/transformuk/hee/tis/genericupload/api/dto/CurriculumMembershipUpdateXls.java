package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CurriculumMembershipUpdateXls extends TemplateXLS {

  @ExcelColumn(name = "TIS_CurriculumMembership_ID*", required = true)
  private String tisCurriculumMembershipId;

  @ExcelColumn(name = "TIS_ProgrammeMembership_ID*", required = true)
  private String tisProgrammeMembershipId;

  @ExcelColumn(name = "Curriculum Start Date")
  private LocalDate curriculumStartDate;

  @ExcelColumn(name = "Curriculum End Date")
  private LocalDate curriculumEndDate;
}
