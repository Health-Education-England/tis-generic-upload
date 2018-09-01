package com.transformuk.hee.tis.genericupload.service.service;


import java.io.Serializable;
import java.util.Objects;

/**
 * A Reason.
 */
public class AssessmentReason implements Serializable {

  public AssessmentReason(Long id, String code, String label, boolean requireOther, boolean isLegacy) {
    this.id = id;
    this.code = code;
    this.label = label;
    this.requireOther = requireOther;
    this.isLegacy = isLegacy;
  }

  private Long id;

  private String code;

  private String label;

  private boolean requireOther;

  private boolean isLegacy;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public boolean isRequireOther() {
    return requireOther;
  }

  public void setRequireOther(boolean requireOther) {
    this.requireOther = requireOther;
  }

  public boolean isLegacy() {
    return isLegacy;
  }

  public void setLegacy(boolean legacy) {
    isLegacy = legacy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AssessmentReason assessment = (AssessmentReason) o;
    if (assessment.getId() == null || getId() == null) {
      return false;
    }
    return Objects.equals(getId(), assessment.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public String toString() {
    return "Reason{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", label='" + label + '\'' +
            ", requireOther=" + requireOther +
            ", isLegacy=" + isLegacy +
            '}';
  }
}
