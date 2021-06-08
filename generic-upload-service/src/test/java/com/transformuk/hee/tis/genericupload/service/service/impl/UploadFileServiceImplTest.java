package com.transformuk.hee.tis.genericupload.service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class UploadFileServiceImplTest {

  @Test
  public void removeRowDoesNotActuallyRemoveARow() throws IOException, EncryptedDocumentException {
    try (InputStream is = new ClassPathResource("TIS People Import Template - empty row.xlsx")
        .getInputStream()) {
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);
      int numberOfRows = sheet.getLastRowNum();
      sheet.removeRow(sheet.getRow(3));
      int rowsAfterDeletion = sheet.getLastRowNum();
      assertThat(numberOfRows).isEqualTo(rowsAfterDeletion);
    }
  }

  @Test
  public void haveToShiftRowsUpToRemoveARow() throws IOException, EncryptedDocumentException {
    try (InputStream is = new ClassPathResource("TIS People Import Template - empty row.xlsx")
        .getInputStream()) {
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);
      int numberOfRows = sheet.getLastRowNum();
      UploadFileServiceImpl.removeRow(sheet, 3);
      int rowsAfterDeletion = sheet.getLastRowNum();
      assertThat(numberOfRows).isGreaterThan(rowsAfterDeletion);
    }
  }
}
