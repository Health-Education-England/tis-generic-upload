package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

public class PeopleByPHNFetcher extends DTOFetcher<String, PersonDTO> {
	public PeopleByPHNFetcher(TcsServiceImpl tcsService) {
		super.dtoFetchingServiceCall = tcsService::findPeopleByPublicHealthNumbersIn;
		super.keyFunction = PersonDTO::getPublicHealthNumber;
	}
}
