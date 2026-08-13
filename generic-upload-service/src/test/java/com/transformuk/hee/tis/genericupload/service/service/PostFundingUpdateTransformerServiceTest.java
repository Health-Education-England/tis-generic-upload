package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingCreateTransformerService.ERROR_INVALID_FUNDING_REASON;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingCreateTransformerService.FUNDING_TYPE_REQUIRES_SUBTYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.PostFundingUpdateRow;
import com.transformuk.hee.tis.reference.api.dto.FundingReasonDto;
import com.transformuk.hee.tis.reference.api.dto.FundingSubTypeDto;
import com.transformuk.hee.tis.reference.api.dto.FundingTypeDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.time.LocalDate;
import java.util.Calendar;
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

/**
 * The unit tests for {@link PostFundingUpdateTransformerService}.
 */
@ExtendWith(MockitoExtension.class)
class PostFundingUpdateTransformerServiceTest {

  private static final Long POST_FUNDING_ID = 1L;
  private static final String FUNDING_TYPE_ORIGINAL = "originalType";
  private static final String FUNDING_TYPE_NEW = "newType";
  private static final String FUNDING_TYPE_OTHER = "Other";
  private static final String FUNDING_TYPE_ACADEMIC = "academicType";
  private static final String FUNDING_BODY_VALID = "validFundingBody";
  private static final Long TRUST_ID = 1L;
  private static final String FUNDING_SUBTYPE = "fundingSubtype";
  private static final UUID FUNDING_SUBTYPE_ID = UUID.randomUUID();
  private static final String FUNDING_REASON = "fundingReason";
  private static final UUID FUNDING_REASON_UUID = UUID.randomUUID();

  @InjectMocks
  private PostFundingUpdateTransformerService transformerService;

  @Mock
  private TcsServiceImpl tcsServiceImpl;
  @Mock
  private ReferenceServiceImpl referenceServiceImpl;

  @Captor
  private ArgumentCaptor<PostFundingDTO> postFundingDtoArgumentCaptor;

  private PostFundingUpdateRow postFundingUpdateRow;
  private PostFundingDTO postFundingDto;
  private FundingTypeDTO fundingTypeDto;
  private FundingSubTypeDto fundingSubTypeDto;
  private FundingReasonDto fundingReasonDto;

  @BeforeEach
  public void setUp() {

    postFundingUpdateRow = new PostFundingUpdateRow();
    postFundingUpdateRow.setPostFundingTisId(POST_FUNDING_ID.toString());
    postFundingUpdateRow.setFundingType(FUNDING_TYPE_NEW);
    postFundingUpdateRow.setFundingTypeOther(FUNDING_TYPE_OTHER);
    postFundingUpdateRow.setFundingBody(FUNDING_BODY_VALID);
    postFundingUpdateRow.setPostTisId("1");
    postFundingUpdateRow.setFundingReason(FUNDING_REASON);
    Calendar cFrom = Calendar.getInstance();
    cFrom.set(2019, Calendar.SEPTEMBER, 1);
    postFundingUpdateRow.setDateFrom(cFrom.getTime());
    Calendar cTo = Calendar.getInstance();
    cTo.set(2019, Calendar.SEPTEMBER, 2);
    postFundingUpdateRow.setDateTo(cTo.getTime());

    fundingTypeDto = new FundingTypeDTO();
    fundingTypeDto.setId(3L);
    fundingTypeDto.setLabel(FUNDING_TYPE_NEW);
    fundingTypeDto.setAllowDetails(false);

    postFundingDto = new PostFundingDTO();
    postFundingDto.setFundingBodyId("2");
    postFundingDto.setFundingType(FUNDING_TYPE_ORIGINAL);
    postFundingDto.setInfo(null);
    postFundingDto.setStartDate(LocalDate.now());
    postFundingDto.setEndDate(LocalDate.now().plusDays(1));
    postFundingDto.setPostId(1L);

    fundingSubTypeDto = new FundingSubTypeDto();
    fundingSubTypeDto.setLabel(FUNDING_SUBTYPE);
    fundingSubTypeDto.setId(FUNDING_SUBTYPE_ID);
    fundingSubTypeDto.setFundingType(fundingTypeDto);

    fundingReasonDto = new FundingReasonDto();
    fundingReasonDto.setId(FUNDING_REASON_UUID);
    fundingReasonDto.setReason(FUNDING_REASON);
  }

  @Test
  void canHandleUnknownPostFundingId() {
    String id = "999";
    postFundingUpdateRow.setPostFundingTisId(id);
    when(tcsServiceImpl.getPostFundingById(999L)).thenReturn(null);

    transformerService.processRows(
        Collections.singletonList(postFundingUpdateRow));

    assertThat("Can handle unknown post funding id", postFundingUpdateRow.getErrorMessage(),
        containsString(String
            .format(PostFundingUpdateTransformerService.DID_NOT_FIND_POST_FUNDING_FOR_ID, id)));
  }

