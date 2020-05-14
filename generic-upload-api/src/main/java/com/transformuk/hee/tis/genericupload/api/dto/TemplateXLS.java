package com.transformuk.hee.tis.genericupload.api.dto;

import java.util.List;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class TemplateXLS {

  private int rowNumber;
  private String errorMessage;
  private boolean successfullyImported;

  public void addErrorMessage(String errorMessage) {
    this.errorMessage = this.errorMessage == null ? errorMessage
        : this.errorMessage + System.lineSeparator() + errorMessage;
  }

  public void addErrorMessages(List<String> errorMessages) {
    for (String errorMessage : errorMessages) {
      addErrorMessage(errorMessage);
    }
  }

  public boolean hasErrors() {
    return !StringUtils.isEmpty(this.errorMessage);
  }

  public void initialiseSuccessfullyImported() {
    this.successfullyImported = false;
  }
}
