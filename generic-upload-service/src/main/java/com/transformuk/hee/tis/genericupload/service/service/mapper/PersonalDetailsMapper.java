package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.tcs.api.dto.PersonalDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersonalDetailsMapper {

  @Mapping(source = "tisPersonId", target = "id")
  PersonalDetailsDTO toDto(PersonUpdateXls xls);
}
