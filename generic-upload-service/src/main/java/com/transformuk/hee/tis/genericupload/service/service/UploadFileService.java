package com.transformuk.hee.tis.genericupload.service.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadFileService {
    //TODO externalise to environment variable
    public static final String CONTAINER_NAME = "dev";

  /**
   * Files to upload on Azure blob account
   *
   * @param files
   * @return
   * @throws Exception
   */
  long upload(List<MultipartFile> files, String username) throws Exception;
}
