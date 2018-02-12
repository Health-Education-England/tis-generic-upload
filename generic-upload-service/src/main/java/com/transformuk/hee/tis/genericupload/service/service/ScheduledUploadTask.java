package com.transformuk.hee.tis.genericupload.service.service;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper;
import com.transformuk.hee.tis.genericupload.service.parser.PersonHeaderMapper;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ScheduledUploadTask {
    private static final Logger logger = getLogger(ScheduledUploadTask.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final FileStorageRepository fileStorageRepository;
    private final ApplicationTypeRepository applicationTypeRepository;

    @Autowired
    public ScheduledUploadTask(FileStorageRepository fileStorageRepository,
                               ApplicationTypeRepository applicationTypeRepository) {
        this.fileStorageRepository = fileStorageRepository;
        this.applicationTypeRepository = applicationTypeRepository;
    }


    //waits fixedDelay milliseconds after the last run task
    @Scheduled(fixedDelay = 5000, initialDelay = 2000) //TODO externalise this wait interval,
    public void scheduleTaskWithFixedDelay() {
        logger.info("Fixed Delay Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
        for(ApplicationType applicationType : applicationTypeRepository.findByFileStatusOrderByStartDate(FileStatus.PENDING)) {
            //set to in progress
            applicationType.setFileStatus(FileStatus.IN_PROGRESS);
            applicationTypeRepository.save(applicationType);

            ByteArrayOutputStream baos = (ByteArrayOutputStream) fileStorageRepository.download(applicationType.getLogId(), UploadFileService.CONTAINER_NAME, applicationType.getFileName());
            InputStream bis = new ByteArrayInputStream(baos.toByteArray());
            ExcelToObjectMapper excelToObjectMapper = null;
            List<PersonXLS> personXLSS = null;
            try {
                excelToObjectMapper = new ExcelToObjectMapper(bis);
                personXLSS = excelToObjectMapper.map(PersonXLS.class,
                        new PersonHeaderMapper().getFieldMap());
            } catch (InvalidFormatException e) {
                logger.error("Error while reading excel file : " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error while reading excel file and mapping headers : " + e.getMessage());
            }
        }
    }
}
