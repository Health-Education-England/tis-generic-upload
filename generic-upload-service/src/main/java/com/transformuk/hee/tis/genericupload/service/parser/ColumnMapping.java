package com.transformuk.hee.tis.genericupload.service.parser;

/**
 * A data type to represent a column mapping between import data field and internal fields.
 */
public class ColumnMapping {

  private final String sourceFieldName;
  private final String targetFieldName;

  private final boolean required;

  /**
   * Create a new column mapping.
   *
   * @param targetFieldName The name of the target field.
   * @param sourceFieldName The name of the source field.
   * @deprecated To be removed in favour of using annotations to determine whether required.
   */
  @Deprecated
  ColumnMapping(String targetFieldName, String sourceFieldName) {
    this(targetFieldName, sourceFieldName, sourceFieldName.endsWith("*"));
  }

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
