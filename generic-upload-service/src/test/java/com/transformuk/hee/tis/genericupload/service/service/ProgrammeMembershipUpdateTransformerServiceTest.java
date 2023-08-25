package com.transformuk.hee.tis.genericupload.service.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.genericupload.api.dto.ProgrammeMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.service.service.mapper.ProgrammeMembershipMapperImpl;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;

@RunWith(SpringRunner.class)
public class ProgrammeMembershipUpdateTransformerServiceTest {

  private static final UUID PROGRAMME_MEMBERSHIP_UUID = UUID.randomUUID();
  private static final String PROGRAMME_MEMBERSHIP_ID = PROGRAMME_MEMBERSHIP_UUID.toString();
  private static final String PROGRAMME_MEMBERSHIP_TYPE = "SUBSTANTIVE";

  @Mock
  TcsServiceImpl tcsServiceMock;
  @InjectMocks
  private ProgrammeMembershipUpdateTransformerService testObj;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(testObj, "pmMapper", new ProgrammeMembershipMapperImpl());
  }

  @Test
  public void shouldReturnErrorWhenPmTypeNotExists() {
    ProgrammeMembershipUpdateXls xls = new ProgrammeMembershipUpdateXls();
    String pmType = "Not found";
    xls.setProgrammeMembershipType(pmType);

    List<String> errMsg = testObj.initialValidate(xls);
    Assert.assertEquals(1, errMsg.size());
    Assert.assertEquals(String.format(
            ProgrammeMembershipUpdateTransformerService.PROGRAMME_MEMBERSHIP_TYPE_NOT_EXISTS, pmType),
        errMsg.get(0));
  }

  @Test
  public void shouldNotReturnErrorWhenPmTypeExists() {
    ProgrammeMembershipUpdateXls xls = new ProgrammeMembershipUpdateXls();
    xls.setProgrammeMembershipType(PROGRAMME_MEMBERSHIP_TYPE);

    List<String> errMsg = testObj.initialValidate(xls);
    Assert.assertEquals(0, errMsg.size());
  }

  @Test
  public void shouldHandleDuplicateIds() {
    ProgrammeMembershipUpdateXls xls1 = new ProgrammeMembershipUpdateXls();
    ProgrammeMembershipUpdateXls xls2 = new ProgrammeMembershipUpdateXls();
    ProgrammeMembershipUpdateXls xls3 = new ProgrammeMembershipUpdateXls();
    xls1.setProgrammeMembershipId(PROGRAMME_MEMBERSHIP_ID);
    xls2.setProgrammeMembershipId(PROGRAMME_MEMBERSHIP_ID);
    String id3 = UUID.randomUUID().toString();
    xls3.setProgrammeMembershipId(id3);

    List<ProgrammeMembershipUpdateXls> xlsList =
        testObj.handleDuplicateIds(Lists.newArrayList(xls1, xls2, xls3));

    Assert.assertEquals(1, xlsList.size());
    Assert.assertEquals(id3, xlsList.get(0).getProgrammeMembershipId());
  }

  @Test
  public void testProcessPmUpdateUpload_duplicateIds() {
    ProgrammeMembershipUpdateXls xls1 = new ProgrammeMembershipUpdateXls();
    ProgrammeMembershipUpdateXls xls2 = new ProgrammeMembershipUpdateXls();
    xls1.setProgrammeMembershipId(PROGRAMME_MEMBERSHIP_ID);
    xls2.setProgrammeMembershipId(PROGRAMME_MEMBERSHIP_ID);

    List<ProgrammeMembershipUpdateXls> xlsList = Lists.newArrayList(xls1, xls2);

    testObj.processProgrammeMembershipsUpdateUpload(xlsList);

    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(String.format(ProgrammeMembershipUpdateTransformerService.PM_ID_IS_DUPLICATE,
            PROGRAMME_MEMBERSHIP_ID)));
    assertThat("Should get error", xlsList.get(1).getErrorMessage(),
        is(String.format(ProgrammeMembershipUpdateTransformerService.PM_ID_IS_DUPLICATE,
            PROGRAMME_MEMBERSHIP_ID)));
  }

  @Test
  public void testProcessPmUpdateUpload_noError() {
    ProgrammeMembershipUpdateXls xls = Mockito.spy(new ProgrammeMembershipUpdateXls());
    xls.setProgrammeMembershipId(PROGRAMME_MEMBERSHIP_ID);
    xls.setProgrammeMembershipType(PROGRAMME_MEMBERSHIP_TYPE);
    List<ProgrammeMembershipUpdateXls> xlsList = Lists.newArrayList(xls);

    ProgrammeMembershipDTO pmDto = new ProgrammeMembershipDTO();
    pmDto.setUuid(PROGRAMME_MEMBERSHIP_UUID);
    pmDto.setProgrammeMembershipType(ProgrammeMembershipType.SUBSTANTIVE);

    when(tcsServiceMock.patchProgrammeMembership(any(ProgrammeMembershipDTO.class))).thenReturn(
        pmDto);

    testObj.processProgrammeMembershipsUpdateUpload(xlsList);

    verify(xls).setSuccessfullyImported(true);
    Assert.assertNull(xls.getErrorMessage());
  }

  @Test
  public void testProcessPmUpdateUpload_errMsg() {
    ProgrammeMembershipUpdateXls xls = Mockito.spy(new ProgrammeMembershipUpdateXls());
    xls.setProgrammeMembershipId(PROGRAMME_MEMBERSHIP_ID);
    xls.setProgrammeMembershipType(PROGRAMME_MEMBERSHIP_TYPE);
    List<ProgrammeMembershipUpdateXls> xlsList = Lists.newArrayList(xls);

    ProgrammeMembershipDTO pmDto = new ProgrammeMembershipDTO();
    pmDto.setUuid(PROGRAMME_MEMBERSHIP_UUID);
    pmDto.setProgrammeMembershipType(ProgrammeMembershipType.SUBSTANTIVE);
    String errMsg = "error";
    List<String> errorMsgs = Lists.newArrayList(errMsg);
    pmDto.setMessageList(errorMsgs);

    when(tcsServiceMock.patchProgrammeMembership(any(ProgrammeMembershipDTO.class))).thenReturn(
        pmDto);

    testObj.processProgrammeMembershipsUpdateUpload(xlsList);

    verify(xls).addErrorMessages(errorMsgs);
    Assert.assertEquals(errMsg, xls.getErrorMessage());
  }

  @Test
  public void testProcessPmUpdateUpload_shouldHandleException() {
    ProgrammeMembershipUpdateXls xls = Mockito.spy(new ProgrammeMembershipUpdateXls());
    xls.setProgrammeMembershipId(PROGRAMME_MEMBERSHIP_ID);
    xls.setProgrammeMembershipType(PROGRAMME_MEMBERSHIP_TYPE);
    List<ProgrammeMembershipUpdateXls> xlsList = Lists.newArrayList(xls);

    doThrow(new ResourceAccessException("")).when(tcsServiceMock)
        .patchProgrammeMembership(any(ProgrammeMembershipDTO.class));

    testObj.processProgrammeMembershipsUpdateUpload(xlsList);
    Assert.assertEquals(ProgrammeMembershipUpdateTransformerService.UNEXPECTED_ERROR,
        xls.getErrorMessage());
  }
}
