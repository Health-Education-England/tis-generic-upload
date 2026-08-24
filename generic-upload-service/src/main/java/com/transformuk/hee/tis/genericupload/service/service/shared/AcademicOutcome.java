package com.transformuk.hee.tis.genericupload.service.service.shared;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared set of valid academic outcome labels.
 */
public enum AcademicOutcome {
  CONTINUE_ON_ACADEMIC_COMPONENT("Continue on academic component"),
  DO_NOT_CONTINUE_ON_ACADEMIC_COMPONENT("Do not continue on academic component"),
  SUCCESSFULLY_COMPLETED_ACADEMIC_COMPONENT("Successfully completed academic component");

  public static final List<AcademicOutcome> ALL_VALUES =
      List.of(values());
  public static final Set<String> ALL_LABELS = Arrays.stream(values())
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