  @Test
  void canHandleNonNumberPostFundingId() {
    String id = "XXX";
    postFundingUpdateRow.setPostFundingTisId(id);

    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));

    assertThat("Can handle non-number post funding id", postFundingUpdateRow.getErrorMessage(),
        containsString(
            String
                .format(PostFundingUpdateTransformerService.DID_NOT_FIND_POST_FUNDING_FOR_ID, id)));
  }

  @Test
  void canHandleUnknownFundingBody() {
    String fundingBodyName = "Unknown";
    postFundingUpdateRow.setFundingBody(fundingBodyName);
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);

    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));

    assertThat("Can handle unknown funding body", postFundingUpdateRow.getErrorMessage(),
        containsString(
            String.format(PostFundingUpdateTransformerService.ERROR_INVALID_FUNDING_BODY_NAME,
                fundingBodyName)));
  }

  @Test
  void canHandleRequiredFundingTypeWhenDetailsIsFilled() {
    postFundingUpdateRow.setFundingType(null);
    postFundingUpdateRow.setFundingTypeOther("details");

    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    when(referenceServiceImpl.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.singletonList(fundingReasonDto));

    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));
    assertThat("should throw error when fundingType is empty but fundingDetails is filled",
        postFundingUpdateRow.getErrorMessage(), containsString(
            PostFundingUpdateTransformerService.FUNDING_TYPE_IS_REQUIRED_FOR_DETAILS));
  }

  @Test
  void canHandleUnknownFundingReason() {
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    // Funding Reason not found in Reference Service
    when(referenceServiceImpl.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.emptyList());

    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));
    assertThat("should throw error when fundingReason does not exist in reference",
        postFundingUpdateRow.getErrorMessage(),
        containsString(String.format(ERROR_INVALID_FUNDING_REASON, FUNDING_REASON)));
  }


  @Test
  void canUpdateFields() {
    postFundingUpdateRow.setFundingSubtype(FUNDING_SUBTYPE);
    TrustDTO trustDto = new TrustDTO();
    trustDto.setTrustKnownAs(FUNDING_BODY_VALID);
    trustDto.setId(TRUST_ID);
    when(referenceServiceImpl.findCurrentTrustsByTrustKnownAsIn(
        Collections.singleton(FUNDING_BODY_VALID)))
        .thenReturn(Collections.singletonList(trustDto));
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    when(referenceServiceImpl.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE)))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));
    when(tcsServiceImpl.updateFunding(postFundingDtoArgumentCaptor.capture()))
        .thenReturn(postFundingDto);
    when(referenceServiceImpl.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.singletonList(fundingReasonDto));

    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));

    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();

    assertThat("Should update fundingType", postFundingDtoArgumentCaptorValue.getFundingType(),
        equalTo(postFundingUpdateRow.getFundingType()));
    assertThat("Should update fundingTypeOther", postFundingDtoArgumentCaptorValue.getInfo(),
        equalTo(FUNDING_TYPE_OTHER));
    assertThat("Should update fundingBody", postFundingDtoArgumentCaptorValue.getFundingBodyId(),
        equalTo("1"));
    assertThat("Should update dateFrom", postFundingDtoArgumentCaptorValue.getStartDate(),
        equalTo(convertDate(postFundingUpdateRow.getDateFrom())));
    assertThat("Should update dateTo", postFundingDtoArgumentCaptorValue.getEndDate(),
        equalTo(convertDate(postFundingUpdateRow.getDateTo())));
    assertThat("Should update fundingSubtype",
        postFundingDtoArgumentCaptorValue.getFundingSubTypeId(), equalTo(FUNDING_SUBTYPE_ID));
    assertThat("Should update fundingReason",
        postFundingDtoArgumentCaptorValue.getFundingReasonId(), equalTo(FUNDING_REASON_UUID));
  }

  @Test
  void shouldNotUpdateFieldsWhenNull() {
    postFundingUpdateRow.setFundingType(null);
    postFundingUpdateRow.setFundingTypeOther(null);
    postFundingUpdateRow.setFundingBody(null);
    postFundingUpdateRow.setDateFrom(null);
    postFundingUpdateRow.setDateTo(null);
    postFundingUpdateRow.setFundingReason(null);

    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    when(tcsServiceImpl.updateFunding(postFundingDtoArgumentCaptor.capture()))
        .thenReturn(postFundingDto);
    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));

    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();
    assertThat("Should not update fundingType", postFundingDtoArgumentCaptorValue.getFundingType(),
        equalTo(postFundingDto.getFundingType()));
    assertThat("Should not update fundingTypeOther", postFundingDtoArgumentCaptorValue.getInfo(),
        equalTo(postFundingDto.getInfo()));
    assertThat("Should not update fundingBody",
        postFundingDtoArgumentCaptorValue.getFundingBodyId(),
        equalTo(postFundingDto.getFundingBodyId()));
    assertThat("Should not update dateFrom", postFundingDtoArgumentCaptorValue.getStartDate(),
        equalTo(postFundingDto.getStartDate()));
    assertThat("Should not update dateTo", postFundingDtoArgumentCaptorValue.getEndDate(),
        equalTo(postFundingDto.getEndDate()));
    assertThat("Should not update fundingReason",
        postFundingDtoArgumentCaptorValue.getFundingReasonId(),
        equalTo(postFundingDto.getFundingReasonId()));
  }

  @Test
  void shouldUpdateInfoToNullWhenFundingDetailsAreEmpty() {

    postFundingDto.setFundingBodyId("2");
    postFundingDto.setFundingType(FUNDING_TYPE_ACADEMIC);
    postFundingDto.setInfo("info");
    postFundingDto.setId(2L);
    TrustDTO trustDto = new TrustDTO();
    trustDto.setTrustKnownAs(FUNDING_BODY_VALID);
    trustDto.setId(TRUST_ID);
    when(referenceServiceImpl.findCurrentTrustsByTrustKnownAsIn(
        Collections.singleton(FUNDING_BODY_VALID)))
        .thenReturn(Collections.singletonList(trustDto));
    when(tcsServiceImpl.getPostFundingById(2L)).thenReturn(postFundingDto);

    postFundingUpdateRow.setPostFundingTisId("2");
    postFundingUpdateRow.setFundingType(FUNDING_TYPE_NEW);
    postFundingUpdateRow.setFundingTypeOther(null);

    when(referenceServiceImpl.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.singletonList(fundingReasonDto));
    when(tcsServiceImpl.updateFunding(postFundingDtoArgumentCaptor.capture()))
        .thenReturn(postFundingDto);
    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));

    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();

    assertThat("Should update fundingType", postFundingDtoArgumentCaptorValue.getFundingType(),
        equalTo(FUNDING_TYPE_NEW));
    assertThat("Should update fundingTypeOther", postFundingDtoArgumentCaptorValue.getInfo(),
        nullValue());
  }

  @Test
  void shouldGiveErrorWhenPostIdDoesNotMatch() {
    String postId = "999";
    postFundingUpdateRow.setPostTisId(postId);
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));

    assertThat(postFundingUpdateRow.getErrorMessage(), containsString(
        String.format(PostFundingUpdateTransformerService.POST_FUNDING_ID_AND_POST_ID_NOT_MATCHING,
            postId)));
  }

  @Test
  void shouldErrorWhenFundingSubtypeIsFilledAndFundingTypeEmpty() {
    postFundingUpdateRow.setFundingTypeOther(null);
    postFundingUpdateRow.setFundingType(null);
    postFundingUpdateRow.setFundingSubtype(FUNDING_SUBTYPE);
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);

    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));
    assertThat(
        "should throw error when fundingType is empty but fundingSubtype is filled",
        postFundingUpdateRow.getErrorMessage(),
        containsString(
            PostFundingUpdateTransformerService.FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE));
  }

  @Test
  void shouldErrorWhenFundingSubtypeIsRequiredAndMissing() {
    postFundingUpdateRow.setDateTo(null);
    TrustDTO trustDto = new TrustDTO();
    trustDto.setTrustKnownAs(FUNDING_BODY_VALID);
    trustDto.setId(TRUST_ID);
    when(referenceServiceImpl.findCurrentTrustsByTrustKnownAsIn(
        Collections.singleton(FUNDING_BODY_VALID)))
        .thenReturn(Collections.singletonList(trustDto));
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    when(referenceServiceImpl.findCurrentFundingSubTypesByLabels(Collections.emptySet()))
        .thenReturn(Collections.emptyList());
    when(referenceServiceImpl.findCurrentFundingTypesByLabelIn(
        Collections.singleton(FUNDING_TYPE_NEW)))
        .thenReturn(Collections.singletonList(fundingTypeDto));
    when(referenceServiceImpl.findCurrentFundingSubTypesForFundingTypeId(fundingTypeDto.getId()))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));
    when(referenceServiceImpl.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.singletonList(fundingReasonDto));

    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));
    verify(tcsServiceImpl, never()).updateFunding(any());
    assertThat(postFundingUpdateRow.getErrorMessage(),
        containsString(FUNDING_TYPE_REQUIRES_SUBTYPE));
  }

  @Test
  void shouldNotErrorWhenFundingSubtypeIsNotRequiredAndMissing() {
    postFundingUpdateRow.setDateTo(null);
    TrustDTO trustDto = new TrustDTO();
    trustDto.setTrustKnownAs(FUNDING_BODY_VALID);
    trustDto.setId(TRUST_ID);
    when(referenceServiceImpl.findCurrentTrustsByTrustKnownAsIn(
        Collections.singleton(FUNDING_BODY_VALID)))
        .thenReturn(Collections.singletonList(trustDto));
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    when(referenceServiceImpl.findCurrentFundingSubTypesByLabels(Collections.emptySet()))
        .thenReturn(Collections.singletonList(fundingSubTypeDto));
    when(referenceServiceImpl.findCurrentFundingTypesByLabelIn(
        Collections.singleton(FUNDING_TYPE_NEW)))
        .thenReturn(Collections.singletonList(fundingTypeDto));
    when(referenceServiceImpl.findCurrentFundingSubTypesForFundingTypeId(fundingTypeDto.getId()))
        .thenReturn(Collections.emptyList());
    when(referenceServiceImpl.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.singletonList(fundingReasonDto));
    when(tcsServiceImpl.updateFunding(postFundingDtoArgumentCaptor.capture()))
        .thenReturn(postFundingDto);

    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));
    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();

    assertThat(postFundingDtoArgumentCaptorValue.getFundingType(),
        equalTo(postFundingUpdateRow.getFundingType()));
    assertThat(postFundingDtoArgumentCaptorValue.getInfo(), equalTo(FUNDING_TYPE_OTHER));
    assertThat(postFundingDtoArgumentCaptorValue.getFundingBodyId(), equalTo("1"));
    assertThat(postFundingDtoArgumentCaptorValue.getStartDate(),
        equalTo(convertDate(postFundingUpdateRow.getDateFrom())));
    assertThat(postFundingDtoArgumentCaptorValue.getFundingSubTypeId(),
        nullValue());
    assertThat(postFundingDtoArgumentCaptorValue.getFundingReasonId(),
        equalTo(FUNDING_REASON_UUID));
  }

  @Test
  void shouldUpdateFundingSubtypeIdToNullWhenFundingFundingSubtypeLabelIsEmpty() {

    postFundingDto.setFundingType(FUNDING_TYPE_ACADEMIC);
    postFundingDto.setFundingSubTypeId(FUNDING_SUBTYPE_ID);
    postFundingDto.setId(2L);
    when(tcsServiceImpl.getPostFundingById(2L)).thenReturn(postFundingDto);

    postFundingUpdateRow.setPostFundingTisId("2");
    postFundingUpdateRow.setFundingType(FUNDING_TYPE_NEW);

    TrustDTO trustDto = new TrustDTO();
    trustDto.setTrustKnownAs(FUNDING_BODY_VALID);
    trustDto.setId(TRUST_ID);
    when(referenceServiceImpl.findCurrentTrustsByTrustKnownAsIn(
        Collections.singleton(FUNDING_BODY_VALID)))
        .thenReturn(Collections.singletonList(trustDto));
    when(tcsServiceImpl.updateFunding(postFundingDtoArgumentCaptor.capture()))
        .thenReturn(postFundingDto);
    when(referenceServiceImpl.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.singletonList(fundingReasonDto));
    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));

    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();

    assertThat("Should update fundingType",
        postFundingDtoArgumentCaptorValue.getFundingType(),
        equalTo(FUNDING_TYPE_NEW));
    assertThat("Should update fundingSubtype",
        postFundingDtoArgumentCaptorValue.getFundingSubTypeId(), nullValue()); // value is null
  }

  @Test
  void shouldErrorWhenFundingSubTypeNotFound() {
    postFundingUpdateRow.setFundingSubtype(FUNDING_SUBTYPE);
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    when(referenceServiceImpl.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE)))
        .thenReturn(Collections.emptyList());

    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));

    assertThat(
        "should throw error when fundingSubtype not found",
        postFundingUpdateRow.getErrorMessage(),
        containsString(
            String.format(
                PostFundingUpdateTransformerService.FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE,
                FUNDING_SUBTYPE, FUNDING_TYPE_NEW)));
  }

  @Test
  void shouldThrowErrorWhenFundingEndDateIsBeforeStartDate() {
    Calendar cTo = Calendar.getInstance();
    cTo.set(2019, Calendar.JANUARY, 2);
    postFundingUpdateRow.setDateTo(cTo.getTime());

    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);

    transformerService.processRows(Collections.singletonList(postFundingUpdateRow));

    assertThat("should throw error when funding end date is before start date",
        postFundingUpdateRow.getErrorMessage(),
        containsString(
            String.format(PostFundingUpdateTransformerService.FUNDING_END_DATE_VALIDATION_MSG)));
  }
}
