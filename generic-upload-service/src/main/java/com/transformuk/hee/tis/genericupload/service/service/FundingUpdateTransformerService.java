package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.FundingUpdateXLS;
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
import java.util.Set;
import java.util.stream.Collectors;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class FundingUpdateTransformerService {

  private static final org.slf4j.Logger logger = getLogger(PostUpdateTransformerService.class);

  private static final String DID_NOT_FIND_POST_FUNDING_FOR_ID = "Did not find the postFunding for id \"%s\".";
  private static final String ERROR_INVALID_FUNDING_BODY_NAME = "Funding body could not be found for the name \"%s\".";

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

    for (FundingUpdateXLS fundingUpdateXlS : fundingUpdateXlSs) {
      useMatchingCriteriaToUpdatePostFunding(fundingUpdateXlS, fundingBodyNameToId);
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
      Map<String, String> fundingBodyNameToId) {

    String postFundingId = fundingUpdateXLS.getPostFundingTisId();
    if (!StringUtils.isEmpty(postFundingId)) {
      try {
        PostFundingDTO postFundingDTO = tcsService.getPostFundingById(Long.valueOf(postFundingId));
        if (postFundingDTO != null) {
          updatePostFundingDto(fundingUpdateXLS, postFundingDTO, fundingBodyNameToId);
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
   */
  private void updatePostFundingDto(
      FundingUpdateXLS fundingUpdateXLS,
      PostFundingDTO postFundingDTO,
      Map<String, String> fundingBodyNameToId) {

    String fundingBodyName = fundingUpdateXLS.getFundingBody();
    String fundingBodyId = fundingBodyNameToId.get(fundingBodyName);

    if (fundingBodyName != null && fundingBodyId == null) {
      fundingUpdateXLS
          .addErrorMessage(String.format(ERROR_INVALID_FUNDING_BODY_NAME, fundingBodyName));
    } else if (fundingBodyName != null) {
      postFundingDTO.setFundingBodyId(fundingBodyId);
    }

    String fundingType = fundingUpdateXLS.getFundingType();
    if (!StringUtils.isEmpty(fundingType)) {
      postFundingDTO.setFundingType(fundingType);
    }
    String fundingTypeOther = fundingUpdateXLS.getFundingTypeOther();
    if (!StringUtils.isEmpty(fundingTypeOther)) {
      postFundingDTO.setInfo(fundingTypeOther);
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
      tcsService.updateFunding(postFundingDTO);
      fundingUpdateXLS.setSuccessfullyImported(true);
    }
  }

}
