package com.transformuk.hee.tis.genericupload.service.service.shared.validator;

import static com.transformuk.hee.tis.genericupload.service.service.shared.AcademicOutcome.CONTINUE_ON_ACADEMIC_COMPONENT;
import static com.transformuk.hee.tis.genericupload.service.service.shared.AcademicOutcome.DO_NOT_CONTINUE_ON_ACADEMIC_COMPONENT;
import static com.transformuk.hee.tis.genericupload.service.service.shared.AcademicOutcome.SUCCESSFULLY_COMPLETED_ACADEMIC_COMPONENT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.transformuk.hee.tis.assessment.api.dto.AssessmentDetailDTO;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for AcademicOutcomeAssessmentValidator.
 */
@ExtendWith(MockitoExtension.class)
class AcademicOutcomeAssessmentValidatorTest {

  private static final String CURRICULUM_NAME = "Test Curriculum";
  private static final LocalDate CURRICULUM_START = LocalDate.of(2026, 1, 1);
  private static final LocalDate CURRICULUM_END = LocalDate.of(2026, 12, 31);
  private static final LocalDate PERIOD_FROM = LocalDate.of(2026, 6, 1);
  private static final LocalDate PERIOD_TO = LocalDate.of(2026, 12, 31);

  private final AcademicOutcomeAssessmentValidator validator =
      new AcademicOutcomeAssessmentValidator();

