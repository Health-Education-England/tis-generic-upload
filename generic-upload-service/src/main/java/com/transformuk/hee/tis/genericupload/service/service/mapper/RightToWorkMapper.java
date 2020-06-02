package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.tcs.api.dto.RightToWorkDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PermitToWorkType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RightToWorkMapper {

  RightToWorkDTO toDto(PersonUpdateXls xls);

  default PermitToWorkType PermitToWorkFromString(String permitToWork) {
    return PermitToWorkType.fromString(permitToWork);
  }
}
