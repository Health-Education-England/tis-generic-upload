package com.transformuk.hee.tis.genericupload.service.service.identity;

import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;

public class PhnDTO extends RegNumberDTO<PersonDTO> {
	public PhnDTO(PersonDTO personDTO) {
		super();
		regNumberType = RegNumberType.PH;
		super.setRegNumberDTO(personDTO);
	}

	@Override
	public Long getId() {
		return getRegNumberDTO().getId();
	}
}