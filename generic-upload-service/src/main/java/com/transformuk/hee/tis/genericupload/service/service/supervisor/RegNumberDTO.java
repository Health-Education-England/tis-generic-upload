package com.transformuk.hee.tis.genericupload.service.service.supervisor;

public abstract class RegNumberDTO<T> {
	private T dto;
	protected RegNumberType regNumberType;

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
