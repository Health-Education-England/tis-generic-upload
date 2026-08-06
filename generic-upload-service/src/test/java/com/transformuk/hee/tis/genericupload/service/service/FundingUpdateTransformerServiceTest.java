package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingUpdateTransformerService.ERROR_INVALID_FUNDING_REASON;
import static com.transformuk.hee.tis.genericupload.service.service.PostFundingUpdateTransformerService.FUNDING_TYPE_REQUIRES_SUBTYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.FundingUpdateXLS;
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
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The unit tests for {@link FundingUpdateTransformerService}.
 */
@ExtendWith(MockitoExtension.class)
class FundingUpdateTransformerServiceTest {

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
  private FundingUpdateTransformerService fundingUpdateTransformerService;

  @Mock
  private TcsServiceImpl tcsServiceImpl;
  @Mock
  private ReferenceServiceImpl referenceServiceImpl;

  @Captor
  private ArgumentCaptor<PostFundingDTO> postFundingDtoArgumentCaptor;

  private FundingUpdateXLS fundingUpdateXls;
  private PostFundingDTO postFundingDto;
  private FundingTypeDTO fundingTypeDto;
  private FundingSubTypeDto fundingSubTypeDto;
  private FundingReasonDto fundingReasonDto;

  @BeforeEach
  public void setUp() {

    fundingUpdateXls = new FundingUpdateXLS();
    fundingUpdateXls.setPostFundingTisId(POST_FUNDING_ID.toString());
    fundingUpdateXls.setFundingType(FUNDING_TYPE_NEW);
    fundingUpdateXls.setFundingTypeOther(FUNDING_TYPE_OTHER);
    fundingUpdateXls.setFundingBody(FUNDING_BODY_VALID);
    fundingUpdateXls.setPostTisId("1");
    fundingUpdateXls.setFundingReason(FUNDING_REASON);
    Calendar cFrom = Calendar.getInstance();
    cFrom.set(2019, Calendar.SEPTEMBER, 1);
    fundingUpdateXls.setDateFrom(cFrom.getTime());
    Calendar cTo = Calendar.getInstance();
    cTo.set(2019, Calendar.SEPTEMBER, 2);
    fundingUpdateXls.setDateTo(cTo.getTime());

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
    fundingUpdateXls.setPostFundingTisId(id);
    when(tcsServiceImpl.getPostFundingById(999L)).thenReturn(null);

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    assertThat("Can handle unknown post funding id", fundingUpdateXls.getErrorMessage(),
        CoreMatchers.containsString(
            String.format(FundingUpdateTransformerService.DID_NOT_FIND_POST_FUNDING_FOR_ID, id)));
  }

  @Test
  void canHandNonNumberPostFundingId() {
    String id = "XXX";
    fundingUpdateXls.setPostFundingTisId(id);

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    assertThat("Can handle non-number post funding id", fundingUpdateXls.getErrorMessage(),
        CoreMatchers.containsString(
            String.format(FundingUpdateTransformerService.DID_NOT_FIND_POST_FUNDING_FOR_ID, id)));
  }

  @Test
  void canHandleUnknownFundingBody() {
    String fundingBodyName = "Unknown";
    fundingUpdateXls.setFundingBody(fundingBodyName);
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    assertThat("Can handle unknown funding body", fundingUpdateXls.getErrorMessage(),
        CoreMatchers.containsString(
            String.format(FundingUpdateTransformerService.ERROR_INVALID_FUNDING_BODY_NAME,
                fundingBodyName)));
  }

