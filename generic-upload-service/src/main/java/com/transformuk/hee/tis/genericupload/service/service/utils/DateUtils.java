package com.transformuk.hee.tis.genericupload.service.service.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {
  public static LocalDate toLocalDate(Date date) {
    return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }
}
