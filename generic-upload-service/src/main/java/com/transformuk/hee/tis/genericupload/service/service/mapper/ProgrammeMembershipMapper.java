package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.ProgrammeMembershipUpdateXls;
import com.transformuk.hee.tis.genericupload.service.util.DateUtils;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.RotationDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {DateUtils.class})
public interface ProgrammeMembershipMapper {

  @Mapping(expression = "java(java.util.UUID.fromString(xls.getProgrammeMembershipId()))",
      target = "uuid")
  @Mapping(source = "xls", target = "rotation", qualifiedByName = "setRotation")
  ProgrammeMembershipDTO toDto(ProgrammeMembershipUpdateXls xls);

  /**
   * Convert programme membership type from String to enumeration.
   *
   * @param programmeMembershipType the programme membership type string
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

  /**
   * Custom method to convert rotation name to Rotation Dto.
   * Mapstruct does not support: create target nested object only when source field is not null.
   *
   * @param xls the programme membership xls to convert
   * @return the rotation dto
   */
  @Named("setRotation")
  default RotationDTO nameToRotation(ProgrammeMembershipUpdateXls xls) {
    if (xls == null || xls.getRotation() == null) {
      return null;
    }

    RotationDTO rotationDto = new RotationDTO();
    rotationDto.setName(xls.getRotation());
    return rotationDto;
  }
}
