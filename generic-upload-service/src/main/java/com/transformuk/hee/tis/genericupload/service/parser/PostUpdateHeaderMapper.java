package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;

/**
 * @deprecated Use {@link ColumnMapper#ColumnMapper(Class)} directly instead.
 */
@Deprecated
public class PostUpdateHeaderMapper extends ColumnMapper {

  public PostUpdateHeaderMapper() {
    super(PostUpdateXLS.class);
  }
}
