package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.assessment.client.service.impl.AssessmentServiceImpl;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentDeleteXLS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Component
public class AssessmentDeleteService {

    @Autowired
    private AssessmentServiceImpl assessmentServiceImpl;

    void processAssessmentsDeleteUpload(List<AssessmentDeleteXLS> assessmentDeleteXLSS) {
        assessmentDeleteXLSS.forEach(assessmentDeleteXLS -> {
            if ("DELETE".equalsIgnoreCase(assessmentDeleteXLS.getAssessmentStatus())) {
                try {
                    //TODO implement in assessments client
                    //assessmentServiceImpl.deleteAssessment(Long.valueOf(assessmentDeleteXLS.getAssessmentId()));
                    assessmentDeleteXLS.setSuccessfullyImported(true);
                } catch (ResourceAccessException rae) {
                    new ErrorHandler().recordErrorMessageOnTemplateOrLogUnknown(assessmentDeleteXLS, rae);
                }
            }
        });
    }
}





