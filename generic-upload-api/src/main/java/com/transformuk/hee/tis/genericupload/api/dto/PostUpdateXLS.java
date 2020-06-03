package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PostUpdateXLS extends TemplateXLS {

  @ExcelColumn(name = "TIS_Post_ID*", required = true)
  private String postTISId;

  @ExcelColumn(name = "Approved grade")
  private String approvedGrade;

  @ExcelColumn(name = "Other grades")
  private String otherGrades;

  @ExcelColumn(name = "Specialty")
  private String specialty;

  @ExcelColumn(name = "Other specialties")
  private String otherSpecialties;

  @ExcelColumn(name = "Sub specialties")
  private String subSpecialties;

  @ExcelColumn(name = "Training description")
  private String trainingDescription;

  @ExcelColumn(name = "Main site")
  private String mainSite;

  @ExcelColumn(name = "Other sites")
  private String otherSites;

  @ExcelColumn(name = "Training body")
  private String trainingBody;

  @ExcelColumn(name = "Employing body")
  private String employingBody;

  @ExcelColumn(name = "TIS_Programme_ID*", required = true)
  private String programmeTisId;

  @ExcelColumn(name = "Owner")
  private String owner;

  @ExcelColumn(name = "Rotations")
  private String rotations;

  @ExcelColumn(name = "Status")
  private String status;

  @ExcelColumn(name = "Old Post")
  private String oldPost;
}
