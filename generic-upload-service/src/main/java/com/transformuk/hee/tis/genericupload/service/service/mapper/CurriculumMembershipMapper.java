package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXls;
import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipUpdateXls;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CurriculumMembershipMapper {

  @Mapping(expression = "java(java.util.UUID.fromString(xls.getProgrammeMembershipUuid()))",
      target = "programmeMembershipUuid")
  CurriculumMembershipDTO toDto(CurriculumMembershipCreateXls xls) throws IllegalArgumentException;

  @Mapping(target = "programmeMembershipUuid",
      expression = "java(java.util.UUID.fromString(updateXls.getTisProgrammeMembershipId()))")
  @Mapping(target = "id",
      expression = "java(Long.valueOf(updateXls.getTisCurriculumMembershipId()))")
  CurriculumMembershipDTO toDto(CurriculumMembershipUpdateXls updateXls)
      throws IllegalArgumentException;
}
