package com.transformuk.hee.tis.genericupload.service.parser;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface ColumnMapper {
  Map<String,String> getFieldMap();

  Map<String,String> getMandatoryFieldMap();

  default Map<String, String> createFieldMap(String[] fieldNameSource, String[] fieldNameTarget){
    Map<String,String> fieldMap = Maps.newHashMap();
    if(fieldNameSource != null && fieldNameTarget != null) {
      fieldMap = IntStream.range(0, fieldNameSource.length).boxed()
          .collect(Collectors.toMap(i -> fieldNameSource[i].toLowerCase(),
              i -> fieldNameTarget[i].toLowerCase()));
    }
    return fieldMap;
  }
}
