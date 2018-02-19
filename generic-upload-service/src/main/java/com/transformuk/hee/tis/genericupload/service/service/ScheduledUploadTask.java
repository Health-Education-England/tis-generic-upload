package com.transformuk.hee.tis.genericupload.service.service;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.config.ApplicationConfiguration;
import com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper;
import com.transformuk.hee.tis.genericupload.service.parser.PersonHeaderMapper;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import com.transformuk.hee.tis.tcs.api.dto.*;
import com.transformuk.hee.tis.tcs.api.enumeration.PermitToWorkType;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDateTime;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ScheduledUploadTask {
    private static final Logger logger = getLogger(ScheduledUploadTask.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final long CACHE_EXPIRY_DURATION = 10;//TODO externalise duration

    @Autowired
    private TcsServiceImpl tcsServiceImpl;
    @Autowired
    private final FileStorageRepository fileStorageRepository;
    @Autowired
    private FileProcessService fileProcessService;

    private final ApplicationTypeRepository applicationTypeRepository;
    private ApplicationConfiguration applicationConfiguration;

    private static LoadingCache<String, CurriculumDTO> curriculumDTOMapCache = null;
    private static LoadingCache<ProgrammeCompositekey, ProgrammeDTO> programmeDTOMapCache = null;

    @Autowired
    public ScheduledUploadTask(FileStorageRepository fileStorageRepository,
                               ApplicationTypeRepository applicationTypeRepository) {
        this.fileStorageRepository = fileStorageRepository;
        this.applicationTypeRepository = applicationTypeRepository;
    }

    @PostConstruct
    public void initialiseCaches() throws Exception {
        CacheLoader<String, CurriculumDTO> curriculumDTOMapCacheLoader = new CacheLoader<String, CurriculumDTO>() {
            @Override
            public CurriculumDTO load(String key) {
                return tcsServiceImpl.getCurriculaByName(key).get(0);
            }
        };
        curriculumDTOMapCache = CacheBuilder.newBuilder()
                .expireAfterAccess(CACHE_EXPIRY_DURATION, TimeUnit.SECONDS)
                .build(curriculumDTOMapCacheLoader);

        CacheLoader<ProgrammeCompositekey, ProgrammeDTO> programmeDTOMapCacheLoader = new CacheLoader<ProgrammeCompositekey, ProgrammeDTO>() {
            @Override
            public ProgrammeDTO load(ProgrammeCompositekey key) {
                return tcsServiceImpl.getProgrammeByNameAndNumber(key.programmeName, key.programmeNumber).get(0);
            }
        };
        programmeDTOMapCache = CacheBuilder.newBuilder()
                .expireAfterAccess(CACHE_EXPIRY_DURATION, TimeUnit.SECONDS)
                .build(programmeDTOMapCacheLoader);
    }

    //waits fixedDelay milliseconds after the last run task
    @Scheduled(fixedDelay = 5000, initialDelay = 2000) //TODO externalise this wait interval,
    public void scheduleTaskWithFixedDelay() {
        logger.info("Fixed Delay Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
        for(ApplicationType applicationType : applicationTypeRepository.findByFileStatusOrderByStartDate(FileStatus.PENDING)) {
            //set to in progress
            applicationType.setFileStatus(FileStatus.IN_PROGRESS);
            applicationTypeRepository.save(applicationType);

            ByteArrayOutputStream baos = (ByteArrayOutputStream) fileStorageRepository.download(applicationType.getLogId(), UploadFileService.CONTAINER_NAME, applicationType.getFileName());
            InputStream bis = new ByteArrayInputStream(baos.toByteArray());
            ExcelToObjectMapper excelToObjectMapper = null;
            final List<PersonXLS> personXLSS;
            try {
                excelToObjectMapper = new ExcelToObjectMapper(bis);
                personXLSS = excelToObjectMapper.map(PersonXLS.class, new PersonHeaderMapper().getFieldMap()); // TODO : this is being done twice, once while doing first level validation, consider optimising

                if (!CollectionUtils.isEmpty(personXLSS)) {
                    for(PersonXLS personXLS  : personXLSS) {
                        Set<CurriculumDTO> curricula = new HashSet<>();

                        CurriculumDTO curriculumDTO1 = personXLS.getCurriculum1() == null ? null : curriculumDTOMapCache.get(personXLS.getCurriculum1());
                        CurriculumDTO curriculumDTO2 = personXLS.getCurriculum2() == null ? null : curriculumDTOMapCache.get(personXLS.getCurriculum2());
                        CurriculumDTO curriculumDTO3 = personXLS.getCurriculum3() == null ? null : curriculumDTOMapCache.get(personXLS.getCurriculum3());

                        ProgrammeDTO programmeDTO = null;
                        if(personXLS.getProgrammeName() != null && personXLS.getProgrammeNumber() != null) {
                            programmeDTO = programmeDTOMapCache.get(new ProgrammeCompositekey(personXLS.getProgrammeName(), personXLS.getProgrammeNumber()));
                            programmeDTO.setCurricula(curricula);       //links the programme to the curricula
                        } else {
                            //TODO consider throwing an exception instead
                        }

                        PersonDTO personDTO = getPersonDTO(personXLS, curriculumDTO1, curriculumDTO2, curriculumDTO3, programmeDTO);
                        if(personDTO != null) { //currently can only be null if programme isn't found
                            PersonDTO savedPersonDTO = tcsServiceImpl.createPerson(personDTO);
                            for (ProgrammeMembershipDTO programmeMembershipDTO : personDTO.getProgrammeMemberships()) {
                                programmeMembershipDTO.setPerson(savedPersonDTO);
                                tcsServiceImpl.createProgrammeMembership(programmeMembershipDTO);
                            }
                        }
                    }
                }
                applicationType.setFileStatus(FileStatus.COMPLETED);
            } catch (InvalidFormatException e) {
                logger.error("Error while reading excel file : " + e.getMessage());
                applicationType.setFileStatus(FileStatus.PENDING);
            } catch (Exception e) {
                logger.error("Error while reading excel file and mapping headers : " + e.getMessage());
                e.printStackTrace();
                applicationType.setFileStatus(FileStatus.PENDING);
            } finally {
                applicationTypeRepository.save(applicationType);
            }
        }
    }

    public PersonDTO getPersonDTO(PersonXLS personXLS, CurriculumDTO curriculumDTO1, CurriculumDTO curriculumDTO2, CurriculumDTO curriculumDTO3, ProgrammeDTO programmeDTO) {
        PersonDTO personDTO = new PersonDTO();
        LocalDateTime addedDate = LocalDateTime.now();
        personDTO.setAddedDate(addedDate == null ? LocalDateTime.now() : addedDate);
        personDTO.setInactiveDate(convertDateTime(personXLS.getInactiveDate()));
        personDTO.setPublicHealthNumber(personXLS.getPublicHealthNumber());
        personDTO.setStatus(Status.fromString(personXLS.getRecordStatus()));
        personDTO.setRole(personXLS.getRole());
        //TODO NI Number - waiting for CIO update

        QualificationDTO qualificationDTO = new QualificationDTO();
        qualificationDTO.setCountryOfQualification(personXLS.getCountryOfQualification());
        qualificationDTO.setQualification(personXLS.getQualification());
        qualificationDTO.setMedicalSchool(personXLS.getMedicalSchool());
        qualificationDTO.setQualificationAttainedDate(convertDate(personXLS.getDateAttained()));
        personDTO.setQualifications(new HashSet<QualificationDTO>(){{
            add(qualificationDTO);
        }});

        ContactDetailsDTO contactDetailsDTO = getContactDetailsDTO(personXLS);
        personDTO.setContactDetails(contactDetailsDTO);

        PersonalDetailsDTO personalDetailsDTO = getPersonalDetailsDTO(personXLS);
        personDTO.setPersonalDetails(personalDetailsDTO);

        GmcDetailsDTO gmcDetailsDTO = new GmcDetailsDTO();
        gmcDetailsDTO.setGmcNumber(personXLS.getGmcNumber());
        personDTO.setGmcDetails(gmcDetailsDTO);

        GdcDetailsDTO gdcDetailsDTO = new GdcDetailsDTO();
        gdcDetailsDTO.setGdcNumber(personXLS.getGdcNumber());
        personDTO.setGdcDetails(gdcDetailsDTO);

        RightToWorkDTO rightToWorkDTO = getRightToWorkDTO(personXLS);
        personDTO.setRightToWork(rightToWorkDTO);

        LocalDate programmeEndDate = convertDate(personXLS.getProgrammeEndDate());
        ProgrammeMembershipType programmeMembershipType = ProgrammeMembershipType.fromString(personXLS.getProgrammeMembership());

        HashSet<ProgrammeMembershipDTO> programmeMembershipDTOS = new HashSet<>();
        LocalDate curriculum1StartDateAsProgrammeStartDate = null;
        try {
            curriculum1StartDateAsProgrammeStartDate = personXLS.getCurriculum1StartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }catch (Exception e) {
            e.printStackTrace();
        }
        if(curriculumDTO1 != null) {
            programmeMembershipDTOS.add(getProgrammeMembershipDTO(curriculum1StartDateAsProgrammeStartDate, programmeEndDate, programmeDTO, curriculumDTO1, programmeMembershipType, personXLS.getCurriculum1StartDate(), personXLS.getCurriculum1EndDate()));
        }
        if(curriculumDTO2 != null) {
            programmeMembershipDTOS.add(getProgrammeMembershipDTO(curriculum1StartDateAsProgrammeStartDate, programmeEndDate, programmeDTO, curriculumDTO2, programmeMembershipType, personXLS.getCurriculum2StartDate(), personXLS.getCurriculum2EndDate()));
        }
        if(curriculumDTO3 != null) {
            programmeMembershipDTOS.add(getProgrammeMembershipDTO(curriculum1StartDateAsProgrammeStartDate, programmeEndDate, programmeDTO, curriculumDTO3, programmeMembershipType, personXLS.getCurriculum3StartDate(), personXLS.getCurriculum3EndDate()));
        }
        personDTO.setProgrammeMemberships(programmeMembershipDTOS);

        return personDTO;
    }

    private RightToWorkDTO getRightToWorkDTO(PersonXLS personXLS) {
        RightToWorkDTO rightToWorkDTO = new RightToWorkDTO();
        rightToWorkDTO.setPermitToWork(PermitToWorkType.fromString(personXLS.getPermitToWork()));
        rightToWorkDTO.setSettled(personXLS.getSettled());
        rightToWorkDTO.setVisaDetails(personXLS.getVisaDetails());
        rightToWorkDTO.setVisaValidTo(convertDate(personXLS.getVisaValidTo()));
        rightToWorkDTO.setVisaIssued(convertDate(personXLS.getVisaIssued()));
        rightToWorkDTO.setEeaResident(personXLS.getEeaResident());
        return rightToWorkDTO;
    }

    private PersonalDetailsDTO getPersonalDetailsDTO(PersonXLS personXLS) {
        PersonalDetailsDTO personalDetailsDTO = new PersonalDetailsDTO();
        personalDetailsDTO.setMaritalStatus(personXLS.getMaritalStatus());
        personalDetailsDTO.setDateOfBirth(convertDate(personXLS.getDateOfBirth()));
        personalDetailsDTO.setDisability(personXLS.getDisability());
        personalDetailsDTO.setDisabilityDetails(personXLS.getDisabilityDetails());
        personalDetailsDTO.setNationality(personXLS.getNationality());
        personalDetailsDTO.setGender(personXLS.getGender());
        personalDetailsDTO.setEthnicOrigin(personXLS.getEthnicOrigin());
        personalDetailsDTO.setSexualOrientation(personXLS.getSexualOrientation());
        personalDetailsDTO.setReligiousBelief(personXLS.getReligiousBelief());
        return personalDetailsDTO;
    }

    private ContactDetailsDTO getContactDetailsDTO(PersonXLS personXLS) {
        ContactDetailsDTO contactDetailsDTO = new ContactDetailsDTO();
        contactDetailsDTO.setSurname(personXLS.getSurname());
        contactDetailsDTO.setForenames(personXLS.getForenames());
        contactDetailsDTO.setKnownAs(personXLS.getKnownAs());
        contactDetailsDTO.setTitle(personXLS.getTitle());
        contactDetailsDTO.setTelephoneNumber(personXLS.getTelephone());
        contactDetailsDTO.setMobileNumber(personXLS.getMobile());
        contactDetailsDTO.setEmail(personXLS.getEmailAddress());
        contactDetailsDTO.setAddress1(personXLS.getAddress1());
        contactDetailsDTO.setAddress2(personXLS.getAddress2());
        contactDetailsDTO.setAddress3(personXLS.getAddress3());
        contactDetailsDTO.setPostCode(personXLS.getPostCode());
        return contactDetailsDTO;
    }

    private ProgrammeMembershipDTO getProgrammeMembershipDTO(LocalDate programmeStartDate,
                                                             LocalDate programmeEndDate,
                                                             ProgrammeDTO programmeDTO,
                                                             CurriculumDTO curriculumDTO,
                                                             ProgrammeMembershipType programmeMembershipType,
                                                             Date curriculumStartDate,
                                                             Date curriculumEndDate
                                                             ) {
        ProgrammeMembershipDTO programmeMembershipDTO = new ProgrammeMembershipDTO();
        programmeMembershipDTO.setProgrammeMembershipType(programmeMembershipType);
        programmeMembershipDTO.setProgrammeStartDate(programmeStartDate);
        programmeMembershipDTO.setProgrammeEndDate(programmeEndDate);

        programmeMembershipDTO.setCurriculumId(curriculumDTO.getId());
        programmeMembershipDTO.setProgrammeId(programmeDTO.getId());

        programmeMembershipDTO.setCurriculumStartDate(convertDate(curriculumStartDate));
        programmeMembershipDTO.setCurriculumEndDate(convertDate(curriculumEndDate));
        return programmeMembershipDTO;
    }
}

class ProgrammeCompositekey {
    String programmeName;
    String programmeNumber;

    public ProgrammeCompositekey(String programmeName, String programmeNumber) {
        this.programmeName = programmeName;
        this.programmeNumber = programmeNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgrammeCompositekey that = (ProgrammeCompositekey) o;
        return Objects.equals(programmeName, that.programmeName) &&
                Objects.equals(programmeNumber, that.programmeNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(programmeName, programmeNumber);
    }
}