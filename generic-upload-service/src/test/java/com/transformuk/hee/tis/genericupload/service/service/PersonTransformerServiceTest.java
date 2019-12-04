package com.transformuk.hee.tis.genericupload.service.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static com.transformuk.hee.tis.genericupload.service.service.PersonTransformerService.CCT;
import static com.transformuk.hee.tis.genericupload.service.service.PersonTransformerService.CESR;
import static com.transformuk.hee.tis.genericupload.service.service.PersonTransformerService.N_A;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.tcs.api.dto.ContactDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonalDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.RightToWorkDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PersonTransformerServiceTest {

  Map<Pair<String, String>, List<ProgrammeDTO>> programmeDTOS;
  Map<String, List<CurriculumDTO>> curriculumDTOS;

  @InjectMocks
  private PersonTransformerService personTransformerService;

  @Mock
  private TcsServiceImpl tcsServiceImpl;

  @Before
  public void initialise() {
    programmeDTOS = new HashMap<>();
    ProgrammeDTO programmeDTO = PersonDTOHelper.createProgrammeDTO("A_PROG", "A_PROG_NUM");
    List<ProgrammeDTO> programmeDTOs = new ArrayList<>();
    programmeDTOs.add(programmeDTO);
    programmeDTOS.put(Pair.of(programmeDTO.getProgrammeName(), programmeDTO.getProgrammeNumber()),
        programmeDTOs);

    curriculumDTOS = new HashMap<>();
    CurriculumDTO curriculumDTO = new CurriculumDTO();
    List<CurriculumDTO> curriculumDTOS = new ArrayList<>();
    curriculumDTOS.add(curriculumDTO);
    this.curriculumDTOS.put(curriculumDTO.getName(), curriculumDTOS);
  }

  public List<ProgrammeDTO> getProgrammeDTOByNameAndNumber(String name, String number) {
    return programmeDTOS.get(Pair.of(name, number));
  }


  public List<CurriculumDTO> getCurriculumDTOByName(String name) {
    return curriculumDTOS.get(name);
  }

  @Test
  public void shouldNotThrowAnExceptionWhenProgrammeNameAndNumberIsNull() {
    ProgrammeDTO programmeDTO =
        personTransformerService.getProgrammeDTO(null, null, this::getProgrammeDTOByNameAndNumber);
    Assert.assertNull(programmeDTO);
  }

  @Test
  public void shouldNotThrowAnExceptionWhenProgrammeNameAndNumberIsEmpty() {
    ProgrammeDTO programmeDTO =
        personTransformerService.getProgrammeDTO("", "", this::getProgrammeDTOByNameAndNumber);
    Assert.assertNull(programmeDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenProgrammeNumberIsEmpty() {
    personTransformerService.getProgrammeDTO("aProgramme", "",
        this::getProgrammeDTOByNameAndNumber);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenProgrammeNameIsNull() {
    personTransformerService.getProgrammeDTO(null, "aProgramme",
        this::getProgrammeDTOByNameAndNumber);
  }

  @Test
  public void shouldNotThrowAnExceptionWhenProgrammeNameAndNumberIsValid() {
    ProgrammeDTO programmeDTO = personTransformerService.getProgrammeDTO("A_PROG", "A_PROG_NUM",
        this::getProgrammeDTOByNameAndNumber);
    Assert.assertNotNull(programmeDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenProgrammeNameAndNumberHasMultiple() {
    ProgrammeDTO programmeDTODuplicate = PersonDTOHelper.createProgrammeDTO("A_PROG", "A_PROG_NUM");
    programmeDTODuplicate.setStatus(Status.INACTIVE);
    programmeDTOS.get(Pair.of(programmeDTODuplicate.getProgrammeName(),
        programmeDTODuplicate.getProgrammeNumber())).add(programmeDTODuplicate);

    personTransformerService.getProgrammeDTO("A_PROG", "A_PROG_NUM",
        this::getProgrammeDTOByNameAndNumber);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenProgrammeNameAndNumberDoNotMatch() {
    personTransformerService.getProgrammeDTO("B_PROG", "B_PROG_NUM",
        this::getProgrammeDTOByNameAndNumber);
  }

  @Test
  public void shouldNotThrowAnExceptionWhenCurriculumNameIsNull() {
    CurriculumDTO curriculumDTO =
        personTransformerService.getCurriculumDTO(null, this::getCurriculumDTOByName);
    Assert.assertNull(curriculumDTO);
  }

  @Test
  public void shouldNotThrowAnExceptionWhenCurriculumNameIsEmpty() {
    CurriculumDTO curriculumDTO =
        personTransformerService.getCurriculumDTO("", this::getCurriculumDTOByName);
    Assert.assertNull(curriculumDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenCurriculumNameDoesNotMatch() {
    personTransformerService.getCurriculumDTO("A_CURR", this::getCurriculumDTOByName);
  }

  /**
   * Test that the original National Insurance number is used when no new NI number is given.
   */
  @Test
  public void testUpdateDatastoreWithRowsFromXLS_noNewNiNumber_originalNiNumber() {
    // Set up test data.
    String regNumber = "regNumber";

    PersonDTO personXlsDto = new PersonDTO();
    personXlsDto.setContactDetails(new ContactDetailsDTO());
    personXlsDto.setGdcDetails(new GdcDetailsDTO());
    personXlsDto.setGmcDetails(new GmcDetailsDTO());
    personXlsDto.setRightToWork(new RightToWorkDTO());

    PersonalDetailsDTO personalDetailsXlsDto = new PersonalDetailsDTO();
    personXlsDto.setPersonalDetails(personalDetailsXlsDto);
    Map<String, PersonDTO> regNumberToXlsDto = Collections.singletonMap(regNumber, personXlsDto);

    PersonDTO personDbDto = new PersonDTO();
    PersonalDetailsDTO personalDetailsDbDto = new PersonalDetailsDTO();
    personalDetailsDbDto.setNationalInsuranceNumber("originalNiNumber");
    personDbDto.setPersonalDetails(personalDetailsDbDto);
    Map<String, PersonDTO> regNumberToDbDto = Collections.singletonMap(regNumber, personDbDto);

    PersonXLS personXls = new PersonXLS();
    Map<String, PersonXLS> regNumberToXls = Collections.singletonMap(regNumber, personXls);

    // Call code under test.
    personTransformerService.updateDatastoreWithRowsFromXLS(regNumberToXlsDto, regNumberToDbDto,
        regNumberToXls);

    // Perform assertions.
    PersonalDetailsDTO personalDetails = personDbDto.getPersonalDetails();
    String nationalInsuranceNumber = personalDetails.getNationalInsuranceNumber();
    assertThat("The national insurance number did not match the expected value.",
        nationalInsuranceNumber, is("originalNiNumber"));
  }

  /**
   * Test that the new National Insurance number is used when a new NI number is given.
   */
  @Test
  public void testUpdateDatastoreWithRowsFromXLS_newNiNumber_newNiNumber() {
    // Set up test data.
    String regNumber = "regNumber";

    PersonDTO personXlsDto = new PersonDTO();
    personXlsDto.setContactDetails(new ContactDetailsDTO());
    personXlsDto.setGdcDetails(new GdcDetailsDTO());
    personXlsDto.setGmcDetails(new GmcDetailsDTO());
    personXlsDto.setRightToWork(new RightToWorkDTO());

    PersonalDetailsDTO personalDetailsXlsDto = new PersonalDetailsDTO();
    personalDetailsXlsDto.setNationalInsuranceNumber("newNiNumber");
    personXlsDto.setPersonalDetails(personalDetailsXlsDto);
    Map<String, PersonDTO> regNumberToXlsDto = Collections.singletonMap(regNumber, personXlsDto);

    PersonDTO personDbDto = new PersonDTO();
    PersonalDetailsDTO personalDetailsDbDto = new PersonalDetailsDTO();
    personalDetailsDbDto.setNationalInsuranceNumber("originalNiNumber");
    personDbDto.setPersonalDetails(personalDetailsDbDto);
    Map<String, PersonDTO> regNumberToDbDto = Collections.singletonMap(regNumber, personDbDto);

    PersonXLS personXls = new PersonXLS();
    Map<String, PersonXLS> regNumberToXls = Collections.singletonMap(regNumber, personXls);

    // Call code under test.
    personTransformerService.updateDatastoreWithRowsFromXLS(regNumberToXlsDto, regNumberToDbDto,
        regNumberToXls);

    // Perform assertions.
    PersonalDetailsDTO personalDetails = personDbDto.getPersonalDetails();
    String nationalInsuranceNumber = personalDetails.getNationalInsuranceNumber();
    assertThat("The national insurance number did not match the expected value.",
        nationalInsuranceNumber, is("newNiNumber"));
  }

  @Test
  public void testTrainingPathwayCalculation() {
    // Given programme membership 'cctProgramme' with all possible curricula
    // another 'cesrProgramme' membership with all possible curricula AND that has been set as cesr
    CurriculumDTO cctCurriculum = new CurriculumDTO();
    cctCurriculum.setDoesThisCurriculumLeadToCct(true);
    CurriculumDTO nonCctCurriculum = new CurriculumDTO();
    nonCctCurriculum.setDoesThisCurriculumLeadToCct(false);
    CurriculumDTO nullCctCurriculum = null;
    ProgrammeMembershipDTO cctProgramme = new ProgrammeMembershipDTO();
    cctProgramme.setTrainingPathway(null); // Just explicitly stating that we test this changes
    ProgrammeMembershipDTO cesrProgramme = new ProgrammeMembershipDTO();
    cesrProgramme.setTrainingPathway(CESR);
    CurriculumDTO[] allCurricula = {nonCctCurriculum, nullCctCurriculum, cctCurriculum};

    // When I calculate the training pathway
    personTransformerService.evaluateTrainingPathway(cctProgramme, allCurricula);
    personTransformerService.evaluateTrainingPathway(cesrProgramme, allCurricula);

    // Then
    assertEquals(CCT, cctProgramme.getTrainingPathway());
    assertEquals(CESR, cesrProgramme.getTrainingPathway());

    // When I evaluate the training pathway, with only the non-CCT curriculum.
    personTransformerService.evaluateTrainingPathway(cctProgramme, nonCctCurriculum);
    // Then
    assertEquals(N_A, cctProgramme.getTrainingPathway());

  }
}
