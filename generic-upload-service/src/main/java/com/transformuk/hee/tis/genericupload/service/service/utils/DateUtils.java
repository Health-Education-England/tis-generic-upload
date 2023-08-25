package com.transformuk.hee.tis.genericupload.service.service.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access= AccessLevel.PRIVATE)
public class DateUtils {
  public static LocalDate toLocalDate(Date date) {
    return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }
}
