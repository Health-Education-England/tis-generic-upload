package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.service.Application;
import com.transformuk.hee.tis.genericupload.service.TestUtils;
import com.transformuk.hee.tis.genericupload.service.config.WebSecurityConfig;
import com.transformuk.hee.tis.tcs.api.dto.*;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
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

import java.text.ParseException;
import java.util.List;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WebSecurityConfig.class})
public class ScheduledUploadTaskIntTest {
    @Autowired
    ScheduledUploadTask scheduledUploadTask;
    @Autowired
    private TcsServiceImpl tcsServiceImpl;
    @Autowired
    private RestTemplate tcsRestTemplate;
    @Value("${tcs.service.url}")
    private String serviceUrl;

    PersonXLS personXLS;


    @Before
    public void setup() throws ParseException {
        TestUtils.mockUserprofile("jamesh", "1-AIIDR8", "1-AIIDWA");

        personXLS = PersonDTOHelper.getPersonXLS();
        createCurriculumIfNotPresent(personXLS.getCurriculum1());
        createCurriculumIfNotPresent(personXLS.getCurriculum2());
        createProgrammeIfNotPresent(personXLS.getProgrammeName(), personXLS.getProgrammeNumber());
    }

    @Ignore
    @Test
    public void canBuildAPersonDTOFromPersonXLS() throws ParseException {
        PersonDTO personDTO = scheduledUploadTask.getPersonDTO(personXLS,
                createCurriculumIfNotPresent(personXLS.getCurriculum1()),
                createCurriculumIfNotPresent(personXLS.getCurriculum2()),
                createCurriculumIfNotPresent(personXLS.getCurriculum3()),
                createProgrammeIfNotPresent(personXLS.getProgrammeName(), personXLS.getProgrammeNumber()));
        Assert.assertEquals(personDTO.getContactDetails().getForenames(), personXLS.getForenames());
    }

    @Ignore
    @Test
    public void canUpdateTcsWithDTO() throws ParseException {
        PersonDTO personDTO = scheduledUploadTask.getPersonDTO(personXLS,
                createCurriculumIfNotPresent(personXLS.getCurriculum1()),
                createCurriculumIfNotPresent(personXLS.getCurriculum2()),
                createCurriculumIfNotPresent(personXLS.getCurriculum3()),
                createProgrammeIfNotPresent(personXLS.getProgrammeName(), personXLS.getProgrammeNumber()));
        tcsServiceImpl.createPerson(personDTO);
    }

    private ProgrammeDTO createProgrammeIfNotPresent(String programmeName, String programmeNumber) {
        ProgrammeDTO programmeByNameAndNumber = null;
        try {
            programmeByNameAndNumber = tcsServiceImpl.getProgrammeByNameAndNumber(programmeName, programmeNumber).get(0);
        } catch(IndexOutOfBoundsException e){
            //do nothing
        }
        if(programmeByNameAndNumber == null) {
            ProgrammeDTO programmeDTO = new ProgrammeDTO();
            programmeDTO.setProgrammeName(programmeName);
            programmeDTO.setProgrammeNumber(programmeNumber);
            programmeDTO.setStatus(Status.CURRENT);

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<ProgrammeDTO> httpEntity = new HttpEntity<>(programmeDTO, headers);
            programmeByNameAndNumber = tcsRestTemplate
                    .exchange(serviceUrl + "/api/programme/", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ProgrammeDTO>() {})
                    .getBody();
        }
        return programmeByNameAndNumber;
    }

    private CurriculumDTO createCurriculumIfNotPresent(String curriculumName) {
        CurriculumDTO curriculaByName = null;
        try {
            curriculaByName = tcsServiceImpl.getCurriculaByName(curriculumName).get(0);
        } catch(Exception e){
            //do nothing
        }
        if (curriculaByName == null) {
            List<SpecialtyGroupDTO> specialtyGroupDTOs = getSpecialtyGroupDTO();
            SpecialtyGroupDTO specialtyGroupDTO = specialtyGroupDTOs == null ? specialtyGroupDTOs.get(0) : createSpecialtyGroupDTO();

            List<SpecialtyDTO> specialtyDTOs = getSpecialtyDTO();
            SpecialtyDTO specialtyDTO = specialtyDTOs == null ? specialtyDTOs.get(0) : createSpecialtyDTO(specialtyGroupDTO);


            CurriculumDTO curriculumDTO = PersonDTOHelper.getCurriculumDTO(personXLS.getCurriculum1(),specialtyDTO);

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<CurriculumDTO> httpEntity = new HttpEntity<>(curriculumDTO, headers);
            curriculaByName = tcsRestTemplate.exchange(serviceUrl + "/api/curricula/", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<CurriculumDTO>() {}).getBody();
        }
        return curriculaByName;
    }

    private List<SpecialtyDTO> getSpecialtyDTO() {
        return tcsRestTemplate
                .exchange(serviceUrl + "/api/specialties?searchQuery=" + PersonDTOHelper.DEFAULT_NAME, HttpMethod.GET, null, new ParameterizedTypeReference<List<SpecialtyDTO>>() {})
                .getBody();
    }

    private SpecialtyDTO createSpecialtyDTO(SpecialtyGroupDTO specialtyGroupDTO) {
        SpecialtyDTO specialtyDTO = PersonDTOHelper.createSpecialtyDTO(specialtyGroupDTO);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SpecialtyDTO> httpEntitySpec = new HttpEntity<>(specialtyDTO, headers);
        return tcsRestTemplate.exchange(serviceUrl + "/api/specialties/", HttpMethod.POST, httpEntitySpec, new ParameterizedTypeReference<SpecialtyDTO>() {}).getBody();
    }

    private List<SpecialtyGroupDTO> getSpecialtyGroupDTO() {
        return tcsRestTemplate
                .exchange(serviceUrl + "/api/specialty-groups?searchQuery=+ " + PersonDTOHelper.SPECIALTY_GROUP_NAME, HttpMethod.GET, null, new ParameterizedTypeReference<List<SpecialtyGroupDTO>>() {})
                .getBody();
    }

    private SpecialtyGroupDTO createSpecialtyGroupDTO() {
        SpecialtyGroupDTO specialtyGroupDTO = PersonDTOHelper.createSpecialtyGroupDTO();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SpecialtyGroupDTO> httpEntity = new HttpEntity<>(specialtyGroupDTO, headers);
        return tcsRestTemplate.exchange(serviceUrl + "/api/specialty-groups/", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<SpecialtyGroupDTO>() {}).getBody();
    }
}
