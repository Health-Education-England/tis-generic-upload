package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GdcDetailsMapper {

  @Mapping(source = "tisPersonId", target = "id")
  GdcDetailsDTO toDto(PersonUpdateXls xls);
}
