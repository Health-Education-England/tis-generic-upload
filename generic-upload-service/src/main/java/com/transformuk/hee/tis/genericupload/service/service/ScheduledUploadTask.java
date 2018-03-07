package com.transformuk.hee.tis.genericupload.service.service;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.transformuk.hee.tis.filestorage.repository.FileStorageRepository;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.config.ApplicationConfiguration;
import com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper;
import com.transformuk.hee.tis.genericupload.service.parser.PersonHeaderMapper;
import com.transformuk.hee.tis.genericupload.service.repository.ApplicationTypeRepository;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GDCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GMCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PersonBasicDetailsDTOFetcher;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDateTime;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ScheduledUploadTask {
	private static final Logger logger = getLogger(ScheduledUploadTask.class);
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	@Autowired
	private TcsServiceImpl tcsServiceImpl;
	@Autowired
	private final FileStorageRepository fileStorageRepository;
	@Autowired
	private FileProcessService fileProcessService;

	GMCDTOFetcher gmcDtoFetcher;
	GDCDTOFetcher gdcDtoFetcher;
	PersonBasicDetailsDTOFetcher pbdDtoFetcher;

	private final ApplicationTypeRepository applicationTypeRepository;
	private ApplicationConfiguration applicationConfiguration;

	//TODO externalise
	private final int QUERYSTRING_LENGTH_LIMITING_BATCH_SIZE = 50;

	@Autowired
	public ScheduledUploadTask(FileStorageRepository fileStorageRepository,
	                           ApplicationTypeRepository applicationTypeRepository) {
		this.fileStorageRepository = fileStorageRepository;
		this.applicationTypeRepository = applicationTypeRepository;
	}

	@PostConstruct
	public void initialiseFetchers() {
		this.gmcDtoFetcher = new GMCDTOFetcher(tcsServiceImpl);
		this.gdcDtoFetcher = new GDCDTOFetcher(tcsServiceImpl);
		this.pbdDtoFetcher = new PersonBasicDetailsDTOFetcher(tcsServiceImpl);
	}


	//waits fixedDelay milliseconds after the last run task
	@Scheduled(fixedDelay = 5000, initialDelay = 2000) //TODO externalise this wait interval,
	public void scheduleTaskWithFixedDelay() {
		logger.info("Fixed Delay Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
		for (ApplicationType applicationType : applicationTypeRepository.findByFileStatusOrderByStartDate(FileStatus.PENDING)) {
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

				//deal with unknowns - add all unknown as a new record - ignore duplicates
				Set<PersonXLS> unknownRegNumbers = personXLSS.stream()
						.filter(personXLS ->
								"unknown".equalsIgnoreCase(personXLS.getGmcNumber()) ||
								"unknown".equalsIgnoreCase(personXLS.getGdcNumber()) ||
								"unknown".equalsIgnoreCase(personXLS.getPublicHealthNumber()))
						.collect(Collectors.toSet());
				logger.info("Found {} unknown reg numbers in xml file uploaded. Adding to TIS", unknownRegNumbers.size());
				addPersons(unknownRegNumbers);

				addOrUpdateGMCRecords(personXLSS); //TODO repeat for GDC and Public Health Number

				applicationType.setFileStatus(FileStatus.COMPLETED);
			} catch (InvalidFormatException e) {
				logger.error("Error while reading excel file : " + e.getMessage());
				applicationType.setFileStatus(FileStatus.INVALID_FILE_FORMAT);
			} catch (HttpServerErrorException | HttpClientErrorException e) { //thrown when connecting to TCS
				logger.error("Error while processing excel file : " + e.getMessage());
				applicationType.setFileStatus(FileStatus.PENDING);
			} catch (Exception e) {
				logger.error("Error while reading excel file and mapping headers : " + e.getMessage());
				e.printStackTrace();
				applicationType.setFileStatus(FileStatus.INVALID_HEADERS);
			} finally {
				applicationTypeRepository.save(applicationType);
			}
		}
	}

	public void addOrUpdateGMCRecords(List<PersonXLS> personXLSS) {
		//check whether a GMC record exists in TIS
		Function<PersonXLS, String> getGmcNumber = PersonXLS::getGmcNumber;
		Set<PersonXLS> rowsWithGMCNumbers = getRowsWithRegistrationNumber(personXLSS, getGmcNumber);

		//check whether a GMC record exists in TCS
		Set<String> gmcNumbers = collectRegNumbers(rowsWithGMCNumbers, getGmcNumber);
		Map<String, GmcDetailsDTO> gmcDetailsMap = gmcDtoFetcher.findWithIds(gmcNumbers);

		if (!gmcDetailsMap.isEmpty()) {
			Set<String> personIdsFromGMCDetailsTable = gmcDtoFetcher.extractIds(gmcDetailsMap, GmcDetailsDTO::getId);
			Map<String, PersonBasicDetailsDTO> pbdMapByGMC = pbdDtoFetcher.findWithIds(personIdsFromGMCDetailsTable);

			Set<PersonXLS> knownGMCsInTIS = rowsWithGMCNumbers.stream()
					.filter(personXLS -> {
						String gmcNumber = getGmcNumber.apply(personXLS);
						return gmcDetailsMap.containsKey(gmcNumber) && pbdMapByGMC.get(gmcNumber).getLastName().equalsIgnoreCase(personXLS.getSurname());
					})
					.collect(Collectors.toSet());
			Set<PersonXLS> unknownGMCsInTIS = rowsWithGMCNumbers.stream()
					.filter(personXLS -> {
						String gmcNumber = getGmcNumber.apply(personXLS);
						return gmcDetailsMap.containsKey(gmcNumber) && !pbdMapByGMC.get(gmcNumber).getLastName().equalsIgnoreCase(personXLS.getSurname());
					})
					.collect(Collectors.toSet());
			if (unknownGMCsInTIS.size() > 0) {
				logger.error("GMC's found without matching surnames (no. of records) :  {}", unknownGMCsInTIS.size());
				//TODO add to error sheet
			}

			//deep compare and update if necessary
			Function<PersonDTO, String> personDTOToGmcID = personDTO -> String.valueOf(personDTO.getGmcDetails().getGmcNumber());
			Map<String, PersonDTO> gmcNumberToPersonDTOMap = knownGMCsInTIS.stream()
					.map(this::getPersonDTO)
					.collect(Collectors.toMap(personDTOToGmcID, Function.identity()));
			Map<String, PersonDTO> personDTOMapFromTCS = knownGMCsInTIS.stream()
					.map(personXLS -> tcsServiceImpl.getPerson(String.valueOf(gmcDetailsMap.get(personXLS.getGmcNumber()).getId()))) //TODO optimise to a fetcher
					.collect(Collectors.toMap(personDTOToGmcID, Function.identity()));

			//now that we have both lets set the ids from the DB DTO to the excel DTO
			for(String key : gmcNumberToPersonDTOMap.keySet()) {
				copyIdsFromDB(personDTOMapFromTCS.get(key), gmcNumberToPersonDTOMap.get(key));

				//TODO save the whole object - this does not exist in TCS yet! should be similar to the 'create' person
			}
		} else {
			addPersons(rowsWithGMCNumbers);
		}
	}

	private Set<String> collectRegNumbers(Set<PersonXLS> personXLSS, Function<PersonXLS, String> function) {
		return personXLSS.stream()
				.map(function::apply)
				.collect(Collectors.toSet());
	}

	private Set<PersonXLS> getRowsWithRegistrationNumber(List<PersonXLS> personXLSS, Function<PersonXLS, String> function) {
		return personXLSS.stream()
				.filter(personXLS -> {
					String regNumber = function.apply(personXLS);
					return !"unknown".equalsIgnoreCase(regNumber) && !StringUtils.isEmpty(regNumber);
				})
				.collect(Collectors.toSet());
	}

	private void copyIdsFromDB(PersonDTO personDTOFromDB, PersonDTO personDTOFromXLS) {
		//set ids first
		personDTOFromXLS.setId(personDTOFromDB.getId());
		if (personDTOFromXLS.getQualifications().size() > 0) { //TODO qualifications do not persist on person
			personDTOFromXLS.getQualifications().iterator().next().setId(personDTOFromDB.getQualifications().iterator().next().getId());
		}
		personDTOFromXLS.getContactDetails().setId(personDTOFromDB.getContactDetails().getId());
		personDTOFromXLS.getPersonalDetails().setId(personDTOFromDB.getPersonalDetails().getId());
		personDTOFromXLS.getGmcDetails().setId(personDTOFromDB.getGmcDetails().getId());
		personDTOFromXLS.getGdcDetails().setId(personDTOFromDB.getGdcDetails().getId());
		personDTOFromXLS.getRightToWork().setId(personDTOFromDB.getRightToWork().getId());
	}

	private void addPersons(Set<PersonXLS> personsInXLS) {
		if (!CollectionUtils.isEmpty(personsInXLS)) {
			for (PersonXLS personXLS : personsInXLS) {
				PersonDTO personDTO = getPersonDTO(personXLS);

				if (personDTO != null) { //currently can only be null if programme isn't found
					PersonDTO savedPersonDTO = tcsServiceImpl.createPerson(personDTO);
					//qualifications do not persist on person save; but are retrievable from a personDTO! Saving
					QualificationDTO qualificationDTO = getQualificationDTO(personXLS);
					qualificationDTO.setPerson(personDTO);
					tcsServiceImpl.createQualification(qualificationDTO);


					for (ProgrammeMembershipDTO programmeMembershipDTO : personDTO.getProgrammeMemberships()) {
						programmeMembershipDTO.setPerson(savedPersonDTO);
						// this is being done here as
						tcsServiceImpl.createProgrammeMembership(programmeMembershipDTO);
					}
				}
			}
		}
	}

	private PersonDTO getPersonDTO(PersonXLS personXLS) {
		Set<CurriculumDTO> curricula = new HashSet<>();

		CurriculumDTO curriculumDTO1 = personXLS.getCurriculum1() == null ? null : tcsServiceImpl.getCurriculaByName(personXLS.getCurriculum1()).get(0);
		CurriculumDTO curriculumDTO2 = personXLS.getCurriculum2() == null ? null : tcsServiceImpl.getCurriculaByName(personXLS.getCurriculum2()).get(0);
		CurriculumDTO curriculumDTO3 = personXLS.getCurriculum3() == null ? null : tcsServiceImpl.getCurriculaByName(personXLS.getCurriculum3()).get(0);

		ProgrammeDTO programmeDTO = null;
		if (personXLS.getProgrammeName() != null && personXLS.getProgrammeNumber() != null) {
			programmeDTO = tcsServiceImpl.getProgrammeByNameAndNumber(personXLS.getProgrammeName(), personXLS.getProgrammeNumber()).get(0);
			programmeDTO.setCurricula(curricula);       //links the programme to the curricula
		} else {
			//TODO consider throwing an exception instead
		}

		return getPersonDTO(personXLS, curriculumDTO1, curriculumDTO2, curriculumDTO3, programmeDTO);
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

		personDTO.setContactDetails(getContactDetailsDTO(personXLS));
		personDTO.setPersonalDetails(getPersonalDetailsDTO(personXLS));

		GmcDetailsDTO gmcDetailsDTO = new GmcDetailsDTO();
		gmcDetailsDTO.setGmcNumber(personXLS.getGmcNumber());
		personDTO.setGmcDetails(gmcDetailsDTO);

		GdcDetailsDTO gdcDetailsDTO = new GdcDetailsDTO();
		gdcDetailsDTO.setGdcNumber(personXLS.getGdcNumber());
		personDTO.setGdcDetails(gdcDetailsDTO);

		personDTO.setRightToWork(getRightToWorkDTO(personXLS));

		LocalDate programmeEndDate = convertDate(personXLS.getProgrammeEndDate());
		ProgrammeMembershipType programmeMembershipType = ProgrammeMembershipType.fromString(personXLS.getProgrammeMembership());

		HashSet<ProgrammeMembershipDTO> programmeMembershipDTOS = new HashSet<>();
		LocalDate curriculum1StartDateAsProgrammeStartDate = null;
		try {
			curriculum1StartDateAsProgrammeStartDate = personXLS.getCurriculum1StartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (curriculumDTO1 != null) {
			programmeMembershipDTOS.add(getProgrammeMembershipDTO(curriculum1StartDateAsProgrammeStartDate, programmeEndDate, programmeDTO, curriculumDTO1, programmeMembershipType, personXLS.getCurriculum1StartDate(), personXLS.getCurriculum1EndDate()));
		}
		if (curriculumDTO2 != null) {
			programmeMembershipDTOS.add(getProgrammeMembershipDTO(curriculum1StartDateAsProgrammeStartDate, programmeEndDate, programmeDTO, curriculumDTO2, programmeMembershipType, personXLS.getCurriculum2StartDate(), personXLS.getCurriculum2EndDate()));
		}
		if (curriculumDTO3 != null) {
			programmeMembershipDTOS.add(getProgrammeMembershipDTO(curriculum1StartDateAsProgrammeStartDate, programmeEndDate, programmeDTO, curriculumDTO3, programmeMembershipType, personXLS.getCurriculum3StartDate(), personXLS.getCurriculum3EndDate()));
		}
		personDTO.setProgrammeMemberships(programmeMembershipDTOS);

		return personDTO;
	}

	public QualificationDTO getQualificationDTO(PersonXLS personXLS) {
		QualificationDTO qualificationDTO = new QualificationDTO();
		qualificationDTO.setCountryOfQualification(personXLS.getCountryOfQualification());
		qualificationDTO.setQualification(personXLS.getQualification());
		qualificationDTO.setMedicalSchool(personXLS.getMedicalSchool());
		qualificationDTO.setQualificationAttainedDate(convertDate(personXLS.getDateAttained()));
		return qualificationDTO;
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
