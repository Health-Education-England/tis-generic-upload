package com.transformuk.hee.tis.genericupload.service.service.impl;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class UploadFileServiceImplTest {
	@Test
	public void removeRowDoesNotActuallyRemoveARow() throws IOException, InvalidFormatException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(Files.readAllBytes(Paths.get(new ClassPathResource("TIS People Import Template - empty row.xlsx").getURI().getPath())))) {
			Workbook workbook = WorkbookFactory.create(bis);
			Sheet sheet = workbook.getSheetAt(0);
			int numberOfRows = sheet.getLastRowNum();
			sheet.removeRow(sheet.getRow(3));
			int rowsAfterDeletion = sheet.getLastRowNum();
			assertThat(numberOfRows).isEqualTo(rowsAfterDeletion);
		}
	}

	@Test
	public void haveToShiftRowsUpToRemoveARow() throws IOException, InvalidFormatException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(Files.readAllBytes(Paths.get(new ClassPathResource("TIS People Import Template - empty row.xlsx").getURI().getPath())))) {
			Workbook workbook = WorkbookFactory.create(bis);
			Sheet sheet = workbook.getSheetAt(0);
			int numberOfRows = sheet.getLastRowNum();
			UploadFileServiceImpl.removeRow(sheet, 3);
			int rowsAfterDeletion = sheet.getLastRowNum();
			assertThat(numberOfRows).isGreaterThan(rowsAfterDeletion);
		}
	}
}
