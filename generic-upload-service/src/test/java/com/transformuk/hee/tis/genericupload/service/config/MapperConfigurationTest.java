package com.transformuk.hee.tis.genericupload.service.config;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * The unit tests for {@link MapperConfiguration}.
 */
public class MapperConfigurationTest {

  /**
   * Test that null is returned when the input is null.
   */
  @Test
  public void testConvertDateString_null_null() {
    // Call the code under test.
    LocalDate localDate = MapperConfiguration.convertDate((String) null);

    // Perform assertions.
    MatcherAssert.assertThat("The local date did not match the expected value.", localDate,
        CoreMatchers.nullValue());
  }

  /**
   * Test that an exceptions is thrown when the input contains an unhandled format.
   */
  @Test(expected = DateTimeParseException.class)
  public void testConvertDateString_invalidFormat_exception() {
    // Call the code under test.
    MapperConfiguration.convertDate("1234-5-6");
  }

  /**
   * Test that the expected local date is returned when the input contains single digits with no
   * leading zeros.
   */
  @Test
  public void testConvertDateString_noLeadingZeros_localDate() {
    // Call the code under test.
    LocalDate localDate = MapperConfiguration.convertDate("1/2/3456");

    // Perform assertions.
    LocalDate expectedDate = LocalDate.of(3456, 2, 1);
    MatcherAssert.assertThat("The local date did not match the expected value.", localDate,
        CoreMatchers.is(expectedDate));
  }

  /**
   * Test that the expected local date is returned when the input contains single digits with
   * leading zeros.
   */
  @Test
  public void testConvertDateString_hasLeadingZeros_localDate() {
    // Call the code under test.
    LocalDate localDate = MapperConfiguration.convertDate("01/02/3456");

    // Perform assertions.
    LocalDate expectedDate = LocalDate.of(3456, 2, 1);
    MatcherAssert.assertThat("The local date did not match the expected value.", localDate,
        CoreMatchers.is(expectedDate));
  }

  /**
   * Test that the expected local date is returned when the input contains double digits.
   */
  @Test
  public void testConvertDateString_doubleDigits_localDate() {
    // Call the code under test.
    LocalDate localDate = MapperConfiguration.convertDate("11/12/1314");

    // Perform assertions.
    LocalDate expectedDate = LocalDate.of(1314, 12, 11);
    MatcherAssert.assertThat("The local date did not match the expected value.", localDate,
        CoreMatchers.is(expectedDate));
  }

  /**
   * Test that null is returned when the input is null.
   */
  @Test
  public void testConvertDateDate_null_null() {
    // Call the code under test.
    LocalDate localDate = MapperConfiguration.convertDate((Date) null);

    // Perform assertions.
    MatcherAssert.assertThat("The local date did not match the expected value.", localDate,
        CoreMatchers.nullValue());
  }

  /**
   * Test that an exception is thrown when the date's year is pre-1753.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConvertDateDate_yearPre1753_exception() {
    // Set up test scenario.
    Date date = Date.from(Instant.parse("1752-01-01T00:00:00.00Z"));

    // Call the code under test.
    MapperConfiguration.convertDate(date);
  }

  /**
   * Test that the expected local date is returned when the date's year is pre-1753.
   */
  @Test
  public void testConvertDateDate_yearPost1753_localDate() {
    // Set up test scenario.
    Date date = Date.from(Instant.parse("1753-01-01T00:00:00.00Z"));

    // Call the code under test.
    LocalDate localDate = MapperConfiguration.convertDate(date);

    // Perform assertions.
    LocalDate expectedDate = LocalDate.of(1753, 1, 1);
    MatcherAssert.assertThat("The local date did not match the expected value.", localDate,
        CoreMatchers.is(expectedDate));
  }
}
