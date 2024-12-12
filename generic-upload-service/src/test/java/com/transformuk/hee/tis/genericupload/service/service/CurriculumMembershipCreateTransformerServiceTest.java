package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXLS;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CurriculumMembershipCreateTransformerServiceTest {

  @InjectMocks
  CurriculumMembershipCreateTransformerService service;
  private CurriculumMembershipCreateXLS xlsValid, xlsInvalid1, xlsInvalid2, xlsInvalid3, xlsInvalid4;
  private final String TIS_PROGRAMME_MEMBERSHIP_ID = "1";
  private final String CURRICULUM_NAME = "curriculum1";
  private final LocalDate CURRICULUM_START_DATE = LocalDate.now().minusDays(1);
  private final LocalDate CURRICULUM_END_DATE = LocalDate.now().plusDays(1);
  @Before
  void setUp() {
    xlsValid = new CurriculumMembershipCreateXLS();
    xlsValid.setTisProgrammeMembershipId(TIS_PROGRAMME_MEMBERSHIP_ID);
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
    xlsInvalid2.setTisProgrammeMembershipId(TIS_PROGRAMME_MEMBERSHIP_ID);
    xlsInvalid2.setCurriculumStartDate(CURRICULUM_START_DATE);
    xlsInvalid2.setCurriculumEndDate(CURRICULUM_END_DATE);

    // missing curriculumStartDate
    xlsInvalid3 = new CurriculumMembershipCreateXLS();
    xlsInvalid3.setTisProgrammeMembershipId(TIS_PROGRAMME_MEMBERSHIP_ID);
    xlsInvalid3.setCurriculumName(CURRICULUM_NAME);
    xlsInvalid3.setCurriculumEndDate(CURRICULUM_END_DATE);

    // missing curriculumEndDate
    xlsInvalid4 = new CurriculumMembershipCreateXLS();
    xlsInvalid4.setTisProgrammeMembershipId(TIS_PROGRAMME_MEMBERSHIP_ID);
    xlsInvalid4.setCurriculumName(CURRICULUM_NAME);
    xlsInvalid4.setCurriculumStartDate(CURRICULUM_START_DATE);
  }

  @Test
  void shouldFailValidationIfTisProgrammeMembershipIdNotProvided() {

  }

  @Test
  void shouldFailValidationIfCurriculumNameNotProvided() {

  }

  @Test
  void shouldFailValidationIfCurriculumStartDateNotProvided() {

  }

  @Test
  void shouldFailValidationIfCurriculumEndDateNotProvided() {

  }
}
