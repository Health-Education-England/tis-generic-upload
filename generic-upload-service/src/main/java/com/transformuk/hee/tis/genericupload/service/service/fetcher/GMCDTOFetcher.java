package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

import java.util.Map;
import java.util.Set;

public class GMCDTOFetcher extends DTOFetcher<String, GmcDetailsDTO> {
	public GMCDTOFetcher(TcsServiceImpl tcsService) {
		super.dtoFetchingServiceCall = tcsService::findGmcDetailsIn;
		super.idFunction = GmcDetailsDTO::getGmcNumber;
	}

	@Override
	public Map<String, GmcDetailsDTO> findWithIds(Set<String> ids) {
		return super.findWithIds(ids);
	}
}
