package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.FundingUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.FundingSubTypeDto;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Component
public class FundingUpdateTransformerService {

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
  private static final org.slf4j.Logger logger = getLogger(PostUpdateTransformerService.class);
  private Map<String, UUID> fundingReasonToIdMap = new HashMap<>();

  @Autowired
  private ReferenceServiceImpl referenceService;
  @Autowired
  private TcsServiceImpl tcsService;

  public void processFundingUpdateUpload(List<FundingUpdateXLS> fundingUpdateXlSs) {
    fundingUpdateXlSs.forEach(FundingUpdateXLS::initialiseSuccessfullyImported);

    // Get all funding bodies and retrieve matching funding body IDs.
    Set<String> fundingBodies = fundingUpdateXlSs.stream()
        .map(FundingUpdateXLS::getFundingBody).collect(Collectors.toSet());
    List<TrustDTO> trusts = referenceService.findCurrentTrustsByTrustKnownAsIn(fundingBodies);
    Map<String, String> fundingBodyNameToId = trusts.stream()
        .collect(Collectors.toMap(TrustDTO::getTrustKnownAs, dto -> String.valueOf(dto.getId())));

    // Get all fundingSubtype and retrieve matching fundingSubtype IDs.
    Set<String> fundingSubTypeLabels = fundingUpdateXlSs.stream()
        .map(FundingUpdateXLS::getFundingSubtype).collect(Collectors.toSet());
    List<FundingSubTypeDto> fundingSubTypes =
        referenceService.findCurrentFundingSubTypesByLabels(fundingSubTypeLabels);
    // As fundingSubtype label is not unique for all fundingSubtypes,
    // use (fundingType label, fundingSubtype label) from reference service as key.
    Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId = fundingSubTypes.stream()
        .collect(Collectors.toMap(
            dto -> ImmutablePair.of(dto.getFundingType().getLabel().toLowerCase(),
                dto.getLabel().toLowerCase()),
            FundingSubTypeDto::getId));

    for (FundingUpdateXLS fundingUpdateXlS : fundingUpdateXlSs) {
      useMatchingCriteriaToUpdatePostFunding(fundingUpdateXlS, fundingBodyNameToId,
          fundingSubTypeLabelToId, fundingReasonToIdMap);
    }
  }

  /**
   * Verify postFundingId and get postFundingDto by postFundingId.
   *
   * @param fundingUpdateXls        The FundingUpdateXLS to be verified.
   * @param fundingBodyNameToId     A map which contains all the fundingBodies got from reference
   *                                service.
   * @param fundingSubTypeLabelToId A mapping of (fundingType, fundingSubType) to fundingSubType
   *                                UUID.
   */
  private void useMatchingCriteriaToUpdatePostFunding(
      FundingUpdateXLS fundingUpdateXls,
      Map<String, String> fundingBodyNameToId,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId) {

    String postFundingId = fundingUpdateXls.getPostFundingTisId();

    if (StringUtils.isNotEmpty(postFundingId)) {
      try {
        PostFundingDTO postFundingDto = tcsService.getPostFundingById(Long.valueOf(postFundingId));
        if (postFundingDto != null) {
          if (StringUtils.equals(postFundingDto.getPostId().toString(),
              fundingUpdateXls.getPostTisId())) {
            validateAndUpdatePostFundingDto(fundingUpdateXls, postFundingDto, fundingBodyNameToId,
                fundingSubTypeLabelToId);
          } else {
            fundingUpdateXls
                .addErrorMessage(String.format(POST_FUNDING_ID_AND_POST_ID_NOT_MATCHING,
                    fundingUpdateXls.getPostTisId()));
          }
        } else {
          fundingUpdateXls
              .addErrorMessage(String.format(DID_NOT_FIND_POST_FUNDING_FOR_ID, postFundingId));
        }
      } catch (ResourceAccessException e) {
        fundingUpdateXls
            .addErrorMessage(String.format(DID_NOT_FIND_POST_FUNDING_FOR_ID, postFundingId));
      } catch (NumberFormatException e) {
        fundingUpdateXls
            .addErrorMessage(String.format(DID_NOT_FIND_POST_FUNDING_FOR_ID, postFundingId));
      }
    }
  }

  /**
   * validate postFundingDto and update entity in database.
   *
   * @param fundingUpdateXls        The FundingUpdateXLS to be verified.
   * @param postFundingDto          The PostFundingDTO got from tcs service. and is also used to
   *                                update the entity in database.
   * @param fundingBodyNameToId     A map which contains all the fundingBodies got from reference
   *                                service.
   * @param fundingSubTypeLabelToId A mapping of (fundingType, fundingSubType) to fundingSubType
   *                                UUID.
   */
  private void validateAndUpdatePostFundingDto(
      FundingUpdateXLS fundingUpdateXls,
      PostFundingDTO postFundingDto,
      Map<String, String> fundingBodyNameToId,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId) {

    validateAndUpdateFundingBody(fundingUpdateXls, postFundingDto, fundingBodyNameToId);

    updateFundingType(fundingUpdateXls, postFundingDto);

    validateAndUpdateFundingDetails(fundingUpdateXls, postFundingDto);

    validateAndUpdateSubType(fundingUpdateXls, postFundingDto, fundingSubTypeLabelToId);

    validateFundingStartAndEndDate(fundingUpdateXls, postFundingDto);

    validateAndCacheFundingReasons(fundingUpdateXls, postFundingDto);

    if (!fundingUpdateXls.hasErrors()) {
      logger.info("postFundingDto => {}", postFundingDto);
      try {
        PostFundingDTO returnedPostFundingDto = tcsService.updateFunding(postFundingDto);
        List<String> errorMessages = returnedPostFundingDto.getMessageList();

        if (errorMessages.isEmpty()) {
          fundingUpdateXls.setSuccessfullyImported(true);
        } else {
          fundingUpdateXls.addErrorMessages(errorMessages);
        }
      } catch (ResourceAccessException e) {
        fundingUpdateXls
            .addErrorMessage(UPDATE_FAILED);
      }
    }
  }

