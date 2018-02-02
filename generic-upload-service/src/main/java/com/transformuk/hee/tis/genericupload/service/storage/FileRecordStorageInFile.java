package com.transformuk.hee.tis.genericupload.service.storage;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.transformuk.hee.tis.genericupload.service.event.FileRecordEvent;
import com.transformuk.hee.tis.genericupload.service.exception.FileRecordStorageException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Writer that takes messages off the event bus and writes it to a file under the deadletter directory
 */
public class FileRecordStorageInFile implements FileRecordStorage {

  private static final Logger LOG = LoggerFactory.getLogger(FileRecordStorageInFile.class);
  private static final Gson GSON = new Gson();

  @Value("${deadletterDirectory}")
  private String deadletterDirectory;

  private BufferedWriter bufferedWriter;

  @PostConstruct
  public void init() throws IOException {
    Path deadletterDirectoryPath = null;
    if (StringUtils.isNotEmpty(deadletterDirectory)) {
      deadletterDirectoryPath = Files.createDirectories(Paths.get(deadletterDirectory));
    }

    if (deadletterDirectoryPath != null) {
      DateFormat dateInstance = SimpleDateFormat.getDateInstance();
      String deadletterFilename = dateInstance.format(new Date());
      Path filenamePath = Paths.get(deadletterDirectory, deadletterFilename);
      if (!(filenamePath.toFile().exists())) {
        Files.createFile(filenamePath);
      }
      bufferedWriter = new BufferedWriter(new FileWriter(filenamePath.toFile()));
    }
  }

  @PreDestroy
  public void destroy() throws IOException {
    if (bufferedWriter != null) {
      bufferedWriter.flush();
      IOUtils.closeQuietly(bufferedWriter);
    }
  }

  /**
   * Write the dead letter to file
   *
   * @param fileRecordEvent The dead letter event Pojo
   * @throws IOException As we're writing to disk, an IO exception may occur
   */
  @Subscribe
  @Override
  public void write(FileRecordEvent fileRecordEvent) throws FileRecordStorageException {
    try {

      LOG.info("Writing dead letter to file");
      bufferedWriter.write(deadLetterEventMessage(fileRecordEvent));
      bufferedWriter.newLine();
      bufferedWriter.flush();

    } catch (IOException e) {
      throw new FileRecordStorageException("DeadLetter queue write error" + e);
    }
  }

  /**
   * Create a json string out the the dead letter event
   *
   * @param fileRecordEvent The Pojo representing the dead letter event
   * @return Json string
   */
  private static String deadLetterEventMessage(FileRecordEvent fileRecordEvent) {
    return GSON.toJson(fileRecordEvent);
  }
}
