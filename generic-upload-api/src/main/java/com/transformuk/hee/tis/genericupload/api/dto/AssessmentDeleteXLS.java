package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AssessmentDeleteXLS extends TemplateXLS {
    @ExcelColumn(name = "TIS_Assessment_Id*", required = true)
    private String assessmentId;

    @ExcelColumn(name = "TIS Status*", required = true)
    private String assessmentStatus;
}
