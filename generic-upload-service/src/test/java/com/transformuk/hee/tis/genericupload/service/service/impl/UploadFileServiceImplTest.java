package com.transformuk.hee.tis.genericupload.service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class UploadFileServiceImplTest {

  @Test
  public void removeRowDoesNotActuallyRemoveARow() throws IOException, InvalidFormatException {
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
  public void haveToShiftRowsUpToRemoveARow() throws IOException, InvalidFormatException {
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

  @Test
  public void shouldRemoveCommentsWhenRemoveRows() throws Exception{
    try (InputStream is = new ClassPathResource("TIS Placement Import Template - removeComment.xls")
        .getInputStream()) {
      Workbook workbook = WorkbookFactory.create(is);
      HSSFSheet sheet = (HSSFSheet)workbook.getSheetAt(0);
      Map<CellAddress, HSSFComment> commentMap = sheet.getCellComments();
      int size1 = commentMap.size();
      UploadFileServiceImpl.removeRow(sheet, 4);
      commentMap = sheet.getCellComments();
      int size2 = commentMap.size();
      assertThat(size1).isGreaterThan(size2);
    }
  }
}
