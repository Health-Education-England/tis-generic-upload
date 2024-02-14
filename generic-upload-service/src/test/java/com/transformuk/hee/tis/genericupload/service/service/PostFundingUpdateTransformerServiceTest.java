package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.service.PostFundingUpdateTransformerService.ERROR_FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingUpdateTransformerService.ERROR_FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.PostFundingUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.FundingSubTypeDto;
import com.transformuk.hee.tis.reference.api.dto.FundingTypeDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostFundingUpdateTransformerServiceTest {

  private static final String POST_TIS_ID = "1";
  private static final Long FUNDING_TYPE_ID = 1L;
  private static final String FUNDING_TYPE_LABEL = "fundingType";
  private static final UUID FUNDING_SUBTYPE_UUID = UUID.randomUUID();
  private static final String FUNDING_SUBTYPE_LABEL = "fundingSubtype";
  private static final String ERROR_INVALID_FUNDING_SUB_TYPE = String.format(
      ERROR_FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE, FUNDING_SUBTYPE_LABEL, FUNDING_TYPE_LABEL);
  private FundingTypeDTO fundingTypeDto;
  private FundingSubTypeDto fundingSubTypeDto;

  @InjectMocks
  private PostFundingUpdateTransformerService uploadService;
  @Mock
  private TcsServiceImpl tcsServiceMock;
  @Mock
  private ReferenceServiceImpl referenceServiceMock;

  @Mock
  private PostFundingUpdateXLS postFundingUpdateXls;

  @Captor
  private ArgumentCaptor<PostDTO> postDtoCaptor;

  @BeforeEach
  void setUp() {
    when(postFundingUpdateXls.getPostTisId()).thenReturn(POST_TIS_ID);

    fundingTypeDto = new FundingTypeDTO();
    fundingTypeDto.setId(FUNDING_TYPE_ID);
    fundingTypeDto.setLabel(FUNDING_TYPE_LABEL);

    fundingSubTypeDto = new FundingSubTypeDto();
    fundingSubTypeDto.setId(FUNDING_SUBTYPE_UUID);
    fundingSubTypeDto.setLabel(FUNDING_SUBTYPE_LABEL);
    fundingSubTypeDto.setFundingType(fundingTypeDto);
  }

  @Test
  void shouldSetFundingSubTypeIdWhenLabelIsFound() {
    when(postFundingUpdateXls.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingUpdateXls.getFundingSubtype()).thenReturn(FUNDING_SUBTYPE_LABEL);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    uploadService.processPostFundingUpdateUpload(
        Collections.singletonList(postFundingUpdateXls));

    verify(tcsServiceMock).updatePostFundings(postDtoCaptor.capture());
    verify(postFundingUpdateXls, never()).addErrorMessage(anyString());
    PostDTO postDto = postDtoCaptor.getValue();
    assertEquals(1, postDto.getFundings().size());
    PostFundingDTO postFundingDto = postDto.getFundings().iterator().next();
    assertEquals(postFundingDto.getFundingSubTypeId(), FUNDING_SUBTYPE_UUID);
  }

  @Test
  void shouldSetFundingSubTypeIdCaseInsensitively() {
    final String fundingSubtypeLabelInUpperCase = FUNDING_SUBTYPE_LABEL.toUpperCase();
    when(postFundingUpdateXls.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingUpdateXls.getFundingSubtype()).thenReturn(fundingSubtypeLabelInUpperCase);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(fundingSubtypeLabelInUpperCase)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    uploadService.processPostFundingUpdateUpload(
        Collections.singletonList(postFundingUpdateXls));

    verify(tcsServiceMock).updatePostFundings(postDtoCaptor.capture());
    verify(postFundingUpdateXls, never()).addErrorMessage(anyString());
    PostDTO postDto = postDtoCaptor.getValue();
    assertEquals(1, postDto.getFundings().size());
    PostFundingDTO postFundingDto = postDto.getFundings().iterator().next();
    assertEquals(postFundingDto.getFundingSubTypeId(), FUNDING_SUBTYPE_UUID);
  }

  @Test
  void shouldAddErrorWhenLabelNotFound() {
    when(postFundingUpdateXls.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingUpdateXls.getFundingSubtype()).thenReturn(FUNDING_SUBTYPE_LABEL);
    when(postFundingUpdateXls.getErrorMessage()).thenReturn(ERROR_INVALID_FUNDING_SUB_TYPE);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE_LABEL)))
        .thenReturn(Collections.emptyList());

    uploadService.processPostFundingUpdateUpload(
        Collections.singletonList(postFundingUpdateXls));

    verify(tcsServiceMock, never()).updatePostFundings(any());
    verify(postFundingUpdateXls).addErrorMessage(ERROR_INVALID_FUNDING_SUB_TYPE);
  }

  @Test
  void shouldAddErrorWhenFundingTypeIsNotSpecifiedForFundingSubType() {
    when(postFundingUpdateXls.getFundingType()).thenReturn(null);
    when(postFundingUpdateXls.getFundingSubtype()).thenReturn(FUNDING_SUBTYPE_LABEL);
    when(postFundingUpdateXls.getErrorMessage()).thenReturn(
        ERROR_FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    uploadService.processPostFundingUpdateUpload(
        Collections.singletonList(postFundingUpdateXls));

    verify(tcsServiceMock, never()).updatePostFundings(any());
    verify(postFundingUpdateXls).addErrorMessage(ERROR_FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE);
  }
}
