package com.transformuk.hee.tis.genericupload.service.service.impl;

import com.google.gson.Gson;
import com.microsoft.azure.storage.StorageException;
import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import com.transformuk.hee.tis.genericupload.service.service.FileImportResults;
import com.transformuk.hee.tis.genericupload.service.service.UploadFileService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper.isEmptyRow;
import static com.transformuk.hee.tis.genericupload.service.service.impl.SpecificationFactory.containsLike;

@Service
@Transactional
public class UploadFileServiceImpl implements UploadFileService {

	public static final String REASON_FOR_IMPORT_FAILURE = "Reason for import failure";

	private final Logger LOG = LoggerFactory.getLogger(UploadFileServiceImpl.class);

	private final FileStorageRepository fileStorageRepository;
	private final ApplicationTypeRepository applicationTypeRepository;

	@Autowired
	public UploadFileServiceImpl(FileStorageRepository fileStorageRepository,
	                             ApplicationTypeRepository applicationTypeRepository) {
		this.fileStorageRepository = fileStorageRepository;
		this.applicationTypeRepository = applicationTypeRepository;
		// this.personRepository = personRepository;
	}

	public ApplicationType save(String fileName, long logId, String username, String firstName, String lastName) {
		LOG.info("Request to save ApplicationType based on fileName : {}", fileName);

		ApplicationType applicationType = new ApplicationType();
		applicationType.setFileName(fileName);
		applicationType.setUploadedDate(LocalDateTime.now());
		applicationType.setFileType(FileType.RECRUITMENT);
		applicationType.setFileStatus(FileStatus.PENDING);
		applicationType.setLogId(logId);
		applicationType.setUsername(username);
		applicationType.setFirstName(firstName);
		applicationType.setLastName(lastName);

		return applicationTypeRepository.save(applicationType);
	}

	@Override
	public ApplicationType upload(List<MultipartFile> files, String username, String firstName, String lastName) throws InvalidKeyException, StorageException, URISyntaxException {
		long logId = System.currentTimeMillis();

		ApplicationType applicationType = null;
		if (!ObjectUtils.isEmpty(files)) {
			fileStorageRepository.store(logId, CONTAINER_NAME, files);
			for (MultipartFile file : files) {
				if (!ObjectUtils.isEmpty(file) && StringUtils.isNotEmpty(file.getContentType())) {
					applicationType = save(file.getOriginalFilename(), logId, username, firstName, lastName);
				}
			}
		}
		return applicationType;
	}

	@Override
	public String findErrorsByLogId(Long logId, OutputStream fileWithErrorsOnly) {
		ApplicationType applicationType = applicationTypeRepository.findByLogId(logId);
		Gson gson = new Gson();
		FileImportResults fileImportResults = gson.fromJson(applicationType.getErrorJson(), FileImportResults.class);
		Map<Integer, String> lineNumberErrors = fileImportResults.getLineNumberErrors();
		Set<Integer> setOfLineNumbersWithErrors = lineNumberErrors.keySet();

		try (InputStream bis = new ByteArrayInputStream(fileStorageRepository.download(applicationType.getLogId(), UploadFileService.CONTAINER_NAME, applicationType.getFileName()))) {
			Workbook workbook = WorkbookFactory.create(bis);

			Sheet sheet = workbook.getSheetAt(0);
			int totalColumns = sheet.getRow(0).getLastCellNum();
			int errorReportingColumnIndex = totalColumns;
	    if(sheet.getRow(0).getCell(errorReportingColumnIndex - 1).getStringCellValue().equalsIgnoreCase(REASON_FOR_IMPORT_FAILURE)) {
		    errorReportingColumnIndex--; //overwrite the last error column
	    }
			setErrorHeader(workbook, sheet, errorReportingColumnIndex);

			for (int rowNumber = sheet.getLastRowNum(); rowNumber > 0; rowNumber--) {
				Row row = sheet.getRow(rowNumber);
				if(row == null) {
					continue;
				} else if(isEmptyRow(row) || !setOfLineNumbersWithErrors.contains(rowNumber)) {
					removeRow(sheet, rowNumber);
					continue;
				}

				Cell errorReportingCell = row.createCell(errorReportingColumnIndex, CellType.STRING);
				setFontToRed(workbook, errorReportingCell.getCellStyle(), false);
				errorReportingCell.setCellValue(lineNumberErrors.get(rowNumber));
			}

			sheet.autoSizeColumn(errorReportingColumnIndex);
			workbook.write(fileWithErrorsOnly);
		} catch (IOException | InvalidFormatException e) {
			LOG.error("Error building errors with uploaded template : " + e.getMessage());
		}

		return applicationType.getFileName();
	}

