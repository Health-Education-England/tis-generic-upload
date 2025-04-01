package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import java.time.LocalDate;
import java.util.Date;

public class TestTarget extends TemplateXLS {

  private Long myLong;
  private String myString;
  private Float myFloat;
  private Date myDate;
  private LocalDate myLocalDate;

  public Long getMyLong() {
    return myLong;
  }

  public void setMyLong(Long myLong) {
    this.myLong = myLong;
  }

  public String getMyString() {
    return myString;
  }

  public void setMyString(String myString) {
    this.myString = myString;
  }

  public Float getMyFloat() {
    return myFloat;
  }

  public void setMyFloat(Float myFloat) {
    this.myFloat = myFloat;
  }

  public Date getMyDate() {
    return myDate;
  }

  public void setMyDate(Date myDate) {
    this.myDate = myDate;
  }

  public LocalDate getMyLocalDate() {
    return myLocalDate;
  }

  public void setMyLocalDate(LocalDate myLocalDate) {
    this.myLocalDate = myLocalDate;
  }

}
