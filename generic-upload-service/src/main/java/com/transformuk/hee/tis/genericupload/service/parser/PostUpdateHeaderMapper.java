package com.transformuk.hee.tis.genericupload.service.parser;

import java.util.Arrays;
import java.util.List;

public class PostUpdateHeaderMapper extends ColumnMapper {

  private static final List<ColumnMapping> COLUMN_MAPPINGS = Arrays.asList(
      new ColumnMapping("postTISId", "TIS_Post_ID*"),
      new ColumnMapping("approvedGrade", "Approved grade"),
      new ColumnMapping("otherGrades", "Other grades"),
      new ColumnMapping("specialty", "Specialty"),
      new ColumnMapping("otherSpecialties", "Other specialties"),
      new ColumnMapping("subSpecialties", "Sub specialties"),
      new ColumnMapping("trainingDescription", "Training description"),
      new ColumnMapping("mainSite", "Main site"),
      new ColumnMapping("otherSites", "Other sites"),
      new ColumnMapping("trainingBody", "Training body"),
      new ColumnMapping("employingBody", "Employing body"),
      new ColumnMapping("programmeTisId", "TIS_Programme_ID"),
      new ColumnMapping("owner", "Owner"),
      new ColumnMapping("rotations", "Rotations"),
      new ColumnMapping("status", "Status"),
      new ColumnMapping("oldPost", "Old Post")
  );

  @Override
  List<ColumnMapping> getColumnMappings() {
    return COLUMN_MAPPINGS;
  }
}
