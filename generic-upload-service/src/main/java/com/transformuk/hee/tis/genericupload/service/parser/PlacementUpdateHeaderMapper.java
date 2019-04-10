package com.transformuk.hee.tis.genericupload.service.parser;

import java.util.Arrays;
import java.util.List;

public class PlacementUpdateHeaderMapper extends ColumnMapper {

  private static final List<ColumnMapping> COLUMN_MAPPINGS = Arrays.asList(
      new ColumnMapping("placementId", "TIS_Placement_ID*"),
      new ColumnMapping("intrepidId", "Intrepid_Placement_ID"),
      new ColumnMapping("nationalPostNumber", "National Post Number"),
      new ColumnMapping("dateFrom", "Date From"),
      new ColumnMapping("dateTo", "Date To"),
      new ColumnMapping("placementType", "Placement Type"),
      new ColumnMapping("site", "Site"),
      new ColumnMapping("wte", "WTE"),
      new ColumnMapping("grade", "Grade"),
      new ColumnMapping("specialty1", "Specialty1"),
      new ColumnMapping("specialty2", "Specialty2"),
      new ColumnMapping("specialty3", "Specialty3"),
      new ColumnMapping("clinicalSupervisor", "Clinical Supervisor"),
      new ColumnMapping("educationalSupervisor", "Educational Supervisor"),
      new ColumnMapping("comments", "Comments")
  );

  @Override
  List<ColumnMapping> getColumnMappings() {
    return COLUMN_MAPPINGS;
  }
}
