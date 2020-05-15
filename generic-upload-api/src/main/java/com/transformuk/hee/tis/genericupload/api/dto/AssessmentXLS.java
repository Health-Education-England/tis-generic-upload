package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AssessmentXLS extends TemplateXLS {

  @ExcelColumn(name = "Trainee Surname*", required = true)
  private String surname;

  @ExcelColumn(name = "GMC Number")
  private String gmcNumber;

  @ExcelColumn(name = "GDC Number")
  private String gdcNumber;

  @ExcelColumn(name = "Public Health Number")
  private String publicHealthNumber;

  @ExcelColumn(name = "Type*", required = true)
  private String type;

  @ExcelColumn(name = "Programme name*", required = true)
  private String programmeName;

  @ExcelColumn(name = "Programme number*", required = true)
  private String programmeNumber;

  @ExcelColumn(name = "Review date*", required = true)
  private Date reviewDate;

  @ExcelColumn(name = "Curriculum name*", required = true)
  private String curriculumName;

  @ExcelColumn(name = "Days out of training")
  private String daysOutOfTraining;

  @ExcelColumn(name = "Period covered from")
  private Date periodCoveredFrom;

  @ExcelColumn(name = "Period covered to")
  private Date periodCoveredTo;

  @ExcelColumn(name = "Months OOPR/OOPT counted towards training")
  private String monthsCountedToTraining;

  @ExcelColumn(name = "PYA")
  private String pya;

  @ExcelColumn(name = "Outcome")
  private String outcome;

  @ExcelColumn(name = "Under appeal")
  private String underAppeal;

  @ExcelColumn(name = "Unsatisfactory Outcome/Not Assessed Reason")
  private String outcomeNotAssessed;

  @ExcelColumn(name = "Unsatisfactory Outcome/Not Assessed Reason (Other)")
  private String outcomeNotAssessedOther;

  @ExcelColumn(name = "Academic Outcome")
  private String academicOutcome;

  @ExcelColumn(name = "External trainer")
  private String externalTrainer;

  @ExcelColumn(name = "Grade at next rotation")
  private String nextRotationGradeName;

  @ExcelColumn(name = "Next review date")
  private Date nextReviewDate;

  @ExcelColumn(name = "Comments")
  private String comments;

  @ExcelColumn(name = "10% audit - lay member")
  private String tenPercentAudit;

  @ExcelColumn(name = "Detailed reason")
  private String detailedReasons;

  @ExcelColumn(name = "Mitigating circumstances")
  private String mitigatingCircumstances;

  @ExcelColumn(name = "Competences which need to be developed by next ARCP")
  private String competencesToBeDeveloped;

  @ExcelColumn(name = "Other recommended actions")
  private String otherRecommendedActions;

  @ExcelColumn(name = "Recommended additional training time (if required)")
  private String recommendedAdditionalTrainingTime;

  @ExcelColumn(name = "Additional comments from the panel")
  private String additionalCommentsFromPanel;

  @ExcelColumn(name = "Known concerns")
  private String knownConcerns;

  @ExcelColumn(name = "Concern summary")
  private String concernSummary;

  @ExcelColumn(name = "Responsible officer comments")
  private String responsibleOfficerComments;
}
