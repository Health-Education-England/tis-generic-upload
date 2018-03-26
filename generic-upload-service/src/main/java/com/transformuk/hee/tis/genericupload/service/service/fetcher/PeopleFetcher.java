package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

import java.util.function.Function;

public class PeopleFetcher extends DTOFetcher<Long, PersonDTO> {
	public PeopleFetcher(TcsServiceImpl tcsService) {
		super.dtoFetchingServiceCall = tcsService::findPeopleIn;
	}

	public void setIdMappingFunction(Function idMappingFunction) {
		super.keyFunction = idMappingFunction;
	}
}
