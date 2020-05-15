package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.PostCreateXls;

/**
 * @deprecated Use {@link ColumnMapper#ColumnMapper(Class)} directly instead.
 */
@Deprecated
public class PostCreateHeaderMapper extends ColumnMapper {

  public PostCreateHeaderMapper() {
    super(PostCreateXls.class);
  }
}
