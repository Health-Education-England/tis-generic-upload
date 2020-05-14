package com.transformuk.hee.tis.genericupload.api.dto;

import java.util.Date;
import lombok.Data;

@Data
public class PersonXLS extends TemplateXLS {

  private String title;
  private String surname;
  private String forenames;
  private String gender;
  private Date dateOfBirth;
  private String gmcNumber;
  private String niNumber;
  private String gdcNumber;
  private String eeaResident;
  private String nationality;
  private String ethnicOrigin;
  private String address1;
  private String address2;
  private String address3;
  private String postCode;
  private String telephone;
  private String mobile;
  private String emailAddress;
  private String medicalSchool;
  private String qualification;
  private Date dateAttained;
  private String countryOfQualification;
  private String disability;
  private String disabilityDetails;
  private String sexualOrientation;
  private String religiousBelief;
  private String programmeName;
  private String programmeNumber;
  private String programmeMembership;
  private String rotation1;
  private String ntnProgramme;
  private String curriculum1;
  private Date curriculum1StartDate;
  private Date curriculum1EndDate;
  private String curriculum2;
  private Date curriculum2StartDate;
  private Date curriculum2EndDate;
  private String curriculum3;
  private Date curriculum3StartDate;
  private Date curriculum3EndDate;
  private Date programmeEndDate;
  private String role;
  private String knownAs;
  private String permitToWork;
  private String settled;
  private Date visaIssued;
  private Date visaValidTo;
  private String visaDetails;
  private String maritalStatus;
  private String publicHealthNumber;
}
