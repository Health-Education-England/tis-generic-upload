package com.transformuk.hee.tis.genericupload.service.parser;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.Maps;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * The unit tests for {@link PostCreateHeaderMapper}.
 */
public class PostCreateHeaderMapperTest {

  private PostCreateHeaderMapper mapper;

  @Before
  public void setUp() {
    mapper = new PostCreateHeaderMapper();
  }

  /**
   * Test that all field pairs are returned.
   */
  @Test
  public void getFieldMapShouldReturnAllFields() {
    // When.
    Map<String, String> fieldMap = mapper.getFieldMap();

    // Then.
    assertThat("The number of fields did not contain the expected value.", fieldMap.size(), is(14));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("nationalPostNumber", "National Post Number*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("approvedGrade", "Approved grade*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("otherGrades", "Other grades")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("specialty", "Specialty*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("otherSpecialties", "Other specialties")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("subSpecialties", "Sub specialties")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("trainingDescription", "Training description")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("mainSite", "Main site (Known as)*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("otherSites", "Other sites (Known as)")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("trainingBody", "Training body*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("employingBody", "Employing body*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("programmeTisId", "TIS_Programme_ID*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("owner", "Owner*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("oldPost", "Old Post")));
  }

  /**
   * Test that the mandatory field pairs are returned.
   */
  @Test
  public void getMandatoryFieldMapShouldReturnMandatoryFields() {
    // When.
    Map<String, String> fieldMap = mapper.getMandatoryFieldMap();

    // Then.
    assertThat("The number of fields did not contain the expected value.", fieldMap.size(), is(8));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("nationalPostNumber", "National Post Number*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("approvedGrade", "Approved grade*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("specialty", "Specialty*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("mainSite", "Main site (Known as)*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("trainingBody", "Training body*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("employingBody", "Employing body*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("programmeTisId", "TIS_Programme_ID*")));
    assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
        hasItem(Maps.immutableEntry("owner", "Owner*")));
  }
}