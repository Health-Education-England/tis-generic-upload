package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.FundingUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.FundingTypeDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
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

import java.time.LocalDate;
import java.util.*;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.mockito.Mockito.when;

/**
 * The unit tests for {@link FundingUpdateTransformerService}.
 */
@RunWith(SpringRunner.class)
public class FundingUpdateTransformServiceTest {

  private static final String DID_NOT_FIND_POST_FUNDING_FOR_ID = "Did not find the postFunding for id";
  private static final String ERROR_INVALID_FUNDING_BODY_NAME = "Funding body could not be found for the name";
  private static final String ERROR_INVALID_FUNDING_TYPE = "Funding type could not be found for the label";
  private static final String FUNDING_TYPE_IS_NOT_OTHER = "Funding type specified filled although type is not Other.";
  private static final String UPDATE_FAILED = "Update failed.";

  @InjectMocks
  private FundingUpdateTransformerService fundingUpdateTransformerService;

  @Mock
  private TcsServiceImpl tcsServiceImpl;
  @Mock
  private ReferenceServiceImpl referenceServiceImpl;

  @Captor
  private ArgumentCaptor<PostFundingDTO> postFundingDTOArgumentCaptor;

  private FundingUpdateXLS fundingUpdateXLS;

  private PostFundingDTO postFundingDTO;

  @Before
  public void setUp() {

    // initialise fundingUpdateXLS
    fundingUpdateXLS = new FundingUpdateXLS();
    fundingUpdateXLS.setPostFundingTisId("1");
    fundingUpdateXLS.setFundingType("Academic");
    fundingUpdateXLS.setFundingTypeOther(null);
    fundingUpdateXLS.setFundingBody("validFundingBody");
    Calendar cFrom = Calendar.getInstance();
    cFrom.set(2019, 8, 1); // 2019-09-01
    fundingUpdateXLS.setDateFrom(cFrom.getTime());
    Calendar cTo = Calendar.getInstance();
    cTo.set(2019, 8, 2);
    fundingUpdateXLS.setDateTo(cTo.getTime());

    // initialise trustDTO
    List<TrustDTO> trusts = new ArrayList<>();
    TrustDTO trustDTO = new TrustDTO();
    trustDTO.setTrustKnownAs("validFundingBody");
    trustDTO.setId(1L);
    trusts.add(trustDTO);
    when(referenceServiceImpl.findCurrentTrustsByTrustKnownAsIn(new HashSet<>(Arrays.asList("validFundingBody"))))
        .thenReturn(trusts);

    // initialise fundingTypeLabels
    List<FundingTypeDTO> fundingTypes = new ArrayList<>();
    FundingTypeDTO fundingTypeDTO = new FundingTypeDTO();
    fundingTypeDTO.setLabel("Academic");
    fundingTypes.add(fundingTypeDTO);
    when(referenceServiceImpl.findCurrentFundingTypesByLabelIn(new HashSet<>(Arrays.asList("Academic"))))
        .thenReturn(fundingTypes);

    // initialise postFundingDTO
    postFundingDTO = new PostFundingDTO();
    postFundingDTO.setFundingBodyId("2");
    postFundingDTO.setFundingType("originalType");
    postFundingDTO.setInfo(null);
    postFundingDTO.setStartDate(LocalDate.now());
    postFundingDTO.setEndDate(LocalDate.now().plusDays(1));
    postFundingDTO.setPostId(1L);

    when(tcsServiceImpl.getPostFundingById(1L)).thenReturn(postFundingDTO);
  }

  @Test
  public void canHandleUnknownPostFundingId() {
    fundingUpdateXLS.setPostFundingTisId("999");
    when(tcsServiceImpl.getPostFundingById(999L)).thenReturn(null);

    List<FundingUpdateXLS> fundingUpdateXLSList = Arrays.asList(fundingUpdateXLS);
    fundingUpdateTransformerService.processFundingUpdateUpload(fundingUpdateXLSList);

    MatcherAssert.assertThat("Can handle unknown post funding id",
        fundingUpdateXLS.getErrorMessage(),
        CoreMatchers.containsString(DID_NOT_FIND_POST_FUNDING_FOR_ID));
  }

  @Test
  public void canHandNonNumberPostFundingId() {
    fundingUpdateXLS.setPostFundingTisId("XXX");

    List<FundingUpdateXLS> fundingUpdateXLSList = Arrays.asList(fundingUpdateXLS);
    fundingUpdateTransformerService.processFundingUpdateUpload(fundingUpdateXLSList);

    MatcherAssert.assertThat("Can handle non-number post funding id",
        fundingUpdateXLS.getErrorMessage(),
        CoreMatchers.containsString(DID_NOT_FIND_POST_FUNDING_FOR_ID));
  }

  @Test
  public void canHandleUnknownFundingBody() {
    fundingUpdateXLS.setFundingBody("Unknown");

    List<FundingUpdateXLS> fundingUpdateXLSList = Arrays.asList(fundingUpdateXLS);
    fundingUpdateTransformerService.processFundingUpdateUpload(fundingUpdateXLSList);

    MatcherAssert.assertThat("Can handle unknown funding body",
        fundingUpdateXLS.getErrorMessage(),
        CoreMatchers.containsString(ERROR_INVALID_FUNDING_BODY_NAME));
  }

  @Test
  public void canHandleUnknownFundingType() {
    fundingUpdateXLS.setFundingType("Unknown");

    List<FundingUpdateXLS> fundingUpdateXLSList = Arrays.asList(fundingUpdateXLS);
    fundingUpdateTransformerService.processFundingUpdateUpload(fundingUpdateXLSList);

    MatcherAssert.assertThat("Can handle unknown funding type",
        fundingUpdateXLS.getErrorMessage(),
        CoreMatchers.containsString(ERROR_INVALID_FUNDING_TYPE));
  }

  @Test
  public void canHandleSpecifiedFundingTypeWhenNotOther() {
    fundingUpdateXLS.setFundingType("Academic");
    fundingUpdateXLS.setFundingTypeOther("other type");

    List<FundingUpdateXLS> fundingUpdateXLSList = Arrays.asList(fundingUpdateXLS);
    fundingUpdateTransformerService.processFundingUpdateUpload(fundingUpdateXLSList);

    MatcherAssert.assertThat("Can handle specified funding type when type is not Other",
        fundingUpdateXLS.getErrorMessage(),
        CoreMatchers.containsString(FUNDING_TYPE_IS_NOT_OTHER));
  }

  @Test
  public void canUpdateFields() {
    List<FundingUpdateXLS> fundingUpdateXLSList = Arrays.asList(fundingUpdateXLS);
    when(tcsServiceImpl.updateFunding(postFundingDTOArgumentCaptor.capture()))
        .thenReturn(null);
    fundingUpdateTransformerService.processFundingUpdateUpload(fundingUpdateXLSList);

    PostFundingDTO postFundingDTOArgumentCaptorValue = postFundingDTOArgumentCaptor.getValue();

    MatcherAssert.assertThat("Should update fundingType",
        postFundingDTOArgumentCaptorValue.getFundingType(),
        CoreMatchers.equalTo(fundingUpdateXLS.getFundingType()));
    MatcherAssert.assertThat("Should update fundingTypeOther",
        postFundingDTOArgumentCaptorValue.getInfo(),
        CoreMatchers.nullValue());
    MatcherAssert.assertThat("Should update fundingBody",
        postFundingDTOArgumentCaptorValue.getFundingBodyId(),
        CoreMatchers.equalTo("1"));
    MatcherAssert.assertThat("Should update dateFrom",
        postFundingDTOArgumentCaptorValue.getStartDate(),
        CoreMatchers.equalTo(convertDate(fundingUpdateXLS.getDateFrom())));
    MatcherAssert.assertThat("Should update dateTo",
        postFundingDTOArgumentCaptorValue.getEndDate(),
        CoreMatchers.equalTo(convertDate(fundingUpdateXLS.getDateTo())));
  }

