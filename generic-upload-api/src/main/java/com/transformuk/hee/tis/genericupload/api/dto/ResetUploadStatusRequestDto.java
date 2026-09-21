package com.transformuk.hee.tis.genericupload.api.dto;

import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for resetting the status of a bulk upload job.
 */
@Data
public class ResetUploadStatusRequestDto {

  @NotNull
  private Long jobId;

  @NotNull
  private FileStatus targetStatus;

  @NotNull
  private Long logId;

  @NotNull
  private String fileName;
}
