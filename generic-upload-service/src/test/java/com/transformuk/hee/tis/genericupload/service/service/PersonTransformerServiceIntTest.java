package com.transformuk.hee.tis.genericupload.service.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.service.Application;
import com.transformuk.hee.tis.genericupload.service.TestUtils;
import com.transformuk.hee.tis.genericupload.service.config.WebSecurityConfig;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.QualificationDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyGroupDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WebSecurityConfig.class})
public class PersonTransformerServiceIntTest {

  PersonXLS personXLS;
  @Autowired
  private PersonTransformerService personTransformerService;
  @Autowired
  private TcsServiceImpl tcsServiceImpl;
  @Autowired
  private RestTemplate tcsRestTemplate;
  @Value("${tcs.service.url}")
  private String serviceUrl;

  @Before
  public void setup() throws ParseException {
    TestUtils.mockUserprofile("jamesh", "1-AIIDR8", "1-AIIDWA");
    personXLS = PersonDTOHelper.getPersonXLS();
  }

  @Test
  @Ignore
  public void canBuildAPersonDTOFromPersonXLS() {
    createCurriculumIfNotPresent(personXLS.getCurriculum1());
    createCurriculumIfNotPresent(personXLS.getCurriculum2());
    createProgrammeIfNotPresent(personXLS.getProgrammeName(), personXLS.getProgrammeNumber());

    SpecialtyGroupDTO specialtyGroupDTO = PersonDTOHelper.createSpecialtyGroupDTO();
    SpecialtyDTO specialtyDTO = PersonDTOHelper.createSpecialtyDTO(specialtyGroupDTO);
    PersonDTO personDTO = personTransformerService.getPersonDTO(personXLS,
        PersonDTOHelper.getCurriculumDTO(personXLS.getCurriculum1(), specialtyDTO),
        PersonDTOHelper.getCurriculumDTO(personXLS.getCurriculum2(), specialtyDTO),
        PersonDTOHelper.getCurriculumDTO(personXLS.getCurriculum3(), specialtyDTO),
        PersonDTOHelper
            .createProgrammeDTO(personXLS.getProgrammeName(), personXLS.getProgrammeNumber()));
    Assert.assertEquals(personDTO.getContactDetails().getForenames(), personXLS.getForenames());
    Assert
        .assertEquals(personDTO.getStatus().toString(), personXLS.getRecordStatus().toLowerCase());
  }

  @Test
  @Ignore
  public void canBuildAPersonDTOFromPersonXLS2() {
    PersonDTO personDTO = personTransformerService.getPersonDTO(personXLS,
        createCurriculumIfNotPresent(personXLS.getCurriculum1()),
        createCurriculumIfNotPresent(personXLS.getCurriculum2()),
        createCurriculumIfNotPresent(personXLS.getCurriculum3()),
        createProgrammeIfNotPresent(personXLS.getProgrammeName(), personXLS.getProgrammeNumber()));
    Assert.assertEquals(personDTO.getContactDetails().getForenames(), personXLS.getForenames());
  }

  @Test
  public void buildsANullQualificationIfFieldsAreNull()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    personXLS.setCountryOfQualification(null);
    personXLS.setQualification(null);
    personXLS.setDateAttained(null);
    personXLS.setMedicalSchool(null);

