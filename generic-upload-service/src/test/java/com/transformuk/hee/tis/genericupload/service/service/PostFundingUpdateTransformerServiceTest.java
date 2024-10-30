package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingUpdateTransformerService.ERROR_FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingUpdateTransformerService.ERROR_FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingUpdateTransformerService.ERROR_INVALID_FUNDING_REASON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.PostFundingUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.FundingReasonDto;
import com.transformuk.hee.tis.reference.api.dto.FundingSubTypeDto;
import com.transformuk.hee.tis.reference.api.dto.FundingTypeDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
  private static final String FUNDING_REASON = "fundingReason";
  private static final String INVALID_FUNDING_REASON = "InvalidFundingReason";
  private static final UUID FUNDING_REASON_UUID = UUID.randomUUID();
  private static final String ERROR_INVALID_FUNDING_SUB_TYPE = String.format(
      ERROR_FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE, FUNDING_SUBTYPE_LABEL, FUNDING_TYPE_LABEL);
  private FundingTypeDTO fundingTypeDto;
  private FundingSubTypeDto fundingSubTypeDto;

  private FundingReasonDto fundingReasonDto;

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

    fundingReasonDto = new FundingReasonDto();
    fundingReasonDto.setId(FUNDING_REASON_UUID);
    fundingReasonDto.setReason(FUNDING_REASON);
  }

  @Test
  void shouldSetFundingSubTypeIdWhenLabelIsFound() {
    when(postFundingUpdateXls.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingUpdateXls.getFundingSubtype()).thenReturn(FUNDING_SUBTYPE_LABEL);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    when(postFundingUpdateXls.getDateFrom()).thenReturn(startDate);

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
  void shouldSetReasonIdWhenReasonIsFound() {
    when(postFundingUpdateXls.getFundingReason()).thenReturn(FUNDING_REASON);
    when(postFundingUpdateXls.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingUpdateXls.getFundingSubtype()).thenReturn(FUNDING_SUBTYPE_LABEL);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    when(postFundingUpdateXls.getDateFrom()).thenReturn(startDate);

    when(referenceServiceMock.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.singletonList(fundingReasonDto));

    uploadService.processPostFundingUpdateUpload(
        Collections.singletonList(postFundingUpdateXls));

    verify(tcsServiceMock).updatePostFundings(postDtoCaptor.capture());
    verify(postFundingUpdateXls, never()).addErrorMessage(anyString());
    PostDTO postDto = postDtoCaptor.getValue();
    PostFundingDTO postFundingDto = postDto.getFundings().iterator().next();
    assertEquals(postFundingDto.getFundingReasonId(), FUNDING_REASON_UUID);
  }

  @Test
  void shouldLogErrorsForInvalidReasons() {
    when(postFundingUpdateXls.getFundingReason()).thenReturn(INVALID_FUNDING_REASON);
    when(postFundingUpdateXls.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingUpdateXls.getFundingSubtype()).thenReturn(FUNDING_SUBTYPE_LABEL);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    when(postFundingUpdateXls.getDateFrom()).thenReturn(startDate);

    when(referenceServiceMock.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(INVALID_FUNDING_REASON)))
        .thenReturn(Collections.emptyList());

    uploadService.processPostFundingUpdateUpload(
        Collections.singletonList(postFundingUpdateXls));

    verify(tcsServiceMock).updatePostFundings(postDtoCaptor.capture());
    verify(postFundingUpdateXls).addErrorMessage(
        String.format(ERROR_INVALID_FUNDING_REASON, INVALID_FUNDING_REASON));
    PostDTO postDto = postDtoCaptor.getValue();
    PostFundingDTO postFundingDto = postDto.getFundings().iterator().next();
    assertEquals(postFundingDto.getFundingReasonId(), null);
  }

  @Test
  void shouldSetFundingSubTypeIdCaseInsensitively() {
    final String fundingSubtypeLabelInUpperCase = FUNDING_SUBTYPE_LABEL.toUpperCase();
    when(postFundingUpdateXls.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingUpdateXls.getFundingSubtype()).thenReturn(fundingSubtypeLabelInUpperCase);

    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    when(postFundingUpdateXls.getDateFrom()).thenReturn(startDate);

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

  @Test
  void shouldThrowErrorWhenFundingEndDateIsBeforeStartDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    calendar.set(2013, Calendar.JANUARY, 1);
    Date endDate = calendar.getTime();

    when(postFundingUpdateXls.getDateFrom()).thenReturn(startDate);
    when(postFundingUpdateXls.getDateTo()).thenReturn(endDate);

    uploadService.processPostFundingUpdateUpload(Collections.singletonList(postFundingUpdateXls));

    verify(postFundingUpdateXls, times(1))
        .addErrorMessage(PostFundingUpdateTransformerService.FUNDING_END_DATE_VALIDATION_MSG);
  }

  @Test
  void shouldNotThrowErrorWhenFundingEndDateIsAfterStartDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    calendar.set(2025, Calendar.JANUARY, 1);
    Date endDate = calendar.getTime();

    when(postFundingUpdateXls.getDateFrom()).thenReturn(startDate);
    when(postFundingUpdateXls.getDateTo()).thenReturn(endDate);

    uploadService.processPostFundingUpdateUpload(Collections.singletonList(postFundingUpdateXls));

    verify(postFundingUpdateXls, never())
        .addErrorMessage(PostFundingUpdateTransformerService.FUNDING_END_DATE_VALIDATION_MSG);

    ArgumentCaptor<PostDTO> postDtoCaptor = ArgumentCaptor.forClass(PostDTO.class);

    verify(tcsServiceMock).updatePostFundings(postDtoCaptor.capture());

    PostDTO postDto = postDtoCaptor.getValue();
    assertEquals(1, postDto.getFundings().size());
    PostFundingDTO postFundingDto = postDto.getFundings().iterator().next();
    assertEquals(convertDate(startDate), postFundingDto.getStartDate());
    assertEquals(convertDate(endDate), postFundingDto.getEndDate());
  }

  @Test
  void shouldThrowErrorWhenFundingStartDateIsNull() {
    when(postFundingUpdateXls.getPostTisId()).thenReturn(POST_TIS_ID);
    when(postFundingUpdateXls.getDateFrom()).thenReturn(null);

    uploadService.processPostFundingUpdateUpload(Collections.singletonList(postFundingUpdateXls));

    verify(postFundingUpdateXls, times(1))
        .addErrorMessage(PostFundingUpdateTransformerService.FUNDING_START_DATE_NULL_OR_EMPTY);
  }
}
