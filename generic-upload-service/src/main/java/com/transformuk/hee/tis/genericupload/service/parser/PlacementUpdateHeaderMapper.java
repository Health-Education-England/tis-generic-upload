package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementUpdateXLS;

/**
 * @deprecated Use {@link ColumnMapper#ColumnMapper(Class)} directly instead.
 */
@Deprecated
public class PlacementUpdateHeaderMapper extends ColumnMapper {

  public PlacementUpdateHeaderMapper() {
    super(PlacementUpdateXLS.class);
  }
}
