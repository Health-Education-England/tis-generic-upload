package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.ExcelColumn;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProgrammeMembershipUpdateXLS extends TemplateXLS {

  @ExcelColumn(name = "TIS_ProgrammeMembership_ID*", required = true)
  private String programmeMembershipId;

  @ExcelColumn(name = "Rotation")
  private String rotation;

  @ExcelColumn(name = "Programme Membership Type")
  private String programmeMembershipType;

  @ExcelColumn(name = "Programme Start Date")
  private Date programmeStartDate;

  @ExcelColumn(name = "Programme End Date")
  private Date programmeEndDate;

  @ExcelColumn(name = "Leaving Reason")
  private String leavingReason;

  @ExcelColumn(name = "Training Pathway")
  private String trainingPathway;
}
