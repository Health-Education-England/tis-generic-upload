package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;

/**
 * @deprecated Use {@link ColumnMapper#ColumnMapper(Class)} directly instead.
 */
@Deprecated
public class PersonHeaderMapper extends ColumnMapper {

  public PersonHeaderMapper() {
    super(PersonXLS.class);
  }
}
