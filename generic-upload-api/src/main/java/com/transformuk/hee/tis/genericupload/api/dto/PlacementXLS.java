package com.transformuk.hee.tis.genericupload.api.dto;

import java.util.Date;
import java.util.Objects;

public class PlacementXLS extends TemplateXLS implements PlacementSupervisor{
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
		PlacementXLS that = (PlacementXLS) o;
		return Objects.equals(surname, that.surname) &&
				Objects.equals(gmcNumber, that.gmcNumber) &&
				Objects.equals(gdcNumber, that.gdcNumber) &&
				Objects.equals(publicHealthNumber, that.publicHealthNumber) &&
				Objects.equals(nationalPostNumber, that.nationalPostNumber) &&
				Objects.equals(dateFrom, that.dateFrom) &&
				Objects.equals(dateTo, that.dateTo) &&
				Objects.equals(placementType, that.placementType) &&
				Objects.equals(placementStatus, that.placementStatus) &&
				Objects.equals(site, that.site) &&
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
		return Objects.hash(surname, gmcNumber, gdcNumber, publicHealthNumber, nationalPostNumber, dateFrom, dateTo, placementType, placementStatus, site, wte, grade, specialty1, specialty2, specialty3, clinicalSupervisor, educationalSupervisor, comments);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PlacementXLS{");
		sb.append("surname='").append(surname).append('\'');
		sb.append(", gmcNumber='").append(gmcNumber).append('\'');
		sb.append(", gdcNumber='").append(gdcNumber).append('\'');
		sb.append(", publicHealthNumber='").append(publicHealthNumber).append('\'');
		sb.append(", nationalPostNumber='").append(nationalPostNumber).append('\'');
		sb.append(", dateFrom=").append(dateFrom);
		sb.append(", dateTo=").append(dateTo);
		sb.append(", placementType='").append(placementType).append('\'');
		sb.append(", placementStatus='").append(placementStatus).append('\'');
		sb.append(", site='").append(site).append('\'');
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
