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
  protected static final String INVALID_FUNDING_SUB_TYPE_LABEL =
      "Funding subtype could not be found for the label \"%s\".";

  protected static final String UPDATE_FAILED = "Update failed.";
  private static final org.slf4j.Logger logger = getLogger(PostUpdateTransformerService.class);

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
          fundingSubTypeLabelToId);
    }
  }

  /**
   * Verify postFundingId and get postFundingDto by postFundingId.
   *
   * @param fundingUpdateXls    The FundingUpdateXLS to be verified.
   * @param fundingBodyNameToId A map which contains all the fundingBodies got from reference
   *                            service.
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
   * @param fundingUpdateXls    The FundingUpdateXLS to be verified.
   * @param postFundingDto      The PostFundingDTO got from tcs service. and is also used to update
   *                            the entity in database.
   * @param fundingBodyNameToId A map which contains all the fundingBodies got from reference
   *                            service.
   */
  private void validateAndUpdatePostFundingDto(
      FundingUpdateXLS fundingUpdateXls,
      PostFundingDTO postFundingDto,
      Map<String, String> fundingBodyNameToId,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId) {

    // funding body validation
    final String fundingBodyName = fundingUpdateXls.getFundingBody();
    final String fundingBodyId = fundingBodyNameToId.get(fundingBodyName);
    final String fundingType = fundingUpdateXls.getFundingType();
    final String fundingSubtype = fundingUpdateXls.getFundingSubtype();

    if (fundingBodyName != null && fundingBodyId == null) {
      fundingUpdateXls
          .addErrorMessage(String.format(ERROR_INVALID_FUNDING_BODY_NAME, fundingBodyName));
    } else if (fundingBodyName != null) {
      postFundingDto.setFundingBodyId(fundingBodyId);
    }

    postFundingDto.setInfo(fundingUpdateXls.getFundingTypeOther());

    if (fundingType != null) {
      postFundingDto.setFundingType(fundingType);
    }

    if (postFundingDto.getInfo() != null && postFundingDto.getFundingType() == null) {
      fundingUpdateXls
          .addErrorMessage(FUNDING_TYPE_IS_REQUIRED_FOR_DETAILS);
    }

    if (fundingSubtype != null) {
      final UUID fundingSubTypeId = checkAndGetFundingSubtype(fundingUpdateXls,
          postFundingDto.getFundingType(), fundingSubTypeLabelToId);
      postFundingDto.setFundingSubTypeId(fundingSubTypeId);
    } else {
      postFundingDto.setFundingSubTypeId(null);
    }

    if (fundingUpdateXls.getDateFrom() != null) {
      LocalDate dateFrom = convertDate(fundingUpdateXls.getDateFrom());
      postFundingDto.setStartDate(dateFrom);
    }
    if (fundingUpdateXls.getDateTo() != null) {
      LocalDate dateTo = convertDate(fundingUpdateXls.getDateTo());
      postFundingDto.setEndDate(dateTo);
    }

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

  private UUID checkAndGetFundingSubtype(FundingUpdateXLS fundingUpdateXls,
      String fundingType,
      Map<ImmutablePair<String, String>, UUID> fundingSubTypeLabelToId) {
    String fundingSubtype = fundingUpdateXls.getFundingSubtype();
    UUID fundingSubtypeId = null;

    if (fundingSubtype != null) {
      if (fundingType == null) {
        fundingUpdateXls
            .addErrorMessage(FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE);
      } else {
        fundingSubtypeId = fundingSubTypeLabelToId.get(
            ImmutablePair.of(fundingType.toLowerCase(), fundingSubtype.toLowerCase()));
        if (fundingSubtypeId == null) {
          fundingUpdateXls
              .addErrorMessage(String.format(INVALID_FUNDING_SUB_TYPE_LABEL, fundingSubtype));
        }
      }
    }
    return fundingSubtypeId;
  }
}
