package com.transformuk.hee.tis.genericupload.service.service;

import com.microsoft.azure.storage.StorageException;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.util.List;

public interface UploadFileService {
  /**
   * Files to upload on Azure blob account
   *
   * @param files
   * @return
   * @throws Exception
   */
  ApplicationType upload(List<MultipartFile> files, String username, String firstName, String lastName) throws InvalidKeyException, StorageException, URISyntaxException;

  Page<ApplicationType> getUploadStatus(Pageable pageable);

  Page<ApplicationType> searchUploads(LocalDateTime uploadedDate, String file, String user, Pageable pageable);

  Page<ApplicationType> searchUploads(String text, Pageable pageable);

  String findErrorsByLogId(Long logId, OutputStream outputStream);
}
