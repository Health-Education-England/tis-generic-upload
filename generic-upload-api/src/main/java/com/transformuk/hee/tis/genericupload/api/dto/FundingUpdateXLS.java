package com.transformuk.hee.tis.genericupload.api.dto;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FundingUpdateXLS extends TemplateXLS {

  private String postFundingTisId;
  private String fundingType;
  private String fundingTypeOther;
  private String fundingBody;
  private Date dateFrom;
  private Date dateTo;
}
