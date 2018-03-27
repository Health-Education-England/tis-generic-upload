package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.PersonBasicDetailsDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

public class PersonBasicDetailsDTOFetcher extends DTOFetcher<Long, PersonBasicDetailsDTO> {
	public PersonBasicDetailsDTOFetcher(TcsServiceImpl tcsService) {
		super.dtoFetchingServiceCall = tcsService::findPersonBasicDetailsIn;
		super.keyFunction = PersonBasicDetailsDTO::getId;
	}
}
