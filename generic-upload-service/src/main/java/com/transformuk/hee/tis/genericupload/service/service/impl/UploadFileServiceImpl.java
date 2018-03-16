package com.transformuk.hee.tis.genericupload.service.service.impl;

import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import com.transformuk.hee.tis.genericupload.service.service.UploadFileService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UploadFileServiceImpl implements UploadFileService {

    private final Logger LOG = LoggerFactory.getLogger(UploadFileServiceImpl.class);

    private final FileStorageRepository fileStorageRepository;
    private final ApplicationTypeRepository applicationTypeRepository;

    @Autowired
    public UploadFileServiceImpl(FileStorageRepository fileStorageRepository,
                                 ApplicationTypeRepository applicationTypeRepository) {
        this.fileStorageRepository = fileStorageRepository;
        this.applicationTypeRepository = applicationTypeRepository;
        // this.personRepository = personRepository;
    }

    public ApplicationType save(String fileName, long logId, String username, String firstName, String lastName) {
        LOG.info("Request to save ApplicationType based on fileName : {}", fileName);

        ApplicationType applicationType = new ApplicationType();
        applicationType.setFileName(fileName);
        applicationType.setStartDate(LocalDateTime.now());
        applicationType.setFileType(FileType.RECRUITMENT);
        applicationType.setFileStatus(FileStatus.PENDING);
        applicationType.setLogId(logId);
        applicationType.setUsername(username);
        applicationType.setFirstname(firstName);
        applicationType.setLastname(lastName);

        return applicationTypeRepository.save(applicationType);
    }

    @Override
    public long upload(List<MultipartFile> files, String username, String firstName, String lastName) throws Exception {
        long logId = System.currentTimeMillis();

        if (!ObjectUtils.isEmpty(files)) {
            fileStorageRepository.store(logId, CONTAINER_NAME, files);
            for (MultipartFile file : files) {
                if (!ObjectUtils.isEmpty(file) && StringUtils.isNotEmpty(file.getContentType())) {
                    save(file.getOriginalFilename(), logId, username, firstName, lastName);
                }
            }
        }
        return logId;
    }

    @Override
    public Page<ApplicationType> getUploadStatus(Pageable pageable) {
        return applicationTypeRepository.findAll(pageable);
    }

    @Override
    public Page<ApplicationType> searchUploads(String text, Pageable pageable) {
        return applicationTypeRepository.fullTextSearch(text, pageable);
    }
}
