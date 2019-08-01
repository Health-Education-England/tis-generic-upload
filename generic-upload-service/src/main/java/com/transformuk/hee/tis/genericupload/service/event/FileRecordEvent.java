package com.transformuk.hee.tis.genericupload.service.event;

import java.util.Date;

public class FileRecordEvent {

  private Object dto;
  private Date date;
  private String type;
  private String action;
  private Exception exception;

  public FileRecordEvent(Object dto, Date date, String classType, String action,
      Exception exception) {
    this.dto = dto;
    this.date = date;
    this.type = classType;
    this.action = action;
    this.exception = exception;
  }

  public Object getDto() {
    return dto;
  }

  public Date getDate() {
    return date;
  }

  public String getType() {
    return type;
  }

  public String getAction() {
    return action;
  }

  public Exception getException() {
    return exception;
  }
}
