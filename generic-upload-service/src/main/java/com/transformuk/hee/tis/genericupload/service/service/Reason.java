package com.transformuk.hee.tis.genericupload.service.service;


import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * A Assessment Reason.
 */
public class Reason implements Serializable {

  @ApiModelProperty(value = "System generated ID that is assigned to the reason upon creation. Required for updates")
  private Long id;

  @ApiModelProperty(value = "A user friendly code that end users may know this reason by", required = true)
  private String code;

  @ApiModelProperty(value = "A human readable label that represents the Reason", required = true)
  private String label;

  private Set<Outcome> outcomes;

  @ApiModelProperty(value = "indicator to state that if this reason is selected, that the 'other' field needs to be filled", required = true)
  private boolean requireOther;

  @ApiModelProperty(value = "Whether this reason was from intrepid and therefore is legacy")
  private boolean isLegacy;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Reason id(Long id) {
    this.id = id;
    return this;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Reason code(String code) {
    this.code = code;
    return this;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Reason label(String label) {
    this.label = label;
    return this;
  }

  public Set<Outcome> getOutcomes() {
    return outcomes;
  }

  public void setOutcomes(Set<Outcome> outcomes) {
    this.outcomes = outcomes;
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
    Reason assessment = (Reason) o;
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
            ", outcomes=" + outcomes +
            ", requireOther=" + requireOther +
            ", isLegacy=" + isLegacy +
            '}';
  }
}
