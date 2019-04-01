package com.transformuk.hee.tis.genericupload.api.dto;

import java.time.LocalDate;
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PostFundingUpdateXLS that = (PostFundingUpdateXLS) o;
    return Objects.equals(postTisId, that.postTisId) &&
        Objects.equals(fundingType, that.fundingType) &&
        Objects.equals(fundingTypeOther, that.fundingTypeOther) &&
        Objects.equals(fundingBody, that.fundingBody) &&
        Objects.equals(dateFrom, that.dateFrom) &&
        Objects.equals(dateTo, that.dateTo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(postTisId, fundingType, fundingTypeOther, fundingBody, dateFrom, dateTo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("PostFundingUpdateXLS{");
    sb.append("postTisId=").append(postTisId);
    sb.append(", fundingType=").append(fundingType);
    sb.append(", fundingTypeOther=").append(fundingTypeOther);
    sb.append(", fundingBody=").append(fundingBody);
    sb.append(", dateFrom=").append(dateFrom);
    sb.append(", dateTo=").append(dateTo);
    sb.append("}");
    return sb.toString();
  }
}
