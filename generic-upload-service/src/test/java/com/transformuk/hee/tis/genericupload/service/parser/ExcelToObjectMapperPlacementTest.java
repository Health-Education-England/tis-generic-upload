package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExcelToObjectMapperPlacementTest {

  private ExcelToObjectMapper excelToObjectMapper;

  private static final String FILE_NAME = "TIS Placement Import Template - Test.xls";

  @Before
  public void setUp() throws Exception {
    String filePath = new ClassPathResource(FILE_NAME).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    excelToObjectMapper = new ExcelToObjectMapper(inputStream);
  }

  @Test
  public void shouldReturnParseObject() throws Exception {
    List<PlacementXLS> actual = excelToObjectMapper.map(PlacementXLS.class,
            new PlacementHeaderMapper().getFieldMap());
    Assert.assertNotNull(actual);
  }

  @Test
  public void shouldMapPlacementType() throws Exception {
    List<PlacementXLS> actual = excelToObjectMapper.map(PlacementXLS.class,
        new PlacementHeaderMapper().getFieldMap());
    assertThat(actual.get(0).getPlacementType()).isNotNull();
    assertThat(actual.get(0).getPlacementType()).isEqualToIgnoringCase("In Post");
  }
}
