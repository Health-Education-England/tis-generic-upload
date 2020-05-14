package com.transformuk.hee.tis.genericupload.api.dto;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AssessmentXLS extends TemplateXLS {

  private String surname;
  private String gmcNumber;
  private String gdcNumber;
  private String publicHealthNumber;
  private String type;
  private String programmeName;
  private String programmeNumber;
  private Date reviewDate;
  private String curriculumName;
  private String daysOutOfTraining;
  private Date periodCoveredFrom;
  private Date periodCoveredTo;
  private String monthsCountedToTraining;
  private String pya;
  private String outcome;
  private String underAppeal;
  private String outcomeNotAssessed;
  private String outcomeNotAssessedOther;
  private String academicOutcome;
  private String externalTrainer;
  private String nextRotationGradeName;
  private Date nextReviewDate;
  private String comments;
  private String tenPercentAudit;
  private String detailedReasons;
  private String mitigatingCircumstances;
  private String competencesToBeDeveloped;
  private String otherRecommendedActions;
  private String recommendedAdditionalTrainingTime;
  private String additionalCommentsFromPanel;
  private String knownConcerns;
  private String concernSummary;
  private String responsibleOfficerComments;
}
