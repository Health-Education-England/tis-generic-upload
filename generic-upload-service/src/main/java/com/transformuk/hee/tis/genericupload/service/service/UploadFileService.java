package com.transformuk.hee.tis.genericupload.service.service;

import com.microsoft.azure.storage.StorageException;
import com.transformuk.hee.tis.genericupload.api.dto.ResetUploadStatusRequestDto;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {

  /**
   * Files to upload on Azure blob account
   *
   * @param files
   * @return
   * @throws Exception
   */
  ApplicationType upload(List<MultipartFile> files, FileType fileType, String username,
      String firstName, String lastName)
      throws InvalidKeyException, StorageException, URISyntaxException;

  Page<ApplicationType> getUploadStatus(Pageable pageable);

  Page<ApplicationType> searchUploads(LocalDateTime uploadedDate, String file, String user,
      Pageable pageable);

  Page<ApplicationType> searchUploads(String text, Pageable pageable);

  String findErrorsByLogId(Long logId, OutputStream outputStream);

  /**
   * Reset the status of a bulk upload job to a target status.
   *
   * @param resetUploadStatusRequestDto the request containing the job id and target status
   * @param requesterUserName the username requesting the reset
   * @return the updated ApplicationType of the job
   */
  ApplicationType resetUploadStatus(ResetUploadStatusRequestDto resetUploadStatusRequestDto,
      String requesterUserName);
}
