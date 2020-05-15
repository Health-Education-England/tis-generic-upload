package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PlacementDeleteXLS extends TemplateXLS {

  @ExcelColumn(name = "Placement Id*", required = true)
  private String placementId;

  @ExcelColumn(name = "Placement Status*", required = true)
  private String placementStatus;
}
