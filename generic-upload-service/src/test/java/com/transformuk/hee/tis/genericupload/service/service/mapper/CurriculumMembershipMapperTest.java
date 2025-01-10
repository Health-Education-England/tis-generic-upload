package com.transformuk.hee.tis.genericupload.service.service.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXls;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CurriculumMembershipMapperTest {

  private static final UUID PROGRAMME_MEMBERSHIP_UUID = UUID.randomUUID();
  private static final String CURRICULUM_NAME = "curriculum1";
  private static final LocalDate START_DATE = LocalDate.now().minusDays(1);
  private static final LocalDate END_DATE = LocalDate.now().plusDays(1);

  private CurriculumMembershipMapper cmMapper;

  @BeforeEach
  void setUp() {
    cmMapper = new CurriculumMembershipMapperImpl();
  }

  @Test
  void shouldConvertXlsToDto() {
    CurriculumMembershipCreateXls xls = initialiseXls();

    CurriculumMembershipDTO dto = cmMapper.toDto(xls);

    assertEquals(PROGRAMME_MEMBERSHIP_UUID, dto.getProgrammeMembershipUuid());
    assertEquals(START_DATE, dto.getCurriculumStartDate());
    assertEquals(END_DATE, dto.getCurriculumEndDate());
    assertNull(dto.getCurriculumId());
  }

  @Test
  void shouldThrowExceptionWhenUuidNotValid() {
    CurriculumMembershipCreateXls xls = initialiseXls();
    xls.setProgrammeMembershipUuid("invalidUuid");

    assertThrows(IllegalArgumentException.class, () -> cmMapper.toDto(xls));
  }

  CurriculumMembershipCreateXls initialiseXls() {
    CurriculumMembershipCreateXls xls = new CurriculumMembershipCreateXls();
    xls.setProgrammeMembershipUuid(PROGRAMME_MEMBERSHIP_UUID.toString());
    xls.setCurriculumName(CURRICULUM_NAME);
    xls.setCurriculumStartDate(START_DATE);
    xls.setCurriculumEndDate(END_DATE);
    return xls;
  }
}
