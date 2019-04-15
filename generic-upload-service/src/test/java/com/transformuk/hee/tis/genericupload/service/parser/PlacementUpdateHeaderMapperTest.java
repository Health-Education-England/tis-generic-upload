package com.transformuk.hee.tis.genericupload.service.parser;

import com.google.common.collect.Maps;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

/**
 * The unit tests for {@link PlacementUpdateHeaderMapper}.
 */
public class PlacementUpdateHeaderMapperTest {

  private PlacementUpdateHeaderMapper mapper;

  @Before
  public void setUp() {
    mapper = new PlacementUpdateHeaderMapper();
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
            CoreMatchers.is(15));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("placementId", "TIS_Placement_ID*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("intrepidId", "Intrepid_Placement_ID")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("nationalPostNumber", "National Post Number")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("dateFrom", "Date From")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("dateTo", "Date To")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("placementType", "Placement Type")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("site", "Site")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("otherSites", "Other Sites")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("wte", "WTE")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("grade", "Grade")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("specialty1", "Specialty1")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("specialty2", "Specialty2")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("specialty3", "Specialty3")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("clinicalSupervisor", "Clinical Supervisor")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("educationalSupervisor", "Educational Supervisor")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("comments", "Comments")));
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
            CoreMatchers.is(1));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("placementId", "TIS_Placement_ID*")));
  }
}
