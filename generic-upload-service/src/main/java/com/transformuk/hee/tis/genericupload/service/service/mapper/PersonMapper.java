package com.transformuk.hee.tis.genericupload.service.service.mapper;

import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {ContactDetailsMapper.class, GdcDetailsMapper.class,
    GmcDetailsMapper.class, PersonalDetailsMapper.class, RightToWorkMapper.class})
public interface PersonMapper {

  @Mappings({
      @Mapping(source = "tisPersonId", target = "id"),
      @Mapping(source = ".", target = "contactDetails"),
      @Mapping(source = ".", target = "gdcDetails"),
      @Mapping(source = ".", target = "gmcDetails"),
      @Mapping(source = ".", target = "personalDetails"),
      @Mapping(source = ".", target = "rightToWork")
  })
  PersonDTO toDto(PersonUpdateXls xls);
}
