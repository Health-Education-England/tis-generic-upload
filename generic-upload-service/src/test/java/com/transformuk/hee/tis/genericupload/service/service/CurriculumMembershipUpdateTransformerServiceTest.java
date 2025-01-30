package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.service.CurriculumMembershipCreateTransformerService.PM_ID_NOT_UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.service.service.mapper.CurriculumMembershipMapper;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.io.IOException;
import java.time.LocalDate;
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
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class CurriculumMembershipUpdateTransformerServiceTest {

  private static final UUID PROGRAMME_MEMBERSHIP_UUID = UUID.randomUUID();
  private static final LocalDate CURRICULUM_START_DATE = LocalDate.now().minusDays(1);
  private static final LocalDate CURRICULUM_END_DATE = LocalDate.now().plusDays(1);
  private static final Long CURRICULUM_ID = 1L;
  private static final String CURRICULUM_MEMEBERSHIP_ID_1 = "111";
  private static final String CURRICULUM_MEMEBERSHIP_ID_2 = "112";

  @InjectMocks
  CurriculumMembershipUpdateTransformerService service;
  @Mock
  TcsServiceImpl tcsService;
  @Mock
  CurriculumMembershipMapper cmMapper;
  @Captor
  ArgumentCaptor<CurriculumMembershipDTO> cmCaptor;
  private CurriculumMembershipUpdateXls xls1, xls2;
  private CurriculumMembershipDTO dto1, dto2;
  private CurriculumMembershipDTO patchedCmDto;

  @BeforeEach
  void setUp() {
    xls1 = new CurriculumMembershipUpdateXls();
    xls1.setTisCurriculumMembershipId(CURRICULUM_MEMEBERSHIP_ID_1);
    xls1.setTisProgrammeMembershipId(PROGRAMME_MEMBERSHIP_UUID.toString());
    xls1.setCurriculumStartDate(CURRICULUM_START_DATE);
    xls1.setCurriculumEndDate(CURRICULUM_END_DATE);

    xls2 = new CurriculumMembershipUpdateXls();
    xls2.setTisCurriculumMembershipId(CURRICULUM_MEMEBERSHIP_ID_2);
    xls2.setTisProgrammeMembershipId("123456");
    xls2.setCurriculumStartDate(CURRICULUM_START_DATE);
    xls2.setCurriculumEndDate(CURRICULUM_END_DATE);

    dto1 = new CurriculumMembershipDTO();
    dto1.setId(Long.valueOf(CURRICULUM_MEMEBERSHIP_ID_1));
    dto1.setProgrammeMembershipUuid(PROGRAMME_MEMBERSHIP_UUID);
    dto1.setCurriculumStartDate(CURRICULUM_START_DATE);
    dto1.setCurriculumEndDate(CURRICULUM_END_DATE);

    dto2 = new CurriculumMembershipDTO();
    dto2.setId(Long.valueOf(CURRICULUM_MEMEBERSHIP_ID_2));
    dto2.setProgrammeMembershipUuid(PROGRAMME_MEMBERSHIP_UUID);
    dto2.setCurriculumStartDate(CURRICULUM_START_DATE);
    dto2.setCurriculumEndDate(CURRICULUM_END_DATE);

    patchedCmDto = new CurriculumMembershipDTO();
    patchedCmDto.addMessage("Test error message");
  }

  @Test
  void shouldFailValidationWhenPmUuidNotValid() {
    doThrow(new IllegalArgumentException("Invalid UUID string: " + PROGRAMME_MEMBERSHIP_UUID)).when(
        cmMapper).toDto(xls2);

    service.processCurriculumMembershipUpdateUpload(Collections.singletonList(xls2));

    assertEquals(PM_ID_NOT_UUID, xls2.getErrorMessage());
  }

  @Test
  void shouldFailValidationWhenUpdateCMReturnsMethodArgumentNotValidException() {
    when(cmMapper.toDto(xls1)).thenReturn(dto1);
    CurriculumDTO curriculumDto1 = new CurriculumDTO();
    curriculumDto1.setId(CURRICULUM_ID);

    String errorJson = "{\"message\":\"error.validation\",\"description\":null,\"fieldErrors\":"
        + "[{\"objectName\":\"curriculumMembershipDTO\",\"field\":\"curriculumId\","
        + "\"message\": \"Curriculum is required\"}]}";

    ResourceAccessException rae = new ResourceAccessException("message",
        new IOException(errorJson));
    doThrow(rae).when(tcsService).patchCurriculumMembership(dto1);

    service.processCurriculumMembershipUpdateUpload(Collections.singletonList(xls1));
    assertTrue(xls1.getErrorMessage().contains("Curriculum is required"));
  }

  @Test
  void shouldProcessValidUpdate() {
    when(cmMapper.toDto(xls1)).thenReturn(dto1);

    when(tcsService.patchCurriculumMembership(cmCaptor.capture())).thenReturn(dto1);

    service.processCurriculumMembershipUpdateUpload(Collections.singletonList(xls1));

    assertFalse(xls1.hasErrors());
    assertTrue(xls1.isSuccessfullyImported());
    assertEquals(Long.valueOf(CURRICULUM_MEMEBERSHIP_ID_1), cmCaptor.getValue().getId());
  }

  @Test
  void shouldAddErrorMessagesWhenProcessUpdateReturnsErrors() {
    when(cmMapper.toDto(xls1)).thenReturn(dto1);

    when(tcsService.patchCurriculumMembership(cmCaptor.capture())).thenReturn(patchedCmDto);

    service.processCurriculumMembershipUpdateUpload(Collections.singletonList(xls1));

    assertTrue(xls1.hasErrors());
    assertFalse(xls1.isSuccessfullyImported());
  }

  @Test
  void shouldAddErrorMessagesWhenTemplateHasStartAndDateNull() {
    xls1.setCurriculumStartDate(null);
    xls1.setCurriculumEndDate(null);
    when(cmMapper.toDto(xls1)).thenReturn(dto1);

    service.processCurriculumMembershipUpdateUpload(Collections.singletonList(xls1));

    assertTrue(xls1.hasErrors());
    assertEquals("Start date and end date cannot both be empty.", xls1.getErrorMessage());
    assertFalse(xls1.isSuccessfullyImported());
  }
}