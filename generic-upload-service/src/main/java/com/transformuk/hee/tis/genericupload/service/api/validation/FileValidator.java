package com.transformuk.hee.tis.genericupload.service.api.validation;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
	public FileType validate(List<MultipartFile> files, boolean validateMandatoryFields) throws IOException, NoSuchFieldException, InstantiationException, ParseException, IllegalAccessException, InvalidFormatException, MethodArgumentNotValidException {
		List<FieldError> fieldErrors = new ArrayList<>();
		FileType fileType = null;
		if (!ObjectUtils.isEmpty(files)) {
			for (MultipartFile file : files) {
				if (!ObjectUtils.isEmpty(file) && StringUtils.isNotEmpty(file.getContentType())) {
					ExcelToObjectMapper excelToObjectMapper = new ExcelToObjectMapper(file.getInputStream());
					if (validateMandatoryFields) {
						fileType = getFileType(excelToObjectMapper);
						if (fileType.equals(FileType.PEOPLE)) {
							validateMandatoryFields(fieldErrors, excelToObjectMapper, PersonXLS.class, new PersonHeaderMapper());
						} else if (fileType.equals(FileType.PLACEMENTS)) {
							validateMandatoryFields(fieldErrors, excelToObjectMapper, PlacementXLS.class, new PlacementHeaderMapper());
						}
					}
				}
			}
		}

		if (!fieldErrors.isEmpty()) {
			BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult("Bulk-Upload", files.get(0).getName());
			fieldErrors.forEach(bindingResult::addError);
			throw new MethodArgumentNotValidException(null, bindingResult);
		}
		return fileType;
	}

	private FileType getFileType(ExcelToObjectMapper excelToObjectMapper) {
		return excelToObjectMapper.getHeaders().contains("Placement Type*") ? FileType.PLACEMENTS : FileType.PEOPLE;
	}

	/**
	 * Validate mandatory fields
	 *
	 * @param fieldErrors
	 * @param excelToObjectMapper
	 * @param dtoClass
	 * @param columnMapper
	 * @throws Exception
	 */
	private void validateMandatoryFields(List<FieldError> fieldErrors, ExcelToObjectMapper excelToObjectMapper,
			Class dtoClass, ColumnMapper columnMapper) throws InstantiationException, IllegalAccessException, ParseException, NoSuchFieldException {
		Map<String, String> columnNameToMandatoryColumnsMap = columnMapper.getMandatoryFieldMap();
		List<?> result = excelToObjectMapper.map(dtoClass, columnNameToMandatoryColumnsMap);
		AtomicInteger rowIndex = new AtomicInteger(0);
		result.forEach(row -> {
			rowIndex.incrementAndGet();
			columnNameToMandatoryColumnsMap.keySet().forEach(columnNameToMandatoryColumnsMapKey -> {
				try {
					Field currentField = row.getClass().getDeclaredField(columnNameToMandatoryColumnsMapKey);
					if (currentField != null) {
						currentField.setAccessible(true);
						String value = (String) currentField.get(row);
						if (StringUtils.isBlank(value)) {
							fieldErrors.add(new FieldError("Bulk-Upload", columnNameToMandatoryColumnsMapKey,
									String.format("%s Field is required at line no %d ", columnNameToMandatoryColumnsMapKey, rowIndex.get())));
						}
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					LOG.debug("Field doesn't exists");
				}
			});
		});
	}

}
