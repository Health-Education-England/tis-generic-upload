package com.transformuk.hee.tis.genericupload.service.service;

import com.google.common.collect.Sets;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyGroupDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.AssessmentType;
import com.transformuk.hee.tis.tcs.api.enumeration.SpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PersonDTOHelper {

    public static final String SPECIALTY_GROUP_NAME = "anotherSG";
    private static final String SPECIALTY_INTREPID_ID = "XXXX_INTREPID_ID_XXXX_INTTEST";
    private static final String SPECIALTY_COLLEGE = "SPECIALTY_COLLEGE";
    private static final String NHS_SPECIALTY_CODE = "NHS_SPECIALTY_CODE";
    private static final AssessmentType DEFAULT_ASSESSMENT_TYPE = AssessmentType.ARCP;
    private static final Boolean DEFAULT_DOES_THIS_CURRICULUM_LEAD_TO_CCT = false;
    private static final Integer DEFAULT_PERIOD_OF_GRACE = 1;
    private static final Integer DEFAULT_LENGTH = 1;
    public static final String DEFAULT_NAME = "SPECIALTY_NAME";

    static PersonXLS getPersonXLS() throws ParseException {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

        PersonXLS personXLS = new PersonXLS();
        personXLS.setForenames("Namis");
        personXLS.setSurname("Ahmed");
        personXLS.setGmcNumber("6127800");
        personXLS.setRecordStatus("Current");

        personXLS.setProgrammeName("Cardiology");
        personXLS.setProgrammeNumber("NOR051");
        personXLS.setProgrammeMembership("Substantive");
        personXLS.setProgrammeEndDate(df.parse("2/27/2019"));

        personXLS.setCurriculum1("Cardiology");
        personXLS.setCurriculum1StartDate(df.parse("7/31/2023"));
        personXLS.setCurriculum1EndDate(df.parse("8/1/2018"));

        personXLS.setCurriculum1("General (Internal) Medicine");
        personXLS.setCurriculum1StartDate(df.parse("7/31/2023"));
        personXLS.setCurriculum1EndDate(df.parse("8/1/2018"));

        personXLS.setTitle("Dr");
        personXLS.setTitle("Dr");
        personXLS.setDateOfBirth(df.parse("1/14/1981"));
        personXLS.setEmailAddress("dr.nanishamed@hotmail.co.uk");
        personXLS.setMobile("07480056256");
        personXLS.setTelephone("07480056256");
        personXLS.setAddress1("10 Malborough Flats");
        personXLS.setAddress2("Eugene Street");
        personXLS.setAddress3("Bristol");
        personXLS.setPostCode("BS2 8EY");
        personXLS.setGender("Male");
        personXLS.setNationality("British");
        personXLS.setMaritalStatus("Single");
        personXLS.setEthnicOrigin("Not Stated");
        personXLS.setEeaResident("Yes");
        personXLS.setQualification("MBBS");
        personXLS.setMedicalSchool("University of Newcastle");
        personXLS.setCountryOfQualification("United Kingdom");
        personXLS.setDateAttained(df.parse("7/7/2005"));
        return personXLS;
    }

    static CurriculumDTO getCurriculumDTO(String curriculumName, SpecialtyDTO specialtyDTO){
        CurriculumDTO curriculumDTO = new CurriculumDTO();
        curriculumDTO.setName(curriculumName);
        curriculumDTO.setStatus(Status.CURRENT);
        curriculumDTO.setSpecialty(specialtyDTO);

        curriculumDTO.setAssessmentType(DEFAULT_ASSESSMENT_TYPE);
        curriculumDTO.setDoesThisCurriculumLeadToCct(DEFAULT_DOES_THIS_CURRICULUM_LEAD_TO_CCT);
        curriculumDTO.setPeriodOfGrace(DEFAULT_PERIOD_OF_GRACE);
        curriculumDTO.setLength(DEFAULT_LENGTH);
        return curriculumDTO;
    }

    static SpecialtyDTO createSpecialtyDTO(SpecialtyGroupDTO specialtyGroupDTO) {
        SpecialtyDTO specialtyDTO = new SpecialtyDTO();
        specialtyDTO.setIntrepidId(SPECIALTY_INTREPID_ID);
        specialtyDTO.setName(DEFAULT_NAME);
        specialtyDTO.setStatus(Status.CURRENT);
        specialtyDTO.setCollege(SPECIALTY_COLLEGE);
        specialtyDTO.setSpecialtyCode(NHS_SPECIALTY_CODE);
        specialtyDTO.setSpecialtyTypes(Sets.newHashSet(SpecialtyType.SUB_SPECIALTY));
        specialtyDTO.setSpecialtyGroup(specialtyGroupDTO);
        return specialtyDTO;
    }

    static SpecialtyGroupDTO createSpecialtyGroupDTO() {
        SpecialtyGroupDTO specialtyGroupDTO = new SpecialtyGroupDTO();
        specialtyGroupDTO.setIntrepidId("123333");
        specialtyGroupDTO.setName(SPECIALTY_GROUP_NAME);
        return specialtyGroupDTO;
    }

    static ProgrammeDTO createProgrammeDTO(String programmeName, String programmeNumber) {
        ProgrammeDTO programmeDTO = new ProgrammeDTO();
        programmeDTO.setProgrammeName(programmeName);
        programmeDTO.setProgrammeNumber(programmeNumber);
        programmeDTO.setStatus(Status.CURRENT);
        return programmeDTO;
    }
}
