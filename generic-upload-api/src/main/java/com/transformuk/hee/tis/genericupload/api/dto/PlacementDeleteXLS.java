package com.transformuk.hee.tis.genericupload.api.dto;

import lombok.Data;

@Data
public class PlacementDeleteXLS extends TemplateXLS {

  private String placementId;
  private String placementStatus;
}
