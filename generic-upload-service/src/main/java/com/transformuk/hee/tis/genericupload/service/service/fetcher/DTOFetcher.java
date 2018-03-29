package com.transformuk.hee.tis.genericupload.service.service.fetcher;

import java.util.Collection;
import java.util.HashMap;
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
	protected Set<DTO_KEY> duplicateKeys;

	public Map<DTO_KEY, DTO> findWithKeys(Set<DTO_KEY> ids) {
		//had to incorporate groupingBy to cater for scenarios when the gmc keys existed in TIS on multiple records.
		Map<DTO_KEY, List<DTO>> keysWithDuplicates = StreamSupport.stream(partition(ids, QUERYSTRING_LENGTH_LIMITING_BATCH_SIZE).spliterator(), false) //partition into chunks to get data in batches
				.map(dtoFetchingServiceCall)
				.flatMap(Collection::stream)
				.collect(Collectors.groupingBy(keyFunction, HashMap::new, Collectors.toList()));

		duplicateKeys = keysWithDuplicates.entrySet().stream()
				.filter(dtoEntry -> dtoEntry.getValue().size() > 1)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

		return keysWithDuplicates.entrySet().stream()
				.filter(dtoEntry -> dtoEntry.getValue().size() == 1)
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0)));
	}

	public Set<DTO_KEY> getDuplicateKeys() {
		return duplicateKeys;
	}

	//convenience method to
	public Set<Long> extractIds(Map<DTO_KEY, DTO> dtos, Function<DTO, Long> idFunction) {
		return dtos.entrySet().stream()
				.map(dtoEntry -> idFunction.apply(dtoEntry.getValue()))
				.collect(Collectors.toSet());
	}
}
