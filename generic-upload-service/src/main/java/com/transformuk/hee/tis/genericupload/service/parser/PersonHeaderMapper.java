package com.transformuk.hee.tis.genericupload.service.parser;

import java.util.Arrays;
import java.util.List;

public class PersonHeaderMapper extends ColumnMapper {

  private static final List<ColumnMapping> COLUMN_MAPPINGS = Arrays.asList(
      new ColumnMapping("title", "Title"),
      new ColumnMapping("surname", "Surname *"),
      new ColumnMapping("forenames", "Forenames *"),
      new ColumnMapping("gender", "Gender"),
      new ColumnMapping("dateOfBirth", "Date of Birth"),
      new ColumnMapping("gmcNumber", "GMC Number"),
      new ColumnMapping("niNumber", "NI number"),
      new ColumnMapping("gdcNumber", "GDC Number"),
      new ColumnMapping("eeaResident", "EEA Resident"),
      new ColumnMapping("nationality", "Nationality"),
      new ColumnMapping("ethnicOrigin", "Ethnic Origin"),
      new ColumnMapping("address1", "Address 1"),
      new ColumnMapping("address2", "Address 2"),
      new ColumnMapping("address3", "Address 3"),
      new ColumnMapping("postCode", "Post Code"),
      new ColumnMapping("telephone", "Telephone"),
      new ColumnMapping("mobile", "Mobile"),
      new ColumnMapping("emailAddress", "Email Address"),
      new ColumnMapping("medicalSchool", "Medical School"),
      new ColumnMapping("qualification", "Qualification"),
      new ColumnMapping("dateAttained", "Date Attained"),
      new ColumnMapping("countryOfQualification", "Country of Qualification"),
      new ColumnMapping("disability", "Disability"),
      new ColumnMapping("disabilityDetails", "Disability Details"),
      new ColumnMapping("sexualOrientation", "Sexual Orientation"),
      new ColumnMapping("religiousBelief", "Religious Belief"),
      new ColumnMapping("programmeName", "Programme Name"),
      new ColumnMapping("programmeNumber", "Programme Number"),
      new ColumnMapping("programmeMembership", "Programme Membership Type"),
      new ColumnMapping("rotation1", "Rotation"),
      new ColumnMapping("ntnProgramme", "NTN (Programme)"),
      new ColumnMapping("curriculum1", "Curriculum #1"),
      new ColumnMapping("curriculum1StartDate", "Curriculum #1 Start Date"),
      new ColumnMapping("curriculum1EndDate", "Curriculum #1 End Date"),
      new ColumnMapping("curriculum2", "Curriculum #2"),
      new ColumnMapping("curriculum2StartDate", "Curriculum #2 Start Date"),
      new ColumnMapping("curriculum2EndDate", "Curriculum #2 End Date"),
      new ColumnMapping("curriculum3", "Curriculum #3"),
      new ColumnMapping("curriculum3StartDate", "Curriculum #3 Start Date"),
      new ColumnMapping("curriculum3EndDate", "Curriculum #3 End Date"),
      new ColumnMapping("programmeEndDate", "Programme End Date"),
      new ColumnMapping("role", "Role"),
      new ColumnMapping("knownAs", "Known As"),
      new ColumnMapping("permitToWork", "Permit to Work"),
      new ColumnMapping("settled", "Settled, Permit to Work"),
      new ColumnMapping("visaIssued", "Visa Issued"),
      new ColumnMapping("visaValidTo", "Visa Valid To"),
      new ColumnMapping("visaDetails", "Visa Details/Number"),
      new ColumnMapping("maritalStatus", "Marital Status"),
      new ColumnMapping("recordStatus", "Record Status"),
      new ColumnMapping("inactiveDate", "Inactive Date"),
      new ColumnMapping("publicHealthNumber", "Public Health Number")
  );

  @Override
  List<ColumnMapping> getColumnMappings() {
    return COLUMN_MAPPINGS;
  }
}
