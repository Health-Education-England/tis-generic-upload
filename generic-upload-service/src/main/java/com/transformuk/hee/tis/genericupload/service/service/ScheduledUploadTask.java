package com.transformuk.hee.tis.genericupload.service.service;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentDeleteXLS;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentXLS;
import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXls;
import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.FundingUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementDeleteXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementXls;
import com.transformuk.hee.tis.genericupload.api.dto.PostCreateXls;
import com.transformuk.hee.tis.genericupload.api.dto.PostFundingUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.ProgrammeMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.parser.ColumnMapper;
import com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Component
public class ScheduledUploadTask {

  private static final Logger logger = getLogger(ScheduledUploadTask.class);

  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
      .ofPattern("HH:mm:ss");
  private static final int HOURS_TO_WAIT_BEFORE_RESTARTING = 3; //TODO externalise

  private static final String
      FILE_IMPORT_SUCCESS_AND_ERROR_COUNTS_DON_T_MATCH_INPUT_NUMBER_OF_ROWS =
      "File import success and error counts don't match input number of rows";
  private static final String UNKNOWN_FILE_TYPE = "Unknown FileType";
  private static final String ERROR_WHILE_READING_EXCEL_FILE =
      "Error while reading excel file : {}";
  private static final String ERROR_WHILE_PROCESSING_EXCEL_FILE =
      "Error while processing excel file : {}";
  private static final String UNKNOWN_ERROR_WHILE_PROCESSING_EXCEL_FILE =
      "Unknown Error while processing excel file : {}";
  private final ApplicationTypeRepository applicationTypeRepository;
  private final FileStorageRepository fileStorageRepository;
  @Autowired
  private PlacementTransformerService placementTransformerService;
  @Autowired
  private PlacementDeleteService placementDeleteService;
  @Autowired
  private PersonTransformerService personTransformerService;
  @Autowired
  private PersonUpdateTransformerService personUpdateTransformerService;
  @Autowired
  private AssessmentTransformerService assessmentTransformerService;
  @Autowired
  private AssessmentDeleteService assessmentDeleteService;
  @Autowired
  private PlacementUpdateTransformerService placementUpdateTransformerService;
  @Autowired
  private PostCreateTransformerService postCreateTransformerService;
  @Autowired
  private PostUpdateTransformerService postUpdateTransformerService;
  @Autowired
  private PostFundingUpdateTransformerService postFundingUpdateTransformerService;
  @Autowired
  private FundingUpdateTransformerService fundingUpdateTransformerService;
  @Autowired
  private AssessmentUpdateTransformerService assessmentUpdateTransformerService;
  @Autowired
  private ProgrammeMembershipUpdateTransformerService pmUpdateTransformerService;
  @Autowired
  private CurriculumMembershipCreateTransformerService cmCreateTransformerService;

  @Autowired
  private CurriculumMembershipUpdateTransformerService cmUpdateTransformerService;

  @Autowired
  public ScheduledUploadTask(
      @Qualifier("awsFileStorageRepository") FileStorageRepository fileStorageRepository,
      ApplicationTypeRepository applicationTypeRepository) {
    this.fileStorageRepository = fileStorageRepository;
    this.applicationTypeRepository = applicationTypeRepository;
  }

