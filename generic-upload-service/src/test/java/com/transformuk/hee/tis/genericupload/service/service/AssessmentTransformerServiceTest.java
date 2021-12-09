package com.transformuk.hee.tis.genericupload.service.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentListDTO;
import com.transformuk.hee.tis.assessment.client.service.impl.AssessmentServiceImpl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import com.transformuk.hee.tis.genericupload.api.dto.AssessmentXLS;
import com.transformuk.hee.tis.reference.api.dto.CurriculumSubTypeDTO;
import com.transformuk.hee.tis.tcs.api.dto.*;
import com.transformuk.hee.tis.tcs.api.enumeration.CurriculumSubType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class AssessmentTransformerServiceTest {

    @InjectMocks
    private AssessmentTransformerService assessmentTransformerService;
    @Mock
    private TcsServiceImpl tcsServiceMock;
    @Mock
    private AssessmentServiceImpl assessmentServiceMock;

    @Test
    public void testGetAnyDuplicateAssessmentsMessage_duplicate() {
        Long duplicateAssessmentId = 1L;
        AssessmentDTO assessmentDTO = new AssessmentDTO();

        AssessmentListDTO assessmentListDTO = new AssessmentListDTO();
        assessmentListDTO.setId(duplicateAssessmentId);
        List<AssessmentListDTO> duplicateAssessments = Lists.newArrayList(assessmentListDTO);

        when(assessmentServiceMock.findAssessments(any(), any(), any(), any()))
                .thenReturn(duplicateAssessments);

        String duplicateAssessmentsMessage = assessmentTransformerService
                .getAnyDuplicateAssessmentsMessage(assessmentDTO);

        assertThat("Should get duplicate message", duplicateAssessmentsMessage,
                is(String.format(AssessmentTransformerService.ASSESSMENT_IS_DUPLICATE,
                        duplicateAssessmentId)));
    }
    @Test
    public void testGetAnyDuplicateAssessmentsMessage_noDuplicate() {
        AssessmentDTO assessmentDTO = new AssessmentDTO();
        List<AssessmentListDTO> duplicateAssessments = Collections.emptyList();

        when(assessmentServiceMock.findAssessments(any(), any(), any(), any()))
                .thenReturn(duplicateAssessments);

        String duplicateAssessmentsMessage = assessmentTransformerService
                .getAnyDuplicateAssessmentsMessage(assessmentDTO);

        assertThat("Should not get duplicate message", duplicateAssessmentsMessage,
                is(""));
    }

    @Test
    public void testProcessAssessments_duplicate() {
        Long duplicateAssessmentId = 1L;
        Long traineeId = 1L;
        String lastName = "last name";
        Long programmeMembershipId = 1L;
        Long programmeMembershipCurriculumId = 1L;

        LocalDate curriculumStartDate = LocalDate.parse("2021-01-01");
        LocalDate curriculumEndDate = LocalDate.parse("2021-01-02");
        String programmeName = "programme name";
        String programmeNumber = "programme number";
        String curriculumName = "curriculum name";
        String gmcNumber = "gmc number";
        String assessmentType = "assessment type";

        SpecialtyDTO specialtyDTO = new SpecialtyDTO();
        specialtyDTO.setId(1L);
        specialtyDTO.setName("specialty");
        CurriculumDTO curr = new CurriculumDTO();
        curr.setId(programmeMembershipId);
        curr.setName(curriculumName);
        curr.setCurriculumSubType(CurriculumSubType.ACL);
        curr.setSpecialty(specialtyDTO);

        CurriculumMembershipDTO curriculumMembershipDTO = new CurriculumMembershipDTO();
        curriculumMembershipDTO.setId(1L);
        curriculumMembershipDTO.setCurriculumStartDate(curriculumStartDate);
        curriculumMembershipDTO.setCurriculumEndDate(curriculumEndDate);
        List<CurriculumMembershipDTO> curriculumMembershipDTOList = Lists.newArrayList(curriculumMembershipDTO);

        ProgrammeMembershipCurriculaDTO pmc = new ProgrammeMembershipCurriculaDTO();
        pmc.setProgrammeName(programmeName);
        pmc.setProgrammeNumber(programmeNumber);
        pmc.setId(programmeMembershipCurriculumId);
        pmc.setCurriculumDTO(curr);
        pmc.setCurriculumMemberships(curriculumMembershipDTOList);
        List<ProgrammeMembershipCurriculaDTO> pmcList = Lists.newArrayList(pmc);

        GmcDetailsDTO gmcDetailsDTO = new GmcDetailsDTO();
        gmcDetailsDTO.setGmcNumber(gmcNumber);
        gmcDetailsDTO.setId(1L);
        List<GmcDetailsDTO> gmcDetailsDTOList = Lists.newArrayList(gmcDetailsDTO);

        PersonDTO personDTO = new PersonDTO();
        personDTO.setId(traineeId);
        personDTO.setGmcDetails(gmcDetailsDTO);
        List<PersonDTO> personDTOList = Lists.newArrayList(personDTO);

        PersonBasicDetailsDTO personBasicDetailsDTO = new PersonBasicDetailsDTO();
        personBasicDetailsDTO.setId(traineeId);
        personBasicDetailsDTO.setGmcNumber(gmcNumber);
        personBasicDetailsDTO.setLastName(lastName);
        List<PersonBasicDetailsDTO> personBasicDetailsDTOList = Lists.newArrayList(personBasicDetailsDTO);

        List<String> publicHealthNumberList = Lists.newArrayList(gmcNumber);

        AssessmentXLS xls1 = new AssessmentXLS();
        xls1.setSurname(lastName);
        xls1.setProgrammeName(programmeName);
        xls1.setProgrammeNumber(programmeNumber);
        xls1.setCurriculumName(curriculumName);
        xls1.setGmcNumber(gmcNumber);
        xls1.setType(assessmentType);
        List<AssessmentXLS> xlsList = Lists.newArrayList(xls1);

        AssessmentListDTO assessmentListDTO = new AssessmentListDTO();
        assessmentListDTO.setId(duplicateAssessmentId);
        List<AssessmentListDTO> duplicateAssessments = Lists.newArrayList(assessmentListDTO);

        when(tcsServiceMock.getProgrammeMembershipForTrainee(traineeId))
                .thenReturn(pmcList);
        when(assessmentServiceMock.getAllOutcomes()).thenReturn("[{" +
                "\"id\": 2, " +
                "\"uuid\": \"30386130-6132-3466-2d33-6566622d3131\", " +
                "\"code\": \"OUTCOME_1\", " +
                "\"label\": \"1\", " +
                "\"reasons\": [ ]" +
                "}]");
        when(tcsServiceMock.findPeopleByPublicHealthNumbersIn(publicHealthNumberList))
                .thenReturn(personDTOList);
        when(tcsServiceMock.findGmcDetailsIn(anyList())).thenReturn(gmcDetailsDTOList);
        when(tcsServiceMock.findPersonBasicDetailsIn(anyList())).thenReturn(personBasicDetailsDTOList);
        when(tcsServiceMock.getProgrammeMembershipForTrainee(any())).thenReturn(pmcList);

        when(assessmentServiceMock.findAssessments(any(), any(), any(), any()))
                .thenReturn(duplicateAssessments);

        assessmentTransformerService.initialiseFetchers();
        assessmentTransformerService.processAssessmentsUpload(xlsList);

        assertThat("Should get error", xlsList.get(0).getErrorMessage(),
                is(String.format(AssessmentTransformerService.ASSESSMENT_IS_DUPLICATE,
                        duplicateAssessmentId)));

    }
}
