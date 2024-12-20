package com.transformuk.hee.tis.genericupload.service.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXLS;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.time.LocalDate;

import java.util.Arrays;
import java.util.List;
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
class CurriculumMembershipCreateTransformerServiceTest {

  private static final String NO_PROGRAMME_MEMBERSHIP_FOR_ID = "Could not find the programme membership for id \"%s\".";

  private static final String EMPTY_PROGRAMME_MEMBERSHIP_ID_FIELD = "Programme Membership Id field cannot be null or empty";

  private static final String EMPTY_CURRICULUM_NAME_FIELD = "Curriculum Name field cannot be null or empty";

  private static final String EMPTY_CURRICULUM_START_DATE_FIELD = "Curriculum Start Date field cannot be null or empty";

  private static final String EMPTY_CURRICULUM_END_DATE_FIELD = "Curriculum End Date field cannot be null or empty";
  @InjectMocks
  CurriculumMembershipCreateTransformerService service;

  @Mock
  TcsServiceImpl tcsService;

  @Captor
  ArgumentCaptor<ProgrammeMembershipDTO> pmCaptor;

  private CurriculumMembershipCreateXLS xlsValid, xlsInvalid1, xlsInvalid2, xlsInvalid3, xlsInvalid4;
  private final UUID TIS_PROGRAMME_MEMBERSHIP_ID = UUID.randomUUID();
  private final String CURRICULUM_NAME = "curriculum1";
  private final LocalDate CURRICULUM_START_DATE = LocalDate.now().minusDays(1);
  private final LocalDate CURRICULUM_END_DATE = LocalDate.now().plusDays(1);

  @BeforeEach
  void setUp() {
    xlsValid = new CurriculumMembershipCreateXLS();
    xlsValid.setTisProgrammeMembershipId(TIS_PROGRAMME_MEMBERSHIP_ID.toString());
    xlsValid.setCurriculumName(CURRICULUM_NAME);
    xlsValid.setCurriculumStartDate(CURRICULUM_START_DATE);
    xlsValid.setCurriculumEndDate(CURRICULUM_END_DATE);

    // missing tisProgrammeMembershipId
    xlsInvalid1 = new CurriculumMembershipCreateXLS();
    xlsInvalid1.setCurriculumName(CURRICULUM_NAME);
    xlsInvalid1.setCurriculumStartDate(CURRICULUM_START_DATE);
    xlsInvalid1.setCurriculumEndDate(CURRICULUM_END_DATE);

    // missing curriculumName
    xlsInvalid2 = new CurriculumMembershipCreateXLS();
    xlsInvalid2.setTisProgrammeMembershipId(TIS_PROGRAMME_MEMBERSHIP_ID.toString());
    xlsInvalid2.setCurriculumStartDate(CURRICULUM_START_DATE);
    xlsInvalid2.setCurriculumEndDate(CURRICULUM_END_DATE);

    // missing curriculumStartDate
    xlsInvalid3 = new CurriculumMembershipCreateXLS();
    xlsInvalid3.setTisProgrammeMembershipId(TIS_PROGRAMME_MEMBERSHIP_ID.toString());
    xlsInvalid3.setCurriculumName(CURRICULUM_NAME);
    xlsInvalid3.setCurriculumEndDate(CURRICULUM_END_DATE);

    // missing curriculumEndDate
    xlsInvalid4 = new CurriculumMembershipCreateXLS();
    xlsInvalid4.setTisProgrammeMembershipId(TIS_PROGRAMME_MEMBERSHIP_ID.toString());
    xlsInvalid4.setCurriculumName(CURRICULUM_NAME);
    xlsInvalid4.setCurriculumStartDate(CURRICULUM_START_DATE);
  }

  @Test
  void shouldFailValidationIfTisProgrammeMembershipIdNotProvided() {
    List<CurriculumMembershipCreateXLS> upload = Arrays.asList(xlsInvalid1);

    service.processCurriculumMembershipCreateUpload(upload);

    assertEquals(EMPTY_PROGRAMME_MEMBERSHIP_ID_FIELD, xlsInvalid1.getErrorMessage());
  }

  @Test
  void shouldFailValidationIfCurriculumNameNotProvided() {
    List<CurriculumMembershipCreateXLS> upload = Arrays.asList(xlsInvalid2);

    service.processCurriculumMembershipCreateUpload(upload);

    assertEquals(EMPTY_CURRICULUM_NAME_FIELD, xlsInvalid2.getErrorMessage());
  }

  @Test
  void shouldFailValidationIfCurriculumStartDateNotProvided() {
    List<CurriculumMembershipCreateXLS> upload = Arrays.asList(xlsInvalid3);

    service.processCurriculumMembershipCreateUpload(upload);

    assertEquals(EMPTY_CURRICULUM_START_DATE_FIELD, xlsInvalid3.getErrorMessage());
  }

  @Test
  void shouldFailValidationIfCurriculumEndDateNotProvided() {
    List<CurriculumMembershipCreateXLS> upload = Arrays.asList(xlsInvalid4);

    service.processCurriculumMembershipCreateUpload(upload);

    assertEquals( EMPTY_CURRICULUM_END_DATE_FIELD, xlsInvalid4.getErrorMessage());
  }

  @Test
  void shouldReportErrorIfProgrammeMembershipNotFoundWithGivenId() {
    List<CurriculumMembershipCreateXLS> upload = Arrays.asList(xlsValid);

    when(tcsService.getProgrammeMembershipByUuid(TIS_PROGRAMME_MEMBERSHIP_ID)).thenReturn(null);
    service.processCurriculumMembershipCreateUpload(upload);

    assertEquals(NO_PROGRAMME_MEMBERSHIP_FOR_ID, xlsValid.getErrorMessage());
  }

  @Test
  void shouldProcessValidUpload() {
    List<CurriculumMembershipCreateXLS> upload = Arrays.asList(xlsValid);

    ProgrammeMembershipDTO programmeMembershipDTO = new ProgrammeMembershipDTO();
    programmeMembershipDTO.setUuid(UUID.fromString(xlsValid.getTisProgrammeMembershipId()));

    when(tcsService.getProgrammeMembershipByUuid(TIS_PROGRAMME_MEMBERSHIP_ID)).thenReturn(
        programmeMembershipDTO);
    service.processCurriculumMembershipCreateUpload(upload);

    verify(tcsService.updateProgrammeMembership(pmCaptor.capture()));

    assertEquals("1", pmCaptor.getValue().getCurriculumMemberships().get(0).getCurriculumId());
    assertEquals(null, xlsValid.getErrorMessage());
    assertEquals(true, xlsValid.isSuccessfullyImported());
  }
}
