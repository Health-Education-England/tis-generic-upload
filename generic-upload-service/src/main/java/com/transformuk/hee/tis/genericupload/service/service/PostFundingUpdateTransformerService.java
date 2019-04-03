package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PostFundingUpdateXLS;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class PostFundingUpdateTransformerService {

  @Autowired
  private TcsServiceImpl tcsService;

  public void processPostFundingUpdateUpload(List<PostFundingUpdateXLS> postFundingUpdateXlss) {
    postFundingUpdateXlss.forEach(PostFundingUpdateXLS::initialiseSuccessfullyImported);

    // Group rows by post ID.
    Map<String, List<PostFundingUpdateXLS>> postIdsToPostFundingUpdateXls = postFundingUpdateXlss
        .stream().collect(Collectors.groupingBy(PostFundingUpdateXLS::getPostTisId));

    for (Entry<String, List<PostFundingUpdateXLS>> postIdToPostFundingUpdateXls : postIdsToPostFundingUpdateXls
        .entrySet()) {
      String postId = postIdToPostFundingUpdateXls.getKey();

      Set<PostFundingDTO> fundingDtos = buildFundingDtos(postIdToPostFundingUpdateXls.getValue());
      PostDTO postDto = new PostDTO();
      postDto.setId(Long.parseLong(postId));
      postDto.setFundings(fundingDtos);

      try {
        tcsService.updatePostFundings(postDto);
      } catch (RestClientException e) {
        // TODO: handle error messages properly.
        for (PostFundingUpdateXLS postFundingUpdateXls : postIdToPostFundingUpdateXls.getValue()) {
          postFundingUpdateXls.addErrorMessage(e.getMessage());
        }
      }
    }
  }

  private Set<PostFundingDTO> buildFundingDtos(
      Collection<PostFundingUpdateXLS> postFundingUpdateXlss) {
    Set<PostFundingDTO> postFundingDtos = new HashSet<>();

    for (PostFundingUpdateXLS postFundingUpdateXls : postFundingUpdateXlss) {
      PostFundingDTO postFundingDto = new PostFundingDTO();
      postFundingDto.setFundingType(postFundingUpdateXls.getFundingType());
      postFundingDto.setInfo(postFundingUpdateXls.getFundingTypeOther());
      postFundingDto.setStartDate(postFundingUpdateXls.getDateFrom());
      postFundingDto.setEndDate(postFundingUpdateXls.getDateTo());
      postFundingDto.setFundingBodyId(postFundingUpdateXls.getFundingBody());

      postFundingDtos.add(postFundingDto);
    }

    return postFundingDtos;
  }
}