	public void setErrorHeader(Workbook workbook, Sheet sheet, int totalColumns) {
		Cell errorReportingCellHeader = sheet.getRow(0).createCell(totalColumns, CellType.STRING);
		errorReportingCellHeader.setCellValue(REASON_FOR_IMPORT_FAILURE);

		CellStyle precedingCellHeaderStyle = sheet.getRow(0).getCell(totalColumns - 1).getCellStyle();
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.cloneStyleFrom(precedingCellHeaderStyle);
		errorReportingCellHeader.setCellStyle(headerStyle);

		setFontToRed(workbook, headerStyle, true);
	}

	public void setFontToRed(Workbook workbook, CellStyle cellStyle, boolean embolden) {
		Font font = workbook.createFont();
		font.setColor(IndexedColors.RED.getIndex());
		font.setBold(embolden);
		cellStyle.setFont(font);
	}

	//Helper method to shift rows up to remove a row as the removeRow method only blanks it out - https://stackoverflow.com/a/3554129
	public static void removeRow(Sheet sheet, int rowIndex) {
		int lastRowNum = sheet.getLastRowNum();
		if (rowIndex >= 0 && rowIndex < lastRowNum) {
			sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
		}
		if (rowIndex == lastRowNum) {
			Row removingRow = sheet.getRow(rowIndex);
			if (removingRow != null) {
				sheet.removeRow(removingRow);
			}
		}
	}

	@Override
	public Page<ApplicationType> getUploadStatus(Pageable pageable) {
		return applicationTypeRepository.findAll(pageable);
	}

	private static Specification<ApplicationType> dateIs(final LocalDateTime localDateTime) {
		return (final Root<ApplicationType> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) -> {
			if (localDateTime != null) {
				return builder.between(root.get("uploadedDate"), localDateTime, localDateTime.plusDays(1).minusSeconds(1));
			}
			return builder.conjunction();
		};
	}

	@Transactional(readOnly = true)
	public Page<ApplicationType> searchUploads(LocalDateTime uploadedDate, String file, String user, Pageable pageable) {
		List<Specification<ApplicationType>> specs = new ArrayList<>();
		//add the text search criteria
		if (uploadedDate != null) {
			specs.add(Specifications.where(dateIs(uploadedDate)));
		}
		if (StringUtils.isNotEmpty(user)) {
			specs.add(Specifications.where(containsLike("username", user))
					.or(Specifications.where(containsLike("firstName", user)))
					.or(Specifications.where(containsLike("lastName", user))));
		}
		if (StringUtils.isNotEmpty(file)) {
			specs.add(Specifications.where(containsLike("fileName", file)));
		}

		Page<ApplicationType> result;
		if (!specs.isEmpty()) {
			Specifications<ApplicationType> fullSpec = Specifications.where(specs.get(0));
			//add the rest of the specs that made it in
			for (int i = 1; i < specs.size(); i++) {
				fullSpec = fullSpec.and(specs.get(i));
			}
			result = applicationTypeRepository.findAll(fullSpec, pageable);
		} else {
			result = applicationTypeRepository.findAll(pageable);
		}
		return result;
	}

	@Override
	public Page<ApplicationType> searchUploads(String text, Pageable pageable) {
		return applicationTypeRepository.fullTextSearch(text, pageable);
	}
}