  @Test
  void canHandleRequiredFundingTypeWhenDetailsIsFilled() {
    fundingUpdateXls.setFundingType(null);
    fundingUpdateXls.setFundingTypeOther("details");

    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    when(referenceServiceImpl.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.singletonList(fundingReasonDto));

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));
    assertThat("should throw error when fundingType is empty but fundingDetails is filled",
        fundingUpdateXls.getErrorMessage(), CoreMatchers.containsString(
            FundingUpdateTransformerService.FUNDING_TYPE_IS_REQUIRED_FOR_DETAILS));
  }

  @Test
  void canHandleUnknownFundingReason() {
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    // Funding Reason not found in Reference Service
    when(referenceServiceImpl.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.emptyList());

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));
    assertThat("should throw error when fundingReason does not exist in reference",
        fundingUpdateXls.getErrorMessage(),
        CoreMatchers.containsString(String.format(ERROR_INVALID_FUNDING_REASON, FUNDING_REASON)));
  }


  @Test
  void canUpdateFields() {
    fundingUpdateXls.setFundingSubtype(FUNDING_SUBTYPE);
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

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();

    assertThat("Should update fundingType", postFundingDtoArgumentCaptorValue.getFundingType(),
        equalTo(fundingUpdateXls.getFundingType()));
    assertThat("Should update fundingTypeOther", postFundingDtoArgumentCaptorValue.getInfo(),
        equalTo(FUNDING_TYPE_OTHER));
    assertThat("Should update fundingBody", postFundingDtoArgumentCaptorValue.getFundingBodyId(),
        equalTo("1"));
    assertThat("Should update dateFrom", postFundingDtoArgumentCaptorValue.getStartDate(),
        equalTo(convertDate(fundingUpdateXls.getDateFrom())));
    assertThat("Should update dateTo", postFundingDtoArgumentCaptorValue.getEndDate(),
        equalTo(convertDate(fundingUpdateXls.getDateTo())));
    assertThat("Should update fundingSubtype",
        postFundingDtoArgumentCaptorValue.getFundingSubTypeId(), equalTo(FUNDING_SUBTYPE_ID));
    assertThat("Should update fundingReason",
        postFundingDtoArgumentCaptorValue.getFundingReasonId(), equalTo(FUNDING_REASON_UUID));
  }

  @Test
  void shouldNotUpdateFieldsWhenNull() {
    fundingUpdateXls.setFundingType(null);
    fundingUpdateXls.setFundingTypeOther(null);
    fundingUpdateXls.setFundingBody(null);
    fundingUpdateXls.setDateFrom(null);
    fundingUpdateXls.setDateTo(null);
    fundingUpdateXls.setFundingReason(null);

    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    when(tcsServiceImpl.updateFunding(postFundingDtoArgumentCaptor.capture()))
        .thenReturn(postFundingDto);
    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

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

    fundingUpdateXls.setPostFundingTisId("2");
    fundingUpdateXls.setFundingType(FUNDING_TYPE_NEW);

    when(referenceServiceImpl.findCurrentFundingReasonsByReasonIn(
        Collections.singleton(FUNDING_REASON)))
        .thenReturn(Collections.singletonList(fundingReasonDto));
    when(tcsServiceImpl.updateFunding(postFundingDtoArgumentCaptor.capture()))
        .thenReturn(postFundingDto);
    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();

    assertThat("Should update fundingType", postFundingDtoArgumentCaptorValue.getFundingType(),
        equalTo(FUNDING_TYPE_NEW));
    assertThat("Should update fundingTypeOther", postFundingDtoArgumentCaptorValue.getInfo(),
        equalTo(fundingUpdateXls.getFundingTypeOther())); // value is null
  }

  @Test
  void shouldGiveErrorWhenPostIdDoesNotMatch() {
    String postId = "999";
    fundingUpdateXls.setPostTisId(postId);
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    assertThat(fundingUpdateXls.getErrorMessage(), CoreMatchers.containsString(
        String.format(FundingUpdateTransformerService.POST_FUNDING_ID_AND_POST_ID_NOT_MATCHING,
            postId)));
  }

  @Test
  void shouldErrorWhenFundingSubtypeIsFilledAndFundingTypeEmpty() {
    fundingUpdateXls.setFundingTypeOther(null);
    fundingUpdateXls.setFundingType(null);
    fundingUpdateXls.setFundingSubtype(FUNDING_SUBTYPE);
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));
    assertThat(
        "should throw error when fundingType is empty but fundingSubtype is filled",
        fundingUpdateXls.getErrorMessage(),
        containsString(
            FundingUpdateTransformerService.FUNDING_TYPE_IS_REQUIRED_FOR_SUB_TYPE));
  }

  @Test
  void shouldErrorWhenFundingSubtypeIsRequiredAndMissing() {
    fundingUpdateXls.setDateTo(null);
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

    fundingUpdateTransformerService
        .processFundingUpdateUpload(Collections.singletonList(fundingUpdateXls));
    verify(tcsServiceImpl, never()).updateFunding(any());
    assertThat(fundingUpdateXls.getErrorMessage(), containsString(FUNDING_TYPE_REQUIRES_SUBTYPE));
  }

  @Test
  void shouldNotErrorWhenFundingSubtypeIsNotRequiredAndMissing() {
    fundingUpdateXls.setDateTo(null);
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

    fundingUpdateTransformerService
        .processFundingUpdateUpload(Collections.singletonList(fundingUpdateXls));
    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();

    assertThat(postFundingDtoArgumentCaptorValue.getFundingType(),
        equalTo(fundingUpdateXls.getFundingType()));
    assertThat(postFundingDtoArgumentCaptorValue.getInfo(), equalTo(FUNDING_TYPE_OTHER));
    assertThat(postFundingDtoArgumentCaptorValue.getFundingBodyId(), equalTo("1"));
    assertThat(postFundingDtoArgumentCaptorValue.getStartDate(),
        equalTo(convertDate(fundingUpdateXls.getDateFrom())));
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

    fundingUpdateXls.setPostFundingTisId("2");
    fundingUpdateXls.setFundingType(FUNDING_TYPE_NEW);

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
    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();

    assertThat("Should update fundingType",
        postFundingDtoArgumentCaptorValue.getFundingType(),
        equalTo(FUNDING_TYPE_NEW));
    assertThat("Should update fundingSubtype",
        postFundingDtoArgumentCaptorValue.getFundingSubTypeId(), nullValue()); // value is null
  }

  @Test
  void shouldErrorWhenFundingSubTypeNotFound() {
    fundingUpdateXls.setFundingSubtype(FUNDING_SUBTYPE);
    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);
    when(referenceServiceImpl.findCurrentFundingSubTypesByLabels(
        Collections.singleton(FUNDING_SUBTYPE)))
        .thenReturn(Collections.emptyList());

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    assertThat(
        "should throw error when fundingSubtype not found",
        fundingUpdateXls.getErrorMessage(),
        containsString(
            String.format(FundingUpdateTransformerService.FUNDING_SUB_TYPE_NOT_MATCH_FUNDING_TYPE,
                FUNDING_SUBTYPE, FUNDING_TYPE_NEW)));
  }

  @Test
  void shouldThrowErrorWhenFundingEndDateIsBeforeStartDate() {
    Calendar cTo = Calendar.getInstance();
    cTo.set(2019, Calendar.JANUARY, 2);
    fundingUpdateXls.setDateTo(cTo.getTime());

    when(tcsServiceImpl.getPostFundingById(POST_FUNDING_ID)).thenReturn(postFundingDto);

    fundingUpdateTransformerService
        .processFundingUpdateUpload(Collections.singletonList(fundingUpdateXls));

    assertThat("should throw error when funding end date is before start date",
        fundingUpdateXls.getErrorMessage(),
        containsString(
            String.format(FundingUpdateTransformerService.FUNDING_END_DATE_VALIDATION_MSG)));
  }
}
