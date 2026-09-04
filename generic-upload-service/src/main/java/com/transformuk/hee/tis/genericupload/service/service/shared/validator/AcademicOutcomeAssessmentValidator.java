package com.transformuk.hee.tis.genericupload.service.service.shared.validator;

import com.transformuk.hee.tis.assessment.api.dto.AssessmentDetailDTO;
import com.transformuk.hee.tis.genericupload.service.service.shared.AcademicCurriculumSubType;
import com.transformuk.hee.tis.genericupload.service.service.shared.AcademicOutcome;
import java.time.LocalDate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Validates academic outcome assessment by checking whether the academic outcome value is valid for
 * the curriculum type, and whether the curriculum is an assessed academic curriculum based on the
 * curriculum subtype and period dates.
 */
@Component
public class AcademicOutcomeAssessmentValidator {

  public static final String ACADEMIC_OUTCOME_IS_REQUIRED = "An academic outcome is required.";
  public static final String ACADEMIC_OUTCOME_MUST_BE_VALID = "Academic outcome must be valid";
  public static final String ACADEMIC_OUTCOME_MUST_BE_EMPTY_FOR_NON_ACADEMIC_CURRICULUM =
      "Academic outcome should be empty for non-academic curriculum subtype.";

  /**
   * Validates the academic outcome for an assessment, returning a result that describes: - the
   * curriculum name to record if the curriculum is an assessed academic curriculum. - an error
   * message if validation failed.
   *
   * @param assessmentDetailDto The assessment detail dto containing curriculum and period dates
   * @param academicOutcome     The academic outcome value supplied by the user
   * @return AcademicOutcomeValidationResult carrying the curriculum name or an error
   */
  public AcademicOutcomeValidationResult validate(
      AssessmentDetailDTO assessmentDetailDto,
      String academicOutcome
  ) {
    String curriculumSubType = assessmentDetailDto.getCurriculumSubType();

    boolean isAcademicCurriculum = AcademicCurriculumSubType.isAcademic(curriculumSubType);

    boolean isAssessedAcademicCurriculum = isAssessedAcademicCurriculum(
        assessmentDetailDto,
        isAcademicCurriculum
    );

    return validateAcademicOutcome(
        isAcademicCurriculum,
        academicOutcome,
        isAssessedAcademicCurriculum ? assessmentDetailDto.getCurriculumName() : null
    );
  }

  private boolean isAssessedAcademicCurriculum(
      AssessmentDetailDTO assessmentDetailDto,
      boolean isAcademicCurriculum
  ) {
    return isAcademicCurriculum && academicCurriculumOverlaps(assessmentDetailDto);
  }

  /*
   * Check if the curriculum period overlaps with the assessment period.
   * Overlap occurs if:
   * 1. The curriculum start date is within the assessment period, or
   * 2. The assessment period start date is within the curriculum period.
   */
  private boolean academicCurriculumOverlaps(
      AssessmentDetailDTO assessmentDetailDto
  ) {
    LocalDate curriculumStartDate = assessmentDetailDto.getCurriculumStartDate();
    LocalDate curriculumEndDate = assessmentDetailDto.getCurriculumEndDate();
    LocalDate periodCoveredFrom = assessmentDetailDto.getPeriodCoveredFrom();
    LocalDate periodCoveredTo = assessmentDetailDto.getPeriodCoveredTo();

    if (curriculumStartDate != null && curriculumEndDate != null && periodCoveredFrom != null
        && periodCoveredTo != null) {
      return (!curriculumStartDate.isBefore(periodCoveredFrom)
          && !curriculumStartDate.isAfter(periodCoveredTo)) || (
          !periodCoveredFrom.isBefore(curriculumStartDate)
              && !periodCoveredFrom.isAfter(curriculumEndDate));
    }
    return false;
  }

  private AcademicOutcomeValidationResult validateAcademicOutcome(
      boolean isAcademicCurriculum,
      String academicOutcome,
      String academicCurriculumAssessed
  ) {

    //1. If the curriculum is an academic curriculum, the academic outcome must be provided.
    if (isAcademicCurriculum && StringUtils.isEmpty(academicOutcome)) {
      return new AcademicOutcomeValidationResult(null, ACADEMIC_OUTCOME_IS_REQUIRED);
    }

    //2. If the curriculum is not an academic curriculum, the academic outcome must be empty.
    if (!isAcademicCurriculum && !StringUtils.isEmpty(academicOutcome)) {
      return new AcademicOutcomeValidationResult(null,
          ACADEMIC_OUTCOME_MUST_BE_EMPTY_FOR_NON_ACADEMIC_CURRICULUM);
    }

    //3. If the curriculum is an academic curriculum, the academic outcome must be valid.
    if (!StringUtils.isEmpty(academicOutcome) && !AcademicOutcome.isValid(academicOutcome)) {
      return new AcademicOutcomeValidationResult(null, ACADEMIC_OUTCOME_MUST_BE_VALID);
    }

    return new AcademicOutcomeValidationResult(academicCurriculumAssessed, null);
  }
}
