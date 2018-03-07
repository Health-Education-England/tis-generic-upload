package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.PersonBasicDetailsDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

import java.util.Map;
import java.util.Set;

public class PersonBasicDetailsDTOFetcher extends DTOFetcher<String, PersonBasicDetailsDTO> {
	public PersonBasicDetailsDTOFetcher(TcsServiceImpl tcsService) {
		super.dtoFetchingServiceCall = tcsService::findPersonBasicDetailsIn;
		super.idFunction = PersonBasicDetailsDTO::getGmcNumber;
	}

	@Override
	public Map<String, PersonBasicDetailsDTO> findWithIds(Set<String> ids) {
		return super.findWithIds(ids);
	}
}
