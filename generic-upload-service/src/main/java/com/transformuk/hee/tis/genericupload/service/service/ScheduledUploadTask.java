package com.transformuk.hee.tis.genericupload.service.service;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.dto.*;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.config.AzureProperties;
import com.transformuk.hee.tis.genericupload.service.parser.*;
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
	private static final int HOURS_TO_WAIT_BEFORE_RESTARTING = 3; //TODO externalise

	private static final String FILE_IMPORT_SUCCESS_AND_ERROR_COUNTS_DON_T_MATCH_INPUT_NUMBER_OF_ROWS = "File import success and error counts don't match input number of rows";
	private static final String UNKNOWN_FILE_TYPE = "Unknown FileType";
	private static final String ERROR_WHILE_READING_EXCEL_FILE = "Error while reading excel file : {}";
	private static final String ERROR_WHILE_PROCESSING_EXCEL_FILE = "Error while processing excel file : {}";
	private static final String UNKNOWN_ERROR_WHILE_PROCESSING_EXCEL_FILE = "Unknown Error while processing excel file : {}";


	@Autowired
	private PlacementTransformerService placementTransformerService;
	@Autowired
	private PlacementDeleteService placementDeleteService;
	@Autowired
	private PersonTransformerService personTransformerService;
	@Autowired
	private AssessmentTransformerService assessmentTransformerService;

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

	@Scheduled(fixedDelay = 1000, initialDelay = 2000) //TODO externalise this wait interval,
	public void scheduleTaskWithFixedDelay() {
		if(logger.isDebugEnabled()) {
			logger.debug("Fixed Delay Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
		}
		//TODO circuit-break on tcs/profile/reference/mysql connectivity
		ApplicationType applicationType = applicationTypeRepository.findFirstByFileStatusOrderByUploadedDate(FileStatus.PENDING);
		if(applicationType != null) {
			applicationType.setFileStatus(FileStatus.IN_PROGRESS);
			applicationType.setJobStartTime(LocalDateTime.now());
			applicationTypeRepository.save(applicationType);

			try (InputStream bis = new ByteArrayInputStream(fileStorageRepository.download(applicationType.getLogId(), azureProperties.getContainerName(), applicationType.getFileName()))) {
				ExcelToObjectMapper excelToObjectMapper = new ExcelToObjectMapper(bis, true);

				switch (applicationType.getFileType()) {

					case PEOPLE:
						List<PersonXLS> personXLSS = excelToObjectMapper.map(PersonXLS.class, new PersonHeaderMapper().getFieldMap());
						personTransformerService.processPeopleUpload(personXLSS);
						setJobToCompleted(applicationType, personXLSS);
						break;

					case PLACEMENTS:
						List<PlacementXLS> placementXLSS = excelToObjectMapper.map(PlacementXLS.class, new PlacementHeaderMapper().getFieldMap());
						placementTransformerService.processPlacementsUpload(placementXLSS, applicationType.getUsername());
						setJobToCompleted(applicationType, placementXLSS);
						break;

					case PLACEMENTS_DELETE:
						List<PlacementDeleteXLS> placementDeleteXLSS = excelToObjectMapper.map(PlacementDeleteXLS.class, new PlacementDeleteHeaderMapper().getFieldMap());
						placementDeleteService.processPlacementsDeleteUpload(placementDeleteXLSS);
						setJobToCompleted(applicationType, placementDeleteXLSS);
						break;

					case ASSESSMENTS:
						List<AssessmentXLS> assessmentXLSList = excelToObjectMapper.map(AssessmentXLS.class, new AssessmentHeaderMapper().getFieldMap());
						assessmentTransformerService.processAssessmentsUpload(assessmentXLSList, applicationType.getUsername());
						setJobToCompleted(applicationType, assessmentXLSList);
						break;

					default: logger.error(UNKNOWN_FILE_TYPE);
				}
			} catch (InvalidFormatException e) {
				logger.error(ERROR_WHILE_READING_EXCEL_FILE, e.getMessage(), e);
				applicationType.setFileStatus(FileStatus.INVALID_FILE_FORMAT);
			} catch (HttpServerErrorException | HttpClientErrorException e) { //thrown when connecting to TCS
				logger.error(ERROR_WHILE_PROCESSING_EXCEL_FILE, e.getMessage(), e);
				applicationType.setFileStatus(FileStatus.PENDING);
			} catch (Exception e) {
				logger.error(UNKNOWN_ERROR_WHILE_PROCESSING_EXCEL_FILE, e.getMessage(), e);
				applicationType.setFileStatus(FileStatus.UNEXPECTED_ERROR);
			} finally {
				applicationTypeRepository.save(applicationType);
			}
		}

		resetJobsInProgressIfOverHours(HOURS_TO_WAIT_BEFORE_RESTARTING);
	}

	/** Iterate through the jobs in progress (for over 3 hours) and set them to be restarted;
	 *   - This is needed if the application terminates abruptly while a job is in progress;
	 *   - Also caters for multiple instances of bulk uploads
	 */
	private void resetJobsInProgressIfOverHours(int hours) {
		for(ApplicationType inProgressApplicationType : applicationTypeRepository.findByFileStatusOrderByUploadedDate(FileStatus.IN_PROGRESS)) {
			if(inProgressApplicationType.getJobStartTime().plusHours(hours).isBefore(LocalDateTime.now())) {
				logger.info("Resetting status on job for file {} with log id {}", inProgressApplicationType.getFileName(), inProgressApplicationType.getLogId());
				inProgressApplicationType.setFileStatus(FileStatus.PENDING);
				applicationTypeRepository.save(inProgressApplicationType);
			}
		}
	}

	private void setJobToCompleted(ApplicationType applicationType, List<? extends TemplateXLS> templateXLSS) {
		FileImportResults fir = new FileImportResults();
		int errorCount = 0;
		int successCount = 0;
		for (TemplateXLS templateXLS : templateXLSS) {
			if (templateXLS.isSuccessfullyImported()) {
				successCount++;
			} else if (!StringUtils.isEmpty(templateXLS.getErrorMessage())) {
				errorCount++;
				fir.addError(templateXLS.getRowNumber(), templateXLS.getErrorMessage());
			}
		}

		if(errorCount + successCount != templateXLSS.size()) {
			logger.warn(FILE_IMPORT_SUCCESS_AND_ERROR_COUNTS_DON_T_MATCH_INPUT_NUMBER_OF_ROWS);
		}

		applicationType.setNumberOfErrors(errorCount);
		applicationType.setNumberImported(successCount);
		applicationType.setErrorJson(fir.toJson());
		applicationType.setProcessedDate(LocalDateTime.now());
		applicationType.setFileStatus(FileStatus.COMPLETED);
		logger.info("Job completed for file {} with log id {}", applicationType.getFileName(), applicationType.getLogId());
	}
}
