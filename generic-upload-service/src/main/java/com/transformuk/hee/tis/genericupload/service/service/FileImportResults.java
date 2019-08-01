package com.transformuk.hee.tis.genericupload.service.service;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class FileImportResults {

  private static Gson gson;

  private Map<Integer, String> lineNumberErrors;

  FileImportResults() {
    gson = new Gson();
    lineNumberErrors = new HashMap<>();
  }

  public void addError(int lineNumber, String errorMessage) {
    lineNumberErrors.put(lineNumber, errorMessage);
  }

  public Map<Integer, String> getLineNumberErrors() {
    return lineNumberErrors;
  }

  public String toJson() {
    return gson.toJson(this);
  }
}
