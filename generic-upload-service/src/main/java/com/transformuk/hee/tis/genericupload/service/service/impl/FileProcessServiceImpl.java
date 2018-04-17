package com.transformuk.hee.tis.genericupload.service.service.impl;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.client.impl.ServiceKey;
import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.config.ApplicationConfiguration;
import com.transformuk.hee.tis.genericupload.service.config.StepConfiguration;
import com.transformuk.hee.tis.genericupload.service.event.FileRecordEvent;
import com.transformuk.hee.tis.genericupload.service.exception.ErrorVM;
import com.transformuk.hee.tis.genericupload.service.exception.FileRecordStorageException;
import com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper;
import com.transformuk.hee.tis.genericupload.service.parser.PersonHeaderMapper;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import com.transformuk.hee.tis.genericupload.service.service.EtlService;
import com.transformuk.hee.tis.genericupload.service.service.FileProcessService;
import com.transformuk.hee.tis.genericupload.service.storage.FileRecordStorage;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FileProcessServiceImpl implements FileProcessService {

  private static final String DOT = ".";
  private static final String DEFAULT_MAPPER = "defaultMapper";
  private final Logger LOG = LoggerFactory.getLogger(FileProcessServiceImpl.class);

  private final ApplicationTypeRepository applicationTypeRepository;
  private final FileStorageRepository fileStorageRepository;
  private final EtlService etlService;
  private boolean isProcessRunning = false;
  private Map<String, ModelMapper> modelMappers;
  private ApplicationConfiguration applicationConfiguration;
  private FileRecordStorage fileRecordStorage;


  @Autowired
  public FileProcessServiceImpl(ApplicationTypeRepository applicationTypeRepository,
                                FileStorageRepository fileStorageRepository,
                                EtlService etlService,
                                Map<String, ModelMapper> modelMappers,
                                ApplicationConfiguration applicationConfiguration,
                                FileRecordStorage fileRecordStorage) {
    this.applicationTypeRepository = applicationTypeRepository;
    this.fileStorageRepository = fileStorageRepository;
    this.etlService = etlService;
    this.modelMappers = modelMappers;
    this.applicationConfiguration = applicationConfiguration;
    this.fileRecordStorage = fileRecordStorage;
  }

  @Override
  public List<ApplicationType> loadFilesByStatus(FileStatus status) {
    List<ApplicationType> applicationTypes = applicationTypeRepository.findByFileStatusOrderByUploadedDate(status);

    applicationTypes.forEach(at -> {
      int lastIndexOf = at.getFileName().lastIndexOf(DOT);
      String prefix = at.getFileName().substring(0, lastIndexOf);
      String suffix = at.getFileName().substring(lastIndexOf + 1, at.getFileName().length());
      try {
        Path tempFilePath = File.createTempFile(prefix, suffix).toPath();
        OutputStream outputStream = Files.newOutputStream(tempFilePath);
        //Download file
        fileStorageRepository.download("dev", "100000/" + at.getFileName(), outputStream);
        // update status and save
        at.setFileStatus(FileStatus.IN_PROGRESS);
        applicationTypeRepository.save(at);
        // process the file
        ExcelToObjectMapper excelToObjectMapper = new ExcelToObjectMapper(Files.newInputStream(tempFilePath), false);
        List<PersonXLS> result = excelToObjectMapper.map(PersonXLS.class, new PersonHeaderMapper().getFieldMap());
        if (!CollectionUtils.isEmpty(result)) {
          applicationConfiguration.getSteps().stream().forEach(stepConfiguration -> sendData(Lists.newArrayList(result), stepConfiguration));
        }

      } catch (Exception e) {
        LOG.error("Error while downloading file {}", at.getFileName());
      }

    });
    return null;
  }

  /**
   * Using mapper converted service dtos will be posted to service end point
   *
   * @param items
   * @param stepConfiguration
   */
  public void sendData(List<Object> items, StepConfiguration stepConfiguration) {
    List<Object> processItems = Lists.newArrayList();
    List<Object> newItems = Lists.newArrayList();
    List<Object> updateItems = Lists.newArrayList();


    LOG.info("Sending {} {} items to service using [{}] client to endpoint [{}]", items.size(),
            items.get(0).getClass().getSimpleName(), stepConfiguration.getServiceClientBeanName(), stepConfiguration.getCreateUpdateEndpointUrl());

    items.stream().forEach(item -> processItems.add(view2DtoProcessor(item, stepConfiguration)));

    Map<String, ServiceKey> bulkServiceDataMap = etlService.getBulkServiceData(stepConfiguration, stepConfiguration.getDtoClassName());

    processItems.forEach(item -> {
      try {
        fileRecordStorage.write(new FileRecordEvent(item, new Date(), item.getClass().getTypeName(), "CREATE", new Exception()));
        if (etlService.isNewEntity(item, stepConfiguration, bulkServiceDataMap)) {
          newItems.add(item);
        } else {
          updateItems.add(item);
        }
      } catch (NoSuchFieldException e) {
        LOG.info("Field is not present in DTO {}", stepConfiguration.getDtoClassName().getSimpleName());
      } catch (IllegalAccessException e) {
        LOG.info("Field access error in DTO {}", stepConfiguration.getDtoClassName().getSimpleName());
      } catch (FileRecordStorageException e) {

      }
    });
    etlService.sendData(stepConfiguration, stepConfiguration.getDtoClassName(), newItems, updateItems);
  }

  /**
   * Using a model mapper, convert the view to a type that the service endpoints will understand
   *
   * @param stepConfiguration The object that stores all the configuration for this step
   * @return
   */
  private Object view2DtoProcessor(Object item, StepConfiguration stepConfiguration) {
    final Class dtoClass = stepConfiguration.getDtoClassName();
    final String mapper = stepConfiguration.getMapper();
    ModelMapper modelMapper = new ModelMapper();
    Object mappedObject;
    if (StringUtils.isEmpty(mapper) || StringUtils.equals(mapper, DEFAULT_MAPPER)) {
      mappedObject = modelMapper.map(item, dtoClass);
    } else {
      modelMapper = modelMappers.get(stepConfiguration.getMapper());
      mappedObject = modelMapper.map(item, dtoClass);
    }
    return mappedObject;
  }

  @Override
  public List<ErrorVM> process(InputStream inputStream) {
    return null;
  }
}
