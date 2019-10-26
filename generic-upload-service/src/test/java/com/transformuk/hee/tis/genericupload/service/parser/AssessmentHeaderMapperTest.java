package com.transformuk.hee.tis.genericupload.service.parser;

import com.google.common.collect.Maps;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

/**
 * The unit tests for {@link AssessmentHeaderMapper}.
 */
public class AssessmentHeaderMapperTest {

  private AssessmentHeaderMapper mapper;

  @Before
  public void setUp() {
    mapper = new AssessmentHeaderMapper();
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
            CoreMatchers.is(33));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("surname", "Trainee Surname*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("gmcNumber", "GMC Number")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("gdcNumber", "GDC Number")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("publicHealthNumber", "Public Health Number")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("type", "Type*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("programmeName", "Programme name*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("programmeNumber", "Programme number*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("reviewDate", "Review date*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("curriculumName", "Curriculum name*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("daysOutOfTraining", "Days out of training")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("periodCoveredFrom", "Period covered from")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("periodCoveredTo", "Period covered to")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("monthsCountedToTraining",
                "Months OOPR/OOPT counted towards training")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("pya", "PYA")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("outcome", "Outcome")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("underAppeal", "Under appeal")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("outcomeNotAssessed",
                "Unsatisfactory Outcome/Not Assessed Reason")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("outcomeNotAssessedOther",
                "Unsatisfactory Outcome/Not Assessed Reason (Other)")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("academicOutcome", "Academic Outcome")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("externalTrainer", "External trainer")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers
                .hasItem(Maps.immutableEntry("nextRotationGradeName", "Grade at next rotation")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("nextReviewDate", "Next review date")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("comments", "Comments")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("tenPercentAudit", "10% audit - lay member")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("detailedReasons", "Detailed reason")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(
                Maps.immutableEntry("mitigatingCircumstances", "Mitigating circumstances")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("competencesToBeDeveloped",
                "Competences which need to be developed by next ARCP")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(
                Maps.immutableEntry("otherRecommendedActions", "Other recommended actions")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("recommendedAdditionalTrainingTime",
                "Recommended additional training time (if required)")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("additionalCommentsFromPanel",
                "Additional comments from the panel")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("knownConcerns", "Known concerns")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("concernSummary", "Concern summary")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(
                Maps.immutableEntry("responsibleOfficerComments", "Responsible officer comments")));
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
            CoreMatchers.is(6));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("surname", "Trainee Surname*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("type", "Type*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("programmeName", "Programme name*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("programmeNumber", "Programme number*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("reviewDate", "Review date*")));
    MatcherAssert
        .assertThat("The field map did not contain the expected value.", fieldMap.entrySet(),
            CoreMatchers.hasItem(Maps.immutableEntry("curriculumName", "Curriculum name*")));
  }
}
