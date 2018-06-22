package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class ErrorHandler {
	private static final Logger logger = getLogger(ErrorHandler.class);

	public void recordErrorMessageOnTemplateOrLogUnknown(TemplateXLS templateXLS, ResourceAccessException rae) {
		if(rae.getCause() != null && rae.getCause() instanceof IOException) {
			IOException ioe = (IOException) rae.getCause();
			templateXLS.addErrorMessage(getSingleMessageFromSpringJsonErrorMessages(ioe.getMessage()));
		} else {
			logger.error("Unexpected exception : {}", rae.getMessage());
		}
	}

	public String getSingleMessageFromSpringJsonErrorMessages(String responseJson) {
		JSONObject jsonObject = new JSONObject(responseJson);
		StringBuilder sb = new StringBuilder();

		Object fieldErrorsString = jsonObject.get("fieldErrors");
		if(fieldErrorsString != null && !fieldErrorsString.equals(JSONObject.NULL)) {
			JSONArray fieldErrors = jsonObject.getJSONArray("fieldErrors");
			for (int i = 0; i < fieldErrors.length(); i++) {
				sb.append(fieldErrors.getJSONObject(i).get("message"));
				sb.append(System.lineSeparator());
			}
		}

		Object fieldDescString = jsonObject.get("description");
		if(fieldDescString != null && !fieldDescString.equals(JSONObject.NULL)) {
			sb.append(fieldDescString);
		}
		return sb.toString();
	}
}
