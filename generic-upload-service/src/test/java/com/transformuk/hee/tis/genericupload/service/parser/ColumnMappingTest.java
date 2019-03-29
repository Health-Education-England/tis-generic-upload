package com.transformuk.hee.tis.genericupload.service.parser;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * The unit tests for {@link ColumnMapping}.
 */
public class ColumnMappingTest {

  /**
   * Test that isRequired is false when the source name does not end with an asterisk.
   */
  @Test
  public void isRequired_noAsterisk_false() {
    // Set up test scenario.
    ColumnMapping columnMapping = new ColumnMapping("target", "source");

    // Perform assertions.
    MatcherAssert.assertThat("The isRequired flag did not match the expected value.",
        columnMapping.isRequired(), CoreMatchers.is(false));
  }

  /**
   * Test that isRequired is true when the source name does end with an asterisk.
   */
  @Test
  public void isRequired_hasAsterisk_true() {
    // Set up test scenario.
    ColumnMapping columnMapping = new ColumnMapping("target", "source*");

    // Perform assertions.
    MatcherAssert.assertThat("The isRequired flag did not match the expected value.",
        columnMapping.isRequired(), CoreMatchers.is(true));
  }
}
