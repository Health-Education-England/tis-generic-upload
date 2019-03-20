package com.transformuk.hee.tis.genericupload.api.dto;

import java.util.Date;
import java.util.Objects;

public class PostUpdateXLS extends TemplateXLS{
  private String postTISId;
  private String approvedGrade;
  private String otherGrades;
  private String specialty;
  private String otherSpecialties;
  private String subSpecialties;
  private String trainingDescription;
  private String mainSite;
  private String otherSites;
  private String trainingBody;
  private String employingBody;
  private String programmeName;
  private String programmeNo;
  private String owner;
  private String rotation;
  private String status;
  private String oldPost;
  private String fundingType;
  private String fundingTypeOther;
  private String fundingBody;
  private Date dateFrom;
  private Date dateTo;

  public String getPostTISId() {
    return postTISId;
  }

  public void setPostTISId(String postTISId) {
    this.postTISId = postTISId;
  }

  public String getApprovedGrade() {
    return approvedGrade;
  }

  public void setApprovedGrade(String approvedGrade) {
    this.approvedGrade = approvedGrade;
  }

  public String getOtherGrades() {
    return otherGrades;
  }

  public void setOtherGrades(String otherGrades) {
    this.otherGrades = otherGrades;
  }

  public String getSpecialty() {
    return specialty;
  }

  public void setSpecialty(String specialty) {
    this.specialty = specialty;
  }

  public String getOtherSpecialties() {
    return otherSpecialties;
  }

  public void setOtherSpecialties(String otherSpecialties) {
    this.otherSpecialties = otherSpecialties;
  }

  public String getSubSpecialties() {
    return subSpecialties;
  }

  public void setSubSpecialties(String subSpecialties) {
    this.subSpecialties = subSpecialties;
  }

  public String getTrainingDescription() {
    return trainingDescription;
  }

  public void setTrainingDescription(String trainingDescription) {
    this.trainingDescription = trainingDescription;
  }

  public String getMainSite() {
    return mainSite;
  }

  public void setMainSite(String mainSite) {
    this.mainSite = mainSite;
  }

  public String getOtherSites() {
    return otherSites;
  }

  public void setOtherSites(String otherSites) {
    this.otherSites = otherSites;
  }

  public String getTrainingBody() {
    return trainingBody;
  }

  public void setTrainingBody(String trainingBody) {
    this.trainingBody = trainingBody;
  }

  public String getEmployingBody() {
    return employingBody;
  }

  public void setEmployingBody(String employingBody) {
    this.employingBody = employingBody;
  }

  public String getProgrammeName() {
    return programmeName;
  }

  public void setProgrammeName(String programmeName) {
    this.programmeName = programmeName;
  }

  public String getProgrammeNo() {
    return programmeNo;
  }

  public void setProgrammeNo(String programmeNo) {
    this.programmeNo = programmeNo;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getRotation() {
    return rotation;
  }

  public void setRotation(String rotation) {
    this.rotation = rotation;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getOldPost() {
    return oldPost;
  }

  public void setOldPost(String oldPost) {
    this.oldPost = oldPost;
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
    if (this == o) return true;
    if (!(o instanceof PostUpdateXLS)) return false;
    PostUpdateXLS that = (PostUpdateXLS) o;
    return Objects.equals(getPostTISId(), that.getPostTISId()) &&
        Objects.equals(getApprovedGrade(), that.getApprovedGrade()) &&
        Objects.equals(getOtherGrades(), that.getOtherGrades()) &&
        Objects.equals(getSpecialty(), that.getSpecialty()) &&
        Objects.equals(getOtherSpecialties(), that.getOtherSpecialties()) &&
        Objects.equals(getSubSpecialties(), that.getSubSpecialties()) &&
        Objects.equals(getTrainingDescription(), that.getTrainingDescription()) &&
        Objects.equals(getMainSite(), that.getMainSite()) &&
        Objects.equals(getOtherSites(), that.getOtherSites()) &&
        Objects.equals(getTrainingBody(), that.getTrainingBody()) &&
        Objects.equals(getEmployingBody(), that.getEmployingBody()) &&
        Objects.equals(getProgrammeName(), that.getProgrammeName()) &&
        Objects.equals(getProgrammeNo(), that.getProgrammeNo()) &&
        Objects.equals(getOwner(), that.getOwner()) &&
        Objects.equals(getRotation(), that.getRotation()) &&
        Objects.equals(getStatus(), that.getStatus()) &&
        Objects.equals(getOldPost(), that.getOldPost()) &&
        Objects.equals(getFundingType(), that.getFundingType()) &&
        Objects.equals(getFundingTypeOther(), that.getFundingTypeOther()) &&
        Objects.equals(getFundingBody(), that.getFundingBody()) &&
        Objects.equals(getDateFrom(), that.getDateFrom()) &&
        Objects.equals(getDateTo(), that.getDateTo());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getPostTISId(), getApprovedGrade(), getOtherGrades(), getSpecialty(), getOtherSpecialties(), getSubSpecialties(), getTrainingDescription(), getMainSite(), getOtherSites(), getTrainingBody(), getEmployingBody(), getProgrammeName(), getProgrammeNo(), getOwner(), getRotation(), getStatus(), getOldPost(), getFundingType(), getFundingTypeOther(), getFundingBody(), getDateFrom(), getDateTo());
  }

  @Override
  public String toString() {
    return "PostUpdateXLS{" +
        "postTISId='" + postTISId + '\'' +
        ", approvedGrade='" + approvedGrade + '\'' +
        ", otherGrades='" + otherGrades + '\'' +
        ", specialty='" + specialty + '\'' +
        ", otherSpecialties='" + otherSpecialties + '\'' +
        ", subSpecialties='" + subSpecialties + '\'' +
        ", trainingDescription='" + trainingDescription + '\'' +
        ", mainSite='" + mainSite + '\'' +
        ", otherSites='" + otherSites + '\'' +
        ", trainingBody='" + trainingBody + '\'' +
        ", employingBody='" + employingBody + '\'' +
        ", programmeName='" + programmeName + '\'' +
        ", programmeNo='" + programmeNo + '\'' +
        ", owner='" + owner + '\'' +
        ", rotation='" + rotation + '\'' +
        ", status='" + status + '\'' +
        ", oldPost='" + oldPost + '\'' +
        ", fundingType='" + fundingType + '\'' +
        ", fundingTypeOther='" + fundingTypeOther + '\'' +
        ", fundingBody='" + fundingBody + '\'' +
        ", dateFrom=" + dateFrom +
        ", dateTo=" + dateTo +
        '}';
  }
}