    QualificationDTO result = getQualificationDTO(personXLS);
    assertThat(result).isNull();
  }

  @Test
  public void buildsAQualificationIfSomeFieldsAreNotNull()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    personXLS.setCountryOfQualification(null);
    personXLS.setQualification("Medical");
    personXLS.setDateAttained(null);
    personXLS.setMedicalSchool(null);

    QualificationDTO result = getQualificationDTO(personXLS);
    assertThat(result).isNotNull();
  }

  //have to use reflection to invoke
  public QualificationDTO getQualificationDTO(PersonXLS personXLS)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Class[] parameterTypes = new Class[1];
    parameterTypes[0] = PersonXLS.class;
    Method m = PersonTransformerService.class
        .getDeclaredMethod("getQualificationDTO", parameterTypes);
    m.setAccessible(true);
    return (QualificationDTO) m.invoke(personTransformerService, personXLS);
  }


  private ProgrammeDTO createProgrammeIfNotPresent(String programmeName, String programmeNumber) {
    ProgrammeDTO programmeByNameAndNumber = null;
    try {
      programmeByNameAndNumber = tcsServiceImpl
          .getProgrammeByNameAndNumber(programmeName, programmeNumber).get(0);
    } catch (IndexOutOfBoundsException e) {
      //do nothing
    }
    if (programmeByNameAndNumber == null) {
      ProgrammeDTO programmeDTO = new ProgrammeDTO();
      programmeDTO.setProgrammeName(programmeName);
      programmeDTO.setProgrammeNumber(programmeNumber);
      programmeDTO.setStatus(Status.CURRENT);

      HttpHeaders headers = new HttpHeaders();
      HttpEntity<ProgrammeDTO> httpEntity = new HttpEntity<>(programmeDTO, headers);
      programmeByNameAndNumber = tcsRestTemplate
          .exchange(serviceUrl + "/api/programme/", HttpMethod.POST, httpEntity,
              new ParameterizedTypeReference<ProgrammeDTO>() {
              })
          .getBody();
    }
    return programmeByNameAndNumber;
  }

  private CurriculumDTO createCurriculumIfNotPresent(String curriculumName) {
    CurriculumDTO curriculaByName = null;
    try {
      curriculaByName = tcsServiceImpl.getCurriculaByName(curriculumName).get(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (curriculaByName == null) {
      List<SpecialtyGroupDTO> specialtyGroupDTOs = getSpecialtyGroupDTO();
      SpecialtyGroupDTO specialtyGroupDTO =
          specialtyGroupDTOs == null ? specialtyGroupDTOs.get(0) : createSpecialtyGroupDTO();

      List<SpecialtyDTO> specialtyDTOs = getSpecialtyDTO();
      SpecialtyDTO specialtyDTO =
          specialtyDTOs == null ? specialtyDTOs.get(0) : createSpecialtyDTO(specialtyGroupDTO);

      CurriculumDTO curriculumDTO = PersonDTOHelper
          .getCurriculumDTO(personXLS.getCurriculum1(), specialtyDTO);

      HttpHeaders headers = new HttpHeaders();
      HttpEntity<CurriculumDTO> httpEntity = new HttpEntity<>(curriculumDTO, headers);
      curriculaByName = tcsRestTemplate
          .exchange(serviceUrl + "/api/curricula/", HttpMethod.POST, httpEntity,
              new ParameterizedTypeReference<CurriculumDTO>() {
              }).getBody();
    }
    return curriculaByName;
  }


  private List<SpecialtyDTO> getSpecialtyDTO() {
    return tcsRestTemplate
        .exchange(serviceUrl + "/api/specialties?searchQuery=" + PersonDTOHelper.DEFAULT_NAME,
            HttpMethod.GET, null, new ParameterizedTypeReference<List<SpecialtyDTO>>() {
            })
        .getBody();
  }

  private SpecialtyDTO createSpecialtyDTO(SpecialtyGroupDTO specialtyGroupDTO) {
    SpecialtyDTO specialtyDTO = PersonDTOHelper.createSpecialtyDTO(specialtyGroupDTO);

    HttpHeaders headers = new HttpHeaders();
    HttpEntity<SpecialtyDTO> httpEntitySpec = new HttpEntity<>(specialtyDTO, headers);
    return tcsRestTemplate
        .exchange(serviceUrl + "/api/specialties/", HttpMethod.POST, httpEntitySpec,
            new ParameterizedTypeReference<SpecialtyDTO>() {
            }).getBody();
  }

  private List<SpecialtyGroupDTO> getSpecialtyGroupDTO() {
    return tcsRestTemplate
        .exchange(serviceUrl + "/api/specialty-groups?searchQuery=+ "
                + PersonDTOHelper.SPECIALTY_GROUP_NAME, HttpMethod.GET, null,
            new ParameterizedTypeReference<List<SpecialtyGroupDTO>>() {
            })
        .getBody();
  }

  private SpecialtyGroupDTO createSpecialtyGroupDTO() {
    SpecialtyGroupDTO specialtyGroupDTO = PersonDTOHelper.createSpecialtyGroupDTO();

    HttpHeaders headers = new HttpHeaders();
    HttpEntity<SpecialtyGroupDTO> httpEntity = new HttpEntity<>(specialtyGroupDTO, headers);
    return tcsRestTemplate
        .exchange(serviceUrl + "/api/specialty-groups/", HttpMethod.POST, httpEntity,
            new ParameterizedTypeReference<SpecialtyGroupDTO>() {
            }).getBody();
  }

}
