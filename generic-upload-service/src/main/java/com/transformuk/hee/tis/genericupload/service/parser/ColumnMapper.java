package com.transformuk.hee.tis.genericupload.service.parser;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class ColumnMapper {
  protected static String[] fieldNameSource, fieldNameTarget;

  public Map<String, String> getFieldMap() {
    return createFieldMap(fieldNameSource, fieldNameTarget);
  }

  public Map<String,String> getMandatoryFieldMap(){
    return this.getFieldMap().entrySet().stream().filter(map -> map.getValue().contains("*")).
        collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
  }


  protected Map<String, String> createFieldMap(String[] fieldNameSource, String[] fieldNameTarget){
    Map<String,String> fieldMap = Maps.newHashMap();
    if(fieldNameSource != null && fieldNameTarget != null) {
      fieldMap = IntStream.range(0, fieldNameSource.length).boxed()
          .collect(Collectors.toMap(i -> fieldNameSource[i].toLowerCase(),
              i -> fieldNameTarget[i].toLowerCase()));
    }
    return fieldMap;
  }
}
