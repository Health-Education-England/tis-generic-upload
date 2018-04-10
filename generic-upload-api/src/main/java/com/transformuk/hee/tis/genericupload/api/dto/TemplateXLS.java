package com.transformuk.hee.tis.genericupload.api.dto;

import org.springframework.util.StringUtils;

public class TemplateXLS {
	private int rowNumber;
	private String errorMessage;
	private boolean successfullyImported;

	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void addErrorMessage(String errorMessage) {
		this.errorMessage = this.errorMessage == null ? errorMessage : this.errorMessage + System.lineSeparator() + errorMessage;
	}

	public boolean hasErrors() {
		return !StringUtils.isEmpty(this.errorMessage);
	}

	public boolean isSuccessfullyImported() {
		return successfullyImported;
	}

	public void setSuccessfullyImported(boolean successfullyImported) {
		this.successfullyImported = successfullyImported;
	}

	public void initialiseSuccessfullyImported() { this.successfullyImported = false; }
}
