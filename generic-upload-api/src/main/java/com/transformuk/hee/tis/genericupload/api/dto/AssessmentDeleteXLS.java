package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AssessmentDeleteXLS extends TemplateXLS {
    @ExcelColumn(name = "TIS_Assessment_ID*", required = true)
    private String assessmentId;

    @ExcelColumn(name = "Assessment Status*", required = true)
    private String assessmentStatus;
}
