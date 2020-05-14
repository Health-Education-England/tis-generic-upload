package com.transformuk.hee.tis.genericupload.api.dto;

import lombok.Data;

@Data
public class PostUpdateXLS extends TemplateXLS {

  private String postTISId;
  private String approvedGrade;
  private String otherGrades;
  private String specialty;
  private String otherSpecialties;
  private String subSpecialties;
  private String trainingDescription;
  private String mainSite;
  private String otherSites;
  private String trainingBody;
  private String employingBody;
  private String programmeTisId;
  private String owner;
  private String rotations;
  private String status;
  private String oldPost;

  public String getPostTISId() {
    return postTISId;
  }

  public void setPostTISId(String postTISId) {
    this.postTISId = postTISId;
  }

  public String getApprovedGrade() {
    return approvedGrade;
  }

  public void setApprovedGrade(String approvedGrade) {
    this.approvedGrade = approvedGrade;
  }

  public String getOtherGrades() {
    return otherGrades;
  }

  public void setOtherGrades(String otherGrades) {
    this.otherGrades = otherGrades;
  }

  public String getSpecialty() {
    return specialty;
  }

  public void setSpecialty(String specialty) {
    this.specialty = specialty;
  }

  public String getOtherSpecialties() {
    return otherSpecialties;
  }

  public void setOtherSpecialties(String otherSpecialties) {
    this.otherSpecialties = otherSpecialties;
  }

  public String getSubSpecialties() {
    return subSpecialties;
  }

  public void setSubSpecialties(String subSpecialties) {
    this.subSpecialties = subSpecialties;
  }

  public String getTrainingDescription() {
    return trainingDescription;
  }

  public void setTrainingDescription(String trainingDescription) {
    this.trainingDescription = trainingDescription;
  }

  public String getMainSite() {
    return mainSite;
  }

  public void setMainSite(String mainSite) {
    this.mainSite = mainSite;
  }

  public String getOtherSites() {
    return otherSites;
  }

  public void setOtherSites(String otherSites) {
    this.otherSites = otherSites;
  }

  public String getTrainingBody() {
    return trainingBody;
  }

  public void setTrainingBody(String trainingBody) {
    this.trainingBody = trainingBody;
  }

  public String getEmployingBody() {
    return employingBody;
  }

  public void setEmployingBody(String employingBody) {
    this.employingBody = employingBody;
  }

  public String getProgrammeTisId() {
    return programmeTisId;
  }

  public void setProgrammeTisId(String programmeTisId) {
    this.programmeTisId = programmeTisId;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getRotations() {
    return rotations;
  }

  public void setRotations(String rotations) {
    this.rotations = rotations;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getOldPost() {
    return oldPost;
  }

  public void setOldPost(String oldPost) {
    this.oldPost = oldPost;
  }
}
