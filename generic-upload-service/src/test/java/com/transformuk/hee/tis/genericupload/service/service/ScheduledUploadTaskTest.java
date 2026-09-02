/*
 * The MIT License (MIT)
 *
 * Copyright 2026 Crown Copyright (NHS England)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.transformuk.hee.tis.genericupload.service.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentDeleteXLS;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentXLS;
import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXls;
import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementDeleteXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementXls;
import com.transformuk.hee.tis.genericupload.api.dto.PostCreateXls;
import com.transformuk.hee.tis.genericupload.api.dto.PostFundingCreateRow;
import com.transformuk.hee.tis.genericupload.api.dto.PostFundingUpdateRow;
import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.ProgrammeMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.config.AzureProperties;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
@RunWith(MockitoJUnitRunner.class)
public class ScheduledUploadTaskTest {

  ApplicationType applicationType;
  @Mock
  private FileStorageRepository fileStorageRepository;
  @Mock
  private ApplicationTypeRepository applicationTypeRepository;
  @Mock
  private AzureProperties azureProperties;
  @Mock
  private PersonTransformerService personTransformerService;
  @Mock
  private PersonUpdateTransformerService personUpdateTransformerService;
  @Mock
  private PlacementTransformerService placementTransformerService;
  @Mock
  private PlacementUpdateTransformerService placementUpdateTransformerService;
  @Mock
  private PlacementDeleteService placementDeleteService;
  @Mock
  private AssessmentTransformerService assessmentTransformerService;
  @Mock
  private AssessmentUpdateTransformerService assessmentUpdateTransformerService;
  @Mock
  private AssessmentDeleteService assessmentDeleteService;
  @Mock
  private PostCreateTransformerService postCreateTransformerService;
  @Mock
  private PostUpdateTransformerService postUpdateTransformerService;
  @Mock
  private PostFundingCreateTransformerService postFundingCreateTransformerService;
  @Mock
  private PostFundingUpdateTransformerService postFundingUpdateTransformerService;
  @Mock
  private ProgrammeMembershipUpdateTransformerService pmUpdateTransformerService;
  @Mock
  private CurriculumMembershipCreateTransformerService cmCreateTransformerService;
  @Mock
  private CurriculumMembershipUpdateTransformerService cmUpdateTransformerService;
  private ScheduledUploadTask scheduledUploadTask;

  @Before
  public void setUp() {
    applicationType = new ApplicationType();
    applicationType.setUsername("test-user");

    scheduledUploadTask = new ScheduledUploadTask(fileStorageRepository, applicationTypeRepository,
        azureProperties);

    ReflectionTestUtils.setField(scheduledUploadTask, "personTransformerService",
        personTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "personUpdateTransformerService",
        personUpdateTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "placementTransformerService",
        placementTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "placementUpdateTransformerService",
        placementUpdateTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "placementDeleteService",
        placementDeleteService);
    ReflectionTestUtils.setField(scheduledUploadTask, "assessmentTransformerService",
        assessmentTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "assessmentUpdateTransformerService",
        assessmentUpdateTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "assessmentDeleteService",
        assessmentDeleteService);
    ReflectionTestUtils.setField(scheduledUploadTask, "postCreateTransformerService",
        postCreateTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "postUpdateTransformerService",
        postUpdateTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "postFundingCreateTransformerService",
        postFundingCreateTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "postFundingUpdateTransformerService",
        postFundingUpdateTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "pmUpdateTransformerService",
        pmUpdateTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "cmCreateTransformerService",
        cmCreateTransformerService);
    ReflectionTestUtils.setField(scheduledUploadTask, "cmUpdateTransformerService",
        cmUpdateTransformerService);

    when(azureProperties.getContainerName()).thenReturn("test-container");

    when(applicationTypeRepository.findByFileStatusOrderByUploadedDate(
        FileStatus.IN_PROGRESS)).thenReturn(Collections.emptyList());
  }

  @Test
  public void shouldProcessPeopleUpload() throws Exception {
    setApplicationTypeRepository(12345L, "people create.xlsx", FileType.PEOPLE, FileStatus.PENDING);

    byte[] excelFile = createExcelFile("People", new String[]{"Surname *", "Forenames *", "Role *"},
        new String[]{"Smith", "John", "Consultant"});

    when(fileStorageRepository.download(12345L, "test-container", "people create.xlsx")).thenReturn(
        excelFile);

    doAnswer(invocation -> {
      List<PersonXLS> people = invocation.getArgument(0);
      people.forEach(person -> person.setSuccessfullyImported(true));
      return null;
    }).when(personTransformerService).processPeopleUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<PersonXLS>> peopleCaptor = ArgumentCaptor.forClass(List.class);

    verify(personTransformerService).processPeopleUpload(peopleCaptor.capture());

    List<PersonXLS> capturedPeople = peopleCaptor.getValue();

    assertEquals(1, capturedPeople.size());

    PersonXLS person = capturedPeople.get(0);

    assertEquals("Smith", person.getSurname());
    assertEquals("John", person.getForenames());
    assertEquals("Consultant", person.getRole());

    assertCompletedSuccessfully();

    verify(fileStorageRepository).download(12345L, "test-container", "people create.xlsx");
  }

  @Test
  public void shouldProcessPeopleUpdateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "people update.xlsx", FileType.PEOPLE_UPDATE,
        FileStatus.PENDING);

    byte[] excelFile = createExcelFile("People Update",
        new String[]{"TIS_Person_ID*", "GMC Number", "Surname"},
        new String[]{"2345", "1234567", "Smith"});

    when(fileStorageRepository.download(12345L, "test-container", "people update.xlsx")).thenReturn(
        excelFile);

    doAnswer(invocation -> {
      List<PersonUpdateXls> people = invocation.getArgument(0);
      people.forEach(person -> person.setSuccessfullyImported(true));
      return null;
    }).when(personUpdateTransformerService).processUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<PersonUpdateXls>> peopleCaptor = ArgumentCaptor.forClass(List.class);

    verify(personUpdateTransformerService).processUpload(peopleCaptor.capture());

    List<PersonUpdateXls> capturedPeople = peopleCaptor.getValue();

    assertEquals(1, capturedPeople.size());

    PersonUpdateXls person = capturedPeople.get(0);

    assertEquals("2345", person.getTisPersonId());
    assertEquals("1234567", person.getGmcNumber());
    assertEquals("Smith", person.getSurname());

    assertCompletedSuccessfully();

    verify(fileStorageRepository).download(12345L, "test-container", "people update.xlsx");
  }

  @Test
  public void shouldProcessPlacementsCreateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "placements create.xlsx", FileType.PLACEMENTS,
        FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Placements", new String[]{"National Post Number*"},
        new String[]{"ABC/123/001"});

    when(fileStorageRepository.download(12345L, "test-container",
        "placements create.xlsx")).thenReturn(excelFile);

    doAnswer(invocation -> {
      List<PlacementXls> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(placementTransformerService).processPlacementsUpload(anyList(), anyString());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<PlacementXls>> captor = ArgumentCaptor.forClass(List.class);

    verify(placementTransformerService).processPlacementsUpload(captor.capture(), eq("test-user"));

    List<PlacementXls> capturedRows = captor.getValue();

    assertEquals(1, capturedRows.size());
    assertEquals("ABC/123/001", capturedRows.get(0).getNationalPostNumber());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldProcessPlacementsUpdateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "placements update.xlsx",
        FileType.PLACEMENTS_UPDATE, FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Placements Update",
        new String[]{"TIS_Placement_ID*"},
        new String[]{"12345"});

    when(fileStorageRepository.download(12345L, "test-container",
        "placements update.xlsx")).thenReturn(excelFile);

    doAnswer(invocation -> {
      List<PlacementUpdateXLS> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(placementUpdateTransformerService)
        .processPlacementsUpdateUpload(anyList(), anyString());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<PlacementUpdateXLS>> captor = ArgumentCaptor.forClass(List.class);

    verify(placementUpdateTransformerService).processPlacementsUpdateUpload(captor.capture(),
        eq("test-user"));

    assertEquals(1, captor.getValue().size());

    assertEquals("12345", captor.getValue().get(0).getPlacementId());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldProcessPlacementsDeleteUpload() throws Exception {
    setApplicationTypeRepository(12345L, "placements delete.xlsx", FileType.PLACEMENTS_DELETE,
        FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Placements Delete",
        new String[]{"Placement Id*"}, new String[]{"98765"});

    when(fileStorageRepository.download(12345L, "test-container",
        "placements delete.xlsx")).thenReturn(excelFile);

    doAnswer(invocation -> {
      List<PlacementDeleteXLS> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(placementDeleteService).processPlacementsDeleteUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<PlacementDeleteXLS>> captor = ArgumentCaptor.forClass(List.class);

    verify(placementDeleteService).processPlacementsDeleteUpload(captor.capture());

    assertEquals(1, captor.getValue().size());

    assertEquals("98765", captor.getValue().get(0).getPlacementId());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldProcessAssessmentsCreateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "assessments.xlsx", FileType.ASSESSMENTS,
        FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Assessments",
        new String[]{"Trainee Surname*"},
        new String[]{"Smith"});

    when(fileStorageRepository.download(12345L, "test-container", "assessments.xlsx")).thenReturn(
        excelFile);

    doAnswer(invocation -> {
      List<AssessmentXLS> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(assessmentTransformerService).processAssessmentsUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<AssessmentXLS>> captor = ArgumentCaptor.forClass(List.class);

    verify(assessmentTransformerService).processAssessmentsUpload(captor.capture());

    assertEquals(1, captor.getValue().size());

    assertEquals("Smith", captor.getValue().get(0).getSurname());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldProcessPostCreateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "post create.xlsx", FileType.POSTS_CREATE,
        FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Post Create",
        new String[]{"National Post Number*"},
        new String[]{"POST/001"});

    when(fileStorageRepository.download(12345L, "test-container", "post create.xlsx")).thenReturn(
        excelFile);

    doAnswer(invocation -> {
      List<PostCreateXls> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(postCreateTransformerService).processUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<PostCreateXls>> captor = ArgumentCaptor.forClass(List.class);

    verify(postCreateTransformerService).processUpload(captor.capture());

    assertEquals(1, captor.getValue().size());

    assertEquals("POST/001", captor.getValue().get(0).getNationalPostNumber());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldProcessPostUpdateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "post update.xlsx", FileType.POSTS_UPDATE,
        FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Post Update",
        new String[]{"TIS_Post_ID*"},
        new String[]{"45678"});

    when(fileStorageRepository.download(12345L, "test-container", "post update.xlsx")).thenReturn(
        excelFile);

    doAnswer(invocation -> {
      List<PostUpdateXLS> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(postUpdateTransformerService).processPostUpdateUpload(anyList(), anyString());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<PostUpdateXLS>> captor = ArgumentCaptor.forClass(List.class);

    verify(postUpdateTransformerService).processPostUpdateUpload(captor.capture(), eq("test-user"));

    assertEquals(1, captor.getValue().size());

    assertEquals("45678", captor.getValue().get(0).getPostTISId());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldProcessPostFundingCreateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "post funding create.xlsx", FileType.POSTS_FUNDING_CREATE,
        FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Post Funding Create",
        new String[]{"TIS_Post_ID*"},
        new String[]{"10001"});

    when(fileStorageRepository.download(12345L, "test-container",
        "post funding create.xlsx")).thenReturn(excelFile);

    doAnswer(invocation -> {
      List<PostFundingCreateRow> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(postFundingCreateTransformerService).processRows(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<PostFundingCreateRow>> captor = ArgumentCaptor.forClass(List.class);

    verify(postFundingCreateTransformerService).processRows(captor.capture());

    assertEquals(1, captor.getValue().size());
    assertEquals("10001", captor.getValue().get(0).getPostTisId());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldProcessPostFundingUpdateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "post funding update.xlsx", FileType.POSTS_FUNDING_UPDATE,
        FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Post Funding Update",
        new String[]{"TIS_PostFunding_ID*"},
        new String[]{"20001"});

    when(fileStorageRepository.download(12345L, "test-container",
        "post funding update.xlsx")).thenReturn(excelFile);

    doAnswer(invocation -> {
      List<PostFundingUpdateRow> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(postFundingUpdateTransformerService)
        .processRows(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<PostFundingUpdateRow>> captor = ArgumentCaptor.forClass(List.class);

    verify(postFundingUpdateTransformerService).processRows(captor.capture());

    assertEquals(1, captor.getValue().size());

    assertEquals("20001", captor.getValue().get(0).getPostFundingTisId());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldProcessAssessmentUpdateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "assessment update.xlsx", FileType.ASSESSMENTS_UPDATE,
        FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Assessment Update", new String[]{"TIS_Assessment_ID*"},
        new String[]{"30001"});

    when(fileStorageRepository.download(12345L, "test-container",
        "assessment update.xlsx")).thenReturn(excelFile);

    doAnswer(invocation -> {
      List<AssessmentUpdateXLS> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(assessmentUpdateTransformerService).processAssessmentsUpdateUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<AssessmentUpdateXLS>> captor = ArgumentCaptor.forClass(List.class);

    verify(assessmentUpdateTransformerService).processAssessmentsUpdateUpload(captor.capture());

    assertEquals(1, captor.getValue().size());

    assertEquals("30001", captor.getValue().get(0).getAssessmentId());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldProcessAssessmentsDeleteUpload() throws Exception {
    setApplicationTypeRepository(12345L, "assessments delete.xlsx", FileType.ASSESSMENTS_DELETE,
        FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Assessments Delete", new String[]{"TIS_Assessment_ID*"},
        new String[]{"70001"});

    when(fileStorageRepository.download(12345L, "test-container",
        "assessments delete.xlsx")).thenReturn(excelFile);

    doAnswer(invocation -> {
      List<AssessmentDeleteXLS> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(assessmentDeleteService).processAssessmentsDeleteUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<AssessmentDeleteXLS>> captor = ArgumentCaptor.forClass(List.class);

    verify(assessmentDeleteService).processAssessmentsDeleteUpload(captor.capture());

    List<AssessmentDeleteXLS> capturedRows = captor.getValue();

    assertEquals(1, capturedRows.size());

    assertEquals("70001", capturedRows.get(0).getAssessmentId());

    assertCompletedSuccessfully();

    verify(fileStorageRepository).download(12345L, "test-container", "assessments delete.xlsx");
  }

  @Test
  public void shouldProcessProgrammeMembershipUpdateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "programme membership update.xlsx",
        FileType.PROGRAMME_MEMBERSHIP_UPDATE, FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Programme Membership Update",
        new String[]{"TIS_ProgrammeMembership_ID*"},
        new String[]{"40001"});

    when(fileStorageRepository.download(12345L, "test-container",
        "programme membership update.xlsx")).thenReturn(excelFile);

    doAnswer(invocation -> {
      List<ProgrammeMembershipUpdateXls> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(pmUpdateTransformerService).processProgrammeMembershipsUpdateUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<ProgrammeMembershipUpdateXls>> captor = ArgumentCaptor.forClass(List.class);

    verify(pmUpdateTransformerService).processProgrammeMembershipsUpdateUpload(captor.capture());

    assertEquals(1, captor.getValue().size());

    assertEquals("40001", captor.getValue().get(0).getProgrammeMembershipId());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldProcessCurriculumMembershipCreateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "curriculum-membership-create.xlsx",
        FileType.CURRICULUM_MEMBERSHIP_CREATE, FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Curriculum Membership Create",
        new String[]{"TIS_ProgrammeMembership_ID*"}, new String[]{"50001"});

    when(fileStorageRepository.download(12345L, "test-container",
        "curriculum-membership-create.xlsx")).thenReturn(excelFile);

    doAnswer(invocation -> {
      List<CurriculumMembershipCreateXls> rows = invocation.getArgument(0);
      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(cmCreateTransformerService).processCurriculumMembershipCreateUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<CurriculumMembershipCreateXls>> captor = ArgumentCaptor.forClass(
        List.class);

    verify(cmCreateTransformerService).processCurriculumMembershipCreateUpload(captor.capture());

    assertEquals(1, captor.getValue().size());

    assertEquals("50001", captor.getValue().get(0).getProgrammeMembershipUuid());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldProcessCurriculumMembershipUpdateUpload() throws Exception {
    setApplicationTypeRepository(12345L, "curriculum-membership-update.xlsx",
        FileType.CURRICULUM_MEMBERSHIP_UPDATE, FileStatus.PENDING);

    byte[] excelFile = createExcelFile("Curriculum Membership Update",
        new String[]{"TIS_CurriculumMembership_ID*"}, new String[]{"60001"});

    when(fileStorageRepository.download(12345L, "test-container",
        "curriculum-membership-update.xlsx")).thenReturn(excelFile);

    doAnswer(invocation -> {
      List<CurriculumMembershipUpdateXls> rows = invocation.getArgument(0);

      rows.forEach(row -> row.setSuccessfullyImported(true));
      return null;
    }).when(cmUpdateTransformerService).processCurriculumMembershipUpdateUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    ArgumentCaptor<List<CurriculumMembershipUpdateXls>> captor = ArgumentCaptor.forClass(
        List.class);

    verify(cmUpdateTransformerService).processCurriculumMembershipUpdateUpload(captor.capture());

    assertEquals(1, captor.getValue().size());

    assertEquals("60001", captor.getValue().get(0).getTisCurriculumMembershipId());

    assertCompletedSuccessfully();
  }

  @Test
  public void shouldSetInvalidFileFormatWhenInvalidFormatExceptionOccurs() throws Exception {
    setApplicationTypeRepository(12345L, "people.xlsx", FileType.PEOPLE, FileStatus.PENDING);

    byte[] excelFile = createExcelFile("People",
        new String[]{"Surname *", "Forenames *", "Role *"},
        new String[]{"Smith", "John", "Consultant"});

    when(fileStorageRepository.download(12345L, "test-container", "people.xlsx")).thenReturn(
        excelFile);

    InvalidFormatException exception = mock(InvalidFormatException.class);

    when(exception.getMessage()).thenReturn("Invalid Excel format");

    doAnswer(invocation -> {
      throw exception;
    }).when(personTransformerService).processPeopleUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    assertEquals(FileStatus.INVALID_FILE_FORMAT, applicationType.getFileStatus());

    verify(applicationTypeRepository, atLeastOnce()).save(applicationType);
  }

  @Test
  public void shouldSetPendingWhenHttpClientErrorExceptionOccurs() throws Exception {
    setApplicationTypeRepository(12345L, "people.xlsx", FileType.PEOPLE, FileStatus.PENDING);

    byte[] excelFile = createExcelFile("People",
        new String[]{"Surname *", "Forenames *", "Role *"},
        new String[]{"Smith", "John", "Consultant"});

    when(fileStorageRepository.download(12345L, "test-container", "people.xlsx")).thenReturn(
        excelFile);

    HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.BAD_REQUEST,
        "Bad Request", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);

    doThrow(exception).when(personTransformerService).processPeopleUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    assertEquals(FileStatus.PENDING, applicationType.getFileStatus());

    verify(applicationTypeRepository, atLeastOnce()).save(applicationType);
  }

  @Test
  public void shouldSetUnexpectedErrorWhenUnexpectedExceptionOccurs() throws Exception {
    setApplicationTypeRepository(12345L, "people.xlsx", FileType.PEOPLE, FileStatus.PENDING);

    byte[] excelFile = createExcelFile("People",
        new String[]{"Surname *", "Forenames *", "Role *"},
        new String[]{"Smith", "John", "Consultant"});

    when(fileStorageRepository.download(12345L, "test-container", "people.xlsx"))
        .thenReturn(excelFile);

    doThrow(new RuntimeException("Unexpected processing error")).when(personTransformerService)
        .processPeopleUpload(anyList());

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    assertEquals(FileStatus.UNEXPECTED_ERROR, applicationType.getFileStatus());

    verify(applicationTypeRepository, atLeastOnce()).save(applicationType);
  }

  @Test
  public void shouldResetJobToPendingWhenInProgressForTooLong() {
    ApplicationType stuckJob = new ApplicationType();
    stuckJob.setLogId(98765L);
    stuckJob.setFileName("stuck upload file.xlsx");
    stuckJob.setFileStatus(FileStatus.IN_PROGRESS);

    //3-hour threshold
    stuckJob.setJobStartTime(LocalDateTime.now().minusHours(4));

    when(applicationTypeRepository.findFirstByFileStatusOrderByUploadedDate(
        FileStatus.PENDING)).thenReturn(null);

    when(applicationTypeRepository.findByFileStatusOrderByUploadedDate(
        FileStatus.IN_PROGRESS)).thenReturn(Collections.singletonList(stuckJob));

    scheduledUploadTask.scheduleTaskWithFixedDelay();

    assertEquals(FileStatus.PENDING, stuckJob.getFileStatus());

    verify(applicationTypeRepository).save(stuckJob);
  }

  private byte[] createExcelFile(String sheetName, String[] headers, String[] values)
      throws Exception {
    if (headers.length != values.length) {
      throw new IllegalArgumentException(
          "Headers and values must contain the same number of elements.");
    }

    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet(sheetName);
      Row headerRow = sheet.createRow(0);

      for (int i = 0; i < headers.length; i++) {
        headerRow.createCell(i).setCellValue(headers[i]);
      }

      Row dataRow = sheet.createRow(1);

      for (int i = 0; i < values.length; i++) {
        dataRow.createCell(i).setCellValue(values[i]);
      }

      workbook.write(outputStream);

      return outputStream.toByteArray();
    }
  }

  private void setApplicationTypeRepository(Long logId, String fileName, FileType fileType,
      FileStatus fileStatus) {
    applicationType.setLogId(logId);
    applicationType.setFileName(fileName);
    applicationType.setFileType(fileType);
    applicationType.setFileStatus(fileStatus);

    when(applicationTypeRepository.findFirstByFileStatusOrderByUploadedDate(
        FileStatus.PENDING)).thenReturn(applicationType);
  }

  private void assertCompletedSuccessfully() {
    assertEquals(FileStatus.COMPLETED, applicationType.getFileStatus());
    assertEquals(Integer.valueOf(1), applicationType.getNumberImported());
    assertEquals(Integer.valueOf(0), applicationType.getNumberOfErrors());
    assertNotNull(applicationType.getProcessedDate());
  }
}
