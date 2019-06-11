package com.transformuk.hee.tis.genericupload.service.api;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.genericupload.service.Application;
import com.transformuk.hee.tis.genericupload.service.api.validation.FileValidator;
import com.transformuk.hee.tis.genericupload.service.config.WebSecurityConfig;
import com.transformuk.hee.tis.genericupload.service.exception.ExceptionTranslator;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import com.transformuk.hee.tis.genericupload.service.service.UploadFileService;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UploadFileResourceStatusTest {

  @Mock
  private UploadFileService uploadFileService;

  @InjectMocks
  private UploadFileResource uploadFileResource;

  private MockMvc mockMvc;

  @Autowired
  protected WebApplicationContext webApplicationContext;

  @Autowired
  FileValidator fileValidator;

  @Autowired
  private MappingJackson2HttpMessageConverter jacksonMessageConverter;

  @Autowired
  private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

  @Autowired
  private ExceptionTranslator exceptionTranslator;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    UploadFileResource uploadFileResource = new UploadFileResource(uploadFileService, fileValidator);
    this.mockMvc = MockMvcBuilders.standaloneSetup(uploadFileResource)
        .setCustomArgumentResolvers(pageableArgumentResolver)
        .setControllerAdvice(exceptionTranslator)
        .setMessageConverters(jacksonMessageConverter).build();
  }

  @Test
  public void shouldSanitizeWhenGetBulkUploadStatus() throws Exception{
    ApplicationType at = new ApplicationType();
    at.setFirstName("James\\\"");
    at.setFileName("TIS Placement Import.xls");
    Page<ApplicationType> page = new PageImpl<ApplicationType>(Lists.newArrayList(at));

    ArgumentCaptor<LocalDateTime> argument_date = ArgumentCaptor.forClass(LocalDateTime.class);
    ArgumentCaptor<String> argument_file = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> argument_user = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> argument_searchString = ArgumentCaptor.forClass(String.class);

    when(uploadFileService.searchUploads(argument_date.capture(), argument_file.capture(), argument_user.capture(), any())).thenReturn(page);
    when(uploadFileService.searchUploads(argument_searchString.capture(), any())).thenReturn(page);

    mockMvc.perform(get(new URI("/api/status?searchQuery=James%5C%22"))).andExpect(status().isOk());
    String converted_searchString = argument_searchString.getValue();
    Assert.assertThat("should sanitize search string", converted_searchString, CoreMatchers.is("James\\\\\\\""));

    mockMvc.perform(get(new URI("/api/status?file=TIS%20Placement%20Import.xls&user=James%5C%22&uploadedDate=2019-06-03%0D%0A"))).andExpect(status().isOk());
    String converted_date = argument_date.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String converted_file = argument_file.getValue();
    String converted_user = argument_user.getValue();
    Assert.assertThat("should sanitize date", converted_date, CoreMatchers.is("2019-06-03"));
    Assert.assertThat("should sanitize file", converted_file, CoreMatchers.is("TIS Placement Import.xls"));
    Assert.assertThat("should sanitize user", converted_user, CoreMatchers.is("James\\\\\\\""));
  }
}
