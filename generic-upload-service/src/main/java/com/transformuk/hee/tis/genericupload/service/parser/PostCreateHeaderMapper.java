package com.transformuk.hee.tis.genericupload.service.parser;

import java.util.Arrays;
import java.util.List;

/**
 * A mapper for mapping post creation template field names to internal names.
 */
public class PostCreateHeaderMapper extends ColumnMapper {

  private static final List<ColumnMapping> COLUMN_MAPPINGS = Arrays.asList(
      new ColumnMapping("nationalPostNumber", "National Post Number*"),
      new ColumnMapping("approvedGrade", "Approved grade*"),
      new ColumnMapping("otherGrades", "Other grades"),
      new ColumnMapping("specialty", "Specialty*"),
      new ColumnMapping("otherSpecialties", "Other specialties"),
      new ColumnMapping("subSpecialties", "Sub specialties"),
      new ColumnMapping("trainingDescription", "Training description"),
      new ColumnMapping("mainSite", "Main site (Known as)*"),
      new ColumnMapping("otherSites", "Other sites (Known as)"),
      new ColumnMapping("trainingBody", "Training body*"),
      new ColumnMapping("employingBody", "Employing body*"),
      new ColumnMapping("programmeTisId", "TIS_Programme_ID*"),
      new ColumnMapping("owner", "Owner*"),
      new ColumnMapping("oldPost", "Old Post")
  );

  @Override
  List<ColumnMapping> getColumnMappings() {
    return COLUMN_MAPPINGS;
  }
}
