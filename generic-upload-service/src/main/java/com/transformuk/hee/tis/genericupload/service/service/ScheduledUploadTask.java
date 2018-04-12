package com.transformuk.hee.tis.genericupload.service.service;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.config.AzureProperties;
import com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper;
import com.transformuk.hee.tis.genericupload.service.parser.PersonHeaderMapper;
import com.transformuk.hee.tis.genericupload.service.parser.PlacementHeaderMapper;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ScheduledUploadTask {
	private static final Logger logger = getLogger(ScheduledUploadTask.class);
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	@Autowired
	private PlacementTransformerService placementTransformerService;
	@Autowired
	private PersonTransformerService personTransformerService;

	private final ApplicationTypeRepository applicationTypeRepository;
	private final AzureProperties azureProperties;
	private final FileStorageRepository fileStorageRepository;

	@Autowired
	public ScheduledUploadTask(FileStorageRepository fileStorageRepository,
	                           ApplicationTypeRepository applicationTypeRepository,
	                           AzureProperties azureProperties) {
		this.fileStorageRepository = fileStorageRepository;
		this.applicationTypeRepository = applicationTypeRepository;
		this.azureProperties = azureProperties;
	}

	//waits fixedDelay milliseconds after the last run task
	@Scheduled(fixedDelay = 5000, initialDelay = 2000) //TODO externalise this wait interval,
	public void scheduleTaskWithFixedDelay() {
		logger.info("Fixed Delay Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
		//TODO circuit-break on tcs/profile/reference/mysql connectivity
		for (ApplicationType applicationType : applicationTypeRepository.findByFileStatusOrderByUploadedDate(FileStatus.PENDING)) {
			//set to in progress
			applicationType.setFileStatus(FileStatus.IN_PROGRESS);
			applicationTypeRepository.save(applicationType);

			try (InputStream bis = new ByteArrayInputStream(fileStorageRepository.download(applicationType.getLogId(), azureProperties.getContainerName(), applicationType.getFileName()))) {
				ExcelToObjectMapper excelToObjectMapper = new ExcelToObjectMapper(bis);

				switch (applicationType.getFileType()) {

					case PEOPLE:
						List<PersonXLS> personXLSS = excelToObjectMapper.map(PersonXLS.class, new PersonHeaderMapper().getFieldMap());
						personTransformerService.processPeopleUpload(personXLSS);
						setJobToCompleted(applicationType, personXLSS);
						break;

					case PLACEMENTS:
						List<PlacementXLS> placementXLSS = excelToObjectMapper.map(PlacementXLS.class, new PlacementHeaderMapper().getFieldMap());
						placementTransformerService.processPlacementsUpload(placementXLSS);
						setJobToCompleted(applicationType, placementXLSS);
						break;

					default: logger.error("Unknown FileType");
				}
			} catch (InvalidFormatException e) {
				logger.error("Error while reading excel file : " + e.getMessage());
				applicationType.setFileStatus(FileStatus.INVALID_FILE_FORMAT);
			} catch (HttpServerErrorException | HttpClientErrorException e) { //thrown when connecting to TCS
				logger.error("Error while processing excel file : " + e.getMessage());
				applicationType.setFileStatus(FileStatus.PENDING);
			} catch (Exception e) {
				logger.error("Unknown Error while processing excel file : " + e.getMessage());
				e.printStackTrace();
				applicationType.setFileStatus(FileStatus.UNEXPECTED_ERROR);
			} finally {
				applicationTypeRepository.save(applicationType);
			}
		}
	}

	private void setJobToCompleted(ApplicationType applicationType, List<? extends TemplateXLS> templateXLSS) {
		FileImportResults fir = new FileImportResults();
		int errorCount = 0, successCount = 0;
		for (TemplateXLS templateXLS : templateXLSS) {
			if (templateXLS.isSuccessfullyImported()) {
				successCount++;
			} else if (!StringUtils.isEmpty(templateXLS.getErrorMessage())) {
				errorCount++;
				fir.addError(templateXLS.getRowNumber(), templateXLS.getErrorMessage());
			}
		}

		applicationType.setNumberOfErrors(errorCount);
		applicationType.setNumberImported(successCount);
		applicationType.setErrorJson(fir.toJson());
		applicationType.setProcessedDate(LocalDateTime.now());
		applicationType.setFileStatus(FileStatus.COMPLETED);
	}
}
