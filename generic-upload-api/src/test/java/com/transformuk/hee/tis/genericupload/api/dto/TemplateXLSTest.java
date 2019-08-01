package com.transformuk.hee.tis.genericupload.api.dto;

import org.junit.Assert;
import org.junit.Test;

public class TemplateXLSTest {

  @Test
  public void checksForErrorMessages() {
    TemplateXLS templateXLS = new TemplateXLS();
    templateXLS.addErrorMessage("its an error");

    Assert.assertTrue(templateXLS.hasErrors());
  }
}

