package com.transformuk.hee.tis.genericupload.service.api.validation;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.api.UploadFileResource;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.service.parser.ColumnMapper;
import com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper;
import com.transformuk.hee.tis.genericupload.service.parser.PersonHeaderMapper;
import com.transformuk.hee.tis.genericupload.service.parser.PlacementHeaderMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BeanPropertyBindingResult;
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

	private final Logger LOG = LoggerFactory.getLogger(UploadFileResource.class);

	/**
	 * Custom validator used during file upload checks for mandatory fields
	 *
	 * @param files
	 *            The provided files to validate
	 * @throws MethodArgumentNotValidException
	 */
	public FileType validate(List<MultipartFile> files, boolean validateMandatoryFields, boolean validateDates) throws IOException, InvalidFormatException, MethodArgumentNotValidException, ReflectiveOperationException {
		List<FieldError> fieldErrors = new ArrayList<>();
		FileType fileType = null;

		if (!ObjectUtils.isEmpty(files)) {
			for (MultipartFile file : files) {
				if (!ObjectUtils.isEmpty(file) && StringUtils.isNotEmpty(file.getContentType())) {
					ExcelToObjectMapper excelToObjectMapper = new ExcelToObjectMapper(file.getInputStream(), validateDates);
					if (validateMandatoryFields) {
						Set<String> headers = excelToObjectMapper.getHeaders();
						if(headers.contains("Placement Type*")) {
							fileType = FileType.PLACEMENTS;
							validateMandatoryFieldsOrThrowError(files, fieldErrors, PlacementXLS.class, excelToObjectMapper, new PlacementHeaderMapper());
						} else if(headers.contains("Email Address")) { //TODO do something more robust than this
							fileType = FileType.PEOPLE;
							validateMandatoryFieldsOrThrowError(files, fieldErrors, PersonXLS.class, excelToObjectMapper, new PersonHeaderMapper());
						} else {
							throw new InvalidFormatException("Unrecognised upload template");
						}
					}
				}
			}
		}


		return fileType;
	}

	public void validateMandatoryFieldsOrThrowError(List<MultipartFile> files, List<FieldError> fieldErrors, Class templateXLS, ExcelToObjectMapper excelToObjectMapper, ColumnMapper columnMapper) throws ReflectiveOperationException, MethodArgumentNotValidException {
		validateMandatoryFields(fieldErrors, excelToObjectMapper, templateXLS, columnMapper);
		if (!fieldErrors.isEmpty()) {
			BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(templateXLS.getSimpleName(), files.get(0).getName());
			fieldErrors.forEach(bindingResult::addError);

			throw new MethodArgumentNotValidException(null, bindingResult);
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
					Field currentField = row.getClass().getDeclaredField(columnNameToMandatoryColumnsMapKey);
					if (currentField != null) {
						currentField.setAccessible(true);
						if(currentField.getType() == String.class) {
							String value = (String) currentField.get(row);
							if (StringUtils.isBlank(value)) {
								fieldErrors.add(new FieldError(mappedToClass.getSimpleName(), columnNameToMandatoryColumnsMapKey,
										String.format("%s Field is required at line no %d ", columnNameToMandatoryColumnsMapKey, rowIndex.get())));
							}
						} else if(currentField.getType() == Date.class) {
							//TODO validate Date Fields
						} else if(currentField.getType() == Float.class) {
							//TODO validate Float Fields
						}
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					LOG.error("Field doesn't exists : " + columnNameToMandatoryColumnsMapKey);
				}
			});
		});
	}

}
