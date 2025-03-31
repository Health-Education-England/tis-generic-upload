/*
 * The MIT License (MIT)
 *
 * Copyright 2025 Crown Copyright (Health Education England)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import java.time.LocalDate;
import java.util.Date;
import lombok.Data;

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
