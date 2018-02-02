package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.exception.ErrorVM;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;

import java.io.InputStream;
import java.util.List;

public interface FileProcessService {
  List<ApplicationType> loadFilesByStatus(FileStatus status);

  List<ErrorVM> process(InputStream inputStream);
}
