package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.service.CurriculumMembershipCreateTransformerService.CURRICULUM_NOT_FOUND;
import static com.transformuk.hee.tis.genericupload.service.service.CurriculumMembershipCreateTransformerService.MULTIPLE_CURRICULA_FOUND;
import static com.transformuk.hee.tis.genericupload.service.service.CurriculumMembershipCreateTransformerService.PM_ID_NOT_UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXLS;
import com.transformuk.hee.tis.genericupload.service.service.mapper.CurriculumMembershipMapper;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.assertj.core.util.Lists;
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
class CurriculumMembershipCreateTransformerServiceTest {

  private static final UUID PROGRAMME_MEMBERSHIP_UUID = UUID.randomUUID();
  private static final String CURRICULUM_NAME = "curriculum1";
  private static final Long CURRICULUM_ID = 1L;
  private static final LocalDate CURRICULUM_START_DATE = LocalDate.now().minusDays(1);
  private static final LocalDate CURRICULUM_END_DATE = LocalDate.now().plusDays(1);
  @InjectMocks
  CurriculumMembershipCreateTransformerService service;
  @Mock
  TcsServiceImpl tcsService;
  @Mock
  CurriculumMembershipMapper cmMapper;
  @Captor
  ArgumentCaptor<CurriculumMembershipDTO> cmCaptor;
  private CurriculumMembershipCreateXLS xls1, xls2;
  private CurriculumMembershipDTO dto1, dto2;

  @BeforeEach
  void setUp() {
    xls1 = new CurriculumMembershipCreateXLS();
    xls1.setProgrammeMembershipUuid(PROGRAMME_MEMBERSHIP_UUID.toString());
    xls1.setCurriculumName(CURRICULUM_NAME);
    xls1.setCurriculumStartDate(CURRICULUM_START_DATE);
    xls1.setCurriculumEndDate(CURRICULUM_END_DATE);

    xls2 = new CurriculumMembershipCreateXLS();
    xls2.setProgrammeMembershipUuid("123456");
    xls2.setCurriculumName(CURRICULUM_NAME);
    xls2.setCurriculumStartDate(CURRICULUM_START_DATE);
    xls2.setCurriculumEndDate(CURRICULUM_END_DATE);

    dto1 = new CurriculumMembershipDTO();
    dto1.setProgrammeMembershipUuid(PROGRAMME_MEMBERSHIP_UUID);
    dto1.setCurriculumStartDate(CURRICULUM_START_DATE);
    dto1.setCurriculumEndDate(CURRICULUM_END_DATE);

    dto2 = new CurriculumMembershipDTO();
    dto2.setId(1L);
    dto2.setProgrammeMembershipUuid(PROGRAMME_MEMBERSHIP_UUID);
    dto2.setCurriculumStartDate(CURRICULUM_START_DATE);
    dto2.setCurriculumEndDate(CURRICULUM_END_DATE);
  }

  @Test
  void shouldFailValidationWhenPmUuidNotValid() {
    doThrow(new IllegalArgumentException("Invalid UUID string: " + PROGRAMME_MEMBERSHIP_UUID)).when(
        cmMapper).toDto(xls2);

    service.processCurriculumMembershipCreateUpload(Collections.singletonList(xls2));

    assertEquals(PM_ID_NOT_UUID, xls2.getErrorMessage());
  }

  @Test
  void shouldFailValidationWhenCurriculumNotFound() {
    when(cmMapper.toDto(xls1)).thenReturn(dto1);
    when(tcsService.getCurriculaByName(CURRICULUM_NAME)).thenReturn(Lists.emptyList());

    service.processCurriculumMembershipCreateUpload(Collections.singletonList(xls1));

    assertEquals(String.format(CURRICULUM_NOT_FOUND, CURRICULUM_NAME), xls1.getErrorMessage());
  }

  @Test
  void shouldFailValidationWhenMultipleCurriculaFound() {
    when(cmMapper.toDto(xls1)).thenReturn(dto1);
    CurriculumDTO curriculumDto1 = new CurriculumDTO();
    CurriculumDTO curriculumDto2 = new CurriculumDTO();
    when(tcsService.getCurriculaByName(CURRICULUM_NAME)).thenReturn(
        Arrays.asList(curriculumDto1, curriculumDto2));

    service.processCurriculumMembershipCreateUpload(Collections.singletonList(xls1));

    assertEquals(String.format(MULTIPLE_CURRICULA_FOUND, CURRICULUM_NAME), xls1.getErrorMessage());
  }

  @Test
  void shouldFailValidationWhenCreateCMReturnsMethodArgumentNotValidException() {
    when(cmMapper.toDto(xls1)).thenReturn(dto1);
    CurriculumDTO curriculumDto1 = new CurriculumDTO();
    curriculumDto1.setId(CURRICULUM_ID);
    when(tcsService.getCurriculaByName(CURRICULUM_NAME)).thenReturn(
        Collections.singletonList(curriculumDto1));

    String errorJson = "{\"message\":\"error.validation\",\"description\":null,\"fieldErrors\":"
        + "[{\"objectName\":\"curriculumMembershipDTO\",\"field\":\"curriculumId\","
        + "\"message\": \"Curriculum is required\"}]}";
    ResourceAccessException rae = new ResourceAccessException("message",
        new IOException(errorJson));
    doThrow(rae).when(tcsService).createCurriculumMembership(dto1);

    service.processCurriculumMembershipCreateUpload(Collections.singletonList(xls1));

    assertTrue(xls1.getErrorMessage().contains("Curriculum is required"));
  }

  @Test
  void shouldProcessValidUpload() {
    when(cmMapper.toDto(xls1)).thenReturn(dto1);
    CurriculumDTO curriculumDto1 = new CurriculumDTO();
    curriculumDto1.setId(1L);
    when(tcsService.getCurriculaByName(CURRICULUM_NAME)).thenReturn(
        Collections.singletonList(curriculumDto1));
    when(tcsService.createCurriculumMembership(cmCaptor.capture())).thenReturn(dto2);

    service.processCurriculumMembershipCreateUpload(Collections.singletonList(xls1));

    assertFalse(xls1.hasErrors());
    assertTrue(xls1.isSuccessfullyImported());
    assertEquals(CURRICULUM_ID, cmCaptor.getValue().getCurriculumId());
  }
}
