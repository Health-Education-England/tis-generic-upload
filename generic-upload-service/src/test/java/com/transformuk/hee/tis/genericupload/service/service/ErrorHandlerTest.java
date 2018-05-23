package com.transformuk.hee.tis.genericupload.service.service;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorHandlerTest {
	@Test
	public void canParseFieldErrorsIfNull() {
		String errorJson = "{\"message\":\"error.internalServerError\",\"description\":\"Internal server error\",\"fieldErrors\":null}";
		String errorMessage = new ErrorHandler().getSingleMessageFromSpringJsonErrorMessages(errorJson);
		assertThat(errorMessage).isNotNull();
	}

	@Test
	public void canIgnoreFieldErrorsAndUseDescription() {
		String errorJson = "{\"message\":\"error.internalServerError\",\"description\":\"Internal server error\",\"fieldErrors\":null}";
		String errorMessage = new ErrorHandler().getSingleMessageFromSpringJsonErrorMessages(errorJson);
		assertThat(errorMessage).isEqualToIgnoringCase("Internal server error");
	}
}
