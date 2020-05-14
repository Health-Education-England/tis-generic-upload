package com.transformuk.hee.tis.genericupload.api.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class PostFundingUpdateXLS extends TemplateXLS {

  private String postTisId;
  private String fundingType;
  private String fundingTypeOther;
  private String fundingBody;
  private LocalDate dateFrom;
  private LocalDate dateTo;
}
