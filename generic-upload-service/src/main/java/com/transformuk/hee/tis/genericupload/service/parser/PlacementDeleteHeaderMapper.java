package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementDeleteXLS;

/**
 * @deprecated Use {@link ColumnMapper#ColumnMapper(Class)} directly instead.
 */
@Deprecated
public class PlacementDeleteHeaderMapper extends ColumnMapper {

  public PlacementDeleteHeaderMapper() {
    super(PlacementDeleteXLS.class);
  }
}
