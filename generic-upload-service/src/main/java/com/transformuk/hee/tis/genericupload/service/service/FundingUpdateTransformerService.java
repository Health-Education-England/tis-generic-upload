package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.FundingUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.FundingTypeDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class FundingUpdateTransformerService {

  private static final org.slf4j.Logger logger = getLogger(PostUpdateTransformerService.class);

  private static final String DID_NOT_FIND_POST_FUNDING_FOR_ID = "Did not find the postFunding for id \"%s\".";
  private static final String ERROR_INVALID_FUNDING_BODY_NAME = "Funding body could not be found for the name \"%s\".";
  private static final String ERROR_INVALID_FUNDING_TYPE = "Funding type could not be found for the label \"%s\".";
  private static final String FUNDING_TYPE_IS_NOT_OTHER = "Funding type specified filled although type is not Other.";
  private static final String UPDATE_FAILED = "Update failed.";

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
    List<FundingTypeDTO> fundingTypes = referenceService.findCurrentFundingTypesByLabelIn(fundingTypeLabels);
    Set<String> fundingTypeLableSet = fundingTypes.stream().map(dto -> dto.getLabel()).collect(Collectors.toSet());

    for (FundingUpdateXLS fundingUpdateXlS : fundingUpdateXlSs) {
      useMatchingCriteriaToUpdatePostFunding(fundingUpdateXlS, fundingBodyNameToId, fundingTypeLableSet);
    }
  }

  /**
   * Verify postFundingId and get postFundingDTO by postFundingId.
   *
   * @param fundingUpdateXLS    The FundingUpdateXLS to be verified.
   * @param fundingBodyNameToId A map which contains all the fundingBodies got from reference service.
   */
  private void useMatchingCriteriaToUpdatePostFunding(
      FundingUpdateXLS fundingUpdateXLS,
      Map<String, String> fundingBodyNameToId,
      Set<String> fundingTypeLableSet) {

    String postFundingId = fundingUpdateXLS.getPostFundingTisId();
    if (!StringUtils.isEmpty(postFundingId)) {
      try {
        PostFundingDTO postFundingDTO = tcsService.getPostFundingById(Long.valueOf(postFundingId));
        if (postFundingDTO != null) {
          updatePostFundingDto(fundingUpdateXLS, postFundingDTO, fundingBodyNameToId, fundingTypeLableSet);
        } else {
          fundingUpdateXLS
              .addErrorMessage(String.format(DID_NOT_FIND_POST_FUNDING_FOR_ID, postFundingId));
        }
      } catch (ResourceAccessException e) {
        fundingUpdateXLS
            .addErrorMessage(String.format(DID_NOT_FIND_POST_FUNDING_FOR_ID, postFundingId));
      } catch (NumberFormatException e) {
        fundingUpdateXLS
            .addErrorMessage(String.format(DID_NOT_FIND_POST_FUNDING_FOR_ID, postFundingId));
      }
    }
  }

  /**
   * Verify fundingBodyName and update entity in database.
   *
   * @param fundingUpdateXLS    The FundingUpdateXLS to be verified.
   * @param postFundingDTO      The PostFundingDTO got from tcs service.
   *                            and is also used to update the entity in database.
   * @param fundingBodyNameToId A map which contains all the fundingBodies got from reference service.
   * @param fundingTypeLabelSet A set which contains all the fundingTypelable got from reference service.
   */
  private void updatePostFundingDto(
      FundingUpdateXLS fundingUpdateXLS,
      PostFundingDTO postFundingDTO,
      Map<String, String> fundingBodyNameToId,
      Set<String> fundingTypeLabelSet) {

    // funding body validation
    String fundingBodyName = fundingUpdateXLS.getFundingBody();
    String fundingBodyId = fundingBodyNameToId.get(fundingBodyName);

    if (fundingBodyName != null && fundingBodyId == null) {
      fundingUpdateXLS
          .addErrorMessage(String.format(ERROR_INVALID_FUNDING_BODY_NAME, fundingBodyName));
    } else if (fundingBodyName != null) {
      postFundingDTO.setFundingBodyId(fundingBodyId);
    }

    // funding type validation
    String fundingType = fundingUpdateXLS.getFundingType();
    boolean fundingTypeOtherFlag = false;
    if (!StringUtils.isEmpty(fundingType)) {
      if (Objects.equals(fundingType, "Other")) {
        fundingTypeOtherFlag = true;
        postFundingDTO.setFundingType(fundingType);
      } else {
        if (fundingTypeLabelSet.contains(fundingType)) {
          postFundingDTO.setFundingType(fundingType);
          postFundingDTO.setInfo(null);
        } else {
          fundingUpdateXLS
              .addErrorMessage(String.format(ERROR_INVALID_FUNDING_TYPE, fundingType));
        }
      }
    }

    // funding type-other validation
    String fundingTypeOther = fundingUpdateXLS.getFundingTypeOther();
    if (fundingTypeOtherFlag && !StringUtils.isEmpty(fundingTypeOther)) {
      postFundingDTO.setInfo(fundingTypeOther);
    } else if (!fundingTypeOtherFlag && !StringUtils.isEmpty(fundingTypeOther)) {
      fundingUpdateXLS
          .addErrorMessage(FUNDING_TYPE_IS_NOT_OTHER);
    }

    if (fundingUpdateXLS.getDateFrom() != null) {
      LocalDate dateFrom = convertDate(fundingUpdateXLS.getDateFrom());
      postFundingDTO.setStartDate(dateFrom);
    }
    if (fundingUpdateXLS.getDateTo() != null) {
      LocalDate dateTo = convertDate(fundingUpdateXLS.getDateTo());
      postFundingDTO.setEndDate(dateTo);
    }

    if (!fundingUpdateXLS.hasErrors()) {
      logger.info("postFundingDTO => {}", postFundingDTO);
      try {
        tcsService.updateFunding(postFundingDTO);
        fundingUpdateXLS.setSuccessfullyImported(true);
      } catch (ResourceAccessException e) {
        fundingUpdateXLS
            .addErrorMessage(UPDATE_FAILED);
      }
    }
  }
}
