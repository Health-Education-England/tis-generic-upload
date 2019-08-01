package com.transformuk.hee.tis.genericupload.service.storage;

import com.transformuk.hee.tis.genericupload.service.event.FileRecordEvent;
import com.transformuk.hee.tis.genericupload.service.exception.FileRecordStorageException;

public interface ServiceBusPublish {

  void sendMessage(String queueOrTopicName, FileRecordEvent fileRecordEvent)
      throws FileRecordStorageException;

}
