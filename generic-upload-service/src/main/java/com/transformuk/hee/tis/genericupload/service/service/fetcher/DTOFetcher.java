package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Iterables.partition;

public abstract class DTOFetcher<DTO_KEY, DTO> {
	private final int QUERYSTRING_LENGTH_LIMITING_BATCH_SIZE = 32;	//TODO externalise

	protected Function<List<DTO_KEY>, List<DTO>> dtoFetchingServiceCall;
	protected Function<DTO, DTO_KEY> keyFunction;

	public Map<DTO_KEY, DTO> findWithKeys(Set<DTO_KEY> ids) {
		return StreamSupport.stream(partition(ids, QUERYSTRING_LENGTH_LIMITING_BATCH_SIZE).spliterator(), false) //partition into chunks to get data in batches
				.map(dtoFetchingServiceCall)
				.flatMap(Collection::stream)
				.collect(Collectors.toMap(keyFunction, Function.identity()));
	}

	//convenience method to
	public Set<Long> extractIds(Map<DTO_KEY, DTO> dtos, Function<DTO, Long> idFunction) {
		return dtos.entrySet().stream()
				.map(dtoEntry -> idFunction.apply(dtoEntry.getValue()))
				.collect(Collectors.toSet());
	}
}
