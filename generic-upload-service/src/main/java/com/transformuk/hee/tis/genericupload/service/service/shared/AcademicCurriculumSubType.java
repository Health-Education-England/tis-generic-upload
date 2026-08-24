package com.transformuk.hee.tis.genericupload.service.service.shared;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Subset of curriculum subtypes that require an academic outcome.
 */
public enum AcademicCurriculumSubType {
  AFT,
  ACLNIHR_FUNDING,
  ACL_OTHER_FUNDING,
  ACFNIHR_FUNDING,
  ACF_OTHER_FUNDING;

  private static final Set<String> ALL_VALUES = Arrays.stream(values())
      .map(Enum::name)
      .collect(Collectors.toSet());

  /**
   * Returns true if the given curriculum subtype is an academic curriculum subtype.
   */
  public static boolean isAcademic(String curriculumSubType) {
    return ALL_VALUES.contains(curriculumSubType);
  }
}

