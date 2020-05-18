package com.transformuk.hee.tis.genericupload.service.parser;

/**
 * A data type to represent a column mapping between import data field and internal fields.
 */
public class ColumnMapping {

  private final String sourceFieldName;
  private final String targetFieldName;

  private final boolean required;

  ColumnMapping(String targetFieldName, String sourceFieldName, boolean required) {
    this.targetFieldName = targetFieldName;
    this.sourceFieldName = sourceFieldName;
    this.required = required;
  }

  public String getSourceFieldName() {
    return sourceFieldName;
  }

  public String getTargetFieldName() {
    return targetFieldName;
  }

  public boolean isRequired() {
    return required;
  }
}
