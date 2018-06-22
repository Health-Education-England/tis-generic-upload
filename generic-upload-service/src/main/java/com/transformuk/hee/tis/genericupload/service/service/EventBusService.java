package com.transformuk.hee.tis.genericupload.service.service;

import com.google.common.eventbus.EventBus;
import com.transformuk.hee.tis.genericupload.service.event.FileRecordEvent;
import com.transformuk.hee.tis.genericupload.service.event.FileRecordServiceFactory;
import com.transformuk.hee.tis.genericupload.service.storage.FileRecordStorage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * service that is in charge of placing objects on a queue for processing when file process
 */

public class EventBusService {

  @Autowired
  private EventBus eventBus;

  @Autowired
  private List<FileRecordStorage> fileRecordStorages;

  @PostConstruct
  public void init() {
    for (FileRecordStorage fileRecordStorage : fileRecordStorages) {
      eventBus.register(fileRecordStorage);
    }
  }

  /**
   * Create a file record event object using the data, action and exception and post it to the event queue
   *
   * @param dto    The data that was sent to the service
   * @param action The action which was attempted
   * @param e      The exception thrown by the service
   */
  public void placeOnFileRecordQueue(Object dto, String action, Exception e) {
    final FileRecordEvent deadLetterEvent = FileRecordServiceFactory.createInstance(dto, action, e);
    eventBus.post(deadLetterEvent);
  }
}
