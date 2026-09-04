package com.transformuk.hee.tis.genericupload.service.service.shared.validator;

import static com.transformuk.hee.tis.genericupload.service.service.shared.AcademicCurriculumSubType.AFT;
import static com.transformuk.hee.tis.genericupload.service.service.shared.AcademicOutcome.CONTINUE_ON_ACADEMIC_COMPONENT;
import static com.transformuk.hee.tis.genericupload.service.service.shared.AcademicOutcome.DO_NOT_CONTINUE_ON_ACADEMIC_COMPONENT;
import static com.transformuk.hee.tis.genericupload.service.service.shared.AcademicOutcome.SUCCESSFULLY_COMPLETED_ACADEMIC_COMPONENT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.transformuk.hee.tis.assessment.api.dto.AssessmentDetailDTO;
import com.transformuk.hee.tis.genericupload.service.service.shared.AcademicCurriculumSubType;
import com.transformuk.hee.tis.genericupload.service.service.shared.AcademicOutcome;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
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
  @EnumSource(value = AcademicOutcome.class, names = {
      "CONTINUE_ON_ACADEMIC_COMPONENT",
      "DO_NOT_CONTINUE_ON_ACADEMIC_COMPONENT",
      "SUCCESSFULLY_COMPLETED_ACADEMIC_COMPONENT"
  })
  void testValidAcademicOutcomeValues(AcademicOutcome validOutcome) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO(AFT.name(),
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, validOutcome.getLabel());

    assertFalse(result.hasError());
  }

  @ParameterizedTest(name = "Invalid outcome: {0}")
  @ValueSource(strings = {
      "Invalid Outcome",
      "",
      "   "
  })
  void testInvalidAcademicOutcomeValues(String invalidOutcome) {
    String outcome = invalidOutcome.trim().isEmpty() ? null : invalidOutcome;
    AssessmentDetailDTO dto = createAssessmentDetailDTO(AFT.name(),
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, outcome);

    if (outcome == null) {
      assertThat("Should have error for missing outcome",
          result.getError().orElse(null),
          is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_IS_REQUIRED));
    } else {
      assertThat("Should have error for invalid outcome",
          result.getError().orElse(null),
          is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_MUST_BE_VALID));
    }
  }

  @ParameterizedTest(name = "Academic curriculum subtype: {0}")
  @EnumSource(AcademicCurriculumSubType.class)
  void testAcademicCurriculumRequiresOutcome(AcademicCurriculumSubType academicSubType) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO(academicSubType.name(),
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, null);

    assertThat(result.getError().orElse(null),
        is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_IS_REQUIRED));
  }

  @ParameterizedTest(name = "Non-academic curriculum subtype: {0}")
  @ValueSource(strings = {"ACL", "ACF", "DENTAL", "CCT_SUBSPECIALTY", "GENERIC"})
  void testNonAcademicCurriculumWithValidOutcomeFails(String nonAcademicSubType) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO(nonAcademicSubType,
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        CONTINUE_ON_ACADEMIC_COMPONENT.getLabel());

    assertThat(result.getError().orElse(null),
        is(AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_MUST_BE_EMPTY_FOR_NON_ACADEMIC_CURRICULUM));
  }

  @ParameterizedTest(name = "Non-academic curriculum subtype: {0}")
  @ValueSource(strings = {"ACL", "ACF", "DENTAL", "CCT_SUBSPECIALTY"})
  void testNonAcademicCurriculumWithoutOutcomeSucceeds(String nonAcademicSubType) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO(nonAcademicSubType,
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, null);

    assertFalse(result.hasError());
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
    AssessmentDetailDTO dto = createAssessmentDetailDTO(AFT.name(),
        currStart, currEnd, periodFrom, periodTo);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        CONTINUE_ON_ACADEMIC_COMPONENT.getLabel());

    assertFalse(result.hasError());
    assertTrue(result.getAcademicCurriculumAssessed().isPresent());
  }

  @ParameterizedTest(name = "No overlap test: curriculum [{0}, {1}], period [{2}, {3}]")
  @CsvSource(value = {
      // Period ends before curriculum starts
      "2020-06-01,2020-12-31,2020-01-01,2020-05-31",
      // Curriculum ends before period starts
      "2020-01-01,2020-05-31,2020-06-01,2020-12-31",
      // Far apart
      "2020-01-01,2020-06-30,2021-01-01,2021-12-31",
      // Missing one of the required date boundaries
      "NULL,2026-12-31,2026-06-01,2026-12-31",
      "2026-01-01,NULL,2026-06-01,2026-12-31",
      "2026-01-01,2026-12-31,NULL,2026-12-31",
      "2026-01-01,2026-12-31,2026-06-01,NULL"
  }, nullValues = "NULL")
  void testNoDateOverlaps(LocalDate currStart, LocalDate currEnd,
      LocalDate periodFrom, LocalDate periodTo) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO(AFT.name(),
        currStart, currEnd, periodFrom, periodTo);

    AcademicOutcomeValidationResult result = validator.validate(dto,
        CONTINUE_ON_ACADEMIC_COMPONENT.getLabel());

    assertTrue(result.getAcademicCurriculumAssessed().isEmpty());
    assertFalse(result.hasError());
  }

  @Test
  void testValidAcademicCurriculumWithValidOutcomeAndOverlappingDates() {
    AssessmentDetailDTO dto = createAssessmentDetailDTO(AFT.name(),
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
    AssessmentDetailDTO dto = createAssessmentDetailDTO(AFT.name(),
        LocalDate.of(2020, 1, 1), LocalDate.of(2020, 5, 31),
        LocalDate.of(2020, 6, 1), LocalDate.of(2020, 12, 31));

    AcademicOutcomeValidationResult result = validator.validate(dto,
        DO_NOT_CONTINUE_ON_ACADEMIC_COMPONENT.getLabel());

    assertFalse(result.hasError());
    assertTrue(result.getAcademicCurriculumAssessed().isEmpty());
  }

  @ParameterizedTest(name = "Subtype: {0}, Outcome: {1}, Expected: {2}")
  @MethodSource("academicCurriculumValidationCases")
  void testAcademicCurriculumValidationErrors(AcademicCurriculumSubType subType,
      String outcome, String expectedError) {
    AssessmentDetailDTO dto = createAssessmentDetailDTO(subType.name(),
        CURRICULUM_START, CURRICULUM_END, PERIOD_FROM, PERIOD_TO);

    AcademicOutcomeValidationResult result = validator.validate(dto, outcome);

    assertTrue(result.hasError());
    assertThat(result.getError().orElse(null), is(expectedError));
  }

  private static java.util.stream.Stream<Arguments> academicCurriculumValidationCases() {
    return java.util.stream.Stream.of(
        Arguments.of(AFT, null,
            AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_IS_REQUIRED),
        Arguments.of(AcademicCurriculumSubType.ACLNIHR_FUNDING, "BadOutcome",
            AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_MUST_BE_VALID),
        Arguments.of(AcademicCurriculumSubType.ACF_OTHER_FUNDING, null,
            AcademicOutcomeAssessmentValidator.ACADEMIC_OUTCOME_IS_REQUIRED)
    );
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
