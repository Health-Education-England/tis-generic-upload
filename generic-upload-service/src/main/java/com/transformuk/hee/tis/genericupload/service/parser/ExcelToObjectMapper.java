package com.transformuk.hee.tis.genericupload.service.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

public class ExcelToObjectMapper {
  private static final Logger logger = getLogger(ExcelToObjectMapper.class);

  public static final String ROW_NUMBER = "rowNumber";
  public static final String ERROR_MESSAGE = "errorMessage";
  public static final String SUCCESSFULLY_IMPORTED = "successfullyImported";
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yy");
  private Workbook workbook;

  public ExcelToObjectMapper(InputStream excelFile) throws IOException, InvalidFormatException {
    workbook = createWorkBook(excelFile);
  }

  /**
   * Create Apache POI @{@link Workbook} for given excel file.
   *
   * @param excelFile
   * @return
   * @throws IOException
   */
  private Workbook createWorkBook(InputStream excelFile) throws IOException, InvalidFormatException {
    return WorkbookFactory.create(excelFile);
  }

  /**
   * Read data from Excel file and convert each rows into list of given object of Type T.
   *
   * @param cls Class of Type T.
   * @param <T> Generic type T, result will list of type T objects.
   * @return List of object of type T.
   * @throws Exception if failed to generate mapping.
   */
  public <T> ArrayList<T> map(Class<T> cls, Map<String, String> columnMap) throws NoSuchFieldException, IllegalAccessException, InstantiationException, ParseException {
    ArrayList<T> list = new ArrayList();

    Field rowNumberFieldInXLS = cls.getDeclaredField(ROW_NUMBER);
    rowNumberFieldInXLS.setAccessible(true);
    Sheet sheet = workbook.getSheetAt(0);
    int lastRow = sheet.getLastRowNum();
    for (int rowNumber = 1; rowNumber <= lastRow; rowNumber++) {
      if(sheet.getRow(rowNumber) == null || isEmptyRow(sheet.getRow(rowNumber))) continue;
    	Object obj = cls.newInstance();
      Field[] fields = obj.getClass().getDeclaredFields();
      for (Field field : fields) {
        String fieldName = field.getName();
        if(shouldSkipField(fieldName)) continue;
        String xlsColumnName = columnMap.get(fieldName.toLowerCase());
        int index;
        if (StringUtils.isNotEmpty(xlsColumnName)) {
          index = getHeaderIndex(xlsColumnName, workbook);
        } else {
          index = getHeaderIndex(fieldName, workbook);
        }
        Cell cell = sheet.getRow(rowNumber).getCell(index);
        Field classField = obj.getClass().getDeclaredField(fieldName);
        setObjectFieldValueFromCell(obj, classField, cell);
      }
      rowNumberFieldInXLS.setInt(obj, rowNumber);
      if(!isAllBlanks(obj))
        list.add((T) obj);
    }
    return list;
  }

  //https://stackoverflow.com/a/20002688
  public static boolean isEmptyRow(Row row){
    boolean isEmptyRow = true;
    for(int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++){
      Cell cell = row.getCell(cellNum);
      if(cell != null && cell.getCellTypeEnum() != CellType.BLANK && StringUtils.isNotBlank(cell.toString())){
        isEmptyRow = false;
      }
    }
    return isEmptyRow;
  }

  private boolean isAllBlanks(Object obj) throws IllegalAccessException {
    boolean allBlanks = true;
    for (Field f : obj.getClass().getDeclaredFields()) {
    	  if(f.getName().startsWith("$") || //skip surefire jacoco fields
            shouldSkipField(f.getName())) continue;
        f.setAccessible(true);
        allBlanks = allBlanks && org.springframework.util.StringUtils.isEmpty(f.get(obj));
    }
    return allBlanks;
  }

  private boolean shouldSkipField(String fieldName) {
    return fieldName.equalsIgnoreCase(ROW_NUMBER) ||
        fieldName.equalsIgnoreCase(ERROR_MESSAGE) ||
        fieldName.equalsIgnoreCase(SUCCESSFULLY_IMPORTED);
  }

  /**
   * Read value from Cell and set it to given field of given object.
   * Note: supported data Type: String, Date, int, long, float, double and boolean.
   *
   * @param obj   Object whom given field belong.
   * @param field Field which value need to be set.
   * @param cell  Apache POI cell from which value needs to be retrived.
   */
  private void setObjectFieldValueFromCell(Object obj, Field field, Cell cell) throws IllegalAccessException, ParseException {
    Class<?> cls = field.getType();
    field.setAccessible(true);
    if (cell == null) {
      setNullValueToObject(obj, field);
    } else {
      switch (cell.getCellTypeEnum()) {
        case STRING:
          String trim = cell.getStringCellValue().trim();
          if (cls == Date.class) {
            field.set(obj, getDate(trim));
          } else {
            field.set(obj, trim);
          }
          break;
        case NUMERIC:
          if (DateUtil.isCellDateFormatted(cell)) {
            field.set(obj, cell.getDateCellValue());
          } else {
            cell.setCellType(CellType.STRING);
            field.set(obj, cell.getStringCellValue());
          }
          break;
        case BLANK:
          break;
        default:
          logger.warn("Unknown data type ");
          break;
      }
    }
  }

  public static Date getDate(String date) throws ParseException {
    return removeTime(dateFormat.parse(date));
  }

  public static Date removeTime(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  /**
   * set null value if the value is not found
   * @param obj
   * @param field
   */
  private void setNullValueToObject(Object obj, Field field) {
    try {
      field.set(obj, null);
    } catch (IllegalAccessException e1) {
      e1.printStackTrace();
    }
  }


  /**
   * Read first row/header of Excel file, match given header name and return its index.
   *
   * @param headerName
   * @param workbook
   * @return Index number of header name.
   * @throws Exception
   */
  private int getHeaderIndex(String headerName, Workbook workbook) throws NoSuchFieldException {
    Sheet sheet = workbook.getSheetAt(0);
    int totalColumns = sheet.getRow(0).getLastCellNum();
    int index = -1;
    for (index = 0; index < totalColumns; index++) {
      Cell cell = sheet.getRow(0).getCell(index);
      if (cell.getStringCellValue().toLowerCase().trim().equals(headerName.toLowerCase())) {
        break;
      }
    }
    if (index == -1) {
      throw new NoSuchFieldException("Invalid object field name provided.");
    }
    return index;
  }
}
