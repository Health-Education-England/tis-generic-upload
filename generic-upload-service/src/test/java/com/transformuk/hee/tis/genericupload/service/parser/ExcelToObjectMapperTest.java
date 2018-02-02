package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ExcelToObjectMapperTest {

  private ExcelToObjectMapper excelToObjectMapper;

  private static final String FILE_NAME = "Intrepid Recruitment Import Template v9.xls";

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

}
