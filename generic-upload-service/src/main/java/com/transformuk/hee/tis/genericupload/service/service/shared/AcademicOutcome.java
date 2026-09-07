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


package com.transformuk.hee.tis.genericupload.service.service.shared;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared set of valid academic outcome labels.
 */
public enum AcademicOutcome {
  CONTINUE_ON_ACADEMIC_COMPONENT("Continue on academic component"),
  DO_NOT_CONTINUE_ON_ACADEMIC_COMPONENT("Do not continue on academic component"),
  SUCCESSFULLY_COMPLETED_ACADEMIC_COMPONENT("Successfully completed academic component");

  private static final Set<String> ALL_LABELS = Arrays.stream(values())
      .map(AcademicOutcome::getLabel)
      .collect(Collectors.toSet());

  private final String label;

  AcademicOutcome(String label) {
    this.label = label;
  }

  /**
   * Returns the label of the academic outcome.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns true if the given label is a valid academic outcome label.
   */
  public static boolean isValid(String label) {
    return ALL_LABELS.contains(label);
  }
}
