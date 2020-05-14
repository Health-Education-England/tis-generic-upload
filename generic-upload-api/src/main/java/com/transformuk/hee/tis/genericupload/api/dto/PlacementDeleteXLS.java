package com.transformuk.hee.tis.genericupload.api.dto;

import lombok.Data;

@Data
public class PlacementDeleteXLS extends TemplateXLS {

  private String placementId;
  private String placementStatus;

  public String getPlacementId() {
    return placementId;
  }

  public void setPlacementId(String placementId) {
    this.placementId = placementId;
  }

  public String getPlacementStatus() {
    return placementStatus;
  }

  public void setPlacementStatus(String placementStatus) {
    this.placementStatus = placementStatus;
  }
}
