package com.transformuk.hee.tis.genericupload.service.parser;

import com.google.common.collect.Maps;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

/**
 * The unit tests for {@link PersonHeaderMapper}.
 */
public class PersonHeaderMapperTest {

  private PersonHeaderMapper mapper;

  @Before
  public void setUp() {
    mapper = new PersonHeaderMapper();
  }


  /**
   * Test that the expected field pairs are returned.
   */
  @Test
  public void testGetFieldMap_expectedFieldPairs() {
    // Call the code under test.
    Map<String, String> fieldMap = mapper.getFieldMap();

    // Perform assertions.
    MatcherAssert
        .assertThat("The number of fields did not contain the expected value.", fieldMap.size(),
            CoreMatchers.is(52));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("title", "Title")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("surname", "Surname *")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("forenames", "Forenames *")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("gender", "Gender")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("dateOfBirth", "Date of Birth")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("gmcNumber", "GMC Number")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("niNumber", "NI number")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("gdcNumber", "GDC Number")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("eeaResident", "EEA Resident")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("nationality", "Nationality")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("ethnicOrigin", "Ethnic Origin")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("address1", "Address 1")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("address2", "Address 2")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("address3", "Address 3")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("postCode", "Post Code")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("telephone", "Telephone")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("mobile", "Mobile")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("emailAddress", "Email Address")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("medicalSchool", "Medical School")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("qualification", "Qualification")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("dateAttained", "Date Attained")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(
                Maps.immutableEntry("countryOfQualification", "Country of Qualification")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("disability", "Disability")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("disabilityDetails", "Disability Details")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("sexualOrientation", "Sexual Orientation")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("religiousBelief", "Religious Belief")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("programmeName", "Programme Name")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("programmeNumber", "Programme Number")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("programmeMembership", "Programme Membership Type")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("rotation1", "Rotation")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("ntnProgramme", "NTN (Programme)")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("curriculum1", "Curriculum #1")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("curriculum1StartDate", "Curriculum #1 Start Date")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("curriculum1EndDate", "Curriculum #1 End Date")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("curriculum2", "Curriculum #2")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("curriculum2StartDate", "Curriculum #2 Start Date")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("curriculum2EndDate", "Curriculum #2 End Date")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("curriculum3", "Curriculum #3")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("curriculum3StartDate", "Curriculum #3 Start Date")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("curriculum3EndDate", "Curriculum #3 End Date")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("programmeEndDate", "Programme End Date")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("role", "Role")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("knownAs", "Known As")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("permitToWork", "Permit to Work")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("settled", "Settled, Permit to Work")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("visaIssued", "Visa Issued")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("visaValidTo", "Visa Valid To")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("visaDetails", "Visa Details/Number")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("maritalStatus", "Marital Status")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("recordStatus", "Record Status")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("inactiveDate", "Inactive Date")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("publicHealthNumber", "Public Health Number")));
  }

  /**
   * Test that the expected field pairs are returned.
   */
  @Test
  public void testGetMandatoryFieldMap_expectedFieldPairs() {
    // Call the code under test.
    Map<String, String> fieldMap = mapper.getMandatoryFieldMap();

    // Perform assertions.
    MatcherAssert
        .assertThat("The number of fields did not contain the expected value.", fieldMap.size(),
            CoreMatchers.is(2));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("surname", "Surname *")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("forenames", "Forenames *")));
  }
}
