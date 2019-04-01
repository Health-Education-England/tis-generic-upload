package com.transformuk.hee.tis.genericupload.service.parser;

import java.util.Arrays;
import java.util.List;

public class PostFundingUpdateHeaderMapper extends ColumnMapper {

  private static final List<ColumnMapping> COLUMN_MAPPINGS = Arrays.asList(
      new ColumnMapping("postTisId", "TIS_Post_ID*"),
      new ColumnMapping("fundingType", "Funding type"),
      new ColumnMapping("fundingTypeOther", "Funding type - If 'Other' please specify"),
      new ColumnMapping("fundingBody", "Funding Body"),
      new ColumnMapping("dateFrom", "Date From"),
      new ColumnMapping("dateTo", "Date to")
  );

  //@Override
  List<ColumnMapping> getColumnMappings() {
    return COLUMN_MAPPINGS;
  }
}
