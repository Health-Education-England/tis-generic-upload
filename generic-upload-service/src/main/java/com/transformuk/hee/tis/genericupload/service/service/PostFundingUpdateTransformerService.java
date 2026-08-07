package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingCreateTransformerService.FUNDING_TYPE_REQUIRES_SUBTYPE;
import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.PostFundingUpdateRow;
import com.transformuk.hee.tis.reference.api.dto.FundingSubTypeDto;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.client.ResourceAccessException;

@Service
public class PostFundingUpdateTransformerService {

  protected static final String POST_FUNDING_ID_AND_POST_ID_NOT_MATCHING =
      "This post funding is not under the post id: \"%s\"";
  protected static final String DID_NOT_FIND_POST_FUNDING_FOR_ID =
      "Did not find the postFunding for id \"%s\".";
  protected static final String ERROR_INVALID_FUNDING_BODY_NAME =
      "Funding body could not be found for the name \"%s\".";
  protected static final String FUNDING_TYPE_IS_REQUIRED_FOR_DETAILS =
      "Funding type is required when funding details is filled.";
  protected static final String FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE =
      "Funding type is required when funding subtype is filled.";
  protected static final String FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE =
      "Funding subtype \"%s\" does not match funding type \"%s\".";
  protected static final String FUNDING_START_DATE_NULL_OR_EMPTY =
      "Post funding start date cannot be null or empty";
  protected static final String FUNDING_END_DATE_VALIDATION_MSG =
      "Post funding end date must not be equal to or before start date if included.";
  protected static final String ERROR_INVALID_FUNDING_REASON =
      "Funding reason could not be found for the name \"%s\".";
  protected static final String UPDATE_FAILED = "Update failed.";

  private static final org.slf4j.Logger logger = getLogger(
      PostFundingUpdateTransformerService.class);

  private final Map<String, UUID> fundingReasonToIdMap = new HashMap<>();
  private Clock clock = Clock.systemDefaultZone();

  @Autowired
  private ReferenceServiceImpl referenceService;
  @Autowired
  private TcsServiceImpl tcsService;

  /**
   * Validates and applies valid updates to existing post funding records. Problems are added to the
   * XLS row.
   *
   * @param postFundingUpdateRows List of Spreadsheet rows containing updates for post funding
   *                              records.
   */
  public void processRows(List<PostFundingUpdateRow> postFundingUpdateRows) {
    Set<String> fundingBodies = new HashSet<>();
    Set<String> fundingSubTypeLabels = new HashSet<>();
    Set<String> fundingTypeLabels = new HashSet<>();
    postFundingUpdateRows.forEach(xls -> {
      xls.initialiseSuccessfullyImported();
      Optional.ofNullable(xls.getFundingBody()).ifPresent(fundingBodies::add);
      Optional.ofNullable(xls.getFundingType()).ifPresent(fundingTypeLabels::add);
      Optional.ofNullable(xls.getFundingSubtype()).ifPresent(fundingSubTypeLabels::add);
    });

    // Get all funding bodies and retrieve matching funding body IDs.
    Map<String, String> fundingBodyNameToId =
        referenceService.findCurrentTrustsByTrustKnownAsIn(fundingBodies).stream()
            .collect(
                Collectors.toMap(TrustDTO::getTrustKnownAs, dto -> String.valueOf(dto.getId())));

    // Get all fundingSubtype and retrieve matching fundingSubtype IDs.
    // As fundingSubtype label is not unique for all fundingSubtypes,
    // use (fundingType label, fundingSubtype label) from reference service as key.
    Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId =
        referenceService.findCurrentFundingSubTypesByLabels(fundingSubTypeLabels).stream()
            .collect(Collectors.toMap(
                dto -> ImmutablePair.of(dto.getFundingType().getLabel().toLowerCase(),
                    dto.getLabel().toLowerCase()),
                FundingSubTypeDto::getId));
    Map<String, List<FundingSubTypeDto>> fundingTypeToSubtypes =
        new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    referenceService.findCurrentFundingTypesByLabelIn(fundingTypeLabels)
        .forEach(t -> {
          List<FundingSubTypeDto> subtypes =
              referenceService.findCurrentFundingSubTypesForFundingTypeId(t.getId());
          fundingTypeToSubtypes.put(t.getLabel(), subtypes);
        });

    for (PostFundingUpdateRow postFundingUpdateRow : postFundingUpdateRows) {
      useMatchingCriteriaToUpdatePostFunding(postFundingUpdateRow, fundingBodyNameToId,
          fundingSubTypeLabelToId, fundingTypeToSubtypes);
    }
  }

