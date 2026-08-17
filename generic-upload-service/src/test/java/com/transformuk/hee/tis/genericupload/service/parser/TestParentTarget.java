package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;

public class TestParentTarget extends TemplateXLS {

  private Long myInheritedLong;

  public Long getMyInheritedLong() {
    return myInheritedLong;
  }

  public void setMyInheritedLong(Long myInheritedLong) {
    this.myInheritedLong = myInheritedLong;
  }

}
