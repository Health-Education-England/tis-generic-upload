package com.transformuk.hee.tis.genericupload.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PlacementDeleteXLS extends TemplateXLS {

  private String placementId;
  private String placementStatus;
}