  /**
   * Verify postFundingId and get postFundingDto by postFundingId.
   *
   * @param postFundingUpdateRow    The PostFundingUpdateRow to be verified.
   * @param fundingBodyNameToId     A map which contains all the fundingBodies got from reference
   *                                service.
   * @param fundingSubTypeLabelToId A mapping of (fundingType, fundingSubType) to fundingSubType
   *                                UUID.
   * @param fundingTypeToSubtypes   A map containing subtypes for each funding type.
   */
  private void useMatchingCriteriaToUpdatePostFunding(PostFundingUpdateRow postFundingUpdateRow,
      Map<String, String> fundingBodyNameToId,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId,
      Map<String, List<FundingSubTypeDto>> fundingTypeToSubtypes) {

    String postFundingId = postFundingUpdateRow.getPostFundingTisId();

    if (StringUtils.isNotEmpty(postFundingId)) {
      try {
        PostFundingDTO postFundingDto = tcsService.getPostFundingById(Long.valueOf(postFundingId));
        if (postFundingDto == null) {
          postFundingUpdateRow
              .addErrorMessage(String.format(DID_NOT_FIND_POST_FUNDING_FOR_ID, postFundingId));
        } else if (postFundingDto.getPostId().toString()
            .equals(postFundingUpdateRow.getPostTisId())) {
          validateAndUpdatePostFundingDto(postFundingUpdateRow, postFundingDto, fundingBodyNameToId,
              fundingSubTypeLabelToId, fundingTypeToSubtypes);
        } else {
          postFundingUpdateRow.addErrorMessage(
              String.format(POST_FUNDING_ID_AND_POST_ID_NOT_MATCHING,
                  postFundingUpdateRow.getPostTisId()));
        }
      } catch (ResourceAccessException | NumberFormatException e) {
        logger.warn("Unable to find post funding record for id: [{}]", postFundingId, e);
        postFundingUpdateRow
            .addErrorMessage(String.format(DID_NOT_FIND_POST_FUNDING_FOR_ID, postFundingId));
      }
    }
  }

  /**
   * validate postFundingDto and update entity in database.
   *
   * @param postFundingUpdateRow    The PostFundingUpdateRow to be verified.
   * @param postFundingDto          The PostFundingDTO got from tcs service. and is also used to
   *                                update the entity in database.
   * @param fundingBodyNameToId     A map which contains all the fundingBodies got from reference
   *                                service.
   * @param fundingSubTypeLabelToId A mapping of (fundingType, fundingSubType) to fundingSubType
   *                                UUID.
   * @param fundingTypeToSubtypes   A map containing subtypes for each funding type.
   */
  private void validateAndUpdatePostFundingDto(
      PostFundingUpdateRow postFundingUpdateRow,
      PostFundingDTO postFundingDto,
      Map<String, String> fundingBodyNameToId,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId,
      Map<String, List<FundingSubTypeDto>> fundingTypeToSubtypes) {

    validateAndUpdateFundingBody(postFundingUpdateRow, postFundingDto, fundingBodyNameToId);

    validateAndUpdateFundingDetails(postFundingUpdateRow, postFundingDto);

    validateAndUpdateSubType(postFundingUpdateRow, postFundingDto, fundingSubTypeLabelToId);

    validateFundingStartAndEndDate(postFundingUpdateRow, postFundingDto);

    // Validation of type relies on the target DTO containing the updated dates.
    updateFundingType(postFundingUpdateRow, postFundingDto, fundingTypeToSubtypes);

    validateAndCacheFundingReasons(postFundingUpdateRow, postFundingDto);

    if (!postFundingUpdateRow.hasErrors()) {
      logger.info("postFundingDto => {}", postFundingDto);
      try {
        PostFundingDTO returnedPostFundingDto = tcsService.updateFunding(postFundingDto);
        List<String> errorMessages = returnedPostFundingDto.getMessageList();

        if (errorMessages.isEmpty()) {
          postFundingUpdateRow.setSuccessfullyImported(true);
        } else {
          postFundingUpdateRow.addErrorMessages(errorMessages);
        }
      } catch (ResourceAccessException e) {
        postFundingUpdateRow
            .addErrorMessage(UPDATE_FAILED);
      }
    }
  }

  private void validateAndUpdateFundingBody(PostFundingUpdateRow postFundingUpdateRow,
      PostFundingDTO postFundingDto,
      Map<String, String> fundingBodyNameToId) {
    final String fundingBodyName = postFundingUpdateRow.getFundingBody();
    final String fundingBodyId = fundingBodyNameToId.get(fundingBodyName);

    if (StringUtils.isNotEmpty(fundingBodyName) && StringUtils.isEmpty(fundingBodyId)) {
      postFundingUpdateRow
          .addErrorMessage(String.format(ERROR_INVALID_FUNDING_BODY_NAME, fundingBodyName));
    } else if (fundingBodyName != null) {
      postFundingDto.setFundingBodyId(fundingBodyId);
    }
  }

