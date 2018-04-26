package com.transformuk.hee.tis.genericupload.service.api.validation;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

import static org.slf4j.LoggerFactory.getLogger;

@ContextConfiguration(classes = FileValidator.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class FileValidatorTest {
	private static final Logger logger = getLogger(FileValidatorTest.class);

	@Autowired
	FileValidator fileValidator;

	@Test(expected = ValidationException.class)
	public void shouldValidateMandatoryFields() throws ReflectiveOperationException, InvalidFormatException, ValidationException, IOException {
		String filename = "TIS Placement Import Template - Test 4 (multiple errors).xls";
		String filePath = new ClassPathResource(filename).getURI().getPath();
		FileInputStream inputStream = new FileInputStream(filePath);
		MultipartFile multipartFile = new MockMultipartFile("file",
				filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n", IOUtils.toByteArray(inputStream));

		fileValidator.validate(Collections.singletonList(multipartFile), true, false);
	}

	@Test
	public void shouldErrorWithMessage() throws ReflectiveOperationException, InvalidFormatException, ValidationException, IOException {
		String filename = "TIS Placement Import Template - Test 4 (multiple errors).xls";
		String filePath = new ClassPathResource(filename).getURI().getPath();
		FileInputStream inputStream = new FileInputStream(filePath);
		MultipartFile multipartFile = new MockMultipartFile("file",
				filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n", IOUtils.toByteArray(inputStream));
		try {
			fileValidator.validate(Collections.singletonList(multipartFile), true, false);
		} catch(ValidationException ve){
			BindingResult bindingResult = ve.getBindingResult();
			Assert.assertEquals(4, bindingResult.getErrorCount());
		}
	}

	@Test
	public void shouldErrorForIncorrectDatesOnMandatoryDateFieldsWithMessage() throws ReflectiveOperationException, InvalidFormatException, ValidationException, IOException {
		String filename = "TIS Placement Import Template - Test 4 (multiple errors).xls";
		String filePath = new ClassPathResource(filename).getURI().getPath();
		FileInputStream inputStream = new FileInputStream(filePath);
		MultipartFile multipartFile = new MockMultipartFile("file",
				filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n", IOUtils.toByteArray(inputStream));
		try {
			fileValidator.validate(Collections.singletonList(multipartFile), true, true);
		} catch(ValidationException ve){
			Assert.assertTrue(oneOfTheFieldErrorsIs(ve.getBindingResult(), "Date missing"));
		}
	}

	private boolean oneOfTheFieldErrorsIs(BindingResult bindingResult, String subStringErrorToLookFor) {
		logger.info(bindingResult.getFieldErrors().toString());

		for(FieldError fieldError : bindingResult.getFieldErrors()) {
			if(fieldError.toString().contains(subStringErrorToLookFor)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void shouldErrorForMissingDatesOnMandatoryDateFieldsWithMessage() throws ReflectiveOperationException, InvalidFormatException, ValidationException, IOException {
		String filename = "TIS Placement Import Template - Test 4 (multiple errors).xls";
		String filePath = new ClassPathResource(filename).getURI().getPath();
		FileInputStream inputStream = new FileInputStream(filePath);
		MultipartFile multipartFile = new MockMultipartFile("file",
				filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n", IOUtils.toByteArray(inputStream));
		try {
			fileValidator.validate(Collections.singletonList(multipartFile), true, true);
		} catch(ValidationException ve){
			Assert.assertTrue(oneOfTheFieldErrorsIs(ve.getBindingResult(), "Date missing"));
		}
	}
}
