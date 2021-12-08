package com.transformuk.hee.tis.genericupload.service.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentListDTO;
import com.transformuk.hee.tis.assessment.client.service.impl.AssessmentServiceImpl;
import java.util.Collections;
import java.util.List;
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
}
