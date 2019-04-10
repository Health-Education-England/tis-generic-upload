package com.transformuk.hee.tis.genericupload.service.parser;

/**
 * A data type to represent a column mapping between import data field and internal fields.
 */
public class ColumnMapping {

  private String sourceFieldName;
  private String targetFieldName;

  private boolean required;

  ColumnMapping(String targetFieldName, String sourceFieldName) {
    this.targetFieldName = targetFieldName;
    this.sourceFieldName = sourceFieldName;
    this.required = sourceFieldName.endsWith("*");
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
