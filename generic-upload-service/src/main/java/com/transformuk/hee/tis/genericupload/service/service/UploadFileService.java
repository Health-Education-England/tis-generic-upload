package com.transformuk.hee.tis.genericupload.service.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadFileService {


  /**
   * Files to upload on Azure blob account
   *
   * @param files
   * @return
   * @throws Exception
   */
  String upload(List<MultipartFile> files) throws Exception;
}