  private void updateFundingType(PostFundingUpdateRow postFundingUpdateRow,
      PostFundingDTO postFundingDto, Map<String, List<FundingSubTypeDto>> fundingTypeToSubtypes) {
    final String fundingType = postFundingUpdateRow.getFundingType();
    if (StringUtils.isNotEmpty(fundingType)) {
      /*
      Rather than using "FundingStatus = CURRENT", the requirement is a subtype is required when:
      - The funding has not expired (i.e. the end date is null or no earlier than today)
      - A funding type has been provided
      - A funding subtype has not been provided
      - The funding type has subtypes (i.e. the list of subtypes for the funding type is not empty)
        - A non-existent funding type is treated as having no subtypes
       */
      boolean expired = postFundingDto.getEndDate() != null
          && postFundingDto.getEndDate().isBefore(LocalDate.now(clock));
      if (!expired
          && postFundingUpdateRow.getFundingSubtype() == null
          && CollectionUtils.isNotEmpty(fundingTypeToSubtypes.get(fundingType))) {
        postFundingUpdateRow.addErrorMessage(FUNDING_TYPE_REQUIRES_SUBTYPE);
      } else {
        postFundingDto.setFundingType(fundingType);
      }
    }
  }

  private void validateAndUpdateFundingDetails(PostFundingUpdateRow postFundingUpdateRow,
      PostFundingDTO postFundingDto) {
    final String fundingType = postFundingUpdateRow.getFundingType();
    final String fundingDetails = postFundingUpdateRow.getFundingTypeOther();

    if (StringUtils.isEmpty(fundingType)) {
      if (StringUtils.isNotEmpty(fundingDetails)) {
        postFundingUpdateRow.addErrorMessage(FUNDING_TYPE_IS_REQUIRED_FOR_DETAILS);
      }
      return;
    }
    postFundingDto.setInfo(fundingDetails);
  }

  private void validateAndUpdateSubType(PostFundingUpdateRow postFundingUpdateRow,
      PostFundingDTO postFundingDto,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId) {
    if (StringUtils.isNotEmpty(postFundingUpdateRow.getFundingSubtype())) {
      final UUID fundingSubTypeId = checkAndGetFundingSubtypeId(postFundingUpdateRow,
          fundingSubTypeLabelToId);
      postFundingDto.setFundingSubTypeId(fundingSubTypeId);
    } else {
      postFundingDto.setFundingSubTypeId(null);
    }
  }

  private UUID checkAndGetFundingSubtypeId(PostFundingUpdateRow postFundingUpdateRow,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId) {
    final String fundingSubtype = postFundingUpdateRow.getFundingSubtype();
    final String fundingType = postFundingUpdateRow.getFundingType();
    UUID fundingSubtypeId = null;

    if (StringUtils.isNotEmpty(fundingSubtype)) {
      if (StringUtils.isEmpty(fundingType)) {
        postFundingUpdateRow
            .addErrorMessage(FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE);
      } else {
        fundingSubtypeId = fundingSubTypeLabelToId.get(
            ImmutablePair.of(fundingType.toLowerCase(), fundingSubtype.toLowerCase()));
        if (fundingSubtypeId == null) {
          postFundingUpdateRow.addErrorMessage(
              String.format(FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE, fundingSubtype, fundingType));
        }
      }
    }
    return fundingSubtypeId;
  }

  private void validateFundingStartAndEndDate(PostFundingUpdateRow postFundingUpdateRow,
      PostFundingDTO postFundingDto) {
    LocalDate dateFrom = null;
    if (postFundingUpdateRow.getDateFrom() == null && postFundingUpdateRow.getDateTo() == null) {
      return;
    }

    if (postFundingUpdateRow.getDateFrom() != null) {
      dateFrom = convertDate(postFundingUpdateRow.getDateFrom());
      if (dateFrom == null) {
        postFundingUpdateRow.addErrorMessage(String.format(FUNDING_START_DATE_NULL_OR_EMPTY));
      } else {
        postFundingDto.setStartDate(dateFrom);
      }
    }

    if (postFundingUpdateRow.getDateTo() != null && postFundingUpdateRow.getDateFrom() != null) {
      LocalDate dateTo = convertDate(postFundingUpdateRow.getDateTo());
      if (dateTo != null && dateFrom != null && dateTo.isAfter(dateFrom)) {
        postFundingDto.setEndDate(dateTo);
      } else {
        postFundingUpdateRow.addErrorMessage(FUNDING_END_DATE_VALIDATION_MSG);
      }
    }
  }

  private void validateAndCacheFundingReasons(PostFundingUpdateRow postFundingUpdateRow,
      PostFundingDTO postFundingDto) {
    String fundingReason = postFundingUpdateRow.getFundingReason();
    updateFundingReasonCache(fundingReason);

    if (fundingReason != null && !fundingReasonToIdMap.containsKey(fundingReason)) {
      postFundingUpdateRow.addErrorMessage(
          String.format(ERROR_INVALID_FUNDING_REASON, fundingReason));
      postFundingDto.setFundingReasonId(null);
    } else {
      postFundingDto.setFundingReasonId(fundingReasonToIdMap.get(fundingReason));
    }
  }

  private void updateFundingReasonCache(String fundingReason) {
    if (!fundingReasonToIdMap.containsKey(fundingReason)) {
      referenceService.findCurrentFundingReasonsByReasonIn(Collections.singleton(fundingReason))
          .forEach(dto -> fundingReasonToIdMap.put(dto.getReason(), dto.getId()));
    }
  }

  void setClock(Clock clock) {
    this.clock = clock;
  }
}
