package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.FundingUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.FundingTypeDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collections;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The unit tests for {@link FundingUpdateTransformerService}.
 */
@RunWith(SpringRunner.class)
public class FundingUpdateTransformServiceTest {

  private static final String FUNDING_TYPE_ORIGINAL = "originalType";
  private static final String FUNDING_TYPE_NEW = "newType";
  private static final String FUNDING_TYPE_OTHER = "Other";
  private static final String FUNDING_TYPE_ACADEMIC = "academicType";
  private static final String FUNDING_BODY_VALID = "validFundingBody";

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

  @Before
  public void setUp() {

    // initialise fundingUpdateXls
    fundingUpdateXls = new FundingUpdateXLS();
    fundingUpdateXls.setPostFundingTisId("1");
    fundingUpdateXls.setFundingType(FUNDING_TYPE_NEW);
    fundingUpdateXls.setFundingTypeOther(null);
    fundingUpdateXls.setFundingBody(FUNDING_BODY_VALID);
    Calendar cFrom = Calendar.getInstance();
    cFrom.set(2019, Calendar.SEPTEMBER, 1); // 2019-09-01
    fundingUpdateXls.setDateFrom(cFrom.getTime());
    Calendar cTo = Calendar.getInstance();
    cTo.set(2019, Calendar.SEPTEMBER, 2);
    fundingUpdateXls.setDateTo(cTo.getTime());

    // initialise trustDto
    TrustDTO trustDto = new TrustDTO();
    trustDto.setTrustKnownAs(FUNDING_BODY_VALID);
    trustDto.setId(1L);
    when(referenceServiceImpl.findCurrentTrustsByTrustKnownAsIn(
        Collections.singleton(FUNDING_BODY_VALID)))
        .thenReturn(Collections.singletonList(trustDto));

    // initialise fundingTypeDto
    FundingTypeDTO fundingTypeDto = new FundingTypeDTO();
    fundingTypeDto.setLabel(FUNDING_TYPE_NEW);
    fundingTypeDto.setAcademic(false);
    when(referenceServiceImpl.findCurrentFundingTypesByLabelIn(
        Collections.singleton(FUNDING_TYPE_NEW)))
        .thenReturn(Collections.singletonList(fundingTypeDto));

    FundingTypeDTO fundingTypeDto_1 = new FundingTypeDTO();
    fundingTypeDto_1.setLabel(FUNDING_TYPE_ACADEMIC);
    fundingTypeDto_1.setAcademic(true);
    when(referenceServiceImpl.findCurrentFundingTypesByLabelIn(
        Collections.singleton(FUNDING_TYPE_ACADEMIC)))
        .thenReturn(Collections.singletonList(fundingTypeDto_1));

    // initialise postFundingDto
    postFundingDto = new PostFundingDTO();
    postFundingDto.setFundingBodyId("2");
    postFundingDto.setFundingType(FUNDING_TYPE_ORIGINAL);
    postFundingDto.setInfo(null);
    postFundingDto.setStartDate(LocalDate.now());
    postFundingDto.setEndDate(LocalDate.now().plusDays(1));
    postFundingDto.setPostId(1L);

    when(tcsServiceImpl.getPostFundingById(1L)).thenReturn(postFundingDto);
  }

  @Test
  public void canHandleUnknownPostFundingId() {
    String id = "999";
    fundingUpdateXls.setPostFundingTisId(id);
    when(tcsServiceImpl.getPostFundingById(999L)).thenReturn(null);

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    MatcherAssert.assertThat("Can handle unknown post funding id",
        fundingUpdateXls.getErrorMessage(),
        CoreMatchers.containsString(
            String.format(FundingUpdateTransformerService.DID_NOT_FIND_POST_FUNDING_FOR_ID, id)));
  }

  @Test
  public void canHandNonNumberPostFundingId() {
    String id = "XXX";
    fundingUpdateXls.setPostFundingTisId(id);

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    MatcherAssert.assertThat("Can handle non-number post funding id",
        fundingUpdateXls.getErrorMessage(),
        CoreMatchers.containsString(
            String.format(FundingUpdateTransformerService.DID_NOT_FIND_POST_FUNDING_FOR_ID, id)));
  }

  @Test
  public void canHandleUnknownFundingBody() {
    String fundingBodyName = "Unknown";
    fundingUpdateXls.setFundingBody(fundingBodyName);

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    MatcherAssert.assertThat("Can handle unknown funding body",
        fundingUpdateXls.getErrorMessage(),
        CoreMatchers.containsString(
            String.format(FundingUpdateTransformerService.ERROR_INVALID_FUNDING_BODY_NAME,
                fundingBodyName)));
  }

  @Test
  public void canHandleUnknownFundingType() {
    String fundingType = "Unknown";
    fundingUpdateXls.setFundingType(fundingType);

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    MatcherAssert.assertThat("Can handle unknown funding type",
        fundingUpdateXls.getErrorMessage(),
        CoreMatchers.containsString(
            String.format(FundingUpdateTransformerService.ERROR_INVALID_FUNDING_TYPE,
                fundingType)));
  }

  @Test
  public void canHandleSpecifiedFundingTypeWhenNotOther() {
    fundingUpdateXls.setFundingType(FUNDING_TYPE_NEW);
    fundingUpdateXls.setFundingTypeOther("details");

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    MatcherAssert.assertThat("Can handle specified funding type when type is not Other",
        fundingUpdateXls.getErrorMessage(),
        CoreMatchers.containsString(
            FundingUpdateTransformerService.FUNDING_TYPE_IS_NOT_OTHER_OR_ACADEMIC));
  }

