package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.service.util.POIUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.slf4j.LoggerFactory.getLogger;

public class ExcelToObjectMapper {
  private static final Logger logger = getLogger(ExcelToObjectMapper.class);

  public static final String ROW_NUMBER = "rowNumber";
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
  private Workbook workbook;

  public ExcelToObjectMapper(InputStream excelFile, boolean validateDates) throws IOException, InvalidFormatException {
    workbook = createWorkBook(excelFile);
    dateFormat.setLenient(!validateDates);
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
  public <T> List<T> map(Class<T> cls, Map<String, String> columnMap) throws ReflectiveOperationException {
    List<T> list = new ArrayList();

    Field rowNumberFieldInXLS = cls.getSuperclass().getDeclaredField(ROW_NUMBER);
    rowNumberFieldInXLS.setAccessible(true);
    Sheet sheet = workbook.getSheetAt(0);
    int lastRow = sheet.getLastRowNum();
    for (int rowNumber = 1; rowNumber <= lastRow; rowNumber++) {
      POIUtil poiUtil = new POIUtil();
      if(sheet.getRow(rowNumber) == null || poiUtil.isEmptyRow(sheet.getRow(rowNumber))) continue;
        Object obj = cls.newInstance();
      Field[] fields = obj.getClass().getDeclaredFields();
      for (Field field : fields) {
        String fieldName = field.getName();
        String xlsColumnName = columnMap.get(fieldName);
        int index;
        if (StringUtils.isNotEmpty(xlsColumnName)) {
          index = getHeaderIndex(xlsColumnName, workbook);
        } else {
          index = getHeaderIndex(fieldName, workbook);
        }
        Cell cell = sheet.getRow(rowNumber).getCell(index);
        Field classField = obj.getClass().getDeclaredField(fieldName);
        try {
          setObjectFieldValueFromCell(obj, classField, cell);
        } catch (ParseException | IllegalArgumentException e) {
          logger.info("Error while extracting cell value from object : {} ", e.getMessage());
          Method method = obj.getClass().getMethod("addErrorMessage", String.class);
          method.invoke(obj, e.getMessage());
          Method addErrorMessageMethod = cls.getSuperclass().getDeclaredMethod("addErrorMessage", String.class);
          addErrorMessageMethod.invoke(obj, e.getMessage());
        }
      }
      rowNumberFieldInXLS.setInt(obj, rowNumber);
      if(!isAllBlanks(obj))
        list.add((T) obj);
    }
    return list;
  }

  public Set<String> getHeaders() {
    Row headerRow = workbook.getSheetAt(0).getRow(0);
    return IntStream.range(headerRow.getFirstCellNum(), headerRow.getLastCellNum())
        .mapToObj(headerRow::getCell)
        .map(Cell::getStringCellValue)
        .collect(Collectors.toSet());
  }

  private boolean isAllBlanks(Object obj) throws IllegalAccessException {
    boolean allBlanks = true;
    for (Field f : obj.getClass().getDeclaredFields()) {
    	  if(f.getName().startsWith("$")) //skip surefire jacoco fields
    	    continue;
        f.setAccessible(true);
        allBlanks = allBlanks && org.springframework.util.StringUtils.isEmpty(f.get(obj));
    }
    return allBlanks;
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
          } else if(cls == Float.class) {
            field.set(obj, Float.valueOf(trim));
          } else {
            field.set(obj, trim);
          }
          break;
        case NUMERIC:
          if (DateUtil.isCellDateFormatted(cell)) {
            field.set(obj, cell.getDateCellValue());
          } else if(cls == Float.class) {
            field.set(obj, (float) cell.getNumericCellValue());
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

    String regex = "(([1-9]|0[1-9]|[12]\\d|3[01])/([1-9]|0[1-9]|1[0-2])/[12]\\d{3})";
    boolean matches = date.matches(regex);
    if (matches) {
      return removeTime(dateFormat.parse(date));
    }
    throw new ParseException("Date is not in valid dd/mm/yyyy format",0);

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
      logger.error(e1.getMessage());
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
      if (cell.getStringCellValue().trim().equalsIgnoreCase(headerName)) {
        break;
      }
    }
    if (index == -1) {
      throw new NoSuchFieldException("Invalid object field name provided.");
    }
    return index;
  }
}
