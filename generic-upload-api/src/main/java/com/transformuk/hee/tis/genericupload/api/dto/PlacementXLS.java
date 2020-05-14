package com.transformuk.hee.tis.genericupload.api.dto;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PlacementXLS extends TemplateXLS implements PlacementSupervisor {

  private String surname;
  private String gmcNumber;
  private String gdcNumber;
  private String publicHealthNumber;
  private String nationalPostNumber;
  private Date dateFrom;
  private Date dateTo;
  private String placementType;
  private String placementStatus;
  private String site;
  private String otherSites;
  private Float wte;
  private String grade;
  private String specialty1;
  private String specialty2;
  private String specialty3;
  private String clinicalSupervisor;
  private String educationalSupervisor;
  private String comments;
}
