package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;

/**
 * @deprecated Use {@link ColumnMapper#ColumnMapper(Class)} directly instead.
 */
@Deprecated
public class PlacementHeaderMapper extends ColumnMapper {

  public PlacementHeaderMapper() {
    super(PlacementXLS.class);
  }
}
