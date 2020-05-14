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

  public String getPostTisId() {
    return postTisId;
  }

  public void setPostTisId(String postTisId) {
    this.postTisId = postTisId;
  }

  public String getFundingType() {
    return fundingType;
  }

  public void setFundingType(String fundingType) {
    this.fundingType = fundingType;
  }

  public String getFundingTypeOther() {
    return fundingTypeOther;
  }

  public void setFundingTypeOther(String fundingTypeOther) {
    this.fundingTypeOther = fundingTypeOther;
  }

  public String getFundingBody() {
    return fundingBody;
  }

  public void setFundingBody(String fundingBody) {
    this.fundingBody = fundingBody;
  }

  public LocalDate getDateFrom() {
    return dateFrom;
  }

  public void setDateFrom(LocalDate dateFrom) {
    this.dateFrom = dateFrom;
  }

  public LocalDate getDateTo() {
    return dateTo;
  }

  public void setDateTo(LocalDate dateTo) {
    this.dateTo = dateTo;
  }
}
