package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

public class PeopleByIdFetcher extends DTOFetcher<Long, PersonDTO> {

  public PeopleByIdFetcher(TcsServiceImpl tcsService) {
    super.dtoFetchingServiceCall = tcsService::findPeopleIn;
    super.keyFunction = PersonDTO::getId;
  }
}
