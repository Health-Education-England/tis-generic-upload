package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CurriculumMembershipCreateXLS extends TemplateXLS {
  @ExcelColumn(name = "TIS_ProgrammeMembership_ID*", required = true)
  private String tisProgrammeMembershipId;
  @ExcelColumn(name = "Curriculum Start Date*", required = true)
  private LocalDate curriculumStartDate;

  @ExcelColumn(name = "Curriculum End Date*", required = true)
  private LocalDate curriculumEndDate;

  @ExcelColumn(name = "Curriculum Name*", required = true)
  private String curriculumName;
}
