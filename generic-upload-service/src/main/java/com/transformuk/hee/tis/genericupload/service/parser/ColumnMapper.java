package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A mapper for mapping between Excel columns and DTO field names.
 */
public class ColumnMapper {

  private final List<ColumnMapping> columnMappings;

  /**
   * Create a column mapper for a given XLS DTO.
   *
   * @param dtoClass The class of the XLS DTO to create mappings for.
   */
  public ColumnMapper(Class<? extends TemplateXLS> dtoClass) {
    this.columnMappings = List.copyOf(buildMappings(dtoClass));
  }

  private Set<ColumnMapping> buildMappings(Class<? extends TemplateXLS> clazz) {
    Set<ColumnMapping> mappings = TemplateXLS.class.equals(clazz) ? new HashSet<>()
        : buildMappings((Class<? extends TemplateXLS>) clazz.getSuperclass());
    for (Field field : clazz.getDeclaredFields()) {
      ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
      if (excelColumn != null) {
        mappings.add(
            new ColumnMapping(field.getName(), excelColumn.name(), excelColumn.required()));
      }
    }
    return mappings;
  }

  List<ColumnMapping> getColumnMappings() {
    return columnMappings;
  }

  /**
   * Get mappings for all mapped fields.
   *
   * @return A map where the key is the DTO field name and the value is the Excel column name.
   */
  public Map<String, String> getFieldMap() {
    List<ColumnMapping> mappings = getColumnMappings();
    return mappings.stream().collect(
        Collectors.toMap(ColumnMapping::getTargetFieldName, ColumnMapping::getSourceFieldName));
  }

  /**
   * Get mappings for all mapped required fields.
   *
   * @return A map where the key is the DTO field name and the value is the Excel column name.
   */
  public Map<String, String> getMandatoryFieldMap() {
    List<ColumnMapping> mappings = getColumnMappings();
    return mappings.stream().filter(ColumnMapping::isRequired).collect(
        Collectors.toMap(ColumnMapping::getTargetFieldName, ColumnMapping::getSourceFieldName));
  }
}
