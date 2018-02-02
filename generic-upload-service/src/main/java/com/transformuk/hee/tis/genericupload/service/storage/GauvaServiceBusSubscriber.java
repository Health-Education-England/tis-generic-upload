package com.transformuk.hee.tis.genericupload.service.storage;

import com.transformuk.hee.tis.genericupload.service.exception.FileRecordStorageException;

public class GauvaServiceBusSubscriber implements ServiceBusSubscriber {
  @Override
  public Object receiveQueueMessage(String queueName) throws FileRecordStorageException {
    return null;
  }

  @Override
  public Object receiveTopicMessage(String topicName) throws FileRecordStorageException {
    return null;
  }
}
