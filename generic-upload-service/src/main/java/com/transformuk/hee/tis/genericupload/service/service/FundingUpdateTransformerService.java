package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.FundingUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.FundingTypeDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Component
public class FundingUpdateTransformerService {

  protected static final String DID_NOT_FIND_POST_FUNDING_FOR_ID =
      "Did not find the postFunding for id \"%s\".";
  protected static final String ERROR_INVALID_FUNDING_BODY_NAME =
      "Funding body could not be found for the name \"%s\".";
  protected static final String ERROR_INVALID_FUNDING_TYPE =
      "Funding type could not be found for the label \"%s\".";
  protected static final String FUNDING_TYPE_IS_NOT_OTHER_OR_ACADEMIC =
      "funding type specified filled although type is neither Other nor an academic type.";
  protected static final String FUNDING_TYPE_IS_REQUIRED_FOR_DETAILS =
      "Funding type is required when funding details is filled.";
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

    Set<String> fundingTypeLabels = fundingUpdateXlSs.stream()
        .map(FundingUpdateXLS::getFundingType).collect(Collectors.toSet());
    List<FundingTypeDTO> fundingTypeDtos = referenceService.findCurrentFundingTypesByLabelIn(
        fundingTypeLabels);

    for (FundingUpdateXLS fundingUpdateXlS : fundingUpdateXlSs) {
      useMatchingCriteriaToUpdatePostFunding(fundingUpdateXlS, fundingBodyNameToId,
          fundingTypeDtos);
    }
  }

  /**
   * Verify postFundingId and get postFundingDto by postFundingId.
   *
   * @param fundingUpdateXls    The FundingUpdateXLS to be verified.
   * @param fundingBodyNameToId A map which contains all the fundingBodies got from reference
   *                            service.
   * @param fundingTypeDtos     A list which contains all the fundingTypeDTOs got from reference
   *                            service.
   */
  private void useMatchingCriteriaToUpdatePostFunding(
      FundingUpdateXLS fundingUpdateXls,
      Map<String, String> fundingBodyNameToId,
      List<FundingTypeDTO> fundingTypeDtos) {

    String postFundingId = fundingUpdateXls.getPostFundingTisId();
    if (!StringUtils.isEmpty(postFundingId)) {
      try {
        PostFundingDTO postFundingDto = tcsService.getPostFundingById(Long.valueOf(postFundingId));
        if (postFundingDto != null) {
          updatePostFundingDto(fundingUpdateXls, postFundingDto, fundingBodyNameToId,
              fundingTypeDtos);
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
   * Verify fundingBodyName and update entity in database.
   *
   * @param fundingUpdateXls    The FundingUpdateXLS to be verified.
   * @param postFundingDto      The PostFundingDTO got from tcs service. and is also used to update
   *                            the entity in database.
   * @param fundingBodyNameToId A map which contains all the fundingBodies got from reference
   *                            service.
   * @param fundingTypeDtos     A list which contains all the fundingTypeDTOs got from reference
   *                            service.
   */
  private void updatePostFundingDto(
      FundingUpdateXLS fundingUpdateXls,
      PostFundingDTO postFundingDto,
      Map<String, String> fundingBodyNameToId,
      List<FundingTypeDTO> fundingTypeDtos) {

    // funding body validation
    String fundingBodyName = fundingUpdateXls.getFundingBody();
    String fundingBodyId = fundingBodyNameToId.get(fundingBodyName);

    if (fundingBodyName != null && fundingBodyId == null) {
      fundingUpdateXls
          .addErrorMessage(String.format(ERROR_INVALID_FUNDING_BODY_NAME, fundingBodyName));
    } else if (fundingBodyName != null) {
      postFundingDto.setFundingBodyId(fundingBodyId);
    }

    updateFundingType(fundingUpdateXls, postFundingDto, fundingTypeDtos);

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
        tcsService.updateFunding(postFundingDto);
        fundingUpdateXls.setSuccessfullyImported(true);
      } catch (ResourceAccessException e) {
        fundingUpdateXls
            .addErrorMessage(UPDATE_FAILED);
      }
    }
  }

  private void updateFundingType(FundingUpdateXLS fundingUpdateXls,
      PostFundingDTO postFundingDto, List<FundingTypeDTO> fundingTypeDtos) {

    boolean fundingTypeInXls = !StringUtils.isEmpty(fundingUpdateXls.getFundingType());
    boolean fundingDetailsInXls = !StringUtils.isEmpty(fundingUpdateXls.getFundingTypeOther());

    if (!fundingTypeInXls && !fundingDetailsInXls) {
      return;
    }

    if (!fundingTypeInXls && fundingDetailsInXls) {
      fundingUpdateXls
          .addErrorMessage(FUNDING_TYPE_IS_REQUIRED_FOR_DETAILS);
      return;
    }

    String fundingType = fundingUpdateXls.getFundingType();
    FundingTypeDTO matchedFundingTypeDto = fundingTypeDtos.stream()
        .filter(dto -> StringUtils.equalsIgnoreCase(fundingType, dto.getLabel())).findAny()
        .orElse(null);
    if (matchedFundingTypeDto == null) {
      fundingUpdateXls
          .addErrorMessage(String.format(ERROR_INVALID_FUNDING_TYPE, fundingType));
    } else {
      // If fundingType is found, replace it with the label from Reference service.
      postFundingDto.setFundingType(matchedFundingTypeDto.getLabel());
      updateFundingDetails(fundingUpdateXls, postFundingDto, matchedFundingTypeDto);
    }
  }

  private void updateFundingDetails(FundingUpdateXLS fundingUpdateXls,
      PostFundingDTO postFundingDto, FundingTypeDTO matchedFundingTypeDto) {
    String fundingDetails = fundingUpdateXls.getFundingTypeOther();
    if (StringUtils.isEmpty(fundingDetails)) {
      return;
    }
    if (matchedFundingTypeDto.isAcademic() || StringUtils.equals(postFundingDto.getFundingType(),
        "Other")) {
      postFundingDto.setInfo(fundingDetails);
    } else {
      fundingUpdateXls
          .addErrorMessage(FUNDING_TYPE_IS_NOT_OTHER_OR_ACADEMIC);
    }
  }
}
