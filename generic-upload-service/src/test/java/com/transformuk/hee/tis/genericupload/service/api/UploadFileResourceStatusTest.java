package com.transformuk.hee.tis.genericupload.service.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.genericupload.service.Application;
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
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class UploadFileResourceStatusTest {

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

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    UploadFileResource uploadFileResource = new UploadFileResource(uploadFileService,
        fileValidator);
    this.mockMvc = MockMvcBuilders.standaloneSetup(uploadFileResource)
        .setCustomArgumentResolvers(pageableArgumentResolver)
        .setControllerAdvice(exceptionTranslator)
        .setMessageConverters(jacksonMessageConverter).build();
  }

  @Test
  public void shouldSanitizeWhenGetBulkUploadStatus() throws Exception {
    ApplicationType at = new ApplicationType();
    at.setFirstName("James\\\"");
    at.setFileName("TIS Placement Import.xls");
    Page<ApplicationType> page = new PageImpl<ApplicationType>(Lists.newArrayList(at));

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
}
