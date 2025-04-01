package com.transformuk.hee.tis.genericupload.service.parser;

import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration;
import com.transformuk.hee.tis.genericupload.service.util.POIUtil;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.springframework.util.ObjectUtils;
import uk.nhs.tis.StringConverter;

public class ExcelToObjectMapper {

  public static final String ROW_NUMBER = "rowNumber";
  private static final Logger logger = getLogger(ExcelToObjectMapper.class);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");

  // The valid date formats defined in DATE_REGEX are d/mm/yyyy and dd/mm/yyyy
  private static final String DATE_REGEX = "(([1-9]|0[1-9]|[12]\\d|3[01])/([1-9]|0[1-9]|1[0-2])/[12]\\d{3})";

  private final Workbook workbook;


  public ExcelToObjectMapper(InputStream excelFile, boolean validateDates)
      throws IOException {
    workbook = createWorkBook(excelFile);
    dateFormat.setLenient(!validateDates);
  }

  /**
   * getDate() receives a date input, checks if it conforms to the DATE_REGEX, returns converted
   * date without time.
   *
   * @param date the date (string) to be passed to the method.
   * @return Date.
   * @throws ParseException when date doesn't conform to DATE_REGEX.
   */
  public static Date getDate(String date) throws ParseException {
    boolean matches = date.matches(DATE_REGEX);
    if (matches) {
      return removeTime(dateFormat.parse(date));
    }
    throw new ParseException("Date is not in valid dd/mm/yyyy format", 0);

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
   * Create Apache POI @{@link Workbook} for given excel file.
   *
   * @param excelFile the excel file passed to the method.
   * @return Workbook.
   * @throws IOException Thrown when the workbook cannot be created, e.g. for an empty stream
   */
  private Workbook createWorkBook(InputStream excelFile)
      throws IOException {
    return WorkbookFactory.create(excelFile);
  }

  /**
   * Read data from Excel file and convert each rows into list of given object of Type T.
   *
   * @param cls Class of Type T.
   * @param <T> Generic type T, result will list of type T objects.
   * @return List of object of type T.
   * @throws ReflectiveOperationException if failed to generate mapping.
   */
  public <T> List<T> map(Class<T> cls, Map<String, String> columnMap)
      throws ReflectiveOperationException {
    List<T> list = new ArrayList<>();

    Field rowNumberFieldInXls = cls.getSuperclass().getDeclaredField(ROW_NUMBER);
    rowNumberFieldInXls.setAccessible(true);
    Sheet sheet = workbook.getSheetAt(0);
    int lastRow = sheet.getLastRowNum();
    for (int rowNumber = 1; rowNumber <= lastRow; rowNumber++) {
      POIUtil poiUtil = new POIUtil();
      if (sheet.getRow(rowNumber) == null || poiUtil.isEmptyRow(sheet.getRow(rowNumber))) {
        continue;
      }
      T obj = cls.newInstance();
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
        } catch (DateTimeParseException | ParseException | IllegalArgumentException e) {
          logger.info("Error while extracting cell value from object.", e);
          Method method = obj.getClass().getMethod("addErrorMessage", String.class);
          method.invoke(obj, e.getMessage());
        }
      }
      rowNumberFieldInXls.setInt(obj, rowNumber);
      if (!isAllBlanks(obj)) {
        list.add(obj);
      }
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

  private boolean isAllBlanks(Object obj) {
    // Note the inversions in the operations and return, done to avoid processing all elements.
    return !Arrays.stream(obj.getClass().getDeclaredFields())
        //skip surefire jacoco fields
        .filter(f -> !f.getName().startsWith("$"))
        .anyMatch(o -> !ObjectUtils.isEmpty(o));
  }

  /**
   * Read value from Cell and set it to given field of given object. Note: supported data Type:
   * String, Date, int, long, float, double and boolean.
   *
   * @param obj   Object whom given field belong.
   * @param field Field which value need to be set.
   * @param cell  Apache POI cell from which value needs to be retrieved.
   * @throws DateTimeParseException if the input for LocalDate was not d/M/yyyy or dd/MM/yyyy.
   */
  private void setObjectFieldValueFromCell(Object obj, Field field, Cell cell)
      throws IllegalAccessException, ParseException {
    Class<?> cls = field.getType();
    field.setAccessible(true);
    if (cell == null) {
      setNullValueToObject(obj, field);
    } else {
      switch (cell.getCellType()) {
        case STRING:
          String trim = cell.getStringCellValue().trim();
          if (StringUtils.isEmpty(trim)) {
            break;
          }
          if (cls == LocalDate.class) {
            field.set(obj, MapperConfiguration.convertDate(trim));
          } else if (cls == Date.class) {
            field.set(obj, getDate(trim));
          } else if (cls == Float.class) {
            field.set(obj, Float.valueOf(trim));
          } else if (cls == Long.class) {
            // Parse and then convert for consistency across {@link CellType}s
            field.set(obj, Double.valueOf(trim).longValue());
          } else {
            String setStr = StringConverter.getConverter(trim).escapeForJson().toString();
            field.set(obj, setStr);
          }
          break;
        case NUMERIC:
          if (DateUtil.isCellDateFormatted(cell)) {
            if (cls == LocalDate.class) {
              field.set(obj, MapperConfiguration.convertDate(cell.getDateCellValue()));
            } else {
              field.set(obj, cell.getDateCellValue());
            }
          } else if (cls == Float.class) {
            field.set(obj, (float) cell.getNumericCellValue());
          } else if (cls == Long.class) {
            field.set(obj, (long) cell.getNumericCellValue());
          } else {
            double numericValue = cell.getNumericCellValue();
            String stringValue;

            if (numericValue == (long) numericValue) {
              stringValue = String.valueOf((long) numericValue);
            } else {
              stringValue = String.valueOf(numericValue);
            }

            field.set(obj, stringValue);
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

  /**
   * set null value if the value is not found
   *
   * @param obj   The object containing the field to be nulled
   * @param field The field to set to null
   */
  private void setNullValueToObject(Object obj, Field field) {
    try {
      field.set(obj, null);
    } catch (IllegalAccessException e) {
      logger.error("Unable to set target value to null", e);
    }
  }


  /**
   * Read first row/header of Excel file, match given header name and return its index.
   *
   * @param headerName The complete field name as in the header row
   * @param workbook   the spreadsheet to search for the header
   * @return The numeric column index for the field.
   * @throws NoSuchFieldException Thrown when a field is not found
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
