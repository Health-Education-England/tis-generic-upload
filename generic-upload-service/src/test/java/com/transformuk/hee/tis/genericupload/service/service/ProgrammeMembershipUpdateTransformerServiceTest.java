package com.transformuk.hee.tis.genericupload.service.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.genericupload.api.dto.ProgrammeMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.service.service.mapper.ProgrammeMembershipMapper;
import com.transformuk.hee.tis.genericupload.service.service.mapper.ProgrammeMembershipMapperImpl;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.ResourceAccessException;

@RunWith(MockitoJUnitRunner.class)
public class ProgrammeMembershipUpdateTransformerServiceTest {

  private static final UUID PROGRAMME_MEMBERSHIP_UUID = UUID.randomUUID();
  private static final String PROGRAMME_MEMBERSHIP_ID = PROGRAMME_MEMBERSHIP_UUID.toString();
  private static final String PROGRAMME_MEMBERSHIP_TYPE = "SUBSTANTIVE";

  @Mock
  private TcsServiceImpl tcsServiceMock;
  @InjectMocks
  private ProgrammeMembershipUpdateTransformerService testObj;
  @Spy
  private ProgrammeMembershipMapper pmMapper = new ProgrammeMembershipMapperImpl();

  @Test
  public void shouldReturnErrorWhenPmTypeNotExists() {
    ProgrammeMembershipUpdateXls xls = new ProgrammeMembershipUpdateXls();
    String pmType = "Not found";
    xls.setProgrammeMembershipType(pmType);

    List<String> errMsg = testObj.initialValidate(xls);
    assertEquals(1, errMsg.size());
    assertEquals(String.format(
            ProgrammeMembershipUpdateTransformerService.PROGRAMME_MEMBERSHIP_TYPE_NOT_EXISTS, pmType),
        errMsg.get(0));
  }

  @Test
  public void shouldNotReturnErrorWhenPmTypeExists() {
    ProgrammeMembershipUpdateXls xls = new ProgrammeMembershipUpdateXls();
    xls.setProgrammeMembershipType(PROGRAMME_MEMBERSHIP_TYPE);

    List<String> errMsg = testObj.initialValidate(xls);
    assertEquals(0, errMsg.size());
  }

  @Test
  public void shouldAcceptPmTypeCaseInsensitive() {
    ProgrammeMembershipUpdateXls xls = new ProgrammeMembershipUpdateXls();
    xls.setProgrammeMembershipType("Substantive");

    List<String> errMsg = testObj.initialValidate(xls);
    assertEquals(0, errMsg.size());
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

    assertEquals(1, xlsList.size());
    assertEquals(id3, xlsList.get(0).getProgrammeMembershipId());
  }

  @Test
  public void shouldHandleInvalidUuid() {
    ProgrammeMembershipUpdateXls xls1 = new ProgrammeMembershipUpdateXls();
    ProgrammeMembershipUpdateXls xls2 = new ProgrammeMembershipUpdateXls();
    xls1.setProgrammeMembershipId("invalidId");
    xls2.setProgrammeMembershipId("123456");

    List<ProgrammeMembershipUpdateXls> xlsList = Lists.newArrayList(xls1, xls2);

    testObj.processProgrammeMembershipsUpdateUpload(xlsList);

    assertEquals(String.format(
            ProgrammeMembershipUpdateTransformerService.PM_ID_NOT_UUID),
        xlsList.get(0).getErrorMessage());
    assertEquals(String.format(
            ProgrammeMembershipUpdateTransformerService.PM_ID_NOT_UUID),
        xlsList.get(1).getErrorMessage());
  }

  @Test
  public void testProcessPmUpdateUpload_duplicateIds() {
    ProgrammeMembershipUpdateXls xls1 = new ProgrammeMembershipUpdateXls();
    ProgrammeMembershipUpdateXls xls2 = new ProgrammeMembershipUpdateXls();
    xls1.setProgrammeMembershipId(PROGRAMME_MEMBERSHIP_ID);
    xls2.setProgrammeMembershipId(PROGRAMME_MEMBERSHIP_ID);

    List<ProgrammeMembershipUpdateXls> xlsList = Lists.newArrayList(xls1, xls2);

    testObj.processProgrammeMembershipsUpdateUpload(xlsList);

    assertEquals(String.format(
        ProgrammeMembershipUpdateTransformerService.PM_ID_IS_DUPLICATE,
        PROGRAMME_MEMBERSHIP_ID), xlsList.get(0).getErrorMessage());
    assertEquals(String.format(
        ProgrammeMembershipUpdateTransformerService.PM_ID_IS_DUPLICATE,
        PROGRAMME_MEMBERSHIP_ID), xlsList.get(1).getErrorMessage());
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
    assertNull(xls.getErrorMessage());
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
    assertEquals(errMsg, xls.getErrorMessage());
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
    assertEquals(ProgrammeMembershipUpdateTransformerService.UNEXPECTED_ERROR,
        xls.getErrorMessage());
  }
}
