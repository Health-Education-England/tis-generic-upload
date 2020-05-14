package com.transformuk.hee.tis.genericupload.api.dto;

import java.util.Date;
import lombok.Data;

@Data
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

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getGmcNumber() {
    return gmcNumber;
  }

  public void setGmcNumber(String gmcNumber) {
    this.gmcNumber = gmcNumber;
  }

  public String getGdcNumber() {
    return gdcNumber;
  }

  public void setGdcNumber(String gdcNumber) {
    this.gdcNumber = gdcNumber;
  }

  public String getPublicHealthNumber() {
    return publicHealthNumber;
  }

  public void setPublicHealthNumber(String publicHealthNumber) {
    this.publicHealthNumber = publicHealthNumber;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getProgrammeName() {
    return programmeName;
  }

  public void setProgrammeName(String programmeName) {
    this.programmeName = programmeName;
  }

  public String getProgrammeNumber() {
    return programmeNumber;
  }

  public void setProgrammeNumber(String programmeNumber) {
    this.programmeNumber = programmeNumber;
  }

  public Date getReviewDate() {
    return reviewDate;
  }

  public void setReviewDate(Date reviewDate) {
    this.reviewDate = reviewDate;
  }

  public String getCurriculumName() {
    return curriculumName;
  }

  public void setCurriculumName(String curriculumName) {
    this.curriculumName = curriculumName;
  }

  public String getDaysOutOfTraining() {
    return daysOutOfTraining;
  }

  public void setDaysOutOfTraining(String daysOutOfTraining) {
    this.daysOutOfTraining = daysOutOfTraining;
  }

  public Date getPeriodCoveredFrom() {
    return periodCoveredFrom;
  }

  public void setPeriodCoveredFrom(Date periodCoveredFrom) {
    this.periodCoveredFrom = periodCoveredFrom;
  }

  public Date getPeriodCoveredTo() {
    return periodCoveredTo;
  }

  public void setPeriodCoveredTo(Date periodCoveredTo) {
    this.periodCoveredTo = periodCoveredTo;
  }

  public String getMonthsCountedToTraining() {
    return monthsCountedToTraining;
  }

  public void setMonthsCountedToTraining(String monthsCountedToTraining) {
    this.monthsCountedToTraining = monthsCountedToTraining;
  }

  public String getPya() {
    return pya;
  }

  public void setPya(String pya) {
    this.pya = pya;
  }

  public String getOutcome() {
    return outcome;
  }

  public void setOutcome(String outcome) {
    this.outcome = outcome;
  }

  public String getUnderAppeal() {
    return underAppeal;
  }

  public void setUnderAppeal(String underAppeal) {
    this.underAppeal = underAppeal;
  }

  public String getOutcomeNotAssessed() {
    return outcomeNotAssessed;
  }

  public void setOutcomeNotAssessed(String outcomeNotAssessed) {
    this.outcomeNotAssessed = outcomeNotAssessed;
  }

  public String getOutcomeNotAssessedOther() {
    return outcomeNotAssessedOther;
  }

  public void setOutcomeNotAssessedOther(String outcomeNotAssessedOther) {
    this.outcomeNotAssessedOther = outcomeNotAssessedOther;
  }

  public String getAcademicOutcome() {
    return academicOutcome;
  }

  public void setAcademicOutcome(String academicOutcome) {
    this.academicOutcome = academicOutcome;
  }

  public String getExternalTrainer() {
    return externalTrainer;
  }

  public void setExternalTrainer(String externalTrainer) {
    this.externalTrainer = externalTrainer;
  }

  public String getNextRotationGradeName() {
    return nextRotationGradeName;
  }

  public void setNextRotationGradeName(String nextRotationGradeName) {
    this.nextRotationGradeName = nextRotationGradeName;
  }

  public Date getNextReviewDate() {
    return nextReviewDate;
  }

  public void setNextReviewDate(Date nextReviewDate) {
    this.nextReviewDate = nextReviewDate;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getTenPercentAudit() {
    return tenPercentAudit;
  }

  public void setTenPercentAudit(String tenPercentAudit) {
    this.tenPercentAudit = tenPercentAudit;
  }

  public String getDetailedReasons() {
    return detailedReasons;
  }

  public void setDetailedReasons(String detailedReasons) {
    this.detailedReasons = detailedReasons;
  }

  public String getMitigatingCircumstances() {
    return mitigatingCircumstances;
  }

  public void setMitigatingCircumstances(String mitigatingCircumstances) {
    this.mitigatingCircumstances = mitigatingCircumstances;
  }

  public String getCompetencesToBeDeveloped() {
    return competencesToBeDeveloped;
  }

  public void setCompetencesToBeDeveloped(String competencesToBeDeveloped) {
    this.competencesToBeDeveloped = competencesToBeDeveloped;
  }

  public String getOtherRecommendedActions() {
    return otherRecommendedActions;
  }

  public void setOtherRecommendedActions(String otherRecommendedActions) {
    this.otherRecommendedActions = otherRecommendedActions;
  }

  public String getRecommendedAdditionalTrainingTime() {
    return recommendedAdditionalTrainingTime;
  }

  public void setRecommendedAdditionalTrainingTime(String recommendedAdditionalTrainingTime) {
    this.recommendedAdditionalTrainingTime = recommendedAdditionalTrainingTime;
  }

  public String getAdditionalCommentsFromPanel() {
    return additionalCommentsFromPanel;
  }

  public void setAdditionalCommentsFromPanel(String additionalCommentsFromPanel) {
    this.additionalCommentsFromPanel = additionalCommentsFromPanel;
  }

  public String getKnownConcerns() {
    return knownConcerns;
  }

  public void setKnownConcerns(String knownConcerns) {
    this.knownConcerns = knownConcerns;
  }

  public String getConcernSummary() {
    return concernSummary;
  }

  public void setConcernSummary(String concernSummary) {
    this.concernSummary = concernSummary;
  }

  public String getResponsibleOfficerComments() {
    return responsibleOfficerComments;
  }

  public void setResponsibleOfficerComments(String responsibleOfficerComments) {
    this.responsibleOfficerComments = responsibleOfficerComments;
  }
}
