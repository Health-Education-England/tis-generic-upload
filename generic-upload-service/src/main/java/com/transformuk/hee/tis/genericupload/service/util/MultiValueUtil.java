package com.transformuk.hee.tis.genericupload.service.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MultiValueUtil {

  public static final String MULTI_VALUE_SEPARATOR = ";";

  private MultiValueUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Split string values by semicolon.
   *
   * @param valueToSplit the string values separated by semicolon
   * @return the list of split string values
   */
  public static List<String> splitMultiValueField(String valueToSplit) {
    if (valueToSplit != null && !valueToSplit.isEmpty()) {
      String[] splitValues = valueToSplit.split(MULTI_VALUE_SEPARATOR);
      return Arrays.stream(splitValues)
          .map(String::trim)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }
}
