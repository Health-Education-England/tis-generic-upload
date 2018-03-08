package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExcelToObjectMapperTest {

  private ExcelToObjectMapper excelToObjectMapper;

  private static final String FILE_NAME = "TIS Recruiment Import Template - test.xlsx";

  @Before
  public void setUp() throws Exception {
    String filePath = new ClassPathResource(FILE_NAME).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    excelToObjectMapper = new ExcelToObjectMapper(inputStream);
  }

  @Test
  public void shouldReturnParseObject() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
            new PersonHeaderMapper().getFieldMap());
    Assert.assertNotNull(actual);
  }

  @Test
  public void shouldMapRecordStatus() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
            new PersonHeaderMapper().getFieldMap());
    assertThat(actual.get(0).getRecordStatus()).isNotNull();
  }

  @Test
  public void shouldMapProgrammeMembershipType() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
            new PersonHeaderMapper().getFieldMap());
    assertThat(actual.get(0).getProgrammeMembership()).isNotNull();
  }

  @Test
  public void shouldSkipEmptyRows() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
            new PersonHeaderMapper().getFieldMap());
    Assert.assertEquals(186, actual.size());
  }

  @Test
  public void shouldHaveGMCNumberAndNoNI() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());
    Assert.assertEquals("7463954", actual.get(13).getGmcNumber());
    Assert.assertEquals(null, actual.get(13).getNiNumber());
    Assert.assertEquals(14, actual.get(13).getRowNumber());
  }
}
