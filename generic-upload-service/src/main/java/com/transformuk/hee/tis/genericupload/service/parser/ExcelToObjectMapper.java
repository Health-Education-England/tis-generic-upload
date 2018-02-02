package com.transformuk.hee.tis.genericupload.service.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ExcelToObjectMapper {
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
  public <T> ArrayList<T> map(Class<T> cls, Map<String, String> columnMap) throws Exception {
    ArrayList<T> list = new ArrayList();

    Sheet sheet = workbook.getSheetAt(0);
    int lastRow = sheet.getLastRowNum();
    for (int i = 1; i <= lastRow; i++) {
      Object obj = cls.newInstance();
      Field[] fields = obj.getClass().getDeclaredFields();
      for (Field field : fields) {
        String fieldName = field.getName();
        String xlsColumnName = columnMap.get(fieldName.toLowerCase());
        int index;
        if (StringUtils.isNotEmpty(xlsColumnName)) {
          index = getHeaderIndex(xlsColumnName, workbook);
        } else {
          index = getHeaderIndex(fieldName, workbook);
        }
        Cell cell = sheet.getRow(i).getCell(index);
        Field classField = obj.getClass().getDeclaredField(fieldName);
        setObjectFieldValueFromCell(obj, classField, cell);
      }
      list.add((T) obj);
    }
    return list;
  }

  /**
   * Read value from Cell and set it to given field of given object.
   * Note: supported data Type: String, Date, int, long, float, double and boolean.
   *
   * @param obj   Object whom given field belong.
   * @param field Field which value need to be set.
   * @param cell  Apache POI cell from which value needs to be retrived.
   */
  private void setObjectFieldValueFromCell(Object obj, Field field, Cell cell) {
    Class<?> cls = field.getType();
    field.setAccessible(true);
    if (cls == String.class) {
      try {
        field.set(obj, cell.getStringCellValue());
      } catch (Exception e) {
        setNullValueToObject(obj, field);
      }
    } else if (cls == Date.class) {
      try {
        Date date = cell.getDateCellValue();
        field.set(obj, date);
      } catch (Exception e) {
        setNullValueToObject(obj, field);
      }
    } else if (cls == int.class || cls == long.class || cls == float.class || cls == double.class) {
      double value = cell.getNumericCellValue();
      try {
        if (cls == int.class) {
          field.set(obj, (int) value);
        } else if (cls == long.class) {
          field.set(obj, (long) value);
        } else if (cls == float.class) {
          field.set(obj, (float) value);
        } else {
          //Double value
          field.set(obj, value);
        }
      } catch (Exception e) {
        setNullValueToObject(obj, field);
      }
    } else if (cls == boolean.class) {
      boolean value = cell.getBooleanCellValue();
      try {
        field.set(obj, value);
      } catch (Exception e) {
        setNullValueToObject(obj, field);
      }
    }
        /*else if(cls == Collection.class) {
            double value = cell.getNumericCellValue();
            try {
                field.set(obj, value);
            }catch (Exception e) {
                try {
                    field.set(obj, null);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        }*/
    else {
      // Unsupported data type.
    }

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
  private int getHeaderIndex(String headerName, Workbook workbook) throws Exception {
    Sheet sheet = workbook.getSheetAt(0);
    int totalColumns = sheet.getRow(0).getLastCellNum();
    int index = -1;
    for (index = 0; index < totalColumns; index++) {
      Cell cell = sheet.getRow(0).getCell(index);
      if (cell.getStringCellValue().toLowerCase().equals(headerName.toLowerCase())) {
        break;
      }
    }
    if (index == -1) {
      throw new Exception("Invalid object field name provided.");
    }
    return index;
  }
}
