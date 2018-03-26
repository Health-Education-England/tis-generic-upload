package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.PersonBasicDetailsDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

import java.util.Map;
import java.util.Set;

public class PersonBasicDetailsDTOFetcher extends DTOFetcher<Long, PersonBasicDetailsDTO> {
	public PersonBasicDetailsDTOFetcher(TcsServiceImpl tcsService) {
		super.dtoFetchingServiceCall = tcsService::findPersonBasicDetailsIn;
		super.keyFunction = PersonBasicDetailsDTO::getId;
	}

	public Map<Long, PersonBasicDetailsDTO> findWithKeys(Set<Long> ids) {
		return super.findWithKeys(ids);
	}
}
