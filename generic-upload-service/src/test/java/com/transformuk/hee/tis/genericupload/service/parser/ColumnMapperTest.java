package com.transformuk.hee.tis.genericupload.service.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ColumnMapperTest {

  private ColumnMapper mapper;

  @Before
  public void setUp() {
    mapper = new ColumnMapper(TestDto.class);
  }

  @Test
  public void getFieldMapShouldReturnAllMappedFields() {
    // When.
    Map<String, String> fieldMap = mapper.getFieldMap();

    // Then.
    assertThat("Unexpected number of fields.", fieldMap.size(), is(4));
    assertThat("Unexpected field mapping value.", fieldMap.get("requiredField1"),
        is("Required Field 1*"));
    assertThat("Unexpected field mapping value.", fieldMap.get("requiredField2"),
        is("Required Field 2"));
    assertThat("Unexpected field mapping value.", fieldMap.get("field1"), is("Field 1*"));
    assertThat("Unexpected field mapping value.", fieldMap.get("field2"), is("Field 2"));
  }

  @Test
  public void getMandatoryFieldMapShouldReturnOnlyRequiredMappedFields() {
    // When.
    Map<String, String> fieldMap = mapper.getMandatoryFieldMap();

    // Then.
    assertThat("Unexpected number of fields.", fieldMap.size(), is(2));
    assertThat("Unexpected field mapping value.", fieldMap.get("requiredField1"),
        is("Required Field 1*"));
    assertThat("Unexpected field mapping value.", fieldMap.get("requiredField2"),
        is("Required Field 2"));
  }

  /**
   * A private DTO to test the behaviour of the ColumnMapper in a controlled way.
   */
  @SuppressWarnings("unused")
  private static class TestDto extends TemplateXLS {

    @ExcelColumn(name = "Required Field 1*", required = true)
    private String requiredField1;

    @ExcelColumn(name = "Required Field 2", required = true)
    private String requiredField2;

    @ExcelColumn(name = "Field 1*")
    private String field1;

    @ExcelColumn(name = "Field 2")
    private String field2;

    private String unmappedField;
  }
}
