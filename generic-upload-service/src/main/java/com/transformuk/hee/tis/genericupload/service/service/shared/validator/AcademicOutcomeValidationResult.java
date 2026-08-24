package com.transformuk.hee.tis.genericupload.service.service.shared.validator;

import java.util.Optional;

/**
 * Contains the result of academic outcome validation:
 * - the curriculum name to set on the DTO if the curriculum is an assessed academic curriculum
 * - an error message if validation failed
 */
public class AcademicOutcomeValidationResult {

  private final String academicCurriculumAssessed;
  private final String error;

  public AcademicOutcomeValidationResult(String academicCurriculumAssessed, String error) {
    this.academicCurriculumAssessed = academicCurriculumAssessed;
    this.error = error;
  }

  /**
   * The curriculum name to set as academicCurriculumAssessed on the DTO,
   * present only when the curriculum is an assessed academic curriculum.
   */
  public Optional<String> getAcademicCurriculumAssessed() {
    return Optional.ofNullable(academicCurriculumAssessed);
  }

  /**
   * The validation error message, present only when validation failed.
   */
  public Optional<String> getError() {
    return Optional.ofNullable(error);
  }

  /**
   * Returns true if validation failed and an error message is present.
   */
  public boolean hasError() {
    return error != null;
  }
}

