package com.transformuk.hee.tis.genericupload.api.dto;

import java.util.Date;
import lombok.Data;

@Data
public class FundingUpdateXLS extends TemplateXLS {

  private String postFundingTisId;
  private String fundingType;
  private String fundingTypeOther;
  private String fundingBody;
  private Date dateFrom;
  private Date dateTo;

  public String getPostFundingTisId() {
    return postFundingTisId;
  }

  public void setPostFundingTisId(String postFundingTisId) {
    this.postFundingTisId = postFundingTisId;
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

  public Date getDateFrom() {
    return dateFrom;
  }

  public void setDateFrom(Date dateFrom) {
    this.dateFrom = dateFrom;
  }

  public Date getDateTo() {
    return dateTo;
  }

  public void setDateTo(Date dateTo) {
    this.dateTo = dateTo;
  }
}
