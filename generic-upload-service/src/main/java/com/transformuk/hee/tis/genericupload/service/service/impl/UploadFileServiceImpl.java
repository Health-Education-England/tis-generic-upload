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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;

import static com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper.isEmptyRow;
import static com.transformuk.hee.tis.genericupload.service.service.impl.SpecificationFactory.containsLike;

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
				if(sheet.getRow(rowNumber) == null || isEmptyRow(sheet.getRow(rowNumber))) continue;
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
