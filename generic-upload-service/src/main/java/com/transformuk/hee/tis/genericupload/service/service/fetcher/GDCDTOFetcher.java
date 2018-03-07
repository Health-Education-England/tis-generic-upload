package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

import java.util.Map;
import java.util.Set;

public class GDCDTOFetcher extends DTOFetcher<String, GdcDetailsDTO> {
	public GDCDTOFetcher(TcsServiceImpl tcsService) {
		super.dtoFetchingServiceCall = tcsService::findGdcDetailsIn;
		super.idFunction = GdcDetailsDTO::getGdcNumber;
	}

	@Override
	public Map<String, GdcDetailsDTO> findWithIds(Set<String> ids) {
		return super.findWithIds(ids);
	}
}
