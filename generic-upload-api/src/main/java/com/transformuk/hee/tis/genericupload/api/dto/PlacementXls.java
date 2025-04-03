package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PlacementXls extends TemplateXLS implements PlacementSupervisor {

  @ExcelColumn(name = "Surname*", required = true)
  private String surname;

  @ExcelColumn(name = "GMC Number")
  private String gmcNumber;

  @ExcelColumn(name = "GDC Number")
  private String gdcNumber;

  @ExcelColumn(name = "Public Health Number")
  private String publicHealthNumber;

  @ExcelColumn(name = "TIS Person ID")
  private String personId;

  @ExcelColumn(name = "National Post Number*", required = true)
  private String nationalPostNumber;

  @ExcelColumn(name = "Date From*", required = true)
  private Date dateFrom;

  @ExcelColumn(name = "Date To*", required = true)
  private Date dateTo;

  @ExcelColumn(name = "Placement Type*", required = true)
  private String placementType;

  @ExcelColumn(name = "Placement Status*", required = true)
  private String placementStatus;

  @ExcelColumn(name = "Site*", required = true)
  private String site;

  @ExcelColumn(name = "Other Sites")
  private String otherSites;

  @ExcelColumn(name = "WTE*", required = true)
  private Float wte;

  @ExcelColumn(name = "Grade*", required = true)
  private String grade;

  @ExcelColumn(name = "Specialty1*", required = true)
  private String specialty1;

  @ExcelColumn(name = "Specialty2")
  private String specialty2;

  @ExcelColumn(name = "Specialty3")
  private String specialty3;

  @ExcelColumn(name = "Sub specialty")
  private String subSpecialty;

  @ExcelColumn(name = "Clinical Supervisor")
  private String clinicalSupervisor;

  @ExcelColumn(name = "Educational Supervisor")
  private String educationalSupervisor;

  @ExcelColumn(name = "Comments")
  private String comments;
}
