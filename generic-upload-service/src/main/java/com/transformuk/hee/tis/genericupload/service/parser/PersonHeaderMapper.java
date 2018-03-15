package com.transformuk.hee.tis.genericupload.service.parser;

import java.util.Map;
import java.util.stream.Collectors;

public class PersonHeaderMapper implements ColumnMapper {
  private static final String[] fieldNameSource = {
      "title",
      "surname",
      "forenames",
      "gender",
      "dateOfBirth",
      "gmcNumber",
      "niNumber",
      "gdcNumber",
      "eeaResident",
      "nationality",
      "ethnicOrigin",
      "address1",
      "address2",
      "address3",
      "postCode",
      "telephone",
      "mobile",
      "emailAddress",
      "medicalSchool",
      "qualification",
      "dateAttained",
      "countryOfQualification",
      "disability",
      "disabilityDetails",
      "sexualOrientation",
      "religiousBelief",
      "programmeName",
      "programmeNumber",
      "programmeMembership",
      "ntnProgramme",
      "curriculum1",
      "curriculum1StartDate",
      "curriculum1EndDate",
      "curriculum2",
      "curriculum2StartDate",
      "curriculum2EndDate",
      "curriculum3",
      "curriculum3StartDate",
      "curriculum3EndDate",
      "programmeEndDate",
      "role",
      "knownAs",
      "permitToWork",
      "settled",
      "visaIssued",
      "visaValidTo",
      "visaDetails",
      "maritalStatus",
      "recordStatus",
      "inactiveDate",
      "publicHealthNumber"
  };
  private static final String[] fieldNameTarget = {
      "Title",
      "Surname *",
      "Forenames *",
      "Gender",
      "Date of Birth",
      "GMC Number",
      "NI number",
      "GDC Number",
      "EEA Resident",
      "Nationality",
      "Ethnic Origin",
      "Address 1",
      "Address 2",
      "Address 3",
      "Post Code",
      "Telephone",
      "Mobile",
      "Email Address",
      "Medical School",
      "Qualification",
      "Date Attained",
      "Country of Qualification",
      "Disability",
      "Disability Details",
      "Sexual Orientation",
      "Religious Belief",
      "Programme Name",
      "Programme Number",
      "Programme Membership Type",
      "NTN (Programme)",
      "Curriculum #1",
      "Curriculum #1 Start Date",
      "Curriculum #1 End Date",
      "Curriculum #2",
      "Curriculum #2 Start Date",
      "Curriculum #2 End Date",
      "Curriculum #3",
      "Curriculum #3 Start Date",
      "Curriculum #3 End Date",
      "Programme End Date",
      "Role",
      "Known As",
      "Permit to Work",
      "Settled, Permit to Work",
      "Visa Issued",
      "Visa Valid To",
      "Visa Details/Number",
      "Marital Status",
      "Record Status",
      "Inactive Date",
      "Public Health Number"                };

  @Override
  public Map<String, String> getFieldMap() {
        return createFieldMap(fieldNameSource, fieldNameTarget);
  }

  @Override
  public Map<String,String> getMandatoryFieldMap(){
    return this.getFieldMap().entrySet().stream().filter(map -> map.getValue().contains("*")).
            collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
  }

}
