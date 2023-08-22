package com.transformuk.hee.tis.genericupload.service.service.utils;

import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

public class DateUtilsTest {

  @Test
  public void shouldReturnLocalDate() {
    Calendar cal = Calendar.getInstance();
    cal.set(2023, Calendar.AUGUST, 22);
    Date date = cal.getTime();

    LocalDate localDate = DateUtils.toLocalDate(date);
    Assert.assertEquals(2023, localDate.getYear());
    Assert.assertEquals(Month.AUGUST, localDate.getMonth());
    Assert.assertEquals(22, localDate.getDayOfMonth());
  }

  @Test
  public void shouldReturnNullWhenDateIsNull() {
    LocalDate localDate = DateUtils.toLocalDate(null);
    Assert.assertNull(localDate);
  }
}
