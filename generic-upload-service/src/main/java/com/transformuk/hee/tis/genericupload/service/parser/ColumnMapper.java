package com.transformuk.hee.tis.genericupload.service.parser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ColumnMapper {

  public Map<String, String> getFieldMap() {
    List<ColumnMapping> columnMappings = getColumnMappings();
    return columnMappings.stream().collect(
        Collectors.toMap(ColumnMapping::getTargetFieldName, ColumnMapping::getSourceFieldName));
  }

  public Map<String, String> getMandatoryFieldMap() {
    List<ColumnMapping> columnMappings = getColumnMappings();
    return columnMappings.stream().filter(ColumnMapping::isRequired).collect(
        Collectors.toMap(ColumnMapping::getTargetFieldName, ColumnMapping::getSourceFieldName));
  }

  abstract List<ColumnMapping> getColumnMappings();
}
