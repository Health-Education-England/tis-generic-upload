package com.transformuk.hee.tis.genericupload.service.service.impl;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.dto.ResetUploadStatusRequestDto;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.config.AzureProperties;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

@ExtendWith(MockitoExtension.class)
class UploadFileServiceImplTest {

  private static final Long JOB_ID = 111L;
  private static final String JOB_STATUS_RESET_REQUESTER = "AAA";
  private static final String FILE_NAME = "test.xlsx";
  private static final Long LOG_ID = 1111111L;
  private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.of(2026, Month.SEPTEMBER, 1,
      12, 0, 0);
  private static final Clock CLOCK = Clock.fixed(CURRENT_DATE_TIME.toInstant(UTC), UTC);

  private ResetUploadStatusRequestDto resetUploadStatusRequestDto;
  private ApplicationType applicationType;

  @Mock
  private FileStorageRepository fileStorageRepository;

  @Mock
  private ApplicationTypeRepository applicationTypeRepository;

  @Mock
  private AzureProperties azureProperties;

  private UploadFileServiceImpl uploadFileService;

  @BeforeEach
  void setUp() {
    uploadFileService = new UploadFileServiceImpl(fileStorageRepository, applicationTypeRepository,
        azureProperties, CLOCK);
  }

  void initResetUploadStatusRequestDto(FileStatus fileStatus) {
    resetUploadStatusRequestDto = new ResetUploadStatusRequestDto();
    resetUploadStatusRequestDto.setTargetStatus(fileStatus);
    resetUploadStatusRequestDto.setJobId(JOB_ID);
    resetUploadStatusRequestDto.setFileName(FILE_NAME);
    resetUploadStatusRequestDto.setLogId(LOG_ID);
  }

  void initApplicationType(FileStatus fileStatus) {
    applicationType = new ApplicationType();
    applicationType.setId(JOB_ID);
    applicationType.setFileStatus(fileStatus);
    applicationType.setLogId(LOG_ID);
    applicationType.setFileName(FILE_NAME);
  }