  @Test
  public void canUpdateFieldsWhenFundingTypeIsOther() {
    fundingUpdateXLS.setFundingType("Other");
    fundingUpdateXLS.setFundingTypeOther("other type");
    List<FundingUpdateXLS> fundingUpdateXLSList = Arrays.asList(fundingUpdateXLS);
    when(tcsServiceImpl.updateFunding(postFundingDTOArgumentCaptor.capture()))
        .thenReturn(null);
    fundingUpdateTransformerService.processFundingUpdateUpload(fundingUpdateXLSList);

    PostFundingDTO postFundingDTOArgumentCaptorValue = postFundingDTOArgumentCaptor.getValue();

    MatcherAssert.assertThat("Should update fundingType",
        postFundingDTOArgumentCaptorValue.getFundingType(),
        CoreMatchers.equalTo("Other"));
    MatcherAssert.assertThat("Should update fundingTypeOther",
        postFundingDTOArgumentCaptorValue.getInfo(),
        CoreMatchers.equalTo(fundingUpdateXLS.getFundingTypeOther()));
    MatcherAssert.assertThat("Should update fundingBody",
        postFundingDTOArgumentCaptorValue.getFundingBodyId(),
        CoreMatchers.equalTo("1"));
    MatcherAssert.assertThat("Should update dateFrom",
        postFundingDTOArgumentCaptorValue.getStartDate(),
        CoreMatchers.equalTo(convertDate(fundingUpdateXLS.getDateFrom())));
    MatcherAssert.assertThat("Should update dateTo",
        postFundingDTOArgumentCaptorValue.getEndDate(),
        CoreMatchers.equalTo(convertDate(fundingUpdateXLS.getDateTo())));
  }

  @Test
  public void ShouldNotUpdateFieldsWhenNull() {
    fundingUpdateXLS.setFundingType(null);
    fundingUpdateXLS.setFundingTypeOther(null);
    fundingUpdateXLS.setFundingBody(null);
    fundingUpdateXLS.setDateFrom(null);
    fundingUpdateXLS.setDateTo(null);
    List<FundingUpdateXLS> fundingUpdateXLSList = Arrays.asList(fundingUpdateXLS);

    when(tcsServiceImpl.updateFunding(postFundingDTOArgumentCaptor.capture()))
        .thenReturn(null);
    fundingUpdateTransformerService.processFundingUpdateUpload(fundingUpdateXLSList);

    PostFundingDTO postFundingDTOArgumentCaptorValue = postFundingDTOArgumentCaptor.getValue();
    MatcherAssert.assertThat("Should not update fundingType",
        postFundingDTOArgumentCaptorValue.getFundingType(),
        CoreMatchers.equalTo(postFundingDTO.getFundingType()));
    MatcherAssert.assertThat("Should not update fundingTypeOther",
        postFundingDTOArgumentCaptorValue.getInfo(),
        CoreMatchers.equalTo(postFundingDTO.getInfo()));
    MatcherAssert.assertThat("Should not update fundingBody",
        postFundingDTOArgumentCaptorValue.getFundingBodyId(),
        CoreMatchers.equalTo(postFundingDTO.getFundingBodyId()));
    MatcherAssert.assertThat("Should not update dateFrom",
        postFundingDTOArgumentCaptorValue.getStartDate(),
        CoreMatchers.equalTo(postFundingDTO.getStartDate()));
    MatcherAssert.assertThat("Should not update dateTo",
        postFundingDTOArgumentCaptorValue.getEndDate(),
        CoreMatchers.equalTo(postFundingDTO.getEndDate()));
  }
}
