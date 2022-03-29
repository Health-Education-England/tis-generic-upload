package com.transformuk.hee.tis.genericupload.service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Comment;
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
  public void shouldRemoveCommentsWhenRemoveRowsForXls() throws Exception{
    try (InputStream is = new ClassPathResource("TIS Placement Import Template - removeComment.xls")
            .getInputStream()) {
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);
      Map<CellAddress, Comment> commentMap = (Map<CellAddress, Comment>) sheet.getCellComments();
      int size1 = commentMap.size();
      UploadFileServiceImpl.removeCommentsForRemovedRows(sheet, Collections.singleton(1));
      commentMap = (Map<CellAddress, Comment>) sheet.getCellComments();
      int size2 = commentMap.size();
      assertThat(size1).isGreaterThan(size2);
    }
  }

  @Test
  public void shouldRemoveCommentsWhenRemoveRowsForXlsx() throws Exception{
    try (InputStream is = new ClassPathResource("TIS Placement Import Template - removeComment.xlsx")
            .getInputStream()) {
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);
      Map<CellAddress, Comment> commentMap = (Map<CellAddress, Comment>) sheet.getCellComments();
      int size1 = commentMap.size();
      UploadFileServiceImpl.removeCommentsForRemovedRows(sheet, Collections.singleton(1));
      commentMap = (Map<CellAddress, Comment>) sheet.getCellComments();
      int size2 = commentMap.size();
      assertThat(size1).isGreaterThan(size2);
    }
  }
}
