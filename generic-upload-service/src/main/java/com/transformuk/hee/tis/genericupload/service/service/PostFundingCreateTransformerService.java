package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;

import com.transformuk.hee.tis.genericupload.api.dto.PostFundingCreateRow;
import com.transformuk.hee.tis.reference.api.dto.FundingSubTypeDto;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class PostFundingCreateTransformerService {

  protected static final String ERROR_INVALID_FUNDING_BODY_NAME =
      "Funding body could not be found for the name \"%s\".";
  protected static final String ERROR_FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE =
      "Funding type is required when funding subtype is filled.";
  protected static final String ERROR_FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE =
      "Funding subtype \"%s\" does not match funding type \"%s\".";
  protected static final String FUNDING_START_DATE_NULL_OR_EMPTY =
      "Post funding start date cannot be null or empty";
  protected static final String FUNDING_END_DATE_VALIDATION_MSG =
      "Post funding end date must not be equal to or before start date if included.";
  protected static final String FUNDING_TYPE_REQUIRES_SUBTYPE =
      "One of the appropriate funding subtypes must be included for this type of funding.";
  protected static final String ERROR_INVALID_FUNDING_REASON =
      "Funding reason could not be found for the name \"%s\".";

  private final Map<String, UUID> fundingReasonToIdMap = new HashMap<>();
  private Clock clock = Clock.systemDefaultZone();

  @Autowired
  private ReferenceServiceImpl referenceService;
  @Autowired
  private TcsServiceImpl tcsService;

  public void processRows(List<PostFundingCreateRow> postFundingCreateRows) {
    Set<String> fundingBodies = new HashSet<>();
    Set<String> fundingSubTypeLabels = new HashSet<>();
    Set<String> fundingTypeLabels = new HashSet<>();
    Map<String, List<PostFundingCreateRow>> postIdsToInputRows = new HashMap<>();
    postFundingCreateRows.forEach(row -> {
      row.initialiseSuccessfullyImported();
      Optional.ofNullable(row.getFundingBody()).ifPresent(fundingBodies::add);
      Optional.ofNullable(row.getFundingType()).ifPresent(fundingTypeLabels::add);
      Optional.ofNullable(row.getFundingSubtype()).ifPresent(fundingSubTypeLabels::add);
      String postId = row.getPostTisId();
      if (postId != null) {
        List<PostFundingCreateRow> groupedRows = postIdsToInputRows
            .getOrDefault(postId, new ArrayList<>());

        groupedRows.add(row);
        postIdsToInputRows.put(postId, groupedRows);
      } else {
        row.addErrorMessage("TIS_Post_ID is a required field.");
      }
    });

    // Get all funding bodies and retrieve matching funding body IDs.
    Map<String, String> fundingBodyNameToId = referenceService
        .findCurrentTrustsByTrustKnownAsIn(fundingBodies).stream()
        .collect(Collectors.toMap(TrustDTO::getTrustKnownAs, dto -> String.valueOf(dto.getId())));

    Map<String, List<FundingSubTypeDto>> fundingTypeToSubtypes =
        new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    referenceService
        .findCurrentFundingTypesByLabelIn(fundingTypeLabels).forEach(t -> {
          List<FundingSubTypeDto> subtypes =
              referenceService.findCurrentFundingSubTypesForFundingTypeId(t.getId());
          fundingTypeToSubtypes.put(t.getLabel(), subtypes);
        });
    // Get all fundingSubType and retrieve matching fundingSubType IDs.
    // As fundingSubtype label is not unique for all fundingSubtypes,
    // use (fundingType label, fundingSubtype label) from reference service as key.
    Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId =
        referenceService.findCurrentFundingSubTypesByLabels(fundingSubTypeLabels).stream()
            .collect(Collectors.toMap(
                dto -> ImmutablePair.of(dto.getFundingType().getLabel().toLowerCase(),
                    dto.getLabel().toLowerCase()),
                FundingSubTypeDto::getId));

    // Get all funding reasons and retrieve matching funding body IDs.
    postFundingCreateRows.forEach(this::validateAndCacheFundingReasons);

    for (Entry<String, List<PostFundingCreateRow>> postIdToInputRows :
        postIdsToInputRows.entrySet()) {
      String postId = postIdToInputRows.getKey();

      Map<PostFundingDTO, PostFundingCreateRow> fundingDtosToSource = buildFundingDtos(
          postIdToInputRows.getValue(), fundingBodyNameToId, fundingSubTypeLabelToId,
          fundingTypeToSubtypes, fundingReasonToIdMap);
      Set<PostFundingDTO> builtPostFundingDtos = fundingDtosToSource.keySet();
      if (builtPostFundingDtos.isEmpty()) {
        continue;
      }
      PostDTO postDto = new PostDTO();
      postDto.setId(Long.parseLong(postId));
      postDto.setFundings(builtPostFundingDtos);

      try {
        List<PostFundingDTO> postFundingDtos = tcsService.updatePostFundings(postDto);

        for (PostFundingDTO fundingDto : postFundingDtos) {
          List<String> errorMessages = fundingDto.getMessageList();

          // Get the source row for the DTO and add error messages or success.
          /* N.B. DTOs are populated from spreadsheet values and are normalised in TCS.
          This results in unexpected misses. postFundingCreateRow can be null.
           */
          fundingDto.setMessageList(new ArrayList<>());
          PostFundingCreateRow postFundingCreateRow = fundingDtosToSource.get(fundingDto);

          if (errorMessages.isEmpty()) {
            postFundingCreateRow.setSuccessfullyImported(true);
          } else {
            postFundingCreateRow.addErrorMessages(errorMessages);
          }
        }
      } catch (RestClientException e) {
        postIdToInputRows.getValue().forEach(row -> row.addErrorMessage(e.getMessage()));
      }
    }
  }

  /**
   * Build PostFundingDTOs from the PostFundingCreateRow.
   *
   * @param postFundingCreateRows   The PostFundingCreateRow to build DTOs for.
   * @param fundingBodyNameToId     A mapping of funding body names to IDs, as required by the DTO.
   * @param fundingSubTypeLabelToId A mapping of (fundingType, fundingSubType) to fundingSubType
   *                                UUID.
   * @return A map of built PostFundingDTOs to source PostFundingCreateRow.
   */
  private Map<PostFundingDTO, PostFundingCreateRow> buildFundingDtos(
      Collection<PostFundingCreateRow> postFundingCreateRows,
      Map<String, String> fundingBodyNameToId,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId,
      Map<String, List<FundingSubTypeDto>> fundingTypeToSubtypes,
      Map<String, UUID> fundingReasonToIdMap) {
    Map<PostFundingDTO, PostFundingCreateRow> postFundingDtosToSource = new HashMap<>();

    for (PostFundingCreateRow postFundingCreateRow : postFundingCreateRows) {
      String fundingBodyName = postFundingCreateRow.getFundingBody();
      String fundingBodyId = fundingBodyNameToId.get(fundingBodyName);

      if (fundingBodyName != null && fundingBodyId == null) {
        postFundingCreateRow
            .addErrorMessage(String.format(ERROR_INVALID_FUNDING_BODY_NAME, fundingBodyName));
      }

      final String fundingType = postFundingCreateRow.getFundingType();
      final UUID fundingSubTypeId = checkAndGetFundingSubtype(postFundingCreateRow,
          fundingSubTypeLabelToId);
      boolean expired = postFundingCreateRow.getDateTo() != null
          && convertDate(postFundingCreateRow.getDateTo()).isBefore(LocalDate.now(clock));
      /*
      Rather than using "FundingStatus = CURRENT", the requirement is a subtype is required when:
      - The funding has not expired (i.e. the end date is null or no earlier than today)
      - A funding type has been provided
      - A funding subtype has not been provided
      - The funding type has subtypes (i.e. the list of subtypes for the funding type is not empty)
        - A non-existent funding type is treated as having no subtypes
       */
      if (!expired
          && StringUtils.isNotBlank(fundingType)
          && fundingSubTypeId == null
          && CollectionUtils.isNotEmpty(fundingTypeToSubtypes.get(fundingType))) {
        postFundingCreateRow.addErrorMessage(FUNDING_TYPE_REQUIRES_SUBTYPE);
      }

      if (StringUtils.isNotEmpty(postFundingCreateRow.getErrorMessage())) {
        continue;
      }
      PostFundingDTO postFundingDto = new PostFundingDTO();
      postFundingDto.setFundingType(fundingType);
      postFundingDto.setInfo(postFundingCreateRow.getFundingTypeOther());

      validateFundingStartAndEndDate(postFundingCreateRow, postFundingDto);

      postFundingDto.setFundingBodyId(fundingBodyId);
      postFundingDto.setFundingSubTypeId(fundingSubTypeId);
      postFundingDto.setFundingReasonId(
          fundingReasonToIdMap.get(postFundingCreateRow.getFundingReason()));

      postFundingDtosToSource.put(postFundingDto, postFundingCreateRow);
    }
    return postFundingDtosToSource;
  }

  private UUID checkAndGetFundingSubtype(PostFundingCreateRow postFundingCreateRow,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId) {
    String fundingType = postFundingCreateRow.getFundingType();
    String fundingSubtype = postFundingCreateRow.getFundingSubtype();
    UUID fundingSubtypeId = null;

    if (StringUtils.isNotBlank(fundingSubtype)) {
      if (StringUtils.isBlank(fundingType)) {
        postFundingCreateRow
            .addErrorMessage(ERROR_FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE);
      } else {
        fundingSubtypeId = fundingSubTypeLabelToId.get(
            ImmutablePair.of(fundingType.toLowerCase(), fundingSubtype.toLowerCase()));
        if (fundingSubtypeId == null) {
          postFundingCreateRow
              .addErrorMessage(String.format(ERROR_FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE,
                  fundingSubtype, fundingType));
        }
      }
    }
    return fundingSubtypeId;
  }

  private void validateFundingStartAndEndDate(PostFundingCreateRow postFundingCreateRow,
      PostFundingDTO postFundingDto) {
    LocalDate dateFrom = null;
    LocalDate dateTo = null;

    if (postFundingCreateRow.getDateFrom() != null) {
      dateFrom = convertDate(postFundingCreateRow.getDateFrom());
    } else {
      postFundingCreateRow.addErrorMessage(String.format(FUNDING_START_DATE_NULL_OR_EMPTY));
    }

    if (postFundingCreateRow.getDateTo() != null) {
      dateTo = convertDate(postFundingCreateRow.getDateTo());
    }

    if (dateTo != null && dateFrom != null && !dateTo.isAfter(dateFrom)) {
      postFundingCreateRow.addErrorMessage(FUNDING_END_DATE_VALIDATION_MSG);
    }
    postFundingDto.setStartDate(dateFrom);
    postFundingDto.setEndDate(dateTo);
  }

  private void validateAndCacheFundingReasons(PostFundingCreateRow postFundingCreateRow) {
    String fundingReason = postFundingCreateRow.getFundingReason();
    if (fundingReason == null) {
      return;
    }

    if (!fundingReasonToIdMap.containsKey(fundingReason)) {
      referenceService.findCurrentFundingReasonsByReasonIn(Collections.singleton(fundingReason))
          .forEach(dto -> fundingReasonToIdMap.put(dto.getReason(), dto.getId()));
    }

    if (!fundingReasonToIdMap.containsKey(fundingReason)) {
      postFundingCreateRow.addErrorMessage(
          String.format(ERROR_INVALID_FUNDING_REASON, fundingReason));
    }
  }

  void setClock(Clock clock) {
    this.clock = clock;
  }
}