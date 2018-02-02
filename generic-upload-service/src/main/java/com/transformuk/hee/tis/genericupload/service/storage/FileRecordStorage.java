package com.transformuk.hee.tis.genericupload.service.storage;

import com.transformuk.hee.tis.genericupload.service.event.FileRecordEvent;
import com.transformuk.hee.tis.genericupload.service.exception.FileRecordStorageException;

public interface FileRecordStorage {

  void write(FileRecordEvent fileRecordEvent) throws FileRecordStorageException;
}
