package com.transformuk.hee.tis.genericupload.service.storage;

import com.transformuk.hee.tis.genericupload.service.exception.FileRecordStorageException;

public interface ServiceBusSubscriber {
  Object receiveQueueMessage(String queueName) throws FileRecordStorageException;

  Object receiveTopicMessage(String topicName) throws FileRecordStorageException;
}
