package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.stream.Collectors;

public class PeopleByIdFetcher extends DTOFetcher<String, PersonDTO> {

  public PeopleByIdFetcher(TcsServiceImpl tcsService) {
    super.dtoFetchingServiceCall = ids -> tcsService.findPeopleIn(
        ids.stream().map(Long::parseLong).collect(Collectors.toList()));
    super.keyFunction = p -> String.valueOf(p.getId());
  }
}
