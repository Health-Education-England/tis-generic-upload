package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FundingUpdateXLS extends TemplateXLS {

  @ExcelColumn(name = "TIS_PostFunding_ID*", required = true)
  private String postFundingTisId;

  @ExcelColumn(name = "TIS_Post_ID*", required = true)
  private String postTisId;

  @ExcelColumn(name = "Funding type")
  private String fundingType;

  @ExcelColumn(name = "Funding Details")
  private String fundingTypeOther;

  @ExcelColumn(name = "Funding Body")
  private String fundingBody;

  @ExcelColumn(name = "Funding subtype")
  private String fundingSubtype;

  @ExcelColumn(name = "Funding Reason")
  private String fundingReason;
  @ExcelColumn(name = "Start Date*", required = true)
  private Date dateFrom;

  @ExcelColumn(name = "End Date")
  private Date dateTo;
}