  private void validateAndUpdateFundingBody(FundingUpdateXLS fundingUpdateXls,
      PostFundingDTO postFundingDto,
      Map<String, String> fundingBodyNameToId) {
    final String fundingBodyName = fundingUpdateXls.getFundingBody();
    final String fundingBodyId = fundingBodyNameToId.get(fundingBodyName);

    if (StringUtils.isNotEmpty(fundingBodyName) && StringUtils.isEmpty(fundingBodyId)) {
      fundingUpdateXls
          .addErrorMessage(String.format(ERROR_INVALID_FUNDING_BODY_NAME, fundingBodyName));
    } else if (fundingBodyName != null) {
      postFundingDto.setFundingBodyId(fundingBodyId);
    }
  }

  private void updateFundingType(FundingUpdateXLS fundingUpdateXls,
      PostFundingDTO postFundingDto) {
    final String fundingType = fundingUpdateXls.getFundingType();
    if (StringUtils.isNotEmpty(fundingType)) {
      postFundingDto.setFundingType(fundingType);
    }
  }

  private void validateAndUpdateFundingDetails(FundingUpdateXLS fundingUpdateXls,
      PostFundingDTO postFundingDto) {
    final String fundingType = fundingUpdateXls.getFundingType();
    final String fundingDetails = fundingUpdateXls.getFundingTypeOther();

    if (StringUtils.isEmpty(fundingType)) {
      if (StringUtils.isNotEmpty(fundingDetails)) {
        fundingUpdateXls.addErrorMessage(FUNDING_TYPE_IS_REQUIRED_FOR_DETAILS);
      }
      return;
    }
    postFundingDto.setInfo(fundingDetails);
  }

  private void validateAndUpdateSubType(FundingUpdateXLS fundingUpdateXls,
      PostFundingDTO postFundingDto,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId) {
    if (StringUtils.isNotEmpty(fundingUpdateXls.getFundingSubtype())) {
      final UUID fundingSubTypeId = checkAndGetFundingSubtypeId(fundingUpdateXls,
          fundingSubTypeLabelToId);
      postFundingDto.setFundingSubTypeId(fundingSubTypeId);
    } else {
      postFundingDto.setFundingSubTypeId(null);
    }
  }

  private UUID checkAndGetFundingSubtypeId(FundingUpdateXLS fundingUpdateXls,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId) {
    final String fundingSubtype = fundingUpdateXls.getFundingSubtype();
    final String fundingType = fundingUpdateXls.getFundingType();
    UUID fundingSubtypeId = null;

    if (StringUtils.isNotEmpty(fundingSubtype)) {
      if (StringUtils.isEmpty(fundingType)) {
        fundingUpdateXls
            .addErrorMessage(FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE);
      } else {
        fundingSubtypeId = fundingSubTypeLabelToId.get(
            ImmutablePair.of(fundingType.toLowerCase(), fundingSubtype.toLowerCase()));
        if (fundingSubtypeId == null) {
          fundingUpdateXls.addErrorMessage(
              String.format(FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE, fundingSubtype, fundingType));
        }
      }
    }
    return fundingSubtypeId;
  }

  private void validateFundingStartAndEndDate(FundingUpdateXLS fundingUpdateXls,
      PostFundingDTO postFundingDto) {
    LocalDate dateFrom = null;
    LocalDate dateTo = null;
    if (fundingUpdateXls.getDateFrom() == null && fundingUpdateXls.getDateTo() == null) {
      return;
    }

    if (fundingUpdateXls.getDateFrom() != null) {
      dateFrom = convertDate(fundingUpdateXls.getDateFrom());
      if (dateFrom == null) {
        fundingUpdateXls.addErrorMessage(String.format(FUNDING_START_DATE_NULL_OR_EMPTY));
      } else {
        postFundingDto.setStartDate(dateFrom);
      }
    }

    if (fundingUpdateXls.getDateTo() != null && fundingUpdateXls.getDateFrom() != null) {
      dateTo = convertDate(fundingUpdateXls.getDateTo());
      if (dateTo != null && dateFrom != null && dateTo.isAfter(dateFrom)) {
        postFundingDto.setEndDate(dateTo);
      } else {
        fundingUpdateXls.addErrorMessage(FUNDING_END_DATE_VALIDATION_MSG);
      }
    }
  }

  private void validateAndCacheFundingReasons(FundingUpdateXLS fundingUpdateXls,
      PostFundingDTO postFundingDto) {
    String fundingReason = fundingUpdateXls.getFundingReason();
    updateFundingReasonCache(fundingReason);

    if (fundingReason != null && !fundingReasonToIdMap.containsKey(fundingReason)) {
      fundingUpdateXls.addErrorMessage(
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
}
