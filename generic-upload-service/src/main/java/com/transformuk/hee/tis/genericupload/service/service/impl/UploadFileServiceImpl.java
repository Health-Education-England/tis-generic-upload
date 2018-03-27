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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class UploadFileServiceImpl implements UploadFileService {

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
	public Map<String, OutputStream> findErrorsByLogId(Long logId) {
		ApplicationType applicationType = applicationTypeRepository.findByLogId(logId);
		Gson gson = new Gson();
		FileImportResults fileImportResults = gson.fromJson(applicationType.getErrorJson(), FileImportResults.class);
		Map<Integer, String> lineNumberErrors = fileImportResults.getLineNumberErrors();
		Set<Integer> setOfLineNumbersWithErrors = lineNumberErrors.keySet();
		OutputStream fileWithErrorsOnly = new ByteArrayOutputStream();

		try (InputStream bis = new ByteArrayInputStream(fileStorageRepository.download(applicationType.getLogId(), UploadFileService.CONTAINER_NAME, applicationType.getFileName()))) {
			Workbook workbook = WorkbookFactory.create(bis);

			Sheet sheet = workbook.getSheetAt(0);
			int totalColumns = sheet.getRow(0).getLastCellNum();
			for (int rowNumber = sheet.getLastRowNum(); rowNumber > 0; rowNumber--) {
				if(isEmptyRow(sheet.getRow(rowNumber))) continue;
				int indexInProcessedErrorMap = rowNumber - 1;
				if(setOfLineNumbersWithErrors.contains(indexInProcessedErrorMap)) {
					Cell errorReportingCell = sheet.getRow(rowNumber).createCell(totalColumns, CellType.STRING);
					errorReportingCell.setCellValue(lineNumberErrors.get(indexInProcessedErrorMap));
				} else {
					sheet.removeRow(sheet.getRow(rowNumber));
				}
			}
			workbook.write(fileWithErrorsOnly);
		} catch (IOException | InvalidFormatException e) {
			LOG.error("Error working with uploaded template : " + e.getMessage());
		}

		return Collections.singletonMap(applicationType.getFileName(), fileWithErrorsOnly);
	}

	//https://stackoverflow.com/a/20002688
	boolean isEmptyRow(Row row){
		boolean isEmptyRow = true;
		for(int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++){
			Cell cell = row.getCell(cellNum);
			if(cell != null && cell.getCellTypeEnum() != CellType.BLANK && StringUtils.isNotBlank(cell.toString())){
				isEmptyRow = false;
			}
		}
		return isEmptyRow;
	}

	@Override
	public Page<ApplicationType> getUploadStatus(Pageable pageable) {
		return applicationTypeRepository.findAll(pageable);
	}

	@Override
	public Page<ApplicationType> searchUploads(String text, Pageable pageable) {
		return applicationTypeRepository.fullTextSearch(text, pageable);
	}

	@Transactional(readOnly = true)
	public Page<ApplicationType> searchUploads(String uploadedDate, String file, String user, Pageable pageable) {
		if(!StringUtils.isBlank(file) && !StringUtils.isBlank(user) && !StringUtils.isBlank(uploadedDate)) {
			return applicationTypeRepository.restrictedTextSearchAll(uploadedDate, file, user, pageable);
		} else if(!StringUtils.isBlank(file) && !StringUtils.isBlank(user)) {
			return applicationTypeRepository.restrictedTextSearchFileUser(file, user, pageable);
		} else if(!StringUtils.isBlank(file) && !StringUtils.isBlank(uploadedDate)) {
			return applicationTypeRepository.restrictedTextSearchFileDate(uploadedDate, file, pageable);
		} else if(!StringUtils.isBlank(user) && !StringUtils.isBlank(uploadedDate)) {
			return applicationTypeRepository.restrictedTextSearchUserDate(user, uploadedDate, pageable);
		} else if(!StringUtils.isBlank(user)) {
			return applicationTypeRepository.restrictedTextSearchUser(user, pageable);
		} else if(!StringUtils.isBlank(uploadedDate)) {
			return applicationTypeRepository.restrictedTextSearchDate(uploadedDate, pageable);
		} else if(!StringUtils.isBlank(file)) {
			return applicationTypeRepository.restrictedTextSearchFile(file, pageable);
		} else {
			return applicationTypeRepository.findAll(pageable);
		}
	}
}
