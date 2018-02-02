package com.transformuk.hee.tis.genericupload.service.exception;

/**
 * Thrown if error on writing to dead letter queue
 */
public class FileRecordStorageException extends Exception {
  private final String message;

  public FileRecordStorageException(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
