package com.transformuk.hee.tis.genericupload.api.dto;

import java.util.Date;
import java.util.Objects;

public class PlacementUpdateXLS extends TemplateXLS implements PlacementSupervisor{
  private String placementId;
  private String intrepidId;
  private String nationalPostNumber;
  private Date dateFrom;
  private Date dateTo;
  private String placementType;
  private String site;
  private String otherSites;
  private Float wte;
  private String grade;
  private String specialty1;
  private String specialty2;
  private String specialty3;
  private String clinicalSupervisor;
  private String educationalSupervisor;
  private String comments;

  public String getPlacementId() {
    return placementId;
  }

  public void setPlacementId(String placementId) {
    this.placementId = placementId;
  }

  public String getIntrepidId() {
    return intrepidId;
  }

  public void setIntrepidId(String intrepidId) {
    this.intrepidId = intrepidId;
  }

  public String getNationalPostNumber() {
    return nationalPostNumber;
  }

  public void setNationalPostNumber(String nationalPostNumber) {
    this.nationalPostNumber = nationalPostNumber;
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

  public String getPlacementType() {
    return placementType;
  }

  public void setPlacementType(String placementType) {
    this.placementType = placementType;
  }

  public String getSite() {
    return site;
  }

  public void setSite(String site) {
    this.site = site;
  }

  public String getOtherSites() {
    return otherSites;
  }

  public void setOtherSites(String otherSite) {
    this.otherSites = otherSite;
  }

  public Float getWte() {
    return wte;
  }

  public void setWte(Float wte) {
    this.wte = wte;
  }

  public String getGrade() {
    return grade;
  }

  public void setGrade(String grade) {
    this.grade = grade;
  }

  public String getSpecialty1() {
    return specialty1;
  }

  public void setSpecialty1(String specialty1) {
    this.specialty1 = specialty1;
  }

  public String getSpecialty2() {
    return specialty2;
  }

  public void setSpecialty2(String specialty2) {
    this.specialty2 = specialty2;
  }

  public String getSpecialty3() {
    return specialty3;
  }

  public void setSpecialty3(String specialty3) {
    this.specialty3 = specialty3;
  }

  public String getClinicalSupervisor() {
    return clinicalSupervisor;
  }

  public void setClinicalSupervisor(String clinicalSupervisor) {
    this.clinicalSupervisor = clinicalSupervisor;
  }

  public String getEducationalSupervisor() {
    return educationalSupervisor;
  }

  public void setEducationalSupervisor(String educationalSupervisor) {
    this.educationalSupervisor = educationalSupervisor;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PlacementUpdateXLS that = (PlacementUpdateXLS) o;
    return Objects.equals(placementId, that.placementId) &&
            Objects.equals(intrepidId, that.intrepidId) &&
            Objects.equals(nationalPostNumber, that.nationalPostNumber) &&
            Objects.equals(dateFrom, that.dateFrom) &&
            Objects.equals(dateTo, that.dateTo) &&
            Objects.equals(placementType, that.placementType) &&
            Objects.equals(site, that.site) &&
            Objects.equals(otherSites, that.otherSites) &&
            Objects.equals(wte, that.wte) &&
            Objects.equals(grade, that.grade) &&
            Objects.equals(specialty1, that.specialty1) &&
            Objects.equals(specialty2, that.specialty2) &&
            Objects.equals(specialty3, that.specialty3) &&
            Objects.equals(clinicalSupervisor, that.clinicalSupervisor) &&
            Objects.equals(educationalSupervisor, that.educationalSupervisor) &&
            Objects.equals(comments, that.comments);
  }

  @Override
  public int hashCode() {

    return Objects.hash(placementId, intrepidId, nationalPostNumber, dateFrom, dateTo, placementType, site, wte, grade, specialty1, specialty2, specialty3, clinicalSupervisor, educationalSupervisor, comments);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("PlacementXLS{");
    sb.append("placementId=").append(placementId);
    sb.append(", intrepidId=").append(intrepidId);
    sb.append(", nationalPostNumber='").append(nationalPostNumber).append('\'');
    sb.append(", dateFrom=").append(dateFrom);
    sb.append(", dateTo=").append(dateTo);
    sb.append(", placementType='").append(placementType).append('\'');
    sb.append(", site='").append(site).append('\'');
    sb.append(", otherSites='").append(otherSites).append('\'');
    sb.append(", wte='").append(wte).append('\'');
    sb.append(", grade='").append(grade).append('\'');
    sb.append(", specialty1='").append(specialty1).append('\'');
    sb.append(", specialty2='").append(specialty2).append('\'');
    sb.append(", specialty3='").append(specialty3).append('\'');
    sb.append(", clinicalSupervisor='").append(clinicalSupervisor).append('\'');
    sb.append(", educationalSupervisor='").append(educationalSupervisor).append('\'');
    sb.append(", comments='").append(comments).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
