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
}
