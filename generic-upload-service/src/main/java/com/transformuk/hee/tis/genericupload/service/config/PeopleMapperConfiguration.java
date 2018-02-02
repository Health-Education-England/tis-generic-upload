package com.transformuk.hee.tis.genericupload.service.config;

import com.transformuk.hee.tis.client.ClientService;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.tcs.api.dto.*;
import com.transformuk.hee.tis.tcs.api.enumeration.PermitToWorkType;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import com.transformuk.hee.tis.tcs.api.enumeration.QualificationType;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * Configuration class that defines all People mappers used by each People configuration step
 * <p>
 * If a step doesn't define a mapper, it then uses the default mapper which maps fuzzly on properties.
 * <p>
 * Each mapper binds properties from a source to a target and in the majority of the cases is a view POJO from the resultset
 * to a DTO that the destination service understands.
 * <p>
 * In most cases converters are used as it provides more control over how the target object is built
 */
@Configuration
public class PeopleMapperConfiguration extends MapperConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(PeopleMapperConfiguration.class);

  private static ProgrammeMembershipType convertMembershipType(String membershipType) {
    ProgrammeMembershipType programmeMembershipType = null;
    if (membershipType != null) {
      if ("FTSTA".equalsIgnoreCase(membershipType)) {
        programmeMembershipType = ProgrammeMembershipType.FTSTA;
      } else if ("Military".equalsIgnoreCase(membershipType)) {
        programmeMembershipType = ProgrammeMembershipType.MILITARY;
      } else if ("Substantive".equalsIgnoreCase(membershipType)) {
        programmeMembershipType = ProgrammeMembershipType.SUBSTANTIVE;
      } else if ("Visitor".equalsIgnoreCase(membershipType)) {
        programmeMembershipType = ProgrammeMembershipType.VISITOR;
      } else if ("LAT".equalsIgnoreCase(membershipType)) {
        programmeMembershipType = ProgrammeMembershipType.LAT;
      } else {
        LOG.warn("Membership Type {} was found but not accounted for in if statements", membershipType);
      }
    }
    return programmeMembershipType;
  }

  private static QualificationType convertQualificationType(String qualificationType) {
    QualificationType type = null;
    if (qualificationType != null) {
      if ("Primary qualificatio".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.PRIMARY_QUALIFICATION;
      } else if ("Bachelors Degree".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.BASIC_DEGREE;
      } else if ("Basic Degree".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.BASIC_DEGREE;
      } else if ("BM".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.BM;
      } else if ("Higher qualification".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.HIGHER_QUALIFICATION;
      } else if ("BM BS".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.BMBS;
      } else if ("MD".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.MD;
      } else if ("NVQ".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.NVQ;
      } else if ("Medicine MBChB".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.MBBS;
      } else if ("BMBS".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.BMBS;
      } else if ("PhD".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.PHD;
      } else if ("MB BS/BSc".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.MBBS;
      } else if ("Masters Degree".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.HIGHER_DEGREE;
      } else if ("MB BS".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.MBBS;
      } else if ("Diploma".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.DIPLOMA;
      } else if ("Bachelors Degree Hon".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.BASIC_DEGREE;
      } else if ("BM BCh".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.BMBS;
      } else if ("Certification".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.CERTIFICATION;
      } else if ("Higher Degree".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.HIGHER_DEGREE;
      } else if ("MB BChir".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.MBBS;
      } else if ("BDS".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.BDS;
      } else if ("Non Medical Degree".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.NON_MEDICAL_DEGREE;
      } else if ("Lekarz".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.LEKARZ;
      } else if ("MB BCh".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.MBBS;
      } else if ("MB ChB".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.MBBS;
      } else if ("State Exam".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.STATE_EXAM;
      } else if ("College/Faculty".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.COLLEGE_FACULTY;
      } else if ("MbChB".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.MBBS;
      } else if ("Medicine".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.HIGHER_DEGREE;
      } else if ("MBBS".equalsIgnoreCase(qualificationType)) {
        type = QualificationType.MBBS;
      } else {
        LOG.warn("Qualification Type {} was found but not accounted for in if statements", type);
      }

    }
    return type;
  }

  private static PermitToWorkType convertPermitToWorkType(String value) {
    PermitToWorkType type = null;
    if ("Indefinite leave".equalsIgnoreCase(value)) {
      type = PermitToWorkType.INDEFINITE_LEAVE;
    } else if ("HSMP".equalsIgnoreCase(value)) {
      type = PermitToWorkType.HSMP;
    } else if ("Permit free".equalsIgnoreCase(value)) {
      type = PermitToWorkType.PERMIT_FREE;
    } else if ("Tier 2".equalsIgnoreCase(value)) {
      type = PermitToWorkType.TIER_2;
    } else if ("Tier 1".equalsIgnoreCase(value)) {
      type = PermitToWorkType.TIER_1;
    } else if ("Limited LTR".equalsIgnoreCase(value)) {
      type = PermitToWorkType.LIMITED_LTR;
    } else if ("Work permit".equalsIgnoreCase(value)) {
      type = PermitToWorkType.WORK_PERMIT;
    } else if ("Dependent of HSMP".equalsIgnoreCase(value)) {
      type = PermitToWorkType.DEPENDENT_OF_HSMP;
    } else if ("Spouse of EEA National".equalsIgnoreCase(value)) {
      type = PermitToWorkType.SPOUSE_OF_EEA_NATIONAL;
    } else if ("Spouse of HSMP holder".equalsIgnoreCase(value)) {
      type = PermitToWorkType.SPOUSE_OF_HSMP_HOLDER;
    } else if ("Tier 4".equalsIgnoreCase(value) || "Tier 4 –".equalsIgnoreCase(value)) {
      type = PermitToWorkType.TIER_4;
    } else if ("Student visa".equalsIgnoreCase(value)) {
      type = PermitToWorkType.STUDENT_VISA;
    } else if ("Dependent of work permit".equalsIgnoreCase(value)) {
      type = PermitToWorkType.DEPENDENT_OF_WORK_PERMIT;
    } else if ("Other".equalsIgnoreCase(value)) {
      type = PermitToWorkType.OTHER;
    } else if ("Indefinite Leave to remain".equalsIgnoreCase(value)) {
      type = PermitToWorkType.INDEFINITE_LEAVE_TO_REMAIN;
    } else if ("Tier 5".equalsIgnoreCase(value)) {
      type = PermitToWorkType.TIER_5;
    } else if ("Refugee Doctor".equalsIgnoreCase(value)) {
      type = PermitToWorkType.REFUGEE_DOCTOR;
    } else if ("Postgraduate Visa".equalsIgnoreCase(value)) {
      type = PermitToWorkType.POSTGRADUATE_VISA;
    } else if ("Tier 2 - points based system".equalsIgnoreCase(value)) {
      type = PermitToWorkType.TIER_2_POINTS_BASED_SYSTEM;
    } else if ("UK National".equalsIgnoreCase(value)) {
      type = PermitToWorkType.UK_NATIONAL;
    } else if ("Resident Permit".equalsIgnoreCase(value)) {
      type = PermitToWorkType.RESIDENT_PERMIT;
    } else if ("Indefinate leave".equalsIgnoreCase(value)) {
      type = PermitToWorkType.INDEFINATE_LEAVE;
    } else if ("TIER 4 (GENERAL(S)) STUDENT".equalsIgnoreCase(value)) {
      type = PermitToWorkType.TIER_4_GENERALS_STUDENT;
    } else if ("Ancestry visa".equalsIgnoreCase(value)) {
      type = PermitToWorkType.ANCESTRY_VISA;
    } else if ("Dependent of HMSP".equalsIgnoreCase(value)) {
      type = PermitToWorkType.DEPENDENT_OF_HMSP;
    } else if ("TWES/MTI".equalsIgnoreCase(value)) {
      type = PermitToWorkType.TWES_MTI;
    } else if ("Unspecified".equalsIgnoreCase(value)) {
      type = PermitToWorkType.UNSPECIFIED;
    } else if ("Yes".equalsIgnoreCase(value)) {
      type = PermitToWorkType.YES;
    } else if ("Refugee in the UK".equalsIgnoreCase(value)) {
      type = PermitToWorkType.REFUGEE_IN_THE_UK;
    } else if ("Evidence of Entitlement".equalsIgnoreCase(value)) {
      type = PermitToWorkType.EVIDENCE_OF_ENTITLEMENT;
    } else if ("EC/EEA National".equalsIgnoreCase(value)) {
      type = PermitToWorkType.EC_EEA_NATIONAL;
    } else {
      LOG.warn("PermitToWork Type {} was found but not accounted for in if statements", type);
    }

    return type;
  }

  /*private Person getExistingPersonBySurnameAndGmcNumber(String surname, String gmcNumber) {
    for (Person person : existingPersons) {
      if (surname.equalsIgnoreCase(person.getContactDetails().getSurname()) && gmcNumber.equalsIgnoreCase(person.getGmcDetails().getGmcNumber())) {
        return person;
      }
    }
    return null;
  }*/

  @Bean
  public ModelMapper personMapper() {

    Converter<PersonXLS, PersonDTO> personDetailsConverter = new AbstractConverter<PersonXLS, PersonDTO>() {
      @Override
      protected PersonDTO convert(PersonXLS source) {
        final PersonDTO personDTO = new PersonDTO();
        LocalDateTime addedDate = LocalDateTime.now();
        personDTO.setAddedDate(addedDate == null ? LocalDateTime.now() : addedDate);
        //personDTO.setAmendedDate(convertDateTime(source.getAmendedDate()));
        //personDTO.setComments(source.getComments);
        personDTO.setInactiveDate(convertDateTime(source.getInactiveDate()));
        //personDTO.setInactiveNotes(source.getInactiveReason());
        //personDTO.setIntrepidId(source.getPersonID().toString());
        personDTO.setPublicHealthNumber(source.getPublicHealthNumber());
        //personDTO.setStatus(Status.valueOf(source.getStatus().toUpperCase()));
        return personDTO;
      }

    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(personDetailsConverter);
    return modelMapper;
  }


  @Bean
  public ModelMapper qualificationMapper() {

    Converter<PersonXLS, QualificationDTO> contactDetailConverter = new AbstractConverter<PersonXLS, QualificationDTO>() {
      @Override
      protected QualificationDTO convert(PersonXLS source) {
        //Person person = getExistingPersonBySurnameAndGmcNumber(source.getSurname(), source.getGMCNumber());
        final QualificationDTO qualificationDTO = new QualificationDTO();
        /*if (person != null) {
          PersonDTO personDTO = new PersonDTO();
          //personDTO.setId(person.getId());
          //personDTO.setAmendedDate(person.getAmendedDate());
          //qualificationDTO.setIntrepidId(person.getIntrepidId().toString());
          qualificationDTO.setPerson(personDTO);
          qualificationDTO.setCountryOfQualification(source.getCountryOfQualification());
          qualificationDTO.setQualification(source.getQualification());
          qualificationDTO.setMedicalSchool(source.getMedicalSchool());
          qualificationDTO.setQualificationAttainedDate(convertDate(source.getDateAttained()));
          qualificationDTO.setQualificationType(convertQualificationType(source.getQualificationType()));
        }*/
        return qualificationDTO;
      }
    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(contactDetailConverter);
    return modelMapper;
  }

  @Bean
  public ModelMapper contactDetailMapper() {

    Converter<PersonXLS, ContactDetailsDTO> contactDetailConverter = new AbstractConverter<PersonXLS, ContactDetailsDTO>() {
      @Override
      protected ContactDetailsDTO convert(PersonXLS source) {
        //Person person = getExistingPersonBySurnameAndGmcNumber(source.getSurname(), source.getGMCNumber());
        final ContactDetailsDTO contactDetailsDTO = new ContactDetailsDTO();
        //contactDetailsDTO.setId(person.getId());
        contactDetailsDTO.setSurname(source.getSurname());
        contactDetailsDTO.setForenames(source.getForenames());
        contactDetailsDTO.setKnownAs(source.getKnownAs());
        contactDetailsDTO.setMaidenName(source.getMaidenName());
        contactDetailsDTO.setInitials(source.getInitials());
        contactDetailsDTO.setTitle(source.getTitle());
        contactDetailsDTO.setTelephoneNumber(source.getTelephone());
        contactDetailsDTO.setMobileNumber(source.getMobile());
        contactDetailsDTO.setLegalForenames(source.getLegalForenames());
        contactDetailsDTO.setLegalSurname(source.getLegalSurname());
        contactDetailsDTO.setEmail(source.getEmailAddress());
        //contactDetailsDTO.setWorkEmail(source.getEmailAddress());
        contactDetailsDTO.setAddress1(source.getAddress1());
        contactDetailsDTO.setAddress2(source.getAddress2());
        contactDetailsDTO.setAddress3(source.getAddress3());
        contactDetailsDTO.setAddress4(source.getAddress4());
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

    Converter<PersonXLS, PersonalDetailsDTO> contactDetailConverter = new AbstractConverter<PersonXLS, PersonalDetailsDTO>() {
      @Override
      protected PersonalDetailsDTO convert(PersonXLS source) {
        //Person person = getExistingPersonBySurnameAndGmcNumber(source.getSurname(), source.getGMCNumber());
        final PersonalDetailsDTO personalDetailsDTO = new PersonalDetailsDTO();
        //personalDetailsDTO.setId(person.getId());
        personalDetailsDTO.setMaritalStatus(source.getMaritalStatus());
        personalDetailsDTO.setDateOfBirth(convertDate(source.getDateOfBirth()));
        personalDetailsDTO.setDisability(source.getDisability());
        personalDetailsDTO.setDisabilityDetails(source.getDisabilityDetails());
        personalDetailsDTO.setDualNationality(source.getDualNationality());
        personalDetailsDTO.setNationality(source.getNationality());
        personalDetailsDTO.setGender(source.getGender());
        return personalDetailsDTO;
      }
    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(contactDetailConverter);
    return modelMapper;
  }

  @Bean
  public ModelMapper gmcDetailMapper() {

    Converter<PersonXLS, GmcDetailsDTO> contactDetailConverter = new AbstractConverter<PersonXLS, GmcDetailsDTO>() {
      @Override
      protected GmcDetailsDTO convert(PersonXLS source) {
        //Person person = getExistingPersonBySurnameAndGmcNumber(source.getSurname(), source.getGMCNumber());
        final GmcDetailsDTO gmcDetailsDTO = new GmcDetailsDTO();
        //gmcDetailsDTO.setId(person.getId());
        gmcDetailsDTO.setGmcNumber(source.getGMCNumber());
        gmcDetailsDTO.setGmcStatus(source.getGMCStatus());
        gmcDetailsDTO.setGmcStartDate(convertDate(source.getGMCStartDate()));
        return gmcDetailsDTO;
      }
    };


    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(contactDetailConverter);
    return modelMapper;
  }

  @Bean
  public ModelMapper gdcDetailMapper() {

    Converter<PersonXLS, GdcDetailsDTO> contactDetailConverter = new AbstractConverter<PersonXLS, GdcDetailsDTO>() {
      @Override
      protected GdcDetailsDTO convert(PersonXLS source) {
        //Person person = getExistingPersonBySurnameAndGmcNumber(source.getSurname(), source.getGMCNumber());
        final GdcDetailsDTO gdcDetailsDTO = new GdcDetailsDTO();
        //gdcDetailsDTO.setId(person.getId());
        gdcDetailsDTO.setGdcNumber(source.getGDCNumber());
        //gdcDetailsDTO.setGdcStatus(source.getGMCStatus());
        //gdcDetailsDTO.setGdcStartDate(convertDate(source.getGdcStartDate()));
        return gdcDetailsDTO;
      }
    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(contactDetailConverter);
    return modelMapper;
  }

  @Bean
  public ModelMapper rightToWorkMapper() {

    Converter<PersonXLS, RightToWorkDTO> contactDetailConverter = new AbstractConverter<PersonXLS, RightToWorkDTO>() {
      @Override
      protected RightToWorkDTO convert(PersonXLS source) {
        //Person person = getExistingPersonBySurnameAndGmcNumber(source.getSurname(), source.getGMCNumber());
        final RightToWorkDTO rightToWorkDTO = new RightToWorkDTO();
        //rightToWorkDTO.setId(person.getId());
        rightToWorkDTO.setEeaResident(source.getEEAResident());
        rightToWorkDTO.setPermitToWork(convertPermitToWorkType(source.getPermitToWork()));
        rightToWorkDTO.setSettled(source.getSettled());
        rightToWorkDTO.setVisaDetails(source.getVisaDetails());
        //rightToWorkDTO.setVisaIssued(convertDate(source.getVisaIssued()));
        //rightToWorkDTO.setVisaValidTo(convertDate(source.getVisaValidTo()));
        return rightToWorkDTO;
      }
    };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(contactDetailConverter);
    return modelMapper;
  }


  /*@Bean
  public ModelMapper curriculumMembershipMapper() {
    AbstractConverter<PersonXLS, ProgrammeMembershipDTO> pmConverter =
        new AbstractConverter<PersonXLS, ProgrammeMembershipDTO>() {

          @Override
          protected ProgrammeMembershipDTO convert(PersonXLS source) {
            StepConfiguration personTcsStep = new StepConfiguration();//getStepConfigurationByName(PERSON_TCS_STEP);
            Map<String, ServiceKey> cachePersonMap = etlService.getBulkServiceData(personTcsStep, personTcsStep.getDtoClassName());

            StepConfiguration programmeTcsStep = new StepConfiguration();//getStepConfigurationByName(PROGRAMMES_TCS_STEP);
            Map<String, ServiceKey> cacheProgrammeMap = etlService.getBulkServiceData(programmeTcsStep, programmeTcsStep.getDtoClassName());

            StepConfiguration curriculumTcsStep = new StepConfiguration();//getStepConfigurationByName(CURRICULUM_TCS_STEP);
            Map<String, ServiceKey> cacheCurriculumMap = etlService.getBulkServiceData(curriculumTcsStep, curriculumTcsStep.getDtoClassName());

            ServiceKey serviceKey = cachePersonMap.get(source.getPersonID().toString());
            Long programmeId = Long.parseLong(cacheProgrammeMap.get(source.getProgrammeID().toString()).getId());
            Long curriculumId = Long.parseLong(cacheCurriculumMap.get(source.getCurriculumID().toString()).getId());

            if (serviceKey == null || programmeId == null || curriculumId == null) {
              return null;
            }

            PersonDTO personDTO = new PersonDTO();
            personDTO.setId(Long.parseLong(serviceKey.getId()));
            personDTO.setAmendedDate(serviceKey.getAmendedDate());

            ProgrammeMembershipDTO programmeMembershipDTO = new ProgrammeMembershipDTO();
            programmeMembershipDTO.setIntrepidId(source.getCurriculumMembershipID().toString());
            programmeMembershipDTO.setProgrammeMembershipType(convertMembershipType(source.getMembershipType()));
            programmeMembershipDTO.setRotation(source.getRotation());
            programmeMembershipDTO.setCurriculumStartDate(convertDate(source.getCurriculum1StartDate()));
            programmeMembershipDTO.setCurriculumEndDate(convertDate(source.getCurriculum1EndDate()));
            programmeMembershipDTO.setPeriodOfGrace(null);
            programmeMembershipDTO.setProgrammeStartDate(convertDate(source.getProgrammeMembership()));
            programmeMembershipDTO.setCurriculumCompletionDate(convertDate(source.getCurriculum1EndDate()));
            programmeMembershipDTO.setProgrammeEndDate(convertDate(source.getProgrammeEndDate()));
            programmeMembershipDTO.setLeavingDestination(source.getDestination());
            programmeMembershipDTO.setProgrammeId(programmeId);
            programmeMembershipDTO.setCurriculumId(curriculumId);
            programmeMembershipDTO.setTrainingNumberId(null);
            programmeMembershipDTO.setPerson(personDTO);
            return programmeMembershipDTO;
          }
        };

    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(pmConverter);

    return modelMapper;
  }*/

}
