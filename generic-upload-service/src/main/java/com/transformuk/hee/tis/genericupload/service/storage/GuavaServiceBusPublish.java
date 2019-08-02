package com.transformuk.hee.tis.genericupload.service.storage;

import com.google.common.eventbus.EventBus;
import com.transformuk.hee.tis.genericupload.service.event.FileRecordEvent;
import com.transformuk.hee.tis.genericupload.service.exception.FileRecordStorageException;
import org.springframework.beans.factory.annotation.Autowired;

public class GuavaServiceBusPublish implements ServiceBusPublish {

  @Autowired
  private EventBus eventBus;


  @Override
  public void sendMessage(String queueName, FileRecordEvent fileRecordEvent)
      throws FileRecordStorageException {
    eventBus.post(fileRecordEvent);

  }

}
