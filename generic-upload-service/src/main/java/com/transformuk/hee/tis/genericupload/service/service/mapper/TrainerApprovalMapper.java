package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.tcs.api.dto.TrainerApprovalDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ApprovalStatus;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = PersonMapper.class)
public interface TrainerApprovalMapper {

  @Mappings({
      @Mapping(source = "trainerApprovalStartDate", target = "startDate"),
      @Mapping(source = "trainerApprovalEndDate", target = "endDate"),
      @Mapping(source = "trainerApprovalStatus", target = "approvalStatus"),
      @Mapping(source = ".", target = "person")
  })
  TrainerApprovalDTO toDto(PersonUpdateXls xls);

  default ApprovalStatus PermitToWorkFromString(String trainerApprovalStatus) {
    if (!StringUtils.isEmpty(trainerApprovalStatus) && EnumUtils.isValidEnum(ApprovalStatus.class, trainerApprovalStatus)) {
      return ApprovalStatus.valueOf(trainerApprovalStatus);
    }
    return null;
  }
}
