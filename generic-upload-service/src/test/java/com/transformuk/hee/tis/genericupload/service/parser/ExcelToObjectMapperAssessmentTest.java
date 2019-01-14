package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.AssessmentXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;

import static com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper.getDate;
import static org.assertj.core.api.Assertions.assertThat;

public class ExcelToObjectMapperAssessmentTest {

	private static final String FILE_NAME = "TIS Assessment Import Template - Test-bad-data.xlsx";

	public ExcelToObjectMapper setUpExcelToObjectMapper() throws Exception {
		Path filePath = Paths.get(getClass().getClassLoader().getResource(FILE_NAME).toURI());
		FileInputStream inputStream = new FileInputStream(filePath.toFile());
		ExcelToObjectMapper excelToObjectMapper = new ExcelToObjectMapper(inputStream, true);
		inputStream.close();
		return excelToObjectMapper;
	}

	@Test
	public void canParseDates() throws ParseException {
		LocalDate localDate = new LocalDate(2001, 7, 6);
		Assert.assertEquals(localDate.toDate(), getDate("6/7/2001"));
	}

	@Test(expected = ParseException.class)
	public void throwsAnExceptionOnBadDates() throws ParseException {
		Assert.assertNull(getDate("111/11/2124"));
	}

}