  @Scheduled(fixedDelay = 1000, initialDelay = 2000) //TODO externalise this wait interval,
  public void scheduleTaskWithFixedDelay() {
    if (logger.isDebugEnabled()) {
      logger.debug("Fixed Delay Task :: Execution Time - {}",
          dateTimeFormatter.format(LocalDateTime.now()));
    }
    //TODO circuit-break on tcs/profile/reference/mysql connectivity
    ApplicationType applicationType = applicationTypeRepository
        .findFirstByFileStatusOrderByUploadedDate(FileStatus.PENDING);
    if (applicationType != null) {
      applicationType.setFileStatus(FileStatus.IN_PROGRESS);
      applicationType.setJobStartTime(LocalDateTime.now());
      applicationTypeRepository.save(applicationType);

      try (InputStream bis = new ByteArrayInputStream(fileStorageRepository
          .download(applicationType.getLogId(), "fileupload",
              applicationType.getFileName()))) {
        ExcelToObjectMapper excelToObjectMapper = new ExcelToObjectMapper(bis, true, true);

        switch (applicationType.getFileType()) {

          case PEOPLE:
            List<PersonXLS> personXLSS = excelToObjectMapper
                .map(PersonXLS.class, new ColumnMapper(PersonXLS.class).getFieldMap());
            personTransformerService.processPeopleUpload(personXLSS);
            setJobToCompleted(applicationType, personXLSS);
            break;

          case PEOPLE_UPDATE:
            List<PersonUpdateXls> personUpdateXlss = excelToObjectMapper
                .map(PersonUpdateXls.class, new ColumnMapper(PersonUpdateXls.class).getFieldMap());
            personUpdateTransformerService.processUpload(personUpdateXlss);
            setJobToCompleted(applicationType, personUpdateXlss);
            break;

          case PLACEMENTS:
            List<PlacementXls> xlsRows = excelToObjectMapper
                .map(PlacementXls.class, new ColumnMapper(PlacementXls.class).getFieldMap());
            placementTransformerService
                .processPlacementsUpload(xlsRows, applicationType.getUsername());
            setJobToCompleted(applicationType, xlsRows);
            break;

          case PLACEMENTS_DELETE:
            List<PlacementDeleteXLS> placementDeleteXLSS = excelToObjectMapper
                .map(PlacementDeleteXLS.class,
                    new ColumnMapper(PlacementDeleteXLS.class).getFieldMap());
            placementDeleteService.processPlacementsDeleteUpload(placementDeleteXLSS);
            setJobToCompleted(applicationType, placementDeleteXLSS);
            break;

          case ASSESSMENTS:
            List<AssessmentXLS> assessmentXLSList = excelToObjectMapper
                .map(AssessmentXLS.class, new ColumnMapper(AssessmentXLS.class).getFieldMap());
            assessmentTransformerService.processAssessmentsUpload(assessmentXLSList);
            setJobToCompleted(applicationType, assessmentXLSList);
            break;

          case ASSESSMENTS_DELETE:
            List<AssessmentDeleteXLS> assessmentDeleteXLSS = excelToObjectMapper
                    .map(AssessmentDeleteXLS.class,
                            new ColumnMapper(AssessmentDeleteXLS.class).getFieldMap());
            assessmentDeleteService.processAssessmentsDeleteUpload(assessmentDeleteXLSS);
            setJobToCompleted(applicationType, assessmentDeleteXLSS);
            break;

          case PLACEMENTS_UPDATE:
            List<PlacementUpdateXLS> placementUpdateXLSList = excelToObjectMapper
                .map(PlacementUpdateXLS.class,
                    new ColumnMapper(PlacementUpdateXLS.class).getFieldMap());
            placementUpdateTransformerService.processPlacementsUpdateUpload(placementUpdateXLSList,
                applicationType.getUsername());
            setJobToCompleted(applicationType, placementUpdateXLSList);
            break;

          case POSTS_CREATE:
            List<PostCreateXls> postCreateXslList = excelToObjectMapper
                .map(PostCreateXls.class, new ColumnMapper(PostCreateXls.class).getFieldMap());
            postCreateTransformerService.processUpload(postCreateXslList);
            setJobToCompleted(applicationType, postCreateXslList);
            break;

          case POSTS_UPDATE:
            List<PostUpdateXLS> postUpdateXLSList = excelToObjectMapper
                .map(PostUpdateXLS.class, new ColumnMapper(PostUpdateXLS.class).getFieldMap());
            postUpdateTransformerService
                .processPostUpdateUpload(postUpdateXLSList, applicationType.getUsername());
            setJobToCompleted(applicationType, postUpdateXLSList);
            break;

          case POSTS_FUNDING_UPDATE:
            List<PostFundingUpdateXLS> postFundingUpdateXlsList = excelToObjectMapper
                .map(PostFundingUpdateXLS.class,
                    new ColumnMapper(PostFundingUpdateXLS.class).getFieldMap());
            postFundingUpdateTransformerService
                .processPostFundingUpdateUpload(postFundingUpdateXlsList);
            setJobToCompleted(applicationType, postFundingUpdateXlsList);
            break;

          case FUNDING_UPDATE:
            List<FundingUpdateXLS> fundingUpdateXLSList = excelToObjectMapper
                .map(FundingUpdateXLS.class,
                    new ColumnMapper(FundingUpdateXLS.class).getFieldMap());
            fundingUpdateTransformerService.processFundingUpdateUpload(fundingUpdateXLSList);
            setJobToCompleted(applicationType, fundingUpdateXLSList);
            break;

          case ASSESSMENTS_UPDATE:
            List<AssessmentUpdateXLS> assessmentUpdateXLSList = excelToObjectMapper
                .map(AssessmentUpdateXLS.class,
                    new ColumnMapper(AssessmentUpdateXLS.class).getFieldMap());
            assessmentUpdateTransformerService.processAssessmentsUpdateUpload(
                assessmentUpdateXLSList);
            setJobToCompleted(applicationType, assessmentUpdateXLSList);
            break;

          case PROGRAMME_MEMBERSHIP_UPDATE:
            List<ProgrammeMembershipUpdateXls> programmeMembershipUpdateXlsList =
                excelToObjectMapper.map(ProgrammeMembershipUpdateXls.class,
                    new ColumnMapper(ProgrammeMembershipUpdateXls.class).getFieldMap());
            pmUpdateTransformerService.processProgrammeMembershipsUpdateUpload(
                programmeMembershipUpdateXlsList);
            setJobToCompleted(applicationType, programmeMembershipUpdateXlsList);
            break;

          case CURRICULUM_MEMBERSHIP_CREATE:
            List<CurriculumMembershipCreateXls> cmCreateXlsList =
                excelToObjectMapper.map(CurriculumMembershipCreateXls.class,
                    new ColumnMapper(CurriculumMembershipCreateXls.class).getFieldMap());
            cmCreateTransformerService.processCurriculumMembershipCreateUpload(cmCreateXlsList);
            setJobToCompleted(applicationType, cmCreateXlsList);
            break;

          case CURRICULUM_MEMBERSHIP_UPDATE:
            List<CurriculumMembershipUpdateXls> cmUpdateXlsList =
                excelToObjectMapper.map(CurriculumMembershipUpdateXls.class,
                    new ColumnMapper(CurriculumMembershipUpdateXls.class).getFieldMap());
            cmUpdateTransformerService.processCurriculumMembershipUpdateUpload(cmUpdateXlsList);
            setJobToCompleted(applicationType, cmUpdateXlsList);
            break;

          default:
            logger.error(UNKNOWN_FILE_TYPE);
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

  /**
   * Iterate through the jobs in progress (for over 3 hours) and set them to be restarted; - This is
   * needed if the application terminates abruptly while a job is in progress; - Also caters for
   * multiple instances of bulk uploads
   */
  private void resetJobsInProgressIfOverHours(int hours) {
    for (ApplicationType inProgressApplicationType : applicationTypeRepository
        .findByFileStatusOrderByUploadedDate(FileStatus.IN_PROGRESS)) {
      if (inProgressApplicationType.getJobStartTime().plusHours(hours)
          .isBefore(LocalDateTime.now())) {
        logger.info("Resetting status on job for file {} with log id {}",
            inProgressApplicationType.getFileName(), inProgressApplicationType.getLogId());
        inProgressApplicationType.setFileStatus(FileStatus.PENDING);
        applicationTypeRepository.save(inProgressApplicationType);
      }
    }
  }

  private void setJobToCompleted(ApplicationType applicationType,
      List<? extends TemplateXLS> templateXLSS) {
    FileImportResults fir = new FileImportResults();
    int errorCount = 0;
    int successCount = 0;
    for (TemplateXLS templateXLS : templateXLSS) {
      if (templateXLS.isSuccessfullyImported()) {
        successCount++;
      } else if (StringUtils.isNotEmpty(templateXLS.getErrorMessage())) {
        errorCount++;
        fir.addError(templateXLS.getRowNumber(), templateXLS.getErrorMessage());
      }
    }

    if (errorCount + successCount != templateXLSS.size()) {
      logger.warn(FILE_IMPORT_SUCCESS_AND_ERROR_COUNTS_DON_T_MATCH_INPUT_NUMBER_OF_ROWS);
    }

    applicationType.setNumberOfErrors(errorCount);
    logger.info("Success count or number imported {}", successCount);
    applicationType.setNumberImported(successCount);
    applicationType.setErrorJson(fir.toJson());
    applicationType.setProcessedDate(LocalDateTime.now());
    applicationType.setFileStatus(FileStatus.COMPLETED);
    logger.info("Job completed for file {} with log id {}", applicationType.getFileName(),
        applicationType.getLogId());
  }
}
