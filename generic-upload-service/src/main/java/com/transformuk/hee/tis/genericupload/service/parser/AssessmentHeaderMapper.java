package com.transformuk.hee.tis.genericupload.service.parser;

import java.util.Arrays;
import java.util.List;

public class AssessmentHeaderMapper extends ColumnMapper {

  private static final List<ColumnMapping> COLUMN_MAPPINGS = Arrays.asList(
      new ColumnMapping("surname", "Trainee Surname*"),
      new ColumnMapping("gmcNumber", "GMC Number"),
      new ColumnMapping("gdcNumber", "GDC Number"),
      new ColumnMapping("publicHealthNumber", "Public Health Number"),
      new ColumnMapping("type", "Type*"),
      new ColumnMapping("programmeName", "Programme name*"),
      new ColumnMapping("programmeNumber", "Programme number*"),
      new ColumnMapping("reviewDate", "Review date*"),
      new ColumnMapping("curriculumName", "Curriculum name*"),
      new ColumnMapping("daysOutOfTraining", "Days out of training"),
      new ColumnMapping("periodCoveredFrom", "Period covered from"),
      new ColumnMapping("periodCoveredTo", "Period covered to"),
      new ColumnMapping("monthsCountedToTraining", "Months OOPR/OOPT counted towards training"),
      new ColumnMapping("pya", "PYA"),
      new ColumnMapping("outcome", "Outcome"),
      new ColumnMapping("underAppeal", "Under appeal"),
      new ColumnMapping("outcomeNotAssessed", "Unsatisfactory Outcome/Not Assessed Reason"),
      new ColumnMapping("outcomeNotAssessedOther",
          "Unsatisfactory Outcome/Not Assessed Reason (Other)"),
      new ColumnMapping("academicOutcome", "Academic Outcome"),
      new ColumnMapping("externalTrainer", "External trainer"),
      new ColumnMapping("nextRotationGradeName", "Grade at next rotation"),
      new ColumnMapping("nextReviewDate", "Next review date"),
      new ColumnMapping("comments", "Comments"),
      new ColumnMapping("tenPercentAudit", "10% audit - lay member"),
      new ColumnMapping("detailedReasons", "Detailed reason"),
      new ColumnMapping("mitigatingCircumstances", "Mitigating circumstances"),
      new ColumnMapping("competencesToBeDeveloped",
          "Competences which need to be developed by next ARCP"),
      new ColumnMapping("otherRecommendedActions", "Other recommended actions"),
      new ColumnMapping("recommendedAdditionalTrainingTime",
          "Recommended additional training time (if required)"),
      new ColumnMapping("additionalCommentsFromPanel", "Additional comments from the panel"),
      new ColumnMapping("knownConcerns", "Known concerns"),
      new ColumnMapping("concernSummary", "Concern summary"),
      new ColumnMapping("responsibleOfficerComments", "Responsible officer comments")
  );

  @Override
  List<ColumnMapping> getColumnMappings() {
    return COLUMN_MAPPINGS;
  }
}
