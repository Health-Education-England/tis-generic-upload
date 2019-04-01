package com.transformuk.hee.tis.genericupload.service.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * The unit tests for {@link ExcelToObjectMapper}.
 */
public class ExcelToObjectMapperTest {

  /**
   * Test that an exceptions is thrown when the input contains an unhandled format.
   */
  @Test(expected = DateTimeParseException.class)
  public void testGetLocalDate_invalidFormat_exception() {
    // Call the code under test.
    ExcelToObjectMapper.getLocalDate("1234-5-6");
  }

  /**
   * Test that the expected local date is returned when the input contains single digits with no
   * leading zeros.
   */
  @Test
  public void testGetLocalDate_noLeadingZeros_localDate() {
    // Call the code under test.
    LocalDate localDate = ExcelToObjectMapper.getLocalDate("1/2/3456");

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
  public void testGetLocalDate_hasLeadingZeros_localDate() {
    // Call the code under test.
    LocalDate localDate = ExcelToObjectMapper.getLocalDate("01/02/3456");

    // Perform assertions.
    LocalDate expectedDate = LocalDate.of(3456, 2, 1);
    MatcherAssert.assertThat("The local date did not match the expected value.", localDate,
        CoreMatchers.is(expectedDate));
  }

  /**
   * Test that the expected local date is returned when the input contains double digits.
   */
  @Test
  public void testGetLocalDate_doubleDigits_localDate() {
    // Call the code under test.
    LocalDate localDate = ExcelToObjectMapper.getLocalDate("11/12/1314");

    // Perform assertions.
    LocalDate expectedDate = LocalDate.of(1314, 12, 11);
    MatcherAssert.assertThat("The local date did not match the expected value.", localDate,
        CoreMatchers.is(expectedDate));
  }
}