  @Test
  public void canHandleRequiredFundingTypeWhenDetailsIsFilled() {
    fundingUpdateXls.setFundingType(null);
    fundingUpdateXls.setFundingTypeOther("details");

    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));
    MatcherAssert.assertThat(
        "should throw error when fundingType is empty but fundingDetails is filled",
        fundingUpdateXls.getErrorMessage(),
        CoreMatchers.containsString(
            FundingUpdateTransformerService.FUNDING_TYPE_IS_REQUIRED_FOR_DETAILS));
  }

  @Test
  public void canUpdateFields() {
    when(tcsServiceImpl.updateFunding(postFundingDtoArgumentCaptor.capture()))
        .thenReturn(null);
    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();

    MatcherAssert.assertThat("Should update fundingType",
        postFundingDtoArgumentCaptorValue.getFundingType(),
        CoreMatchers.equalTo(fundingUpdateXls.getFundingType()));
    MatcherAssert.assertThat("Should update fundingTypeOther",
        postFundingDtoArgumentCaptorValue.getInfo(),
        CoreMatchers.nullValue());
    MatcherAssert.assertThat("Should update fundingBody",
        postFundingDtoArgumentCaptorValue.getFundingBodyId(),
        CoreMatchers.equalTo("1"));
    MatcherAssert.assertThat("Should update dateFrom",
        postFundingDtoArgumentCaptorValue.getStartDate(),
        CoreMatchers.equalTo(convertDate(fundingUpdateXls.getDateFrom())));
    MatcherAssert.assertThat("Should update dateTo",
        postFundingDtoArgumentCaptorValue.getEndDate(),
        CoreMatchers.equalTo(convertDate(fundingUpdateXls.getDateTo())));
  }

  @Test
  public void canUpdateFieldsWhenFundingTypeIsOther() {
    String fundingType = FUNDING_TYPE_OTHER.toUpperCase();
    fundingUpdateXls.setFundingType(fundingType);
    fundingUpdateXls.setFundingTypeOther("other type");

    FundingTypeDTO fundingTypeDto = new FundingTypeDTO();
    fundingTypeDto.setLabel(FUNDING_TYPE_OTHER);
    when(referenceServiceImpl.findCurrentFundingTypesByLabelIn(Collections.singleton(fundingType)))
        .thenReturn(Collections.singletonList(fundingTypeDto));
    when(tcsServiceImpl.updateFunding(postFundingDtoArgumentCaptor.capture()))
        .thenReturn(null);
    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();

    MatcherAssert.assertThat("Should update fundingType",
        postFundingDtoArgumentCaptorValue.getFundingType(),
        CoreMatchers.equalTo(FUNDING_TYPE_OTHER));
    MatcherAssert.assertThat("Should update fundingTypeOther",
        postFundingDtoArgumentCaptorValue.getInfo(),
        CoreMatchers.equalTo(fundingUpdateXls.getFundingTypeOther()));
    MatcherAssert.assertThat("Should update fundingBody",
        postFundingDtoArgumentCaptorValue.getFundingBodyId(),
        CoreMatchers.equalTo("1"));
    MatcherAssert.assertThat("Should update dateFrom",
        postFundingDtoArgumentCaptorValue.getStartDate(),
        CoreMatchers.equalTo(convertDate(fundingUpdateXls.getDateFrom())));
    MatcherAssert.assertThat("Should update dateTo",
        postFundingDtoArgumentCaptorValue.getEndDate(),
        CoreMatchers.equalTo(convertDate(fundingUpdateXls.getDateTo())));
  }

  @Test
  public void canUpdateFieldsWhenFundingTypeIsAnAcademicType() {
    fundingUpdateXls.setFundingType(FUNDING_TYPE_ACADEMIC);
    fundingUpdateXls.setFundingTypeOther("details");

    when(tcsServiceImpl.updateFunding(postFundingDtoArgumentCaptor.capture()))
        .thenReturn(null);
    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();

    MatcherAssert.assertThat("Should update fundingType",
        postFundingDtoArgumentCaptorValue.getFundingType(),
        CoreMatchers.equalTo(FUNDING_TYPE_ACADEMIC));
    MatcherAssert.assertThat("Should update fundingTypeOther",
        postFundingDtoArgumentCaptorValue.getInfo(),
        CoreMatchers.equalTo(fundingUpdateXls.getFundingTypeOther()));
    MatcherAssert.assertThat("Should update fundingBody",
        postFundingDtoArgumentCaptorValue.getFundingBodyId(),
        CoreMatchers.equalTo("1"));
    MatcherAssert.assertThat("Should update dateFrom",
        postFundingDtoArgumentCaptorValue.getStartDate(),
        CoreMatchers.equalTo(convertDate(fundingUpdateXls.getDateFrom())));
    MatcherAssert.assertThat("Should update dateTo",
        postFundingDtoArgumentCaptorValue.getEndDate(),
        CoreMatchers.equalTo(convertDate(fundingUpdateXls.getDateTo())));
  }

  @Test
  public void ShouldNotUpdateFieldsWhenNull() {
    fundingUpdateXls.setFundingType(null);
    fundingUpdateXls.setFundingTypeOther(null);
    fundingUpdateXls.setFundingBody(null);
    fundingUpdateXls.setDateFrom(null);
    fundingUpdateXls.setDateTo(null);

    when(tcsServiceImpl.updateFunding(postFundingDtoArgumentCaptor.capture()))
        .thenReturn(null);
    fundingUpdateTransformerService.processFundingUpdateUpload(
        Collections.singletonList(fundingUpdateXls));

    PostFundingDTO postFundingDtoArgumentCaptorValue = postFundingDtoArgumentCaptor.getValue();
    MatcherAssert.assertThat("Should not update fundingType",
        postFundingDtoArgumentCaptorValue.getFundingType(),
        CoreMatchers.equalTo(postFundingDto.getFundingType()));
    MatcherAssert.assertThat("Should not update fundingTypeOther",
        postFundingDtoArgumentCaptorValue.getInfo(),
        CoreMatchers.equalTo(postFundingDto.getInfo()));
    MatcherAssert.assertThat("Should not update fundingBody",
        postFundingDtoArgumentCaptorValue.getFundingBodyId(),
        CoreMatchers.equalTo(postFundingDto.getFundingBodyId()));
    MatcherAssert.assertThat("Should not update dateFrom",
        postFundingDtoArgumentCaptorValue.getStartDate(),
        CoreMatchers.equalTo(postFundingDto.getStartDate()));
    MatcherAssert.assertThat("Should not update dateTo",
        postFundingDtoArgumentCaptorValue.getEndDate(),
        CoreMatchers.equalTo(postFundingDto.getEndDate()));
  }
}
