package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PostFundingUpdateXLS extends TemplateXLS {

  @ExcelColumn(name = "TIS_Post_ID*", required = true)
  private String postTisId;

  @ExcelColumn(name = "Funding type")
  private String fundingType;

  @ExcelColumn(name = "Funding type - If 'Other' please specify")
  private String fundingTypeOther;

  @ExcelColumn(name = "Funding Body")
  private String fundingBody;

  @ExcelColumn(name = "Date From")
  private LocalDate dateFrom;

  @ExcelColumn(name = "Date to")
  private LocalDate dateTo;
}
