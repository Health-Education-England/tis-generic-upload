package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostFundingUpdateRow extends PostFundingAbstractRow {

  @ExcelColumn(name = "TIS_PostFunding_ID*", required = true)
  private String postFundingTisId;
}
