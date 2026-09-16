package com.transformuk.hee.tis.genericupload.service.service.impl;

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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

@ExtendWith(MockitoExtension.class)
class UploadFileServiceImplTest {

  private static final Long JOB_ID = 111L;
  private static final String JOB_STATUS_RESET_REQUESTER = "AAA";
  private static final String FILE_NAME = "test.xlsx";
  private static final Long LOG_ID = 1111111L;
  private static final Long LOG_ID2 = 2222222L;
  private static final String FILE_NAME2 = "test2.xlsx";

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
        azureProperties);
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

  private static Stream<Arguments> resetUploadStatusSuccessCases() {
    return Stream.of(
        Arguments.of(FileStatus.PENDING, FileStatus.UNEXPECTED_ERROR),
        Arguments.of(FileStatus.IN_PROGRESS, FileStatus.PENDING),
        Arguments.of(FileStatus.IN_PROGRESS, FileStatus.UNEXPECTED_ERROR)
    );
  }

  @ParameterizedTest
  @MethodSource("resetUploadStatusSuccessCases")
  void shouldResetJobToAllowedTargetStatus(FileStatus currentStatus, FileStatus targetStatus) {
    initApplicationType(currentStatus);
    when(applicationTypeRepository.findById(JOB_ID)).thenReturn(
        Optional.of(applicationType));
    when(applicationTypeRepository.save(applicationType)).thenReturn(applicationType);

    initResetUploadStatusRequestDto(targetStatus);
    ApplicationType updated = uploadFileService.resetUploadStatus(resetUploadStatusRequestDto,
        JOB_STATUS_RESET_REQUESTER);

    assertThat(updated.getFileStatus()).isEqualTo(targetStatus);
    verify(applicationTypeRepository).save(applicationType);
  }

  @Test
  void shouldRejectResetWhenCurrentStatusIsNotResettable() {
    initApplicationType(FileStatus.COMPLETED);
    when(applicationTypeRepository.findById(JOB_ID)).thenReturn(
        Optional.of(applicationType));

    initResetUploadStatusRequestDto(FileStatus.PENDING);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> uploadFileService.resetUploadStatus(resetUploadStatusRequestDto,
            JOB_STATUS_RESET_REQUESTER));

    assertThat(exception.getMessage()).contains("Allowed current statuses are");
    verify(applicationTypeRepository, never()).save(any());
  }

  @Test
  void shouldRejectResetWhenTargetStatusIsNotAllowed() {
    initApplicationType(FileStatus.PENDING);
    when(applicationTypeRepository.findById(JOB_ID)).thenReturn(
        Optional.of(applicationType));

    initResetUploadStatusRequestDto(FileStatus.COMPLETED);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> uploadFileService.resetUploadStatus(resetUploadStatusRequestDto,
            JOB_STATUS_RESET_REQUESTER));

    assertThat(exception.getMessage()).contains("Invalid target status");
    verify(applicationTypeRepository, never()).save(any());
  }

  @Test
  void shouldRejectResetWhenTargetStatusMatchesCurrentStatus() {
    initApplicationType(FileStatus.PENDING);
    when(applicationTypeRepository.findById(JOB_ID)).thenReturn(
        Optional.of(applicationType));

    initResetUploadStatusRequestDto(FileStatus.PENDING);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> uploadFileService.resetUploadStatus(resetUploadStatusRequestDto,
            JOB_STATUS_RESET_REQUESTER));

    assertThat(exception.getMessage()).contains("Please provide a different target status");
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
    applicationType.setLogId(LOG_ID2); // Set a different log ID
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
    applicationType.setFileName(FILE_NAME2); // Set a different file name
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
