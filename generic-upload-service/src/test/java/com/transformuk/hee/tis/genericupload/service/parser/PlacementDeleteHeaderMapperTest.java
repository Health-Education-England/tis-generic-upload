package com.transformuk.hee.tis.genericupload.service.parser;

import com.google.common.collect.Maps;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

/**
 * The unit tests for {@link PlacementDeleteHeaderMapper}.
 */
public class PlacementDeleteHeaderMapperTest {

  private PlacementDeleteHeaderMapper mapper;

  @Before
  public void setUp() {
    mapper = new PlacementDeleteHeaderMapper();
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
            CoreMatchers.is(2));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("placementId", "Placement Id*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("placementStatus", "Placement Status*")));
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
            CoreMatchers.hasItem(Maps.immutableEntry("placementId", "Placement Id*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("placementStatus", "Placement Status*")));
  }
}
