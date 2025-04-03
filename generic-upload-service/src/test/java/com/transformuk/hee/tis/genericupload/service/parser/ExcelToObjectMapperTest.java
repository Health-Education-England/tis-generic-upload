package com.transformuk.hee.tis.genericupload.service.parser;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ExcelToObjectMapperTest {

  @Test
  void mapShouldFinishWithErrorMessages() throws Exception {
    ExcelToObjectMapper obj = new ExcelToObjectMapper(
        new ClassPathResource("XlsToMapperTestData.xlsx",
            ExcelToObjectMapperTest.class).getInputStream(), false);
    Map<String, String> columnMap = Arrays.stream(TestTarget.class.getDeclaredFields())
        .collect(Collectors.toMap(Field::getName, Field::getName));

    LocalDate expectedLocalDate = LocalDate.parse("1970-01-19");
    Date expectedDate = new SimpleDateFormat("dd/MM/yyyy").parse("19/01/1970");

    List<TestTarget> actual = obj.map(TestTarget.class, columnMap);

    assertThat(actual.size(), equalTo(4));
    assertThat(actual.get(0).getMyLong(), nullValue());
    assertThat(actual.get(0).getMyFloat(), equalTo(1.2F));
    assertThat(actual.get(1).getMyLong(), equalTo(1L));
    assertThat(actual.get(1).getMyFloat(), equalTo(1F));
    for (int i = 2; i < actual.size(); i++) {
      assertThat(actual.get(i).getMyLong(), equalTo(1L));
      assertThat(actual.get(i).getMyFloat(), equalTo(1F));
      assertThat(actual.get(i).getMyDate(), equalTo(expectedDate));
      assertThat(actual.get(i).getMyLocalDate(), equalTo(expectedLocalDate));

    }
  }

  @Test
  void addErrorForNonLongNumber() throws Exception {
    ExcelToObjectMapper obj = new ExcelToObjectMapper(
        new ClassPathResource("XlsNotLongTestData.xlsx",
            ExcelToObjectMapperTest.class).getInputStream(), false);

    List<TestTarget> actual =
        obj.map(TestTarget.class, Collections.singletonMap("myLong", "myLong"));

    for (TestTarget row : actual) {
      assertThat(row.getMyLong(), nullValue());
      assertThat(row.getErrorMessage(),
          containsString("A whole number was expected instead of '1.2'."));
    }
  }

}