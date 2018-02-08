package com.transformuk.hee.tis.genericupload.service.api.validation;

import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.api.UploadFileResource;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.service.parser.ColumnMapper;
import com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper;
import com.transformuk.hee.tis.genericupload.service.parser.PersonHeaderMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
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
	public void validate(List<MultipartFile> files, FileType fileType) throws Exception {
		List<FieldError> fieldErrors = new ArrayList<>();
		if (!ObjectUtils.isEmpty(files)) {
			for (MultipartFile file : files) {
				if (!ObjectUtils.isEmpty(file) && StringUtils.isNotEmpty(file.getContentType())) {
					ExcelToObjectMapper excelToObjectMapper = new ExcelToObjectMapper(file.getInputStream());
					if (fileType.equals(FileType.RECRUITMENT)) {
						validateMandatoryFields(fieldErrors, excelToObjectMapper, PersonXLS.class, new PersonHeaderMapper());
					}
				}
			}
		}

		if (!fieldErrors.isEmpty()) {
			BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult("Bulk-Upload", files.get(0).getName());
			fieldErrors.forEach(bindingResult::addError);
			throw new MethodArgumentNotValidException(null, bindingResult);
		}
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
			Class dtoClass, ColumnMapper columnMapper) throws Exception {
		Map<String, String> columnNameToMandatoryColumnsMap = columnMapper.getMandatoryFieldMap();
		List<PersonXLS> result = excelToObjectMapper.map(dtoClass, columnNameToMandatoryColumnsMap);
		AtomicInteger rowIndex = new AtomicInteger(0);
		result.forEach(row -> {
			rowIndex.incrementAndGet();
			columnNameToMandatoryColumnsMap.entrySet().stream().forEach(columnNameToMandatoryColumnsMapEntry -> {
				try {
					Field currentField = row.getClass().getDeclaredField(columnNameToMandatoryColumnsMapEntry.getKey());
					if (currentField != null) {
						currentField.setAccessible(true);
						String value = (String) currentField.get(row);
						if (StringUtils.isEmpty(value)) {
							fieldErrors.add(new FieldError("Bulk-Upload", columnNameToMandatoryColumnsMapEntry.getKey(),
									String.format("%s Field is required at line no %d ", columnNameToMandatoryColumnsMapEntry.getKey(), rowIndex.get())));
						}
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					LOG.debug("Field doesn't exists");
				}
			});
		});
	}

}
