package com.transformuk.hee.tis.genericupload.service.api.validation;

import com.transformuk.hee.tis.genericupload.api.dto.*;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.api.UploadFileResource;
import com.transformuk.hee.tis.genericupload.service.parser.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FileValidator {

	public static final String DATE_MISSING_OR_INVALID_ON_MANDATORY_FIELD = "Date missing or incorrect format on mandatory field (%1$s)";
	public static final String FIELD_IS_REQUIRED_AT_LINE_NO = "%s Field is required at line no %d ";
	private final Logger logger = LoggerFactory.getLogger(UploadFileResource.class);

	/**
	 * Custom validator used during file upload checks for mandatory fields
	 *
	 * @param files
	 *            The provided files to validate
	 * @throws MethodArgumentNotValidException
	 */
	public FileType validate(List<MultipartFile> files, boolean validateMandatoryFields, boolean validateDates) throws IOException, InvalidFormatException, ValidationException, ReflectiveOperationException {
		List<FieldError> fieldErrors = new ArrayList<>();
		FileType fileType = null;

		if (!ObjectUtils.isEmpty(files)) {
			for (MultipartFile file : files) {
				if (!ObjectUtils.isEmpty(file) && StringUtils.isNotEmpty(file.getContentType())) {
					ExcelToObjectMapper excelToObjectMapper = new ExcelToObjectMapper(file.getInputStream(), validateDates);
					if (validateMandatoryFields) {
						Set<String> headers = excelToObjectMapper.getHeaders();
						fileType = getFileType(files, fieldErrors, excelToObjectMapper, headers);
					}
				}
			}
		}

		return fileType;
	}

	protected FileType getFileType(List<MultipartFile> files, List<FieldError> fieldErrors, ExcelToObjectMapper excelToObjectMapper, Set<String> headers) throws ReflectiveOperationException, ValidationException, InvalidFormatException {
		FileType fileType;
		if(headers.contains("Placement Type*")) {
			fileType = FileType.PLACEMENTS;
			validateMandatoryFieldsOrThrowException(files, fieldErrors, PlacementXLS.class, excelToObjectMapper, new PlacementHeaderMapper());
		} else if(headers.contains("Email Address")) { //TODO do something more robust than this
			fileType = FileType.PEOPLE;
			validateMandatoryFieldsOrThrowException(files, fieldErrors, PersonXLS.class, excelToObjectMapper, new PersonHeaderMapper());
		} else if(headers.contains("Placement Id*") && headers.contains("Placement Status*") ) {
			fileType = FileType.PLACEMENTS_DELETE;
			validateMandatoryFieldsOrThrowException(files, fieldErrors, PlacementDeleteXLS.class, excelToObjectMapper, new PlacementDeleteHeaderMapper());
		} else if(headers.contains("Review date*")){
			fileType = FileType.ASSESSMENTS;
			validateMandatoryFieldsOrThrowException(files,fieldErrors,AssessmentXLS.class,excelToObjectMapper,new AssessmentHeaderMapper());
		} else if(headers.contains("TIS_Placement_ID*") && headers.contains("Intrepid_Placement_ID")){
			fileType = FileType.PLACEMENTS_UPDATE;
			validateMandatoryFieldsOrThrowException(files,fieldErrors,PlacementUpdateXLS.class,excelToObjectMapper,new PlacementUpdateHeaderMapper());
		}
		else {
			throw new InvalidFormatException("Unrecognised upload template");
		}
		return fileType;
	}

	public void validateMandatoryFieldsOrThrowException(List<MultipartFile> files, List<FieldError> fieldErrors, Class templateXLS, ExcelToObjectMapper excelToObjectMapper, ColumnMapper columnMapper) throws ReflectiveOperationException, ValidationException {
		validateMandatoryFields(fieldErrors, excelToObjectMapper, templateXLS, columnMapper);
		if (!fieldErrors.isEmpty()) {
			BindingResult bindingResult = new BeanPropertyBindingResult(templateXLS.getSimpleName(), files.get(0).getName());
			fieldErrors.forEach(bindingResult::addError);

			throw new ValidationException(bindingResult);
		}
	}


	/**
	 * Validate mandatory fields
	 *
	 * @param fieldErrors
	 * @param excelToObjectMapper
	 * @param mappedToClass
	 * @param columnMapper
	 * @throws Exception
	 */
	private void validateMandatoryFields(List<FieldError> fieldErrors, ExcelToObjectMapper excelToObjectMapper,
			Class mappedToClass, ColumnMapper columnMapper) throws ReflectiveOperationException {
		Map<String, String> columnNameToMandatoryColumnsMap = columnMapper.getMandatoryFieldMap();
		List<?> result = excelToObjectMapper.map(mappedToClass, columnNameToMandatoryColumnsMap);
		AtomicInteger rowIndex = new AtomicInteger(0);
		result.forEach(row -> {
			rowIndex.incrementAndGet();
			columnNameToMandatoryColumnsMap.keySet().forEach(columnNameToMandatoryColumnsMapKey -> {
				try {
					validateField(fieldErrors, mappedToClass, rowIndex, row, columnNameToMandatoryColumnsMapKey);
				} catch (NoSuchFieldException | IllegalAccessException e) {
					logger.error("Field doesn't exists : {}", columnNameToMandatoryColumnsMapKey);
				}
			});
		});
	}

	private void validateField(List<FieldError> fieldErrors, Class mappedToClass, AtomicInteger rowIndex, Object row, String columnNameToMandatoryColumnsMapKey) throws NoSuchFieldException, IllegalAccessException {
		Field currentField = row.getClass().getDeclaredField(columnNameToMandatoryColumnsMapKey);
		if (currentField != null) {
			currentField.setAccessible(true);
			if(currentField.getType() == String.class) {
				String value = (String) currentField.get(row);
				if (StringUtils.isBlank(value)) {
					fieldErrors.add(new FieldError(mappedToClass.getSimpleName(), columnNameToMandatoryColumnsMapKey,
							String.format(FIELD_IS_REQUIRED_AT_LINE_NO, columnNameToMandatoryColumnsMapKey, rowIndex.get())));
				}
			} else if(currentField.getType() == Date.class) {
				Date date = (Date) currentField.get(row);//TODO should throw an exception on invalid date
				if (date == null) {
					fieldErrors.add(new FieldError(mappedToClass.getSimpleName(), columnNameToMandatoryColumnsMapKey,
							String.format(DATE_MISSING_OR_INVALID_ON_MANDATORY_FIELD, columnNameToMandatoryColumnsMapKey)));
				}
			} else if(currentField.getType() == Float.class) {
				//TODO validate Float Fields
			}
		}
	}
}
