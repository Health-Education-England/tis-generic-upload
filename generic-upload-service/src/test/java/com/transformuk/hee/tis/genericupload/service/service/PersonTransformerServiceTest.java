package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersonTransformerServiceTest {

  PersonTransformerService personTransformerService;
  Map<Pair<String, String>, List<ProgrammeDTO>> programmeDTOS;
  Map<String, List<CurriculumDTO>> curriculumDTOS;

  @Before
  public void initialise() {
    personTransformerService = new PersonTransformerService();
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
    ProgrammeDTO programmeDTO = personTransformerService
        .getProgrammeDTO(null, null, this::getProgrammeDTOByNameAndNumber);
    Assert.assertNull(programmeDTO);
  }

  @Test
  public void shouldNotThrowAnExceptionWhenProgrammeNameAndNumberIsEmpty() {
    ProgrammeDTO programmeDTO = personTransformerService
        .getProgrammeDTO("", "", this::getProgrammeDTOByNameAndNumber);
    Assert.assertNull(programmeDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenProgrammeNumberIsEmpty() {
    personTransformerService
        .getProgrammeDTO("aProgramme", "", this::getProgrammeDTOByNameAndNumber);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenProgrammeNameIsNull() {
    personTransformerService
        .getProgrammeDTO(null, "aProgramme", this::getProgrammeDTOByNameAndNumber);
  }

  @Test
  public void shouldNotThrowAnExceptionWhenProgrammeNameAndNumberIsValid() {
    ProgrammeDTO programmeDTO = personTransformerService
        .getProgrammeDTO("A_PROG", "A_PROG_NUM", this::getProgrammeDTOByNameAndNumber);
    Assert.assertNotNull(programmeDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenProgrammeNameAndNumberHasMultiple() {
    ProgrammeDTO programmeDTODuplicate = PersonDTOHelper.createProgrammeDTO("A_PROG", "A_PROG_NUM");
    programmeDTODuplicate.setStatus(Status.INACTIVE);
    programmeDTOS.get(Pair.of(programmeDTODuplicate.getProgrammeName(),
        programmeDTODuplicate.getProgrammeNumber())).add(programmeDTODuplicate);

    personTransformerService
        .getProgrammeDTO("A_PROG", "A_PROG_NUM", this::getProgrammeDTOByNameAndNumber);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenProgrammeNameAndNumberDoNotMatch() {
    personTransformerService
        .getProgrammeDTO("B_PROG", "B_PROG_NUM", this::getProgrammeDTOByNameAndNumber);
  }

  @Test
  public void shouldNotThrowAnExceptionWhenCurriculumNameIsNull() {
    CurriculumDTO curriculumDTO = personTransformerService
        .getCurriculumDTO(null, this::getCurriculumDTOByName);
    Assert.assertNull(curriculumDTO);
  }

  @Test
  public void shouldNotThrowAnExceptionWhenCurriculumNameIsEmpty() {
    CurriculumDTO curriculumDTO = personTransformerService
        .getCurriculumDTO("", this::getCurriculumDTOByName);
    Assert.assertNull(curriculumDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenCurriculumNameDoesNotMatch() {
    personTransformerService.getCurriculumDTO("A_CURR", this::getCurriculumDTOByName);
  }
}
