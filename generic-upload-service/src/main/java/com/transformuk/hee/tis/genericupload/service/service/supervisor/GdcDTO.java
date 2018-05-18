package com.transformuk.hee.tis.genericupload.service.service.supervisor;

import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;

public class GdcDTO extends RegNumberDTO<GdcDetailsDTO> {
	public GdcDTO(GdcDetailsDTO gdcDetailsDTO) {
		super();
		regNumberType = RegNumberType.GDC;
		super.setRegNumberDTO(gdcDetailsDTO);
	}

	@Override
	public Long getId() {
		return getRegNumberDTO().getId();
	}
}
