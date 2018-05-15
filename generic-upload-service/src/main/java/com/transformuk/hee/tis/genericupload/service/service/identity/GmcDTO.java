package com.transformuk.hee.tis.genericupload.service.service.identity;

import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;

public class GmcDTO extends RegNumberDTO<GmcDetailsDTO> {
	public GmcDTO(GmcDetailsDTO gmcDetailsDTO) {
		super();
		regNumberType = RegNumberType.GMC;
		super.setRegNumberDTO(gmcDetailsDTO);
	}

	@Override
	public Long getId() {
		return getRegNumberDTO().getId();
	}
}
