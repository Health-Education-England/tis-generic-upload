package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PostFundingUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.FundingSubTypeDto;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class PostFundingUpdateTransformerService {

  protected static final String ERROR_INVALID_FUNDING_BODY_NAME =
      "Funding body could not be found for the name \"%s\".";
  protected static final String ERROR_FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE =
      "Funding type is required when funding sub type is filled.";
  protected static final String ERROR_INVALID_FUNDING_SUB_TYPE_LABEL =
      "Funding body could not be found for the label \"%s\".";

  @Autowired
  private ReferenceServiceImpl referenceService;
  @Autowired
  private TcsServiceImpl tcsService;

  public void processPostFundingUpdateUpload(List<PostFundingUpdateXLS> postFundingUpdateXlss) {
    postFundingUpdateXlss.forEach(PostFundingUpdateXLS::initialiseSuccessfullyImported);

    // Get all funding bodies and retrieve matching funding body IDs.
    Set<String> fundingBodies = postFundingUpdateXlss.stream()
        .map(PostFundingUpdateXLS::getFundingBody).collect(Collectors.toSet());
    List<TrustDTO> trusts = referenceService.findCurrentTrustsByTrustKnownAsIn(fundingBodies);
    Map<String, String> fundingBodyNameToId = trusts.stream()
        .collect(Collectors.toMap(TrustDTO::getTrustKnownAs, dto -> String.valueOf(dto.getId())));

    // Get all fundingSubType and retrieve matching fundingSubType IDs.
    Set<String> fundingSubTypeLabels = postFundingUpdateXlss.stream()
        .map(PostFundingUpdateXLS::getFundingSubType).collect(Collectors.toSet());
    List<FundingSubTypeDto> fundingSubTypes =
        referenceService.findCurrentFundingSubTypesByLabels(fundingSubTypeLabels);
    // As fundingSubType label is not unique for all fundingSubTypes,
    // use fundingType label + fundingSubType label from reference service as key.
    Map<String, UUID> fundingSubTypeLabelToId = fundingSubTypes.stream()
        .collect(Collectors.toMap(dto -> (dto.getFundingType().getLabel() + dto.getLabel()),
            FundingSubTypeDto::getId));

    // Group rows by post ID.
    // TODO: There is an issue with validating the presence of required fields, this can be
    //  simplified again once that scenario is handled properly in FileValidator.
    Map<String, List<PostFundingUpdateXLS>> postIdsToPostFundingUpdateXls = new HashMap<>();
    // Map<String, List<PostFundingUpdateXLS>> postIdsToPostFundingUpdateXls = postFundingUpdateXlss
    //     .stream()
    //     .filter(xls -> xls.getPostTisId() != null)
    //     .collect(Collectors.groupingBy(PostFundingUpdateXLS::getPostTisId));

    for (PostFundingUpdateXLS postFundingUpdateXls : postFundingUpdateXlss) {
      String postId = postFundingUpdateXls.getPostTisId();

      if (postId != null) {
        List<PostFundingUpdateXLS> groupedXls = postIdsToPostFundingUpdateXls
            .getOrDefault(postId, new ArrayList<>());

        groupedXls.add(postFundingUpdateXls);
        postIdsToPostFundingUpdateXls.put(postId, groupedXls);
      } else {
        postFundingUpdateXls.addErrorMessage("TIS_Post_ID is a required field.");
      }
    }

    for (Entry<String, List<PostFundingUpdateXLS>> postIdToPostFundingUpdateXls : postIdsToPostFundingUpdateXls
        .entrySet()) {
      String postId = postIdToPostFundingUpdateXls.getKey();

      Map<PostFundingDTO, PostFundingUpdateXLS> fundingDtosToSource = buildFundingDtos(
          postIdToPostFundingUpdateXls.getValue(), fundingBodyNameToId, fundingSubTypeLabelToId);
      PostDTO postDto = new PostDTO();
      postDto.setId(Long.parseLong(postId));
      postDto.setFundings(fundingDtosToSource.keySet());

      try {
        List<PostFundingDTO> postFundingDtos = tcsService.updatePostFundings(postDto);

        for (PostFundingDTO fundingDto : postFundingDtos) {
          List<String> errorMessages = fundingDto.getMessageList();

          // Get the source XLS for the DTO and add error messages or success.
          fundingDto.setMessageList(new ArrayList<>());
          PostFundingUpdateXLS postFundingUpdateXsl = fundingDtosToSource.get(fundingDto);

          if (errorMessages.isEmpty()) {
            postFundingUpdateXsl.setSuccessfullyImported(true);
          } else {
            postFundingUpdateXsl.addErrorMessages(errorMessages);
          }
        }
      } catch (RestClientException e) {
        for (PostFundingUpdateXLS postFundingUpdateXls : postIdToPostFundingUpdateXls.getValue()) {
          postFundingUpdateXls.addErrorMessage(e.getMessage());
        }
      }
    }
  }

  /**
   * Build PostFundingDTOs from the PostFundingUpdateXLS.
   *
   * @param postFundingUpdateXlss   The PostFundingUpdateXLS to build DTOs for.
   * @param fundingBodyNameToId     A mapping of funding body names to IDs, as required by the DTO.
   * @param fundingSubTypeLabelToId A mapping of fundingType+fundingSubType to fundingSubType UUID.
   * @return A map of built PostFundingDTOs to source PostFundingUpdateXLS.
   */
  private Map<PostFundingDTO, PostFundingUpdateXLS> buildFundingDtos(
      Collection<PostFundingUpdateXLS> postFundingUpdateXlss,
      Map<String, String> fundingBodyNameToId,
      Map<String, UUID> fundingSubTypeLabelToId) {
    Map<PostFundingDTO, PostFundingUpdateXLS> postFundingDtosToSource = new HashMap<>();

    for (PostFundingUpdateXLS postFundingUpdateXls : postFundingUpdateXlss) {
      String fundingBodyName = postFundingUpdateXls.getFundingBody();
      String fundingBodyId = fundingBodyNameToId.get(fundingBodyName);

      if (fundingBodyName != null && fundingBodyId == null) {
        postFundingUpdateXls
            .addErrorMessage(String.format(ERROR_INVALID_FUNDING_BODY_NAME, fundingBodyName));
        continue;
      }

      UUID fundingSubTypeId = checkAndGetFundingSubtype(postFundingUpdateXls,
          fundingSubTypeLabelToId);
      if (StringUtils.isNotEmpty(postFundingUpdateXls.getErrorMessage())) {
        continue;
      }

      PostFundingDTO postFundingDto = new PostFundingDTO();
      postFundingDto.setFundingType(postFundingUpdateXls.getFundingType());
      postFundingDto.setInfo(postFundingUpdateXls.getFundingTypeOther());
      postFundingDto.setStartDate(postFundingUpdateXls.getDateFrom());
      postFundingDto.setEndDate(postFundingUpdateXls.getDateTo());
      postFundingDto.setFundingBodyId(fundingBodyId);
      postFundingDto.setFundingSubTypeId(fundingSubTypeId);

      postFundingDtosToSource.put(postFundingDto, postFundingUpdateXls);
    }
    return postFundingDtosToSource;
  }

  private UUID checkAndGetFundingSubtype(PostFundingUpdateXLS postFundingUpdateXls,
      Map<String, UUID> fundingSubTypeLabelToId) {
    String fundingType = postFundingUpdateXls.getFundingType();
    String fundingSubType = postFundingUpdateXls.getFundingSubType();

    if (fundingType == null && fundingSubType != null) {
      postFundingUpdateXls
          .addErrorMessage(ERROR_FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE);
      return null;
    }
    UUID fundingSubTypeId = fundingSubTypeLabelToId.get(fundingType + fundingSubType);

    if (fundingSubType != null && fundingSubTypeId == null) {
      postFundingUpdateXls
          .addErrorMessage(String.format(ERROR_INVALID_FUNDING_SUB_TYPE_LABEL, fundingSubType));
      return null;
    }
    return fundingSubTypeId;
  }
}
