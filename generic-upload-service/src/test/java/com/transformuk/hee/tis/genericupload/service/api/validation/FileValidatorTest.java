package com.transformuk.hee.tis.genericupload.service.api.validation;

import com.transformuk.hee.tis.genericupload.service.parser.ColumnMapper;
import com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@ContextConfiguration(classes = FileValidator.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class FileValidatorTest {
	@Autowired
	FileValidator fileValidator;

	/**
	 * @see FileValidator#validateMandatoryFieldsOrThrowError(List, List, Class, ExcelToObjectMapper, ColumnMapper) ()
	 * TODO change the method to not instantiate MethodArgumentNotValidException with a null parameter - 'new MethodArgumentNotValidException(null, bindingResult);'
	 *
	 * @see <a href="http://joel-costigliola.github.io/assertj/assertj-core-features-highlight.html#exception-assertion">Exception assertions guide</a>)
	 * TODO use guide above to assert the contents after getting past the 500 Response to a proper Spring error
	 */
	@Test(expected = ValidationException.class)
	public void shouldValidateMandatoryFields() throws ReflectiveOperationException, InvalidFormatException, ValidationException, IOException {
		String filename = "TIS Placement Import Template - Test 4 (multiple errors).xls";
		String filePath = new ClassPathResource(filename).getURI().getPath();
		FileInputStream inputStream = new FileInputStream(filePath);
		MultipartFile multipartFile = new MockMultipartFile("file",
				filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n", IOUtils.toByteArray(inputStream));

		fileValidator.validate(Collections.singletonList(multipartFile), true, false);
	}
}
