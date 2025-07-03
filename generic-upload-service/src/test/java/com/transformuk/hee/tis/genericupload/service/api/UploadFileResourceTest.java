package com.transformuk.hee.tis.genericupload.service.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.transformuk.hee.tis.genericupload.service.Application;
import com.transformuk.hee.tis.genericupload.service.TestUtils;
import com.transformuk.hee.tis.genericupload.service.api.validation.FileValidator;
import com.transformuk.hee.tis.genericupload.service.exception.ExceptionTranslator;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.service.UploadFileService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UploadFileResourceTest {

  public static final String DBC = "DBCs";
  public static final String USER_ID = "James H";

  @Autowired
  private ApplicationTypeRepository applicationTypeRepository;

  @Autowired
  private FileValidator fileValidator;

  @Autowired
  private ExceptionTranslator exceptionTranslator;

  @Autowired
  private MappingJackson2HttpMessageConverter jacksonMessageConverter;

  @Autowired
  private UploadFileService uploadFileService;

  @Autowired
  private RestTemplate restTemplate;

  private MockMvc restContactDetailsMockMvc;
  @Value("${tcs.service.url}")
  private String serviceUrl;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    TestUtils.mockUserprofile(USER_ID, DBC);

    UploadFileResource uploadFileResource = new UploadFileResource(uploadFileService,
        fileValidator);
    this.restContactDetailsMockMvc = MockMvcBuilders.standaloneSetup(uploadFileResource)
        .setControllerAdvice(exceptionTranslator).setMessageConverters(jacksonMessageConverter)
        .build();
  }
  @Ignore("Temporarily disabled")
  @Test
  @Transactional
  public void uploadFile() throws Exception {
    String FILE_NAME = "TIS People Import Template - empty row.xlsx";
    MockMultipartFile multipartFile = new MockMultipartFile("file", FILE_NAME,
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        new ClassPathResource(FILE_NAME).getInputStream());

    MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
    request.addFile(multipartFile);

    // when & then
    restContactDetailsMockMvc.perform(fileUpload("/api/file").file(multipartFile))
        .andExpect(status().isOk());
  }

  @Test
  @Transactional
  public void uploadFileInvalidFormat() throws Exception {
    MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain",
        "Spring Framework".getBytes());
    MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
    request.addFile(multipartFile);

    // when & then
    restContactDetailsMockMvc.perform(fileUpload("/api/file").file(multipartFile))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  @Transactional
  public void uploadFileInvalidFormatWhenContentTypeIsSame() throws Exception {
    MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "Spring Framework".getBytes());
    MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
    request.addFile(multipartFile);

    // when & then
    restContactDetailsMockMvc.perform(fileUpload("/api/file").file(multipartFile))
        .andExpect(status().isUnsupportedMediaType());
  }
}
