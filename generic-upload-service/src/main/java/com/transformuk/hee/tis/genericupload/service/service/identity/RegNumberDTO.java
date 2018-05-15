package com.transformuk.hee.tis.genericupload.service.service.identity;

public abstract class RegNumberDTO<T> {
	private T regNumberDTO;
	protected RegNumberType regNumberType;

	public RegNumberType getRegNumberType() {
		return regNumberType;
	}

	public T getRegNumberDTO() {
		return regNumberDTO;
	}

	public void setRegNumberDTO(T regNumberDTO) {
		this.regNumberDTO = regNumberDTO;
	}

	public abstract Long getId();
}
