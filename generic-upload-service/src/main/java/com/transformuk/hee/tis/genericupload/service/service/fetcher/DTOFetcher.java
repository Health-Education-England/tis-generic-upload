package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Iterables.partition;

public abstract class DTOFetcher<DTO_KEY, DTO> {
	private final int QUERYSTRING_LENGTH_LIMITING_BATCH_SIZE = 50;

	protected Function<Set<DTO_KEY>, List<DTO>> dtoFetchingServiceCall;
	protected Function<DTO, DTO_KEY> idFunction;

	protected Map<DTO_KEY, DTO> findWithIds(Set<DTO_KEY> ids) {
		return StreamSupport.stream(partition(ids, QUERYSTRING_LENGTH_LIMITING_BATCH_SIZE).spliterator(), false) //partition into chunks to get data in batches
				.map(partitionedSet -> new HashSet<DTO_KEY>(partitionedSet))    //convert the List into a HashSet; TODO refactor the service call to expect a list
				.map(dtoFetchingServiceCall)
				.flatMap(Collection::stream) //TODO check if 32 is optimal sizing
				.collect(Collectors.toMap(idFunction, Function.identity()));
	}

	//convenience method to
	public Set<String> extractIds(Map<DTO_KEY, DTO> dtos, Function<DTO, Long> idFunction) {
		return dtos.entrySet().stream()
				.map(stringGmcDetailsDTOEntry -> String.valueOf(idFunction.apply(stringGmcDetailsDTOEntry.getValue())))
				.collect(Collectors.toSet());
	}
}
