package com.transformuk.hee.tis.genericupload.service.service.shared.validator;

import java.util.Optional;

/**
 * Contains the result of academic outcome validation:
 * - an error message if validation failed.
 */
public class AcademicOutcomeValidationResult {

  private final String error;

  public AcademicOutcomeValidationResult(String error) {
    this.error = error;
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

