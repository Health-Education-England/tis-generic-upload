package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.ProgrammeMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.service.service.utils.DateUtils;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DateUtils.class})
public interface ProgrammeMembershipMapper {

  @Mapping(expression = "java(java.util.UUID.fromString(xls.getProgrammeMembershipId()))",
      target = "uuid")
  @Mapping(source = "rotation", target = "rotation.name")
  ProgrammeMembershipDTO toDto(ProgrammeMembershipUpdateXls xls);

  /**
   * Convert programme membership type from String to enumeration.
   *
   * @param programmeMembershipType
   * @return converted enum value, if not found, return null
   */
  default ProgrammeMembershipType programmeMembershipTypeFromString(
      String programmeMembershipType) {

    if (StringUtils.isNotEmpty(programmeMembershipType)
        && EnumUtils.isValidEnum(ProgrammeMembershipType.class, programmeMembershipType)) {
      return ProgrammeMembershipType.valueOf(programmeMembershipType);
    }
    return null;
  }
}
