package com.transformuk.hee.tis.genericupload.service.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.transformuk.hee.tis.genericupload.api.dto.ResetUploadStatusRequestDto;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.Application;
import com.transformuk.hee.tis.genericupload.service.TestUtils;
import com.transformuk.hee.tis.genericupload.service.api.validation.FileValidator;
import com.transformuk.hee.tis.genericupload.service.exception.ExceptionTranslator;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import com.transformuk.hee.tis.genericupload.service.service.UploadFileService;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class UploadFileResourceStatusTest {

  private static final Long JOB_ID = 111L;
  private static final String REQUESTER = "test user";
  private static final Long LOG_ID = 1111111L;
  private static final String FILE_NAME = "test.xlsx";

  @Autowired
  FileValidator fileValidator;
  @MockBean
  private UploadFileService uploadFileService;
  @InjectMocks
  private UploadFileResource uploadFileResource;
  private MockMvc mockMvc;
  @Autowired
  private MappingJackson2HttpMessageConverter jacksonMessageConverter;

  @Autowired
  private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

  @Autowired
  private ExceptionTranslator exceptionTranslator;

  @Autowired
  ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    UploadFileResource uploadFileResource = new UploadFileResource(uploadFileService,
        fileValidator);
    this.mockMvc = MockMvcBuilders.standaloneSetup(uploadFileResource)
        .setCustomArgumentResolvers(pageableArgumentResolver)
        .setControllerAdvice(exceptionTranslator)
        .setMessageConverters(jacksonMessageConverter).build();
  }

  @Test
  void shouldSanitizeWhenGetBulkUploadStatus() throws Exception {
    ApplicationType at = new ApplicationType();
    at.setFirstName("James\\\"");
    at.setFileName("TIS Placement Import.xls");
    Page<ApplicationType> page = new PageImpl<>(Lists.newArrayList(at));

    ArgumentCaptor<LocalDateTime> argument_date = ArgumentCaptor.forClass(LocalDateTime.class);
    ArgumentCaptor<String> argument_file = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> argument_user = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> argument_searchString = ArgumentCaptor.forClass(String.class);

    when(uploadFileService
        .searchUploads(argument_date.capture(), argument_file.capture(), argument_user.capture(),
            any())).thenReturn(page);
    when(uploadFileService.searchUploads(argument_searchString.capture(), any())).thenReturn(page);

    mockMvc.perform(get(new URI("/api/status?searchQuery=James%5C%22"))).andExpect(status().isOk());
    String converted_searchString = argument_searchString.getValue();
    assertThat("should sanitize search string", converted_searchString,
        CoreMatchers.is("James\\\\\\\""));

    mockMvc.perform(get(UriComponentsBuilder.fromUriString("/api/status")
            .queryParam("file", "TIS Placement Import.xls")
            .queryParam("user", "James\\\"")
            .queryParam("uploadedDate", "2019-06-03")
            .build().toUri()))
        .andExpect(status().isOk());

    String converted_date = argument_date.getValue()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String converted_file = argument_file.getValue();
    String converted_user = argument_user.getValue();
    assertThat("should sanitize date", converted_date, CoreMatchers.is("2019-06-03"));
    assertThat("should sanitize file", converted_file,
        CoreMatchers.is("TIS Placement Import.xls"));
    assertThat("should sanitize user", converted_user, CoreMatchers.is("James\\\\\\\""));
  }

  @Test
  void shouldResetBulkUploadStatus() throws Exception {

    TestUtils.mockUserprofile(REQUESTER);

    ResetUploadStatusRequestDto requestDto = new ResetUploadStatusRequestDto();
    requestDto.setTargetStatus(FileStatus.IN_PROGRESS);
    requestDto.setJobId(JOB_ID);
    requestDto.setLogId(LOG_ID);
    requestDto.setFileName(FILE_NAME);

    ApplicationType applicationType = new ApplicationType();
    applicationType.setId(JOB_ID);
    applicationType.setFileStatus(FileStatus.UNEXPECTED_ERROR);

    when(uploadFileService.resetUploadStatus(requestDto, REQUESTER))
        .thenReturn(applicationType);

    mockMvc.perform(put("/api/status", JOB_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk());

    verify(uploadFileService).resetUploadStatus(requestDto, REQUESTER);
  }

  @Test
  void shouldReturnBadRequestWhenResetBulkUploadStatusRequestIsInvalid()
      throws Exception {

    TestUtils.mockUserprofile(REQUESTER);

    ResetUploadStatusRequestDto requestDto = new ResetUploadStatusRequestDto();
    requestDto.setTargetStatus(FileStatus.COMPLETED);
    requestDto.setJobId(JOB_ID);
    requestDto.setFileName(FILE_NAME);

    mockMvc.perform(put("/api/status", JOB_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isBadRequest());

    verify(uploadFileService, never())
        .resetUploadStatus(any(), any());
  }
}
