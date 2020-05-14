package com.transformuk.hee.tis.genericupload.api.dto;

import java.util.Date;
import lombok.Data;

@Data
public class PlacementXLS extends TemplateXLS implements PlacementSupervisor {

  private String surname;
  private String gmcNumber;
  private String gdcNumber;
  private String publicHealthNumber;
  private String nationalPostNumber;
  private Date dateFrom;
  private Date dateTo;
  private String placementType;
  private String placementStatus;
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

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getGmcNumber() {
    return gmcNumber;
  }

  public void setGmcNumber(String gmcNumber) {
    this.gmcNumber = gmcNumber;
  }

  public String getGdcNumber() {
    return gdcNumber;
  }

  public void setGdcNumber(String gdcNumber) {
    this.gdcNumber = gdcNumber;
  }

  public String getPublicHealthNumber() {
    return publicHealthNumber;
  }

  public void setPublicHealthNumber(String publicHealthNumber) {
    this.publicHealthNumber = publicHealthNumber;
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

  public String getPlacementStatus() {
    return placementStatus;
  }

  public void setPlacementStatus(String placementStatus) {
    this.placementStatus = placementStatus;
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

  public void setOtherSites(String otherSites) {
    this.otherSites = otherSites;
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
}
