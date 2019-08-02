package com.transformuk.hee.tis.genericupload.service.config;

import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.tcs.api.dto.ContactDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonalDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipCurriculaDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.QualificationDTO;
import com.transformuk.hee.tis.tcs.api.dto.RightToWorkDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PermitToWorkType;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that defines all People mappers used by each People configuration step
 * <p>
 * If a step doesn't define a mapper, it then uses the default mapper which maps fuzzly on
 * properties.
 * <p>
 * Each mapper binds properties from a source to a target and in the majority of the cases is a view
 * POJO from the resultset to a DTO that the destination service understands.
 * <p>
 * In most cases converters are used as it provides more control over how the target object is
 * built
 */
@Configuration
public class PeopleMapperConfiguration extends MapperConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(PeopleMapperConfiguration.class);

  @Bean
  public ModelMapper personMapper() {
    Converter<PersonXLS, PersonDTO> personDetailsConverter = new AbstractConverter<PersonXLS, PersonDTO>() {
      @Override
      protected PersonDTO convert(PersonXLS source) {
        final PersonDTO personDTO = new PersonDTO();
        LocalDateTime addedDate = LocalDateTime.now();
        personDTO.setAddedDate(addedDate == null ? LocalDateTime.now() : addedDate);
        personDTO.setInactiveDate(convertDateTime(source.getInactiveDate()));
        personDTO.setPublicHealthNumber(source.getPublicHealthNumber());
        personDTO.setStatus(Status.fromString(source.getRecordStatus()));
        personDTO.setRole(source.getRole());
        return personDTO;
      }

    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(personDetailsConverter);
    return modelMapper;
  }

  @Bean
  public ModelMapper qualificationMapper() {
    Converter<PersonXLS, QualificationDTO> qualificationConverter = new AbstractConverter<PersonXLS, QualificationDTO>() {
      @Override
      protected QualificationDTO convert(PersonXLS source) {
        final QualificationDTO qualificationDTO = new QualificationDTO();
        qualificationDTO.setCountryOfQualification(source.getCountryOfQualification());
        qualificationDTO.setQualification(source.getQualification());
        qualificationDTO.setMedicalSchool(source.getMedicalSchool());
        qualificationDTO.setQualificationAttainedDate(convertDate(source.getDateAttained()));
        return qualificationDTO;
      }
    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(qualificationConverter);
    return modelMapper;
  }

  @Bean
  public ModelMapper contactDetailMapper() {
    Converter<PersonXLS, ContactDetailsDTO> contactDetailConverter = new AbstractConverter<PersonXLS, ContactDetailsDTO>() {
      @Override
      protected ContactDetailsDTO convert(PersonXLS source) {
        final ContactDetailsDTO contactDetailsDTO = new ContactDetailsDTO();
        contactDetailsDTO.setSurname(source.getSurname());
        contactDetailsDTO.setForenames(source.getForenames());
        contactDetailsDTO.setKnownAs(source.getKnownAs());
        contactDetailsDTO.setTitle(source.getTitle());
        contactDetailsDTO.setTelephoneNumber(source.getTelephone());
        contactDetailsDTO.setMobileNumber(source.getMobile());
        contactDetailsDTO.setEmail(source.getEmailAddress());
        contactDetailsDTO.setAddress1(source.getAddress1());
        contactDetailsDTO.setAddress2(source.getAddress2());
        contactDetailsDTO.setAddress3(source.getAddress3());
        contactDetailsDTO.setPostCode(source.getPostCode());
        return contactDetailsDTO;
      }
    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(contactDetailConverter);
    return modelMapper;
  }

  @Bean
  public ModelMapper personalDetailMapper() {
    Converter<PersonXLS, PersonalDetailsDTO> personalDetailConverter = new AbstractConverter<PersonXLS, PersonalDetailsDTO>() {
      @Override
      protected PersonalDetailsDTO convert(PersonXLS source) {
        final PersonalDetailsDTO personalDetailsDTO = new PersonalDetailsDTO();
        personalDetailsDTO.setMaritalStatus(source.getMaritalStatus());
        personalDetailsDTO.setDateOfBirth(convertDate(source.getDateOfBirth()));
        personalDetailsDTO.setDisability(source.getDisability());
        personalDetailsDTO.setDisabilityDetails(source.getDisabilityDetails());
        personalDetailsDTO.setNationality(source.getNationality());
        personalDetailsDTO.setGender(source.getGender());
        personalDetailsDTO.setEthnicOrigin(source.getEthnicOrigin());
        personalDetailsDTO.setSexualOrientation(source.getSexualOrientation());
        personalDetailsDTO.setReligiousBelief(source.getReligiousBelief());
        return personalDetailsDTO;
      }
    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(personalDetailConverter);
    return modelMapper;
  }

  @Bean
  public ModelMapper gmcDetailMapper() {
    Converter<PersonXLS, GmcDetailsDTO> gmcDetailConverter = new AbstractConverter<PersonXLS, GmcDetailsDTO>() {
      @Override
      protected GmcDetailsDTO convert(PersonXLS source) {
        final GmcDetailsDTO gmcDetailsDTO = new GmcDetailsDTO();
        gmcDetailsDTO.setGmcNumber(source.getGmcNumber());
        return gmcDetailsDTO;
      }
    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(gmcDetailConverter);
    return modelMapper;
  }

  @Bean
  public ModelMapper gdcDetailMapper() {
    Converter<PersonXLS, GdcDetailsDTO> gdcDetailConverter = new AbstractConverter<PersonXLS, GdcDetailsDTO>() {
      @Override
      protected GdcDetailsDTO convert(PersonXLS source) {
        final GdcDetailsDTO gdcDetailsDTO = new GdcDetailsDTO();
        gdcDetailsDTO.setGdcNumber(source.getGdcNumber());
        return gdcDetailsDTO;
      }
    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(gdcDetailConverter);
    return modelMapper;
  }

  @Bean
  public ModelMapper rightToWorkMapper() {
    Converter<PersonXLS, RightToWorkDTO> rightToWorkConverter = new AbstractConverter<PersonXLS, RightToWorkDTO>() {
      @Override
      protected RightToWorkDTO convert(PersonXLS source) {
        final RightToWorkDTO rightToWorkDTO = new RightToWorkDTO();
        rightToWorkDTO.setPermitToWork(PermitToWorkType.fromString(source.getPermitToWork()));
        rightToWorkDTO.setSettled(source.getSettled());
        rightToWorkDTO.setVisaDetails(source.getVisaDetails());
        rightToWorkDTO.setVisaValidTo(convertDate(source.getVisaValidTo()));
        rightToWorkDTO.setVisaIssued(convertDate(source.getVisaIssued()));
        rightToWorkDTO.setEeaResident(source.getEeaResident());
        return rightToWorkDTO;
      }
    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(rightToWorkConverter);
    return modelMapper;
  }

  @Bean
  public ModelMapper programMembershipMapper() {
    Converter<PersonXLS, ProgrammeDTO> programConverter =
        new AbstractConverter<PersonXLS, ProgrammeDTO>() {
          @Override
          protected ProgrammeDTO convert(PersonXLS source) {
            Set<CurriculumDTO> curricula = new HashSet<>();

            CurriculumDTO curriculumDTO1 = new CurriculumDTO();
            curriculumDTO1.setName(source.getCurriculum1());
            curricula.add(curriculumDTO1);

            CurriculumDTO curriculumDTO2 = new CurriculumDTO();
            curriculumDTO2.setName(source.getCurriculum2());
            curricula.add(curriculumDTO2);

            CurriculumDTO curriculumDTO3 = new CurriculumDTO();
            curriculumDTO3.setName(source.getCurriculum3());
            curricula.add(curriculumDTO3);

            ProgrammeDTO programmeDTO = new ProgrammeDTO();
            programmeDTO.setProgrammeName(source.getProgrammeName());
            programmeDTO.setProgrammeNumber(source.getProgrammeNumber());
            programmeDTO.setCurricula(curricula);       //links the programme to the curricula

            LocalDate programmeEndDate = convertDate(source.getProgrammeEndDate());
            ProgrammeMembershipType programmeMembershipType = ProgrammeMembershipType
                .fromString(source.getProgrammeMembership());

            ProgrammeMembershipDTO programmeMembershipDTO1 = new ProgrammeMembershipDTO();
            programmeMembershipDTO1.setProgrammeMembershipType(programmeMembershipType);
            programmeMembershipDTO1.setProgrammeEndDate(programmeEndDate);
                        /*programmeMembershipDTO1.setCurriculumStartDate(convertDate(source.getCurriculum1StartDate()));
                        programmeMembershipDTO1.setCurriculumEndDate(convertDate(source.getCurriculum1EndDate()));
*/
            ProgrammeMembershipDTO programmeMembershipDTO2 = new ProgrammeMembershipDTO();
            programmeMembershipDTO2.setProgrammeMembershipType(programmeMembershipType);
            programmeMembershipDTO2.setProgrammeEndDate(programmeEndDate);
                       /* programmeMembershipDTO2.setCurriculumStartDate(convertDate(source.getCurriculum2StartDate()));
                        programmeMembershipDTO2.setCurriculumEndDate(convertDate(source.getCurriculum2EndDate()));
*/
            ProgrammeMembershipDTO programmeMembershipDTO3 = new ProgrammeMembershipDTO();
            programmeMembershipDTO3.setProgrammeMembershipType(programmeMembershipType);
            programmeMembershipDTO3.setProgrammeEndDate(programmeEndDate);
  /*                      programmeMembershipDTO3.setCurriculumStartDate(convertDate(source.getCurriculum3StartDate()));
                        programmeMembershipDTO3.setCurriculumEndDate(convertDate(source.getCurriculum3EndDate()));
*/
            //links the programme membership to each curricula
            ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDTO1 = new ProgrammeMembershipCurriculaDTO();
            programmeMembershipCurriculaDTO1.setCurriculumDTO(curriculumDTO1);
            ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDTO2 = new ProgrammeMembershipCurriculaDTO();
            programmeMembershipCurriculaDTO2.setCurriculumDTO(curriculumDTO2);
            ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDTO3 = new ProgrammeMembershipCurriculaDTO();
            programmeMembershipCurriculaDTO3.setCurriculumDTO(curriculumDTO3);

            return programmeDTO;
          }
        };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(programConverter);

    return modelMapper;
  }

}
