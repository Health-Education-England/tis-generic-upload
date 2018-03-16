package com.transformuk.hee.tis.genericupload.service.service;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileImportResults {
	private static Gson gson;

	private int numberOfErrors;
	private int numberImported;
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

	public int getNumberOfErrors() {
		return numberOfErrors;
	}

	public void setNumberOfErrors(int numberOfErrors) {
		this.numberOfErrors = numberOfErrors;
	}

	public int getNumberImported() {
		return numberImported;
	}

	public void setNumberImported(int numberImported) {
		this.numberImported = numberImported;
	}

	public String toJson() {
		return gson.toJson(this);
	}
}
