package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.CurriculumMembershipCreateXLS;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CurriculumMembershipMapper {

  @Mapping(expression = "java(java.util.UUID.fromString(xls.getProgrammeMembershipUuid()))",
      target = "programmeMembershipUuid")
  @Mapping(target = "curriculumId", ignore = true)
  CurriculumMembershipDTO toDto(CurriculumMembershipCreateXLS xls) throws IllegalArgumentException;
}
