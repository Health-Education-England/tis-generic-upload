package com.transformuk.hee.tis.genericupload.service.storage;

import com.google.gson.Gson;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.transformuk.hee.tis.genericupload.service.event.FileRecordEvent;
import com.transformuk.hee.tis.genericupload.service.exception.FileRecordStorageException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AzureTopicServiceBusPublish implements ServiceBusPublish {

  private static final Logger LOG = LoggerFactory.getLogger(AzureTopicServiceBusPublish.class);
  private static final Gson GSON = new Gson();
  private static final String SESSION_ID = "RECRUITMENT_BULKUPLOAD_SESSION_ID";

  @Autowired
  private TopicClient topicClient;

  /**
   * Create a json string out the the dead letter event
   *
   * @param fileRecordEvent The Pojo representing the dead letter event
   * @return Json string
   */
  private static String eventMessage(FileRecordEvent fileRecordEvent) {
    return GSON.toJson(fileRecordEvent);
  }

  @Override
  public void sendMessage(String topicName, FileRecordEvent fileRecordEvent)
      throws FileRecordStorageException {
    try {
      final String messageBody = eventMessage(fileRecordEvent);
      final Message message = new Message(messageBody.getBytes(StandardCharsets.UTF_8));
      message.setSessionId(SESSION_ID);
      topicClient.send(message);
      topicClient.close();
    } catch (ServiceBusException | InterruptedException e) {
      throw new FileRecordStorageException("DeadLetter queue write error" + e);
    }
  }
}
