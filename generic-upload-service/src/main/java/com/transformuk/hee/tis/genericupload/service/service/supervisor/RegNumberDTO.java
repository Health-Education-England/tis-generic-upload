package com.transformuk.hee.tis.genericupload.service.service.supervisor;

public abstract class RegNumberDTO<T> {

  protected RegNumberType regNumberType;
  private T dto;

  public RegNumberType getRegNumberType() {
    return regNumberType;
  }

  public T getRegNumberDTO() {
    return dto;
  }

  public void setRegNumberDTO(T regNumberDTO) {
    this.dto = regNumberDTO;
  }

  public abstract Long getId();
}
