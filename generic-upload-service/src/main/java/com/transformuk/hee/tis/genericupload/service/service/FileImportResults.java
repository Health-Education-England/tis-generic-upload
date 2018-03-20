package com.transformuk.hee.tis.genericupload.service.service;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileImportResults {
	private static Gson gson;

	private Map<Integer, String> lineNumberError;

	FileImportResults() {
		gson = new Gson();
		lineNumberError = new HashMap<>();
	}

	public void addError(int lineNumber, String errorMessage) {
		lineNumberError.put(lineNumber, errorMessage);
	}

	public Map<Integer, String> getLineNumberError() {
		return lineNumberError;
	}

	public String toJson() {
		return gson.toJson(this);
	}
}
