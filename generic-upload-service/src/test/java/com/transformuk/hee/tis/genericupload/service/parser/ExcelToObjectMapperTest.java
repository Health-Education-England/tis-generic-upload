package com.transformuk.hee.tis.genericupload.service.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ExcelToObjectMapperTest {

  ExcelToObjectMapper obj;

  @BeforeEach
  void setupData() throws Exception {
    obj = new ExcelToObjectMapper(
        new ClassPathResource("XlsToMapperTestData.xlsx",
            ExcelToObjectMapperTest.class).getInputStream(), false);

  }

  @Test
  void map() throws Exception {
    Map<String, String> columnMap = Arrays.stream(TestTarget.class.getDeclaredFields())
        .collect(Collectors.toMap(Field::getName, Field::getName));

    LocalDate expectedLocalDate = LocalDate.parse("1970-01-19");
    Date expectedDate = new SimpleDateFormat("dd/MM/yyyy").parse("19/01/1970");

    List<TestTarget> actual = obj.map(TestTarget.class, columnMap);

    assertThat(actual.size(), equalTo(4));
    assertThat(actual.get(0).getMyLong(), equalTo(1L));
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

}