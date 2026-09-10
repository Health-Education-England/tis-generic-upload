/*
 * The MIT License (MIT)
 *
 * Copyright 2026 Crown Copyright (NHS England)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.transformuk.hee.tis.genericupload.service.service.shared.validator;

import java.util.Optional;

/**
 * Contains the result of academic outcome validation:
 * - the curriculum name to set on the DTO if the curriculum is an assessed academic curriculum.
 * - an error message if validation failed.
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

