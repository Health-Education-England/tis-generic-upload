package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;

public class PostFetcher extends DTOFetcher<String, PostDTO> {
	public PostFetcher(TcsServiceImpl tcsService) {
		super.dtoFetchingServiceCall = tcsService::findPostsByNationalPostNumbersIn;
		super.keyFunction = PostDTO::getNationalPostNumber;
	}
}
