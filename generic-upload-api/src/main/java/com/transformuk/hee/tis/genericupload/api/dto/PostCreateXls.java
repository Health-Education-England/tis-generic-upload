package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PostCreateXls extends TemplateXLS {

  @ExcelColumn(name = "National Post Number*", required = true)
  private String nationalPostNumber;

  @ExcelColumn(name = "Approved grade*", required = true)
  private String approvedGrade;

  @ExcelColumn(name = "Other grades")
  private String otherGrades;

  @ExcelColumn(name = "Specialty*", required = true)
  private String specialty;

  @ExcelColumn(name = "Other specialties")
  private String otherSpecialties;

  @ExcelColumn(name = "Sub specialties")
  private String subSpecialties;

  @ExcelColumn(name = "Training description")
  private String trainingDescription;

  @ExcelColumn(name = "Main site (Known as)*", required = true)
  private String mainSite;

  @ExcelColumn(name = "Other sites (Known as)")
  private String otherSites;

  @ExcelColumn(name = "Training body*", required = true)
  private String trainingBody;

  @ExcelColumn(name = "Employing body*", required = true)
  private String employingBody;

  @ExcelColumn(name = "TIS_Programme_ID*", required = true)
  private String programmeTisId;

  @ExcelColumn(name = "Owner*", required = true)
  private String owner;

  @ExcelColumn(name = "Funding Type*", required = true)
  private String fundingType;

  @ExcelColumn(name = "Funding Start Date*", required = true)
  private Date fundingStartDate;

  @ExcelColumn(name = "Funding End Date")
  private Date fundingEndDate;

  @ExcelColumn(name = "Funding Body")
  private String fundingBody;

  @ExcelColumn(name = "Funding Details")
  private String fundingDetails;

  @ExcelColumn(name = "Funding Subtype")
  private String fundingSubtype;

  @ExcelColumn(name = "Old Post")
  private String oldPost;
}
