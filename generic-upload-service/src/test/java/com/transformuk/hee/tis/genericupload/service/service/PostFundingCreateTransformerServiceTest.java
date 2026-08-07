package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingCreateTransformerService.ERROR_FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingCreateTransformerService.ERROR_FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingCreateTransformerService.ERROR_INVALID_FUNDING_REASON;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingCreateTransformerService.FUNDING_TYPE_REQUIRES_SUBTYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.PostFundingCreateRow;
import com.transformuk.hee.tis.reference.api.dto.FundingReasonDto;
import com.transformuk.hee.tis.reference.api.dto.FundingSubTypeDto;
import com.transformuk.hee.tis.reference.api.dto.FundingTypeDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostFundingCreateTransformerServiceTest {

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
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2025-08-16T00:05:01Z"), ZoneOffset.UTC);
  private FundingTypeDTO fundingTypeDto;
  private FundingSubTypeDto fundingSubTypeDto;

  private FundingReasonDto fundingReasonDto;

  @InjectMocks
  private PostFundingCreateTransformerService uploadService;
  @Mock
  private TcsServiceImpl tcsServiceMock;
  @Mock
  private ReferenceServiceImpl referenceServiceMock;

  @Mock
  private PostFundingCreateRow postFundingCreateRow;

  @Captor
  private ArgumentCaptor<PostDTO> postDtoCaptor;

  @BeforeEach
  void setUp() {
    when(postFundingCreateRow.getPostTisId()).thenReturn(POST_TIS_ID);

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

    uploadService.setClock(CLOCK);
  }

  @Test
  void shouldSetFundingSubTypeIdWhenLabelIsFound() {
    when(postFundingCreateRow.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingCreateRow.getFundingSubtype()).thenReturn(FUNDING_SUBTYPE_LABEL);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    when(postFundingCreateRow.getDateFrom()).thenReturn(startDate);

    uploadService.processRows(
        Collections.singletonList(postFundingCreateRow));

    verify(tcsServiceMock).updatePostFundings(postDtoCaptor.capture());
    verify(postFundingCreateRow, never()).addErrorMessage(anyString());
    PostDTO postDto = postDtoCaptor.getValue();
    assertEquals(1, postDto.getFundings().size());
    PostFundingDTO postFundingDto = postDto.getFundings().iterator().next();
    assertEquals(FUNDING_SUBTYPE_UUID, postFundingDto.getFundingSubTypeId());
  }

  @Test
  void shouldSetReasonIdWhenReasonIsFound() {
    when(postFundingCreateRow.getFundingReason()).thenReturn(FUNDING_REASON);
    when(postFundingCreateRow.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingCreateRow.getFundingSubtype()).thenReturn(FUNDING_SUBTYPE_LABEL);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    when(postFundingCreateRow.getDateFrom()).thenReturn(startDate);

    when(referenceServiceMock.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.singletonList(fundingReasonDto));

    uploadService.processRows(
        Collections.singletonList(postFundingCreateRow));

    verify(tcsServiceMock).updatePostFundings(postDtoCaptor.capture());
    verify(postFundingCreateRow, never()).addErrorMessage(anyString());
    PostDTO postDto = postDtoCaptor.getValue();
    PostFundingDTO postFundingDto = postDto.getFundings().iterator().next();
    assertEquals(FUNDING_REASON_UUID, postFundingDto.getFundingReasonId());
  }

  @Test
  void shouldLogErrorsForInvalidReasons() {
    when(postFundingCreateRow.getFundingReason()).thenReturn(INVALID_FUNDING_REASON);
    when(postFundingCreateRow.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingCreateRow.getFundingSubtype()).thenReturn(FUNDING_SUBTYPE_LABEL);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    when(postFundingCreateRow.getDateFrom()).thenReturn(startDate);

    when(referenceServiceMock.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(INVALID_FUNDING_REASON)))
        .thenReturn(Collections.emptyList());

    uploadService.processRows(
        Collections.singletonList(postFundingCreateRow));

    verify(tcsServiceMock).updatePostFundings(postDtoCaptor.capture());
    verify(postFundingCreateRow).addErrorMessage(
        String.format(ERROR_INVALID_FUNDING_REASON, INVALID_FUNDING_REASON));
    PostDTO postDto = postDtoCaptor.getValue();
    PostFundingDTO postFundingDto = postDto.getFundings().iterator().next();
    assertNull(postFundingDto.getFundingReasonId());
  }

  @Test
  void shouldSetFundingSubTypeIdCaseInsensitively() {
    final String fundingSubtypeLabelInUpperCase = FUNDING_SUBTYPE_LABEL.toUpperCase();
    when(postFundingCreateRow.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingCreateRow.getFundingSubtype()).thenReturn(fundingSubtypeLabelInUpperCase);

    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    when(postFundingCreateRow.getDateFrom()).thenReturn(startDate);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(fundingSubtypeLabelInUpperCase)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    uploadService.processRows(
        Collections.singletonList(postFundingCreateRow));

    verify(tcsServiceMock).updatePostFundings(postDtoCaptor.capture());
    verify(postFundingCreateRow, never()).addErrorMessage(anyString());
    PostDTO postDto = postDtoCaptor.getValue();
    assertEquals(1, postDto.getFundings().size());
    PostFundingDTO postFundingDto = postDto.getFundings().iterator().next();
    assertEquals(FUNDING_SUBTYPE_UUID, postFundingDto.getFundingSubTypeId());
  }

  @Test
  void shouldAddErrorWhenLabelNotFound() {
    when(postFundingCreateRow.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingCreateRow.getFundingSubtype()).thenReturn(FUNDING_SUBTYPE_LABEL);
    when(postFundingCreateRow.getErrorMessage()).thenReturn(ERROR_INVALID_FUNDING_SUB_TYPE);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE_LABEL)))
        .thenReturn(Collections.emptyList());

    uploadService.processRows(
        Collections.singletonList(postFundingCreateRow));

    verify(tcsServiceMock, never()).updatePostFundings(any());
    verify(postFundingCreateRow).addErrorMessage(ERROR_INVALID_FUNDING_SUB_TYPE);
  }

  @Test
  void shouldAddErrorWhenFundingTypeIsNotSpecifiedForFundingSubType() {
    when(postFundingCreateRow.getFundingType()).thenReturn(null);
    when(postFundingCreateRow.getFundingSubtype()).thenReturn(FUNDING_SUBTYPE_LABEL);
    when(postFundingCreateRow.getErrorMessage()).thenReturn(
        ERROR_FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE);

    when(referenceServiceMock.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    uploadService.processRows(
        Collections.singletonList(postFundingCreateRow));

    verify(tcsServiceMock, never()).updatePostFundings(any());
    verify(postFundingCreateRow).addErrorMessage(ERROR_FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE);
  }

  @ParameterizedTest
  @NullSource
  @MethodSource("getEndDatesWhenFundingSubtypeRequired")
  void shouldAddErrorWhenFundingTypeRequiresSubtypeButSubtypeMissing(Date endDate) {
    when(postFundingCreateRow.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingCreateRow.getFundingSubtype()).thenReturn(null);
    when(postFundingCreateRow.getErrorMessage()).thenReturn(FUNDING_TYPE_REQUIRES_SUBTYPE);
    when(postFundingCreateRow.getDateTo()).thenReturn(endDate);

    when(referenceServiceMock.findCurrentFundingTypesByLabelIn(
        Collections.singleton(FUNDING_TYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingTypeDto));
    when(referenceServiceMock.findCurrentFundingSubTypesForFundingTypeId(FUNDING_TYPE_ID))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    uploadService.processRows(Collections.singletonList(postFundingCreateRow));

    verify(tcsServiceMock, never()).updatePostFundings(any());
    verify(postFundingCreateRow).addErrorMessage(FUNDING_TYPE_REQUIRES_SUBTYPE);
  }

  @Test
  void shouldUpdateWhenFundingTypeDoesNotRequireSubtype() {
    when(postFundingCreateRow.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    when(postFundingCreateRow.getFundingSubtype()).thenReturn(null);

    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    when(postFundingCreateRow.getDateFrom()).thenReturn(calendar.getTime());

    when(referenceServiceMock.findCurrentFundingTypesByLabelIn(
        Collections.singleton(FUNDING_TYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingTypeDto));
    when(referenceServiceMock.findCurrentFundingSubTypesForFundingTypeId(FUNDING_TYPE_ID))
        .thenReturn(Collections.emptyList());

    uploadService.processRows(Collections.singletonList(postFundingCreateRow));

    verify(tcsServiceMock).updatePostFundings(postDtoCaptor.capture());
    verify(postFundingCreateRow, never()).addErrorMessage(FUNDING_TYPE_REQUIRES_SUBTYPE);
    Set<PostFundingDTO> fundings = postDtoCaptor.getValue().getFundings();
    assertEquals(1, fundings.size());
    fundings.forEach(funding -> {
      assertEquals(FUNDING_TYPE_LABEL, funding.getFundingType());
      assertNull(funding.getFundingSubTypeId());
    });
  }

  @Test
  void shouldUpdateWhenEndedAndSubtypeMissing() {
    when(postFundingCreateRow.getFundingType()).thenReturn(FUNDING_TYPE_LABEL);
    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    when(postFundingCreateRow.getDateTo()).thenReturn(calendar.getTime());

    when(referenceServiceMock.findCurrentFundingTypesByLabelIn(
        Collections.singleton(FUNDING_TYPE_LABEL)))
        .thenReturn(Collections.singletonList(fundingTypeDto));
    when(referenceServiceMock.findCurrentFundingSubTypesForFundingTypeId(FUNDING_TYPE_ID))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));

    uploadService.processRows(Collections.singletonList(postFundingCreateRow));

    verify(tcsServiceMock).updatePostFundings(postDtoCaptor.capture());
    verify(postFundingCreateRow, never()).addErrorMessage(FUNDING_TYPE_REQUIRES_SUBTYPE);
    Set<PostFundingDTO> fundings = postDtoCaptor.getValue().getFundings();
    assertEquals(1, fundings.size());
    fundings.forEach(funding -> {
      assertEquals(FUNDING_TYPE_LABEL, funding.getFundingType());
      assertNull(funding.getFundingSubTypeId());
    });
  }

  @Test
  void shouldThrowErrorWhenFundingEndDateIsBeforeStartDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    calendar.set(2013, Calendar.JANUARY, 1);
    Date endDate = calendar.getTime();

    when(postFundingCreateRow.getDateFrom()).thenReturn(startDate);
    when(postFundingCreateRow.getDateTo()).thenReturn(endDate);

    uploadService.processRows(Collections.singletonList(postFundingCreateRow));

    verify(postFundingCreateRow)
        .addErrorMessage(PostFundingCreateTransformerService.FUNDING_END_DATE_VALIDATION_MSG);
  }

  @Test
  void shouldNotThrowErrorWhenFundingEndDateIsAfterStartDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2023, Calendar.JANUARY, 2);
    Date startDate = calendar.getTime();
    calendar.set(2025, Calendar.JANUARY, 1);
    Date endDate = calendar.getTime();

    when(postFundingCreateRow.getDateFrom()).thenReturn(startDate);
    when(postFundingCreateRow.getDateTo()).thenReturn(endDate);

    uploadService.processRows(Collections.singletonList(postFundingCreateRow));

    verify(postFundingCreateRow, never())
        .addErrorMessage(PostFundingCreateTransformerService.FUNDING_END_DATE_VALIDATION_MSG);

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
    when(postFundingCreateRow.getPostTisId()).thenReturn(POST_TIS_ID);
    when(postFundingCreateRow.getDateFrom()).thenReturn(null);

    uploadService.processRows(Collections.singletonList(postFundingCreateRow));

    verify(postFundingCreateRow)
        .addErrorMessage(PostFundingCreateTransformerService.FUNDING_START_DATE_NULL_OR_EMPTY);
  }

  public static Stream<Arguments> getEndDatesWhenFundingSubtypeRequired() {
    return Stream.of(Arguments.of(Date.from(CLOCK.instant().minusSeconds(40))));
  }
}