  @ParameterizedTest(name = "Academic outcome: {0}")
  @ValueSource(strings = {
      "Continue on academic component",
      "Do not continue on academic component",
      "Successfully completed academic component"
  })
  void testValidAcademicOutcomeValues(String validOutcome) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, validOutcome);

    assertThat(result.hasError(), is(false));
    assertTrue(result.getError().isEmpty(), "Error should be empty");
  }

  @ParameterizedTest(name = "Invalid outcome: {0}")
  @ValueSource(strings = {
      "Invalid Outcome",
      "",
      "   "
  })
  void testInvalidAcademicOutcomeValues(String invalidOutcome) {
    String outcome = invalidOutcome.trim().isEmpty() ? null : invalidOutcome;
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, outcome);

    if (outcome == null || outcome.trim().isEmpty()) {
      assertThat("Should have error for missing outcome",
          result.getError().get(),
          is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_IS_REQUIRED));
    } else {
      assertThat("Should have error for invalid outcome",
          result.getError().get(),
          is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_MUST_BE_VALID));
    }
  }

  @ParameterizedTest(name = "Academic curriculum subtype: {0}")
  @ValueSource(strings = {"AFT", "ACLNIHR_FUNDING", "ACL_OTHER_FUNDING",
      "ACFNIHR_FUNDING", "ACF_OTHER_FUNDING"})
  void testAcademicCurriculumRequiresOutcome(String academicSubType) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO(academicSubType,
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, null);

    assertThat(result.getError().get(),
        is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_IS_REQUIRED));
  }

  @ParameterizedTest(name = "Non-academic curriculum subtype: {0}")
  @ValueSource(strings = {"ACL", "ACF", "DENTAL", "CCT_SUBSPECIALTY", "GENERIC"})
  void testNonAcademicCurriculumWithValidOutcomeFails(String nonAcademicSubType) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO(nonAcademicSubType,
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        "Continue on academic component");

    assertThat(result.getError().get(),
        is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_MUST_BE_EMPTY_FOR_NON_ACADEMIC_CURRICULUM));
  }

  @ParameterizedTest(name = "Non-academic curriculum subtype: {0}")
  @ValueSource(strings = {"ACL", "ACF", "DENTAL", "CCT_SUBSPECIALTY"})
  void testNonAcademicCurriculumWithoutOutcomeSucceeds(String nonAcademicSubType) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO(nonAcademicSubType,
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, null);

    assertThat(result.hasError(), is(false));
  }

  @ParameterizedTest(name = "Overlap test: curriculum [{0}, {1}], period [{2}, {3}]")
  @CsvSource({
      // Curriculum overlaps period
      "2020-01-01,2020-12-31,2020-06-01,2020-12-31",
      // Period overlaps curriculum
      "2020-06-01,2020-12-31,2020-01-01,2020-12-31",
      // Curriculum contains period
      "2020-01-01,2021-12-31,2020-06-01,2020-12-31",
      // Period contains curriculum
      "2020-06-01,2020-08-31,2020-01-01,2021-12-31",
      // Same dates
      "2020-06-01,2020-12-31,2020-06-01,2020-12-31",
      // Adjacent (curriculum end = period from)
      "2020-01-01,2020-06-01,2020-06-01,2020-12-31"
  })
  void testValidDateOverlaps(LocalDate currStart, LocalDate currEnd,
      LocalDate periodFrom, LocalDate periodTo) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        currStart, currEnd, periodFrom, periodTo);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        CONTINUE_ON_ACADEMIC_COMPONENT.getLabel());

    assertThat(result.hasError(), is(false));
    assertTrue(result.getAcademicCurriculumAssessed().isPresent(),
        "Curriculum name should be present for assessed academic curriculum");
  }

  @ParameterizedTest(name = "No overlap test: curriculum [{0}, {1}], period [{2}, {3}]")
  @CsvSource({
      // Period ends before curriculum starts
      "2020-06-01,2020-12-31,2020-01-01,2020-05-31",
      // Curriculum ends before period starts
      "2020-01-01,2020-05-31,2020-06-01,2020-12-31",
      // Far apart
      "2020-01-01,2020-06-30,2021-01-01,2021-12-31"
  })
  void testNoDateOverlaps(LocalDate currStart, LocalDate currEnd,
      LocalDate periodFrom, LocalDate periodTo) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        currStart, currEnd, periodFrom, periodTo);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        CONTINUE_ON_ACADEMIC_COMPONENT.getLabel());

    assertThat(result.getAcademicCurriculumAssessed().isEmpty(), is(true));
    assertThat(result.hasError(), is(false));
  }

  @Test
  void testNullCurriculumStartDateNoOverlap() {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        null, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        CONTINUE_ON_ACADEMIC_COMPONENT.getLabel());

    assertThat(result.getAcademicCurriculumAssessed().isEmpty(), is(true));
  }

  @Test
  void testNullCurriculumEndDateNoOverlap() {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        CURRICULUM_START, null, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        CONTINUE_ON_ACADEMIC_COMPONENT.getLabel());

    assertThat(result.getAcademicCurriculumAssessed().isEmpty(), is(true));
  }

  @Test
  void testNullPeriodFromDateNoOverlap() {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        CURRICULUM_START, CURRICULUM_END, null, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        CONTINUE_ON_ACADEMIC_COMPONENT.getLabel());

    assertThat(result.getAcademicCurriculumAssessed().isEmpty(), is(true));
  }

  @Test
  void testNullPeriodToDateNoOverlap() {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, null);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        CONTINUE_ON_ACADEMIC_COMPONENT.getLabel());

    assertThat(result.getAcademicCurriculumAssessed().isEmpty(), is(true));
  }

  @Test
  void testValidAcademicCurriculumWithValidOutcomeAndOverlappingDates() {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        SUCCESSFULLY_COMPLETED_ACADEMIC_COMPONENT.getLabel());

    assertFalse(result.hasError());
    assertTrue(result.getAcademicCurriculumAssessed().isPresent());
    assertThat(result.getAcademicCurriculumAssessed().get(),
        is(CURRICULUM_NAME));
  }

  @Test
  void testValidAcademicCurriculumWithValidOutcomeButNoDateOverlap() {
    // Dates don't overlap
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        LocalDate.of(2020, 1, 1), LocalDate.of(2020, 5, 31),
        LocalDate.of(2020, 6, 1), LocalDate.of(2020, 12, 31));

    AcademicOutcomeValidationResult result = validator.validate(dto,
        DO_NOT_CONTINUE_ON_ACADEMIC_COMPONENT.getLabel());

    assertFalse(result.hasError());
    assertTrue(result.getAcademicCurriculumAssessed().isEmpty());
  }

  @Test
  void testAcademicCurriculumWithMissingOutcome() {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, null);

    assertTrue(result.hasError());
    assertThat(result.getError().get(),
        is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_IS_REQUIRED));
  }

  @Test
  void testAcademicCurriculumWithInvalidOutcome() {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("ACLNIHR_FUNDING",
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, "BadOutcome");

    assertTrue(result.hasError());
    assertThat(result.getError().get(),
        is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_MUST_BE_VALID));
  }

  @Test
  void testAcademicOutcomeIsRequiredErrorMessage() {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("ACF_OTHER_FUNDING",
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, null);

    assertThat(result.getError().get(),
        is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_IS_REQUIRED));
  }

  @Test
  void testAcademicOutcomeMustBeEmptyErrorMessage() {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("GENERIC",
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        "Continue on academic component");

    assertThat(result.getError().get(),
        is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_MUST_BE_EMPTY_FOR_NON_ACADEMIC_CURRICULUM));
  }

  @Test
  void testAcademicOutcomeMustBeValidErrorMessage() {
    AssessmentDetailDTO dto = createAssessmentDetailDTO("AFT",
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, "NotAValidOutcome");

    assertThat(result.getError().get(),
        is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_MUST_BE_VALID));
  }

  private AssessmentDetailDTO createAssessmentDetailDTO(
      String curriculumSubType,
      LocalDate curriculumStartDate,
      LocalDate curriculumEndDate,
      LocalDate periodCoveredFrom,
      LocalDate periodCoveredTo) {
    AssessmentDetailDTO dto = new AssessmentDetailDTO();
    dto.setCurriculumSubType(curriculumSubType);
    dto.setCurriculumStartDate(curriculumStartDate);
    dto.setCurriculumEndDate(curriculumEndDate);
    dto.setPeriodCoveredFrom(periodCoveredFrom);
    dto.setPeriodCoveredTo(periodCoveredTo);
    dto.setCurriculumName(CURRICULUM_NAME);
    return dto;
  }
}


