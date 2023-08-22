package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.ProgrammeMembershipUpdateXLS;
import com.transformuk.hee.tis.genericupload.service.service.utils.DateUtils;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {DateUtils.class})
public interface ProgrammeMembershipMapper {
  @Mappings({
    @Mapping(expression = "java(java.util.UUID.fromString(xls.getProgrammeMembershipId()))",
        target = "uuid"),
    @Mapping(source = "rotation", target = "rotation.name")
  })
  ProgrammeMembershipDTO toDto(ProgrammeMembershipUpdateXLS xls);

  default ProgrammeMembershipType programmeMembershipTypeFromString(
      String programmeMembershipType) {

    if (StringUtils.isNotEmpty(programmeMembershipType)
        && EnumUtils.isValidEnum(ProgrammeMembershipType.class, programmeMembershipType)) {
      return ProgrammeMembershipType.valueOf(programmeMembershipType);
    }
    return null;
  }
}
