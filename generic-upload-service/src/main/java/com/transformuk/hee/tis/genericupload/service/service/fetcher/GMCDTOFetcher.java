package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

public class GMCDTOFetcher extends DTOFetcher<String, GmcDetailsDTO> {
	public GMCDTOFetcher(TcsServiceImpl tcsService) {
		super.dtoFetchingServiceCall = tcsService::findGmcDetailsIn;
		super.keyFunction = GmcDetailsDTO::getGmcNumber;
	}
}
