package com.transformuk.hee.tis.genericupload.service.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
import java.io.FileInputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class ExcelToObjectMapperPlacementTest {

  private static final String FILE_NAME = "TIS Placement Import Template - Test.xlsx";
  private ExcelToObjectMapper excelToObjectMapper;

  @Before
  public void setUp() throws Exception {
    String filePath = new ClassPathResource(FILE_NAME).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    excelToObjectMapper = new ExcelToObjectMapper(inputStream, false);
  }

  @Test
  public void shouldReturnParseObject() throws Exception {
    List<PlacementXLS> actual = excelToObjectMapper.map(PlacementXLS.class,
        new PlacementHeaderMapper().getFieldMap());
    Assert.assertNotNull(actual);
  }

  @Test
  public void shouldMapPlacement() throws Exception {
    List<PlacementXLS> actual = excelToObjectMapper.map(PlacementXLS.class,
        new PlacementHeaderMapper().getFieldMap());
    assertThat(actual.get(0).getPlacementStatus()).isNotNull();
  }

  @Test
  public void shouldMapPlacementTypeAndWTE() throws Exception {
    List<PlacementXLS> actual = excelToObjectMapper.map(PlacementXLS.class,
        new PlacementHeaderMapper().getFieldMap());
    assertThat(actual.get(0).getPlacementType()).isNotNull();
    assertThat(actual.get(0).getPlacementType()).isEqualToIgnoringCase("In Post");
    assertThat(actual.get(2).getWte()).isEqualTo(0.3f);
  }

  @Test
  public void shouldMapNPNs() throws Exception {
    List<PlacementXLS> actual = excelToObjectMapper.map(PlacementXLS.class,
        new PlacementHeaderMapper().getFieldMap());

    Set<String> placementNPNs = actual.stream()
        .map(PlacementXLS::getNationalPostNumber)
        .collect(Collectors.toSet());
    assertThat(placementNPNs.size()).isEqualTo(9);
  }

  @Test
  public void shouldEscapeForJson() throws Exception {
    List<PlacementXLS> actual = excelToObjectMapper.map(PlacementXLS.class,
        new PlacementHeaderMapper().getFieldMap());
    Assert.assertThat("Should escape for Json", actual.get(9).getSite(),
        CoreMatchers.equalTo("This is for \\\"test\\\\"));
  }
}
