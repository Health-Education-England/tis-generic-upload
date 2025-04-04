package com.transformuk.hee.tis.genericupload.service.api.validation;

import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.parser.ColumnMapper;
import com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidator {

  private static final String TIS_POSTFUNDING_ID_MANDATORY = "TIS_PostFunding_ID*";
  public static final String DATE_MISSING_OR_INVALID_ON_MANDATORY_FIELD =
      "Date missing or incorrect format on mandatory field (%1$s)";
  public static final String FIELD_IS_REQUIRED_AT_LINE_NO = "%s Field is required at line no %d ";
  private final Logger logger = LoggerFactory.getLogger(FileValidator.class);

  /**
   * Custom validator used during file upload checks for mandatory fields.
   *
   * @param files                   The provided files to validate
   * @param validateMandatoryFields Whether to ensure mandatory fields are populated
   * @param validateDates           See {@link  ExcelToObjectMapper}
   * @return The FileType when {@code validateMandatoryFields} is true, otherwise {@code null}
   * @throws IOException                  If there are issues using the files
   * @throws InvalidFormatException       If the type of file is not recognised
   * @throws ValidationException          If validated fields contain invalid data
   * @throws ReflectiveOperationException If the target class or fields are unavailable
   */
  public FileType validate(List<MultipartFile> files, boolean validateMandatoryFields,
      boolean validateDates)
      throws IOException, InvalidFormatException, ValidationException, ReflectiveOperationException {
    List<FieldError> fieldErrors = new ArrayList<>();
    FileType fileType = null;

    if (!ObjectUtils.isEmpty(files)) {
      for (MultipartFile file : files) {
        if (!ObjectUtils.isEmpty(file) && StringUtils.isNotEmpty(file.getContentType())) {
          ExcelToObjectMapper excelToObjectMapper = new ExcelToObjectMapper(file.getInputStream(),
              true, validateDates);
          if (validateMandatoryFields) {
            Set<String> headers = excelToObjectMapper.getHeaders();
            fileType = getFileType(files, fieldErrors, excelToObjectMapper, headers);
          }
        }
      }
    }

    return fileType;
  }

  protected FileType getFileType(List<MultipartFile> files, List<FieldError> fieldErrors,
      ExcelToObjectMapper excelToObjectMapper, Set<String> headers)
      throws ReflectiveOperationException, ValidationException, InvalidFormatException {
    FileType fileType;

    if (headers.contains("Placement Type*")) {
      fileType = FileType.PLACEMENTS;
    } else if (headers.contains("TIS_Person_ID*")) {
      fileType = FileType.PEOPLE_UPDATE;
    } else if (headers.contains("Email Address")) { //TODO do something more robust than this
      fileType = FileType.PEOPLE;
    } else if (headers.contains("Placement Id*") && headers.contains("Placement Status*")) {
      fileType = FileType.PLACEMENTS_DELETE;
    } else if (headers.contains("TIS_Assessment_ID*") && headers.contains("Assessment Status*")) {
      fileType = FileType.ASSESSMENTS_DELETE;
    } else if (headers.contains("Review date*")) {
      fileType = FileType.ASSESSMENTS;
    } else if (headers.contains("TIS_Placement_ID*") && headers.contains("Intrepid_Placement_ID")) {
      fileType = FileType.PLACEMENTS_UPDATE;
    } else if (headers.contains("TIS_Post_ID*") && headers.contains("Funding type")
        && !headers.contains(TIS_POSTFUNDING_ID_MANDATORY)) {
      fileType = FileType.POSTS_FUNDING_UPDATE;
    } else if (headers.contains("National Post Number*")) {
      fileType = FileType.POSTS_CREATE;
    } else if (headers.contains("TIS_Post_ID*") && !headers.contains(
        TIS_POSTFUNDING_ID_MANDATORY)) {
      fileType = FileType.POSTS_UPDATE;
    } else if (headers.contains(TIS_POSTFUNDING_ID_MANDATORY)) {
      fileType = FileType.FUNDING_UPDATE;
    } else if (headers.contains("TIS_Assessment_ID*")) {
      fileType = FileType.ASSESSMENTS_UPDATE;
    } else if (headers.contains("TIS_ProgrammeMembership_ID*")
        && headers.contains("Programme Membership Type")) {
      fileType = FileType.PROGRAMME_MEMBERSHIP_UPDATE;
    } else if (headers.contains("TIS_ProgrammeMembership_ID*")
        && headers.contains("Curriculum Name*")) {
      fileType = FileType.CURRICULUM_MEMBERSHIP_CREATE;
    } else if (headers.contains("TIS_CurriculumMembership_ID*")) {
      fileType = FileType.CURRICULUM_MEMBERSHIP_UPDATE;
    } else {
      throw new InvalidFormatException("Unrecognised upload template");
    }

    Class<? extends TemplateXLS> dtoClass = fileType.getDtoClass();
    validateMandatoryFieldsOrThrowException(files, fieldErrors, dtoClass, excelToObjectMapper);

    return fileType;
  }

  public void validateMandatoryFieldsOrThrowException(List<MultipartFile> files,
      List<FieldError> fieldErrors, Class<? extends TemplateXLS> templateXls,
      ExcelToObjectMapper excelToObjectMapper)
      throws ReflectiveOperationException, ValidationException {
    validateMandatoryFields(fieldErrors, excelToObjectMapper, templateXls);
    if (!fieldErrors.isEmpty()) {
      BindingResult bindingResult = new BeanPropertyBindingResult(templateXls.getSimpleName(),
          files.get(0).getName());
      fieldErrors.forEach(bindingResult::addError);

      throw new ValidationException(bindingResult);
    }
  }

  /**
   * Validate mandatory fields
   *
   * @param fieldErrors         A collection to add errors to.
   * @param excelToObjectMapper The mapper to use to convert excel to objects.
   * @param mappedToClass       The DTO class to use to map the columns.
   * @throws ReflectiveOperationException If the mapper fails to process the file.
   */
  private void validateMandatoryFields(List<FieldError> fieldErrors,
      ExcelToObjectMapper excelToObjectMapper, Class<? extends TemplateXLS> mappedToClass)
      throws ReflectiveOperationException {
    ColumnMapper columnMapper = new ColumnMapper(mappedToClass);
    Map<String, String> columnNameToMandatoryColumnsMap = columnMapper.getMandatoryFieldMap();
    List<?> result = excelToObjectMapper.map(mappedToClass, columnNameToMandatoryColumnsMap);
    AtomicInteger rowIndex = new AtomicInteger(0);
    result.forEach(row -> {
      rowIndex.incrementAndGet();
      columnNameToMandatoryColumnsMap.keySet().forEach(columnNameToMandatoryColumnsMapKey -> {
        try {
          validateField(fieldErrors, mappedToClass, rowIndex, row,
              columnNameToMandatoryColumnsMapKey);
        } catch (NoSuchFieldException | IllegalAccessException e) {
          logger.error("Field doesn't exist: {}", columnNameToMandatoryColumnsMapKey, e);
        }
      });
    });
  }

  private void validateField(List<FieldError> fieldErrors,
      Class<? extends TemplateXLS> mappedToClass,
      AtomicInteger rowIndex, Object row, String fieldName)
      throws NoSuchFieldException, IllegalAccessException {
    Field currentField = row.getClass().getDeclaredField(fieldName);
    //TODO: Simplify
    if (currentField != null) {
      currentField.setAccessible(true);
      if (currentField.getType() == String.class) {
        String value = (String) currentField.get(row);
        if (StringUtils.isBlank(value)) {
          fieldErrors
              .add(new FieldError(mappedToClass.getSimpleName(), fieldName,
                  String.format(FIELD_IS_REQUIRED_AT_LINE_NO, fieldName,
                      rowIndex.get())));
        }
      } else if (currentField.getType() == Date.class) {
        Date date = (Date) currentField.get(row);//TODO should throw an exception on invalid date
        if (date == null) {
          fieldErrors
              .add(new FieldError(mappedToClass.getSimpleName(), fieldName,
                  String.format(DATE_MISSING_OR_INVALID_ON_MANDATORY_FIELD, fieldName)));
        }
      } else if (currentField.getType() == Float.class) {
        //TODO validate Float Fields
      }
    }
  }
}