  @Test
  void removeRowDoesNotActuallyRemoveARow() throws IOException {
    try (InputStream is = new ClassPathResource("TIS People Import Template - empty row.xlsx")
        .getInputStream()) {
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);
      int numberOfRows = sheet.getLastRowNum();
      sheet.removeRow(sheet.getRow(3));
      int rowsAfterDeletion = sheet.getLastRowNum();
      assertThat(numberOfRows).isEqualTo(rowsAfterDeletion);
    }
  }

  @Test
  void haveToShiftRowsUpToRemoveARow() throws IOException {
    try (InputStream is = new ClassPathResource("TIS People Import Template - empty row.xlsx")
        .getInputStream()) {
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);
      int numberOfRows = sheet.getLastRowNum();
      UploadFileServiceImpl.removeRow(sheet, 3);
      int rowsAfterDeletion = sheet.getLastRowNum();
      assertThat(numberOfRows).isGreaterThan(rowsAfterDeletion);
    }
  }

  @Test
  void shouldRemoveCommentsWhenRemoveRowsForXls() throws Exception {
    try (InputStream is = new ClassPathResource("TIS Placement Import Template - removeComment.xls")
        .getInputStream()) {
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);
      Map<CellAddress, Comment> commentMap = (Map<CellAddress, Comment>) sheet.getCellComments();
      int size1 = commentMap.size();
      UploadFileServiceImpl.removeCommentsForRemovedRows(sheet, Collections.singleton(1));
      commentMap = (Map<CellAddress, Comment>) sheet.getCellComments();
      int size2 = commentMap.size();
      assertThat(size1).isGreaterThan(size2);
    }
  }

  @Test
  void shouldRemoveCommentsWhenRemoveRowsForXlsx() throws Exception {
    try (InputStream is = new ClassPathResource("TIS Placement Import Template - removeComment.xlsx")
        .getInputStream()) {
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);
      Map<CellAddress, Comment> commentMap = (Map<CellAddress, Comment>) sheet.getCellComments();
      int size1 = commentMap.size();
      UploadFileServiceImpl.removeCommentsForRemovedRows(sheet, Collections.singleton(1));
      commentMap = (Map<CellAddress, Comment>) sheet.getCellComments();
      int size2 = commentMap.size();
      assertThat(size1).isGreaterThan(size2);
    }
  }

  @ParameterizedTest
  @CsvSource({
      "PENDING,UNEXPECTED_ERROR",
      "IN_PROGRESS,PENDING",
      "IN_PROGRESS,UNEXPECTED_ERROR"
  })
  void shouldResetJobToAllowedTargetStatus(FileStatus currentStatus, FileStatus targetStatus) {
    initApplicationType(currentStatus);
    when(applicationTypeRepository.findById(JOB_ID)).thenReturn(
        Optional.of(applicationType));
    when(applicationTypeRepository.save(applicationType)).thenReturn(applicationType);

    initResetUploadStatusRequestDto(targetStatus);
    ApplicationType updated = uploadFileService.resetUploadStatus(resetUploadStatusRequestDto,
        JOB_STATUS_RESET_REQUESTER);

    assertThat(updated.getFileStatus()).isEqualTo(targetStatus);
    assertThat(updated.getProcessedDate()).isEqualTo(CURRENT_DATE_TIME);
    assertThat(updated.getErrorJson()).contains("Job status reset");
    verify(applicationTypeRepository).save(applicationType);
  }

  @ParameterizedTest
  @CsvSource({
      "COMPLETED,PENDING,Allowed current statuses are",
      "PENDING,COMPLETED,Invalid target status",
      "PENDING,PENDING,Please provide a different target status"
  })
  void shouldRejectResetWhenStatusValidationFails(FileStatus currentStatus, FileStatus targetStatus,
      String expectedMessage) {
    initApplicationType(currentStatus);
    when(applicationTypeRepository.findById(JOB_ID)).thenReturn(
        Optional.of(applicationType));

    initResetUploadStatusRequestDto(targetStatus);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> uploadFileService.resetUploadStatus(resetUploadStatusRequestDto,
            JOB_STATUS_RESET_REQUESTER));

    assertThat(exception.getMessage()).contains(expectedMessage);
    verify(applicationTypeRepository, never()).save(any());
  }

  @Test
  void shouldRejectResetWhenJobDoesNotExist() {
    when(applicationTypeRepository.findById(JOB_ID)).thenReturn(Optional.empty());

    initResetUploadStatusRequestDto(FileStatus.PENDING);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> uploadFileService.resetUploadStatus(resetUploadStatusRequestDto,
            JOB_STATUS_RESET_REQUESTER));

    assertThat(exception.getMessage()).contains("does not exist");
    verify(applicationTypeRepository, never()).save(any());
  }

  @Test
  void shouldRejectResetWhenLogIdDoesNotMatch() {
    initApplicationType(FileStatus.PENDING);
    applicationType.setLogId(2222222L); // Set a different log ID
    when(applicationTypeRepository.findById(JOB_ID)).thenReturn(
        Optional.of(applicationType));

    initResetUploadStatusRequestDto(FileStatus.UNEXPECTED_ERROR);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> uploadFileService.resetUploadStatus(resetUploadStatusRequestDto,
            JOB_STATUS_RESET_REQUESTER));

    assertThat(exception.getMessage()).contains("logId mismatch");
    verify(applicationTypeRepository, never()).save(any());
  }

  @Test
  void shouldRejectResetWhenFileNameDoesNotMatch() {
    initApplicationType(FileStatus.PENDING);
    applicationType.setFileName("test2.xlsx"); // Set a different file name
    when(applicationTypeRepository.findById(JOB_ID)).thenReturn(
        Optional.of(applicationType));

    initResetUploadStatusRequestDto(FileStatus.UNEXPECTED_ERROR);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> uploadFileService.resetUploadStatus(resetUploadStatusRequestDto,
            JOB_STATUS_RESET_REQUESTER));

    assertThat(exception.getMessage()).contains("fileName mismatch");
    verify(applicationTypeRepository, never()).save(any());
  }
}
