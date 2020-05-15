package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PlacementUpdateXLS extends TemplateXLS implements PlacementSupervisor {

  @ExcelColumn(name = "TIS_Placement_ID*", required = true)
  private String placementId;

  @ExcelColumn(name = "Intrepid_Placement_ID")
  private String intrepidId;

  @ExcelColumn(name = "National Post Number")
  private String nationalPostNumber;

  @ExcelColumn(name = "Date From")
  private Date dateFrom;

  @ExcelColumn(name = "Date To")
  private Date dateTo;

  @ExcelColumn(name = "Placement Type")
  private String placementType;

  @ExcelColumn(name = "Site")
  private String site;

  @ExcelColumn(name = "Other Sites")
  private String otherSites;

  @ExcelColumn(name = "WTE")
  private Float wte;

  @ExcelColumn(name = "Grade")
  private String grade;

  @ExcelColumn(name = "Specialty1")
  private String specialty1;

  @ExcelColumn(name = "Specialty2")
  private String specialty2;

  @ExcelColumn(name = "Specialty3")
  private String specialty3;

  @ExcelColumn(name = "Clinical Supervisor")
  private String clinicalSupervisor;

  @ExcelColumn(name = "Educational Supervisor")
  private String educationalSupervisor;

  @ExcelColumn(name = "Comments")
  private String comments;
}
