package com.transformuk.hee.tis.genericupload.service.service;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * An Assessment Outcome.
 */
public class Outcome implements Serializable {

  @ApiModelProperty(value = "System generated ID that is assigned to the outcome upon creation. Required for updates")
  private Long id;

  @ApiModelProperty(value = "A user friendly code that end users may know this outcome by", required = true)
  private String code;

  @ApiModelProperty(value = "A human readable label that represents the Outcome", required = true)
  private String label;

  private Set<Reason> reasons;

  private UUID uuid;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Outcome id(Long id) {
    this.id = id;
    return this;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Outcome code(String code) {
    this.code = code;
    return this;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Outcome label(String label) {
    this.label = label;
    return this;
  }

  public Set<Reason> getReasons() {
    return reasons;
  }

  public void setReasons(Set<Reason> reasons) {
    this.reasons = reasons;
  }

  public Outcome reasons(Set<Reason> reasons) {
    this.reasons = reasons;
    return this;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Outcome assessment = (Outcome) o;
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
        ", uuid=" + uuid +
        ", code='" + code + '\'' +
        ", label='" + label + '\'' +
        '}';
  }
}
