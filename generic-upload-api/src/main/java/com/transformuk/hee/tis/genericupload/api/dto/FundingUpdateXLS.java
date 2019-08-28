package com.transformuk.hee.tis.genericupload.api.dto;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FundingUpdateXLS that = (FundingUpdateXLS) o;
    return Objects.equals(postFundingTisId, that.postFundingTisId) &&
        Objects.equals(fundingType, that.fundingType) &&
        Objects.equals(fundingTypeOther, that.fundingTypeOther) &&
        Objects.equals(fundingBody, that.fundingBody) &&
        Objects.equals(dateFrom, that.dateFrom) &&
        Objects.equals(dateTo, that.dateTo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(postFundingTisId, fundingType, fundingTypeOther, fundingBody, dateFrom, dateTo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("FundingUpdateXLS{");
    sb.append("postFundingTisId=").append(postFundingTisId);
    sb.append(", fundingType=").append(fundingType);
    sb.append(", fundingTypeOther=").append(fundingTypeOther);
    sb.append(", fundingBody=").append(fundingBody);
    sb.append(", dateFrom=").append(dateFrom);
    sb.append(", dateTo=").append(dateTo);
    sb.append("}");
    return sb.toString();
  }
}
