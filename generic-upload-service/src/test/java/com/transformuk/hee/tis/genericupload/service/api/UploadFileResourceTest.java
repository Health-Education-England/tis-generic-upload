package com.transformuk.hee.tis.genericupload.service.api;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.Application;
import com.transformuk.hee.tis.genericupload.service.api.validation.FileValidator;
import com.transformuk.hee.tis.genericupload.service.config.ApplicationConfiguration;
import com.transformuk.hee.tis.genericupload.service.exception.ExceptionTranslator;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import com.transformuk.hee.tis.genericupload.service.service.EtlService;
import com.transformuk.hee.tis.genericupload.service.service.EventBusService;
import com.transformuk.hee.tis.genericupload.service.service.FileProcessService;
import com.transformuk.hee.tis.genericupload.service.service.UploadFileService;
import com.transformuk.hee.tis.genericupload.service.storage.FileRecordStorage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UploadFileResourceTest {

  private static final String FILE_NAME = "Intrepid Recruitment Import Template v9.xls";

  @Autowired
  private ApplicationTypeRepository applicationTypeRepository;

  @Autowired
  private FileProcessService fileProcessService;

  @Autowired
  private FileValidator fileValidator;

  @Autowired
  private ExceptionTranslator exceptionTranslator;

  @Autowired
  private FileStorageRepository fileStorageRepository;

  @Autowired
  private MappingJackson2HttpMessageConverter jacksonMessageConverter;

  @Autowired
  private UploadFileService uploadFileService;

  @Autowired
  private EtlService etlService;

  @Autowired
  private Map<String, ModelMapper> modelMappers;

  @Autowired
  private ApplicationConfiguration applicationConfiguration;

  @Autowired
  private FileRecordStorage fileRecordStorage;


  private MockMvc restContactDetailsMockMvc;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    UploadFileResource uploadFileResource = new UploadFileResource(uploadFileService,fileProcessService,fileValidator);
    this.restContactDetailsMockMvc = MockMvcBuilders.standaloneSetup(uploadFileResource)
        .setControllerAdvice(exceptionTranslator)
        .setMessageConverters(jacksonMessageConverter).build();
  }

  @Test
  @Transactional
  public void uploadFile() throws Exception {
    String filePath = new ClassPathResource(FILE_NAME).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    MockMultipartFile multipartFile =
        new MockMultipartFile("file", FILE_NAME, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                Files.readAllBytes(Paths.get(filePath)));
    List<MultipartFile> multipartFiles = Lists.newArrayList(multipartFile);
    MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
    request.addFile(multipartFile);

    // when & then
    restContactDetailsMockMvc.perform(fileUpload("/api/generic-upload/file")
            .file(multipartFile))
            .andExpect(status().isAccepted());

  }

  @Test
  @Transactional
  public void uploadFileInvalidFormat() throws Exception {
    MockMultipartFile multipartFile =
            new MockMultipartFile("file", "test.txt", "text/plain",
                    "Spring Framework".getBytes());
    List<MultipartFile> multipartFiles = Lists.newArrayList(multipartFile);
    MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
    request.addFile(multipartFile);

    // when & then
    restContactDetailsMockMvc.perform(fileUpload("/api/generic-upload/file")
            .file(multipartFile))
            .andExpect(status().isUnsupportedMediaType());

  }

  @Test
  @Transactional
  public void uploadFileInvalidFormatWhenContentTypeIsSame() throws Exception {
    MockMultipartFile multipartFile =
            new MockMultipartFile("file", "test.txt", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "Spring Framework".getBytes());
    List<MultipartFile> multipartFiles = Lists.newArrayList(multipartFile);
    MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
    request.addFile(multipartFile);

    // when & then
    restContactDetailsMockMvc.perform(fileUpload("/api/generic-upload/file")
            .file(multipartFile))
            .andExpect(status().isUnsupportedMediaType());

  }

  @Test
  @Ignore
  @Transactional
  public void processFile() throws Exception {
    // insert records
    ApplicationType applicationType = new ApplicationType();
    applicationType.setFileName(FILE_NAME);
    applicationType.setFileType(FileType.RECRUITMENT);
    applicationType.setFileStatus(FileStatus.PENDING);
    applicationType.setStartDate(LocalDateTime.now());

    applicationTypeRepository.saveAndFlush(applicationType);
    // when & then
    restContactDetailsMockMvc.perform(fileUpload("/api/generic-upload/process"))
            .andExpect(status().isOk());

  }
}
