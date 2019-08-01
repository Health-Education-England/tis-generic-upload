package com.transformuk.hee.tis.genericupload.service.storage;

import com.google.gson.Gson;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.service.exception.FileRecordStorageException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

public class AzureServiceBusSubscriber implements ServiceBusSubscriber {

  private static final Gson GSON = new Gson();

  @Autowired
  private QueueClient queueClient;

  @Autowired
  private TopicClient topicClient;

  private void receiveQueueMessage() throws ServiceBusException, InterruptedException {
    PersonXLS personXLS = new PersonXLS();
    queueClient.registerMessageHandler(new MessageHandler(personXLS), new MessageHandlerOptions());
    TimeUnit.SECONDS.sleep(5);
    queueClient.close();
  }

  @Override
  public Object receiveQueueMessage(String queueName) throws FileRecordStorageException {
    //receiveQueueMessage();
    return null;
  }

  @Override
  public Object receiveTopicMessage(String topicName) throws FileRecordStorageException {
    return null;
  }

  private static class MessageHandler implements IMessageHandler {

    private PersonXLS personXLS;

    public MessageHandler(PersonXLS personXLS) {
      this.personXLS = personXLS;
    }

    public CompletableFuture<Void> onMessageAsync(IMessage message) {
      final String messageString = new String(message.getBody(), StandardCharsets.UTF_8);
      personXLS = GSON.fromJson(messageString, personXLS.getClass());
      System.out.println("Received message: " + messageString);
      return CompletableFuture.completedFuture(null);
    }

    public void notifyException(Throwable exception, ExceptionPhase phase) {
      System.out.println(phase + " encountered exception:" + exception.getMessage());
    }
  }
}
