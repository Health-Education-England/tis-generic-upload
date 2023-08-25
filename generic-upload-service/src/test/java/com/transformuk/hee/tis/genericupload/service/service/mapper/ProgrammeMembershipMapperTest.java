package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.ProgrammeMembershipUpdateXls;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProgrammeMembershipMapperTest {

  private static final String PROGRAMME_MEMBERSHIP_ID = UUID.randomUUID().toString();
  private static final String PROGRAMME_MEMBERSHIP_TYPE = "SUBSTANTIVE";
  private static final String ROTATION = "rotation";
  private static final String LEAVING_REASON = "leaving_reason";
  private static final String TRAINING_PATHWAY = "training_pathway";

  private ProgrammeMembershipMapper mapper;

  @Before
  public void setUp() {
    mapper = new ProgrammeMembershipMapperImpl();
  }

  ProgrammeMembershipUpdateXls initialiseXls() {
    ProgrammeMembershipUpdateXls xls = new ProgrammeMembershipUpdateXls();
    xls.setProgrammeMembershipId(PROGRAMME_MEMBERSHIP_ID);
    xls.setProgrammeMembershipType(PROGRAMME_MEMBERSHIP_TYPE);
    xls.setRotation(ROTATION);

    Calendar cal = Calendar.getInstance();
    cal.set(2023, Calendar.AUGUST, 22);
    Date startDate = cal.getTime();
    xls.setProgrammeStartDate(startDate);

    cal.set(2024, Calendar.AUGUST, 22);
    Date endDate = cal.getTime();
    xls.setProgrammeEndDate(endDate);

    xls.setLeavingReason(LEAVING_REASON);
    xls.setTrainingPathway(TRAINING_PATHWAY);
    return xls;
  }

  @Test
  public void shouldConvertEntityToDto() {
    ProgrammeMembershipUpdateXls xls = initialiseXls();

    ProgrammeMembershipDTO programmeMembershipDto = mapper.toDto(xls);

    Assert.assertEquals(PROGRAMME_MEMBERSHIP_ID, programmeMembershipDto.getUuid().toString());
    Assert.assertEquals(PROGRAMME_MEMBERSHIP_TYPE,
        programmeMembershipDto.getProgrammeMembershipType().name());
    Assert.assertEquals(ROTATION, programmeMembershipDto.getRotation().getName());
    Assert.assertEquals(TRAINING_PATHWAY, programmeMembershipDto.getTrainingPathway());
    Assert.assertEquals(LEAVING_REASON, programmeMembershipDto.getLeavingReason());

    LocalDate startDate = programmeMembershipDto.getProgrammeStartDate();
    LocalDate endDate = programmeMembershipDto.getProgrammeEndDate();

    Assert.assertEquals(2023, startDate.getYear());
    Assert.assertEquals(Month.AUGUST, startDate.getMonth());
    Assert.assertEquals(22, startDate.getDayOfMonth());

    Assert.assertEquals(2024, endDate.getYear());
    Assert.assertEquals(Month.AUGUST, endDate.getMonth());
    Assert.assertEquals(22, endDate.getDayOfMonth());
  }

  @Test
  public void shouldSetNullWhenPmTypeNotFound() {
    ProgrammeMembershipUpdateXls xls = initialiseXls();
    xls.setProgrammeMembershipType("Not Found");

    ProgrammeMembershipDTO programmeMembershipDto = mapper.toDto(xls);
    Assert.assertNull(programmeMembershipDto.getProgrammeMembershipType());
  }
}
