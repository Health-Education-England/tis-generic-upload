package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.tcs.api.dto.RightToWorkDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RightToWorkMapper {

  RightToWorkDTO toDto(PersonUpdateXls xls);
}
