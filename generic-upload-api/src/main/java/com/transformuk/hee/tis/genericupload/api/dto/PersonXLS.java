package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PersonXLS extends TemplateXLS {

  @ExcelColumn(name = "Title")
  private String title;

  @ExcelColumn(name = "Surname *", required = true)
  private String surname;

  @ExcelColumn(name = "Forenames *", required = true)
  private String forenames;

  @ExcelColumn(name = "Gender")
  private String gender;

  @ExcelColumn(name = "Date of Birth")
  private Date dateOfBirth;

  @ExcelColumn(name = "GMC Number")
  private String gmcNumber;

  @ExcelColumn(name = "NI number")
  private String niNumber;

  @ExcelColumn(name = "GDC Number")
  private String gdcNumber;

  @ExcelColumn(name = "EEA Resident")
  private String eeaResident;

  @ExcelColumn(name = "Nationality")
  private String nationality;

  @ExcelColumn(name = "Ethnic Origin")
  private String ethnicOrigin;

  @ExcelColumn(name = "Address 1")
  private String address1;

  @ExcelColumn(name = "Address 2")
  private String address2;

  @ExcelColumn(name = "Address 3")
  private String address3;

  @ExcelColumn(name = "Post Code")
  private String postCode;

  @ExcelColumn(name = "Telephone")
  private String telephone;

  @ExcelColumn(name = "Mobile")
  private String mobile;

  @ExcelColumn(name = "Email Address")
  private String emailAddress;

  @ExcelColumn(name = "Medical School")
  private String medicalSchool;

  @ExcelColumn(name = "Qualification")
  private String qualification;

  @ExcelColumn(name = "Date Attained")
  private Date dateAttained;

  @ExcelColumn(name = "Country of Qualification")
  private String countryOfQualification;

  @ExcelColumn(name = "Disability")
  private String disability;

  @ExcelColumn(name = "Disability Details")
  private String disabilityDetails;

  @ExcelColumn(name = "Sexual Orientation")
  private String sexualOrientation;

  @ExcelColumn(name = "Religious Belief")
  private String religiousBelief;

  @ExcelColumn(name = "Programme Name")
  private String programmeName;

  @ExcelColumn(name = "Programme Number")
  private String programmeNumber;

  @ExcelColumn(name = "Programme Membership Type")
  private String programmeMembership;

  @ExcelColumn(name = "Rotation")
  private String rotation1;

  @ExcelColumn(name = "NTN (Programme)")
  private String ntnProgramme;

  @ExcelColumn(name = "Curriculum #1")
  private String curriculum1;

  @ExcelColumn(name = "Curriculum #1 Start Date")
  private Date curriculum1StartDate;

  @ExcelColumn(name = "Curriculum #1 End Date")
  private Date curriculum1EndDate;

  @ExcelColumn(name = "Curriculum #2")
  private String curriculum2;

  @ExcelColumn(name = "Curriculum #2 Start Date")
  private Date curriculum2StartDate;

  @ExcelColumn(name = "Curriculum #2 End Date")
  private Date curriculum2EndDate;

  @ExcelColumn(name = "Curriculum #3")
  private String curriculum3;

  @ExcelColumn(name = "Curriculum #3 Start Date")
  private Date curriculum3StartDate;

  @ExcelColumn(name = "Curriculum #3 End Date")
  private Date curriculum3EndDate;

  @ExcelColumn(name = "Programme End Date")
  private Date programmeEndDate;

  @ExcelColumn(name = "Role")
  private String role;

  @ExcelColumn(name = "Known As")
  private String knownAs;

  @ExcelColumn(name = "Permit to Work")
  private String permitToWork;

  @ExcelColumn(name = "Settled, Permit to Work")
  private String settled;

  @ExcelColumn(name = "Visa Issued")
  private Date visaIssued;

  @ExcelColumn(name = "Visa Valid To")
  private Date visaValidTo;

  @ExcelColumn(name = "Visa Details/Number")
  private String visaDetails;

  @ExcelColumn(name = "Marital Status")
  private String maritalStatus;

  @ExcelColumn(name = "Public Health Number")
  private String publicHealthNumber;
}
