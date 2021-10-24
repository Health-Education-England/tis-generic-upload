package com.transformuk.hee.tis.genericupload.service.util;

import java.text.ParseException;

public class BooleanUtil {

  public static Boolean parseBooleanObject(String value) {
    if ("YES".equalsIgnoreCase(value)) {
      return true;
    }
    return false;
  }

  /**
   * Parse a String(YES/NO) to a Boolean, case-insensitive.
   *
   * @param value to be converted
   * @return Boolean value
   * @throws ParseException when the String value is not YES or NO
   */
  public static Boolean parseBoolean(String value) throws ParseException {
    if ("YES".equalsIgnoreCase(value)) {
      return true;
    } else if ("NO".equalsIgnoreCase(value)) {
      return false;
    } else {
      throw new ParseException("Invalid value", 0);
    }
  }
}
