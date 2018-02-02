package com.transformuk.hee.tis.genericupload.service.event;

import java.util.Date;

public final class FileRecordServiceFactory {

  private FileRecordServiceFactory() {
  }

  public static FileRecordEvent createInstance(Object dto, String action, Exception exception) {

    Date date = new Date();
    String type = dto.getClass().getTypeName();

    return new FileRecordEvent(dto, date, type, action, exception);
  }
}
