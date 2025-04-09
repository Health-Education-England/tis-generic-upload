package com.transformuk.hee.tis.genericupload.service.parser;

import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration;
import com.transformuk.hee.tis.genericupload.service.util.POIUtil;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
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
import java.util.stream.Stream;
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

  private static final Logger logger = getLogger(ExcelToObjectMapper.class);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
  POIUtil poiUtil = new POIUtil();

  // The valid date formats defined in DATE_REGEX are d/mm/yyyy and dd/mm/yyyy
  private static final String DATE_REGEX = "(([1-9]|0[1-9]|[12]\\d|3[01])/([1-9]|0[1-9]|1[0-2])/[12]\\d{3})";

  private final Workbook workbook;
  private final boolean ignoreUnmapped;

  /**
   * Create a mapper for converting the provided spreadsheet rows into typed Java representations.
   *
   * @param excelFile      The spreadsheet as an input stream, including a header and data rows
   * @param ignoreUnmapped Whether to map target types when their fields are missing.
   *                       <strong>N.B.</strong>This class will still extract fields where the
   *                       header matches the field name, e.g. a target field `foo` will be
   *                       populated when there is a header cell with the value `Foo`
   * @param validateDates  Whether to require strict matches to the date format
   * @throws IOException Thrown when there is an issue reading the input stream as a workbook
   */
  public ExcelToObjectMapper(InputStream excelFile, boolean ignoreUnmapped, boolean validateDates)
      throws IOException {
    workbook = createWorkBook(excelFile);
    this.ignoreUnmapped = ignoreUnmapped;
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
  public <T extends TemplateXLS> List<T> map(Class<T> cls, Map<String, String> columnMap)
      throws ReflectiveOperationException {
    List<T> list = new ArrayList<>();

    Map<Field, Integer> fieldToIndex = Stream.of(cls.getDeclaredFields())
        //skip surefire jacoco fields
        .filter(f -> !f.getName().startsWith("$"))
        .collect(Collectors.toMap(k -> k,
            s -> getHeaderIndex(columnMap.getOrDefault(s.getName(), s.getName()), workbook)));

    Sheet sheet = workbook.getSheetAt(0);
    int lastRow = sheet.getLastRowNum();
    for (int rowNumber = 1; rowNumber <= lastRow; rowNumber++) {
      int rowIndex = rowNumber;
      if (poiUtil.isEmptyRow(sheet.getRow(rowNumber))) {
        continue;
      }
      T obj = cls.newInstance();
      fieldToIndex.forEach((field, index) -> {
        if (index >= 0) {
          Cell cell = sheet.getRow(rowIndex).getCell(index);
          try {
            setObjectFieldValueFromCell(obj, field, cell);
          } catch (DateTimeParseException | ParseException | IllegalArgumentException
                   | IllegalAccessException e) {
            logger.info("Error while extracting cell value from object.", e);
            obj.addErrorMessage(e.getMessage());
          }
        }
      });
      obj.setRowNumber(rowIndex);
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
            final double d = Double.parseDouble(trim);
            if (d != (long) d) {
              throw new NumberFormatException(
                  String.format("A whole number was expected instead of '%s'.", trim));
            }
            field.set(obj, (long) d);
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
            final double d = cell.getNumericCellValue();
            if (d != (long) d) {
              throw new NumberFormatException(
                  String.format("A whole number was expected instead of '%s'.", d));
            }
            field.set(obj, (long) d);
          } else {
            double numericValue = cell.getNumericCellValue();
            field.set(obj, String.valueOf(numericValue == (long) numericValue
                ? (long) numericValue : String.valueOf(numericValue)));
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
   * @throws IllegalArgumentException Thrown when a field is not found
   */
  private int getHeaderIndex(String headerName, Workbook workbook) throws IllegalArgumentException {
    Row row = workbook.getSheetAt(0).getRow(0);
    int totalColumns = row.getLastCellNum();
    for (int i = 0; i < totalColumns; i++) {
      Cell cell = row.getCell(i);
      if (headerName.equalsIgnoreCase(cell.getStringCellValue().trim())) {
        return i;
      }
    }
    if (ignoreUnmapped) {
      return -1;
    } else {
      throw new IllegalArgumentException(
          String.format("No Spreadsheet header named '%s", headerName));
    }
  }
}
