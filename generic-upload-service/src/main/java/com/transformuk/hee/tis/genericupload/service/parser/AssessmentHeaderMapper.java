package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.AssessmentXLS;

/**
 * @deprecated Use {@link ColumnMapper#ColumnMapper(Class)} directly instead.
 */
@Deprecated
public class AssessmentHeaderMapper extends ColumnMapper {

  public AssessmentHeaderMapper() {
    super(AssessmentXLS.class);
  }
}
