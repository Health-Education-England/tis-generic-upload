package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PersonUpdateXls extends TemplateXLS {

  @ExcelColumn(name = "TIS_Person_ID*", required = true)
  private String tisPersonId;

  @ExcelColumn(name = "Forenames")
  private String forenames;

  @ExcelColumn(name = "Surname")
  private String surname;

  @ExcelColumn(name = "GMC Number")
  private String gmcNumber;

  @ExcelColumn(name = "GDC Number")
  private String gcdNumber;

  @ExcelColumn(name = "Public Health Number")
  private String publicHealthNumber;

  @ExcelColumn(name = "GMC Status")
  private String gmcStatus;

  @ExcelColumn(name = "GDC Status")
  private String gcdStatus;

  @ExcelColumn(name = "Title")
  private String title;

  @ExcelColumn(name = "Known As")
  private String knownAs;

  @ExcelColumn(name = "Date of Birth")
  private Date dateOfBirth;

  @ExcelColumn(name = "NI Number")
  private String niNumber;

  @ExcelColumn(name = "Email Address")
  private String emailAddress;

  @ExcelColumn(name = "Mobile")
  private String mobile;

  @ExcelColumn(name = "Telephone")
  private String telephone;

  @ExcelColumn(name = "Address 1")
  private String address1;

  @ExcelColumn(name = "Address 2")
  private String address2;

  @ExcelColumn(name = "Address 3")
  private String address3;

  @ExcelColumn(name = "Post Code")
  private String postCode;

  @ExcelColumn(name = "Gender")
  private String gender;

  @ExcelColumn(name = "Nationality")
  private String nationality;

  @ExcelColumn(name = "Marital Status")
  private String maritalStatus;

  @ExcelColumn(name = "Religious Belief")
  private String religiousBelief;

  @ExcelColumn(name = "Ethnic Origin")
  private String ethnicOrigin;

  @ExcelColumn(name = "Sexual Orientation")
  private String sexualOrientation;

  @ExcelColumn(name = "Disability")
  private String disability;

  @ExcelColumn(name = "Disability Details")
  private String disabilityDetails;

  @ExcelColumn(name = "EEA Resident")
  private String eeaResident;

  @ExcelColumn(name = "Permit to Work")
  private String permitToWork;

  @ExcelColumn(name = "Settled")
  private String settled;

  @ExcelColumn(name = "Visa Details/Number")
  private String visaDetails;

  @ExcelColumn(name = "Visa Issued")
  private Date visaIssued;

  @ExcelColumn(name = "Visa Valid To")
  private Date visaValidTo;

  @ExcelColumn(name = "Role")
  private String role;

  @ExcelColumn(name = "Trainer Approval Start Date")
  private Date trainerApprovalStartDate;

  @ExcelColumn(name = "Trainer Approval End Date")
  private String trainerApprovalEndDate;

  @ExcelColumn(name = "Trainer Approval Status")
  private String trainerApprovalStatus;
}
