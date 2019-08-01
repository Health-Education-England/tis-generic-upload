package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

public class GDCDTOFetcher extends DTOFetcher<String, GdcDetailsDTO> {

  public GDCDTOFetcher(TcsServiceImpl tcsService) {
    super.dtoFetchingServiceCall = tcsService::findGdcDetailsIn;
    super.keyFunction = GdcDetailsDTO::getGdcNumber;
  }
}
