package com.transformuk.hee.tis.genericupload.service.service;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GDCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GMCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PeopleByPHNFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PeopleFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PersonBasicDetailsDTOFetcher;
import com.transformuk.hee.tis.tcs.api.dto.ContactDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonBasicDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonalDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.QualificationDTO;
import com.transformuk.hee.tis.tcs.api.dto.RightToWorkDTO;
import com.transformuk.hee.tis.tcs.api.dto.RotationDTO;
import com.transformuk.hee.tis.tcs.api.dto.RotationPersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.TrainingNumberDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PermitToWorkType;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDateTime;
import static com.transformuk.hee.tis.genericupload.service.util.ReflectionUtil.copyIfNotNullOrEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PersonTransformerService {
	private static final Logger logger = getLogger(PersonTransformerService.class);

	private static final String REG_NUMBER_IDENTIFIED_AS_DUPLICATE_IN_UPLOADED_FILE = "Registration number (%s) identified as duplicate in uploaded file";
	private static final String REG_NUMBER_DOES_NOT_MATCH_SURNAME_IN_TIS = "Person record for %s Number does not match surname in TIS";
	private static final String REG_NUMBER_EXISTS_ON_MULTIPLE_RECORDS_IN_TIS = "Registration number (%s) exists on multiple records in TIS";
	private static final String PROGRAMME_NOT_FOUND = "Programme not found for programme name (%1$s) and programme number (%2$s)";
	private static final String PROGRAMME_NAME_NOT_SPECIFIED = "Programme name (%s) has not been specified. Both programme name and number are needed to identify the programme";
	private static final String PROGRAMME_NUMBER_NOT_SPECIFIED = "Programme number (%s) has not been specified. Both programme name and number are needed to identify the programme";
	private static final String MULTIPLE_PROGRAMME_FOUND_FOR = "Multiple programmes found for programme name (%1$s) and programme number (%2$s)";
	private static final String CURRICULUM_NOT_FOUND = "Curriculum not found : ";
	private static final String MULTIPLE_CURRICULA_FOUND_FOR = "Multiple curricula found for : ";

	private static final String GDC = "GDC";
	private static final String GMC = "GMC";
	private static final String PHN = "PHN";
	private static final String UNKNOWN = "unknown";

	private static final String PROGRAMME_SHOULD_HAVE_AT_LEAST_ONE_CURRICULA = "Programme should have at least one curricula";
	private static final String AT_LEAST_ONE_OF_THE_THREE_REGISTRATION_NUMBERS_NEEDS_TO_BE_SPECIFIED = "At least one of the three registration numbers needs to be specified";
	private static final String CAN_ONLY_ADD_TO_A_ROTATION_LINKED_TO_THE_PROGRAMME_MEMBERSHIP_YOU_ARE_ADDING = "Can only add to a Rotation linked to the programme membership you are adding";
	private static final String MULTIPLE_ROTATIONS_EXIST_FOR_PERSON_CANNOT_IDENTIFY_A_ROTATION_TO_UPDATE = "Multiple rotations exist for person. Cannot identify a rotation to update";
	private static final String A_VALID_PROGRAMME_MEMBERSHIP_IS_NEEDED_TO_ADD_A_ROTATION = "A valid programme membership is needed to add a rotation";

	@Autowired
	private TcsServiceImpl tcsServiceImpl;

	private GMCDTOFetcher gmcDtoFetcher;
	private GDCDTOFetcher gdcDtoFetcher;
	private PersonBasicDetailsDTOFetcher pbdDtoFetcher;
	private PeopleFetcher peopleFetcher;
	private PeopleByPHNFetcher peopleByPHNFetcher;

	@PostConstruct
	public void initialiseFetchers() {
		this.gmcDtoFetcher = new GMCDTOFetcher(tcsServiceImpl);
		this.gdcDtoFetcher = new GDCDTOFetcher(tcsServiceImpl);
		this.pbdDtoFetcher = new PersonBasicDetailsDTOFetcher(tcsServiceImpl);
		this.peopleFetcher = new PeopleFetcher(tcsServiceImpl);
		this.peopleByPHNFetcher = new PeopleByPHNFetcher(tcsServiceImpl);
	}

	public void processPeopleUpload(List<PersonXLS> personXLSS) {
		personXLSS.forEach(PersonXLS::initialiseSuccessfullyImported);
		markRowsWithoutRegistrationNumbers(personXLSS);

		addPersons(getPersonsWithUnknownRegNumbers(personXLSS));
		addOrUpdateGMCRecords(personXLSS);
		addOrUpdateGDCRecords(personXLSS);
		addOrUpdatePHRecords(personXLSS);
	}

	private Set<PersonXLS> getPersonsWithUnknownRegNumbers(List<PersonXLS> personXLSS) {
		//deal with unknowns - add all unknown as a new record - ignore duplicates
		//TODO determine what to do if
		Set<PersonXLS> unknownRegNumbers = personXLSS.stream()
				.filter(personXLS ->
						UNKNOWN.equalsIgnoreCase(personXLS.getGmcNumber()) ||
						UNKNOWN.equalsIgnoreCase(personXLS.getGdcNumber()) ||
						UNKNOWN.equalsIgnoreCase(personXLS.getPublicHealthNumber()))
				.collect(Collectors.toSet());
		return unknownRegNumbers;
	}

	private void  markRowsWithoutRegistrationNumbers(List<PersonXLS> personXLSS) {
		personXLSS.stream()
					.filter(personXLS ->
							personXLS.getGmcNumber() == null &&
							personXLS.getGdcNumber() == null &&
							personXLS.getPublicHealthNumber() == null )
					.forEach(personXLS -> personXLS.addErrorMessage(AT_LEAST_ONE_OF_THE_THREE_REGISTRATION_NUMBERS_NEEDS_TO_BE_SPECIFIED));
	}

	<DTO> Set<Long> getIdsFromRegNumberDTOsMap(Set<PersonXLS> knownRegNumbersInTIS, Map<String, DTO> regNumberMap, Function<PersonXLS, String> getRegNumberFromXLS, Function<DTO, Long> getId) {
		return knownRegNumbersInTIS.stream()
				.map(personXLS -> getId.apply(regNumberMap.get(getRegNumberFromXLS.apply(personXLS))))
				.collect(Collectors.toSet());
	}

	<DTO> Set<PersonXLS> getKnownRegNumbersInTIS(List<PersonXLS> rowsWithRegNumbers,
	                                             Function<PersonXLS, String> getRegNumberFunction,
	                                             Map<String, DTO> regNumberMap,
	                                             Map<Long, PersonBasicDetailsDTO> pbdMapByRegNumber,
	                                             Function<DTO, Long> getIdFunction,
	                                             String registrationNumberString) {
		Set<PersonXLS> knownRegNumbersInTIS = new HashSet<>();
		for (PersonXLS personXLS : rowsWithRegNumbers) {
			String regNumber = getRegNumberFunction.apply(personXLS);
			if (regNumberMap.containsKey(regNumber)) {
				Long id = getIdFunction.apply(regNumberMap.get(regNumber));
				if (pbdMapByRegNumber.get(id).getLastName().equalsIgnoreCase(personXLS.getSurname())) {
					knownRegNumbersInTIS.add(personXLS);
				} else {
					personXLS.addErrorMessage(String.format(REG_NUMBER_DOES_NOT_MATCH_SURNAME_IN_TIS, registrationNumberString));
				}
			}
		}
		return knownRegNumbersInTIS;
	}

	<KEY_TYPE> void updateDatastoreWithRowsFromXLS(Map<String, PersonDTO> regNumberToPersonDTOFromXLSMap, Map<KEY_TYPE, PersonDTO> personDTOMapFromTCS, Map<String, PersonXLS> regNumberToPersonXLSMap) {
		for (String key : regNumberToPersonDTOFromXLSMap.keySet()) {
			PersonDTO personDTOFromDB = personDTOMapFromTCS.get(key);
			PersonDTO personDTOFromXLS = regNumberToPersonDTOFromXLSMap.get(key);
			if (personDTOFromXLS != null) {
				overwriteDBValuesFromNonEmptyExcelValues(personDTOFromDB, personDTOFromXLS);
				updateOrRecordError(personDTOFromDB, personDTOFromXLS, regNumberToPersonXLSMap.get(key));
			}
		}
	}

	private void addOrUpdatePHRecords(List<PersonXLS> personXLSS) {
		//check whether a PH record exists in TIS
		Function<PersonXLS, String> getPhNumber = PersonXLS::getPublicHealthNumber;
		List<PersonXLS> rowsWithPHNumbers = getRowsWithRegistrationNumberForPeople(personXLSS, getPhNumber);
		flagAndEliminateDuplicates(rowsWithPHNumbers, getPhNumber, PHN);

		Set<String> phNumbers = collectRegNumbers(rowsWithPHNumbers, getPhNumber);
		Map<String, PersonDTO> phnDetailsMap = peopleByPHNFetcher.findWithKeys(phNumbers);

		Function<PersonDTO, String> personDTOToPHNID = PersonDTO::getPublicHealthNumber;
		setErrorMessageForDuplicatesAndEliminateForFurtherProcessing(rowsWithPHNumbers, getPhNumber, peopleByPHNFetcher.getDuplicateKeys(), String.format(REG_NUMBER_EXISTS_ON_MULTIPLE_RECORDS_IN_TIS, PHN));

		if (!phnDetailsMap.isEmpty()) {
			Set<Long> personIds = peopleByPHNFetcher.extractIds(phnDetailsMap, PersonDTO::getId);
			Map<Long, PersonBasicDetailsDTO> pbdMapByPH = pbdDtoFetcher.findWithKeys(personIds);
			Set<PersonXLS> knownPHsInTIS = getKnownRegNumbersInTIS(rowsWithPHNumbers, getPhNumber, phnDetailsMap, pbdMapByPH, PersonDTO::getId, "Public Health");

			Map<String, PersonDTO> phNumberToPersonDTOFromXLSMap = getRegNumberToPersonDTOFromXLSMap(personDTOToPHNID, knownPHsInTIS);
			Map<String, PersonXLS> phnToPersonXLSMap = getRegNumberToPersonXLSMap(getPhNumber, knownPHsInTIS);

			updateDatastoreWithRowsFromXLS(phNumberToPersonDTOFromXLSMap, phnDetailsMap, phnToPersonXLSMap);
		}

		addPersons(getRegNumbersNotInTCS(rowsWithPHNumbers, phnDetailsMap.keySet()));
	}

	private void addOrUpdateGDCRecords(List<PersonXLS> personXLSS) {
		//check whether a GDC record exists in TIS
		Function<PersonXLS, String> getGdcNumber = PersonXLS::getGdcNumber;
		List<PersonXLS> rowsWithGDCNumbers = getRowsWithRegistrationNumberForPeople(personXLSS, getGdcNumber);
		flagAndEliminateDuplicates(rowsWithGDCNumbers, getGdcNumber, GDC);

		Set<String> gdcNumbers = collectRegNumbers(rowsWithGDCNumbers, getGdcNumber);
		Map<String, GdcDetailsDTO> gdcDetailsMap = gdcDtoFetcher.findWithKeys(gdcNumbers);
		setErrorMessageForDuplicatesAndEliminateForFurtherProcessing(rowsWithGDCNumbers, getGdcNumber, gdcDtoFetcher.getDuplicateKeys(), String.format(REG_NUMBER_EXISTS_ON_MULTIPLE_RECORDS_IN_TIS, GDC));

		if (!gdcDetailsMap.isEmpty()) {
			Set<Long> personIdsFromGDCDetailsTable = gdcDtoFetcher.extractIds(gdcDetailsMap, GdcDetailsDTO::getId);
			Map<Long, PersonBasicDetailsDTO> pbdMapByGDC = pbdDtoFetcher.findWithKeys(personIdsFromGDCDetailsTable);
			Set<PersonXLS> knownGDCsInTIS = getKnownRegNumbersInTIS(rowsWithGDCNumbers, getGdcNumber, gdcDetailsMap, pbdMapByGDC, GdcDetailsDTO::getId, GDC);

			//deep compare and update if necessary
			Function<PersonDTO, String> personDTOToGdcID = personDTO -> personDTO.getGdcDetails().getGdcNumber();
			Map<String, PersonDTO> gdcNumberToPersonDTOFromXLSMap = getRegNumberToPersonDTOFromXLSMap(personDTOToGdcID, knownGDCsInTIS);

			Set<Long> personIds = getIdsFromRegNumberDTOsMap(knownGDCsInTIS, gdcDetailsMap, getGdcNumber, GdcDetailsDTO::getId);
			Map<Long, PersonDTO> personDTOMapFromTCS = peopleFetcher.setIdMappingFunction(personDTOToGdcID).findWithKeys(personIds);
			Map<String, PersonXLS> gdcToPersonXLSMap = getRegNumberToPersonXLSMap(getGdcNumber, knownGDCsInTIS);

			updateDatastoreWithRowsFromXLS(gdcNumberToPersonDTOFromXLSMap, personDTOMapFromTCS, gdcToPersonXLSMap);
		}

		addPersons(getRegNumbersNotInTCS(rowsWithGDCNumbers, gdcDetailsMap.keySet()));
	}

	private void addOrUpdateGMCRecords(List<PersonXLS> personXLSS) {
		//check whether a GMC record exists in TIS
		Function<PersonXLS, String> getGmcNumber = PersonXLS::getGmcNumber;
		List<PersonXLS> rowsWithGMCNumbers = getRowsWithRegistrationNumberForPeople(personXLSS, getGmcNumber);
		flagAndEliminateDuplicates(rowsWithGMCNumbers, getGmcNumber, GMC);

		Set<String> gmcNumbers = collectRegNumbers(rowsWithGMCNumbers, getGmcNumber);
		Map<String, GmcDetailsDTO> gmcDetailsMap = gmcDtoFetcher.findWithKeys(gmcNumbers);
		setErrorMessageForDuplicatesAndEliminateForFurtherProcessing(rowsWithGMCNumbers, getGmcNumber, gmcDtoFetcher.getDuplicateKeys(), String.format(REG_NUMBER_EXISTS_ON_MULTIPLE_RECORDS_IN_TIS, GMC));

		if (!gmcDetailsMap.isEmpty()) {
			Set<Long> personIdsFromGMCDetailsTable = gmcDtoFetcher.extractIds(gmcDetailsMap, GmcDetailsDTO::getId);
			Map<Long, PersonBasicDetailsDTO> pbdMapByGMC = pbdDtoFetcher.findWithKeys(personIdsFromGMCDetailsTable);
			Set<PersonXLS> knownGMCsInTIS = getKnownRegNumbersInTIS(rowsWithGMCNumbers, getGmcNumber, gmcDetailsMap, pbdMapByGMC, GmcDetailsDTO::getId, GMC);

			//deep compare and update if necessary
			Function<PersonDTO, String> personDTOToGmcID = personDTO -> personDTO.getGmcDetails().getGmcNumber();
			Map<String, PersonDTO> gmcNumberToPersonDTOFromXLSMap = getRegNumberToPersonDTOFromXLSMap(personDTOToGmcID, knownGMCsInTIS);

			Set<Long> personIds = getIdsFromRegNumberDTOsMap(knownGMCsInTIS, gmcDetailsMap, getGmcNumber, GmcDetailsDTO::getId);
			Map<Long, PersonDTO> personDTOMapFromTCS = peopleFetcher.setIdMappingFunction(personDTOToGmcID).findWithKeys(personIds);
			Map<String, PersonXLS> gmcToPersonXLSMap = getRegNumberToPersonXLSMap(getGmcNumber, knownGMCsInTIS);

			//now that we have both lets copy updated data
			updateDatastoreWithRowsFromXLS(gmcNumberToPersonDTOFromXLSMap, personDTOMapFromTCS, gmcToPersonXLSMap);
		}

		addPersons(getRegNumbersNotInTCS(rowsWithGMCNumbers, gmcDetailsMap.keySet()));
	}

	private void updateOrRecordError(PersonDTO personDTOFromDB, PersonDTO personDTOFromXLS, PersonXLS personXLS) {
		try {
			personDTOFromDB = tcsServiceImpl.updatePersonForBulkWithAssociatedDTOs(personDTOFromDB);
			addQualificationsRotationsAndProgrammeMemberships(personXLS, personDTOFromXLS, personDTOFromDB);
			if (StringUtils.isEmpty(personXLS.getErrorMessage())) {
				personXLS.setSuccessfullyImported(true);
			}
		} catch (HttpClientErrorException e) {
			personXLS.addErrorMessage(new ErrorHandler().getSingleMessageFromSpringJsonErrorMessages(e.getResponseBodyAsString()));
		} catch (ResourceAccessException rae) {
			new ErrorHandler().recordErrorMessageOnTemplateOrLogUnknown(personXLS, rae);
		}
	}

	private Map<String, PersonXLS> getRegNumberToPersonXLSMap(Function<PersonXLS, String> getRegNumber, Set<PersonXLS> knownRegNumbersInTIS) {
		return knownRegNumbersInTIS.stream().collect(Collectors.toMap(getRegNumber, Function.identity()));
	}

	private Map<String, PersonDTO> getRegNumberToPersonDTOFromXLSMap(Function<PersonDTO, String> personDTOToRegNumber, Set<PersonXLS> knownRegNumbersInTIS) {
		Map<String, PersonDTO> regNumberToPersonDTOFromXLSMap = new HashMap<>();
		for (PersonXLS knownRegNumberInTIS : knownRegNumbersInTIS) {
			PersonDTO personDTO = getPersonDTO(knownRegNumberInTIS);
			if (personDTO != null) {
				regNumberToPersonDTOFromXLSMap.put(personDTOToRegNumber.apply(personDTO), personDTO);
			}
		}
		return regNumberToPersonDTOFromXLSMap;
	}


	private void flagAndEliminateDuplicates(List<PersonXLS> personXLSList, Function<PersonXLS, String> extractRegistrationNumber, String regNumberString) {
		Set<String> regNumbersSet = new HashSet<>();
		Set<String> regNumbersDuplicatesSet = new HashSet<>();

		for (PersonXLS personXLS : personXLSList) {
			if (!regNumbersSet.add(extractRegistrationNumber.apply(personXLS))) {
				regNumbersDuplicatesSet.add(extractRegistrationNumber.apply(personXLS));
			}
		}

		setErrorMessageForDuplicatesAndEliminateForFurtherProcessing(personXLSList, extractRegistrationNumber, regNumbersDuplicatesSet, String.format(REG_NUMBER_IDENTIFIED_AS_DUPLICATE_IN_UPLOADED_FILE, regNumberString));
	}


	private void setErrorMessageForDuplicatesAndEliminateForFurtherProcessing(List<PersonXLS> personXLSList, Function<PersonXLS, String> extractRegistrationNumber, Set<String> regNumbersDuplicatesSet, String errorMessage) {
		for (Iterator<PersonXLS> iterator = personXLSList.iterator(); iterator.hasNext(); ) {
			PersonXLS personXLS = iterator.next();
			if (regNumbersDuplicatesSet.contains(extractRegistrationNumber.apply(personXLS))) {
				personXLS.addErrorMessage(errorMessage);
				iterator.remove();
			}
		}
	}

	private Set<PersonXLS> getRegNumbersNotInTCS(List<PersonXLS> rowsWithGMCNumbers, Set<String> regNumbersInTCS) {
		return rowsWithGMCNumbers.stream()
				.filter(personXLS -> !regNumbersInTCS.contains(personXLS.getGmcNumber()))
				.collect(Collectors.toSet());
	}

	private Set<String> collectRegNumbers(List<PersonXLS> personXLSS, Function<PersonXLS, String> extractRegistrationNumber) {
		return personXLSS.stream()
				.map(extractRegistrationNumber::apply)
				.collect(Collectors.toSet());
	}

	private List<PersonXLS> getRowsWithRegistrationNumberForPeople(List<PersonXLS> personXLSS, Function<PersonXLS, String> extractRegistrationNumber) {
		return personXLSS.stream()
				.filter(personXLS -> {
					String regNumber = extractRegistrationNumber.apply(personXLS);
					return !"unknown".equalsIgnoreCase(regNumber) && !StringUtils.isEmpty(regNumber);
				})
				.collect(Collectors.toList());
	}

	private void overwriteDBValuesFromNonEmptyExcelValues(PersonDTO personDTOFromDB, PersonDTO personDTOFromXLS) {
		personDTOFromDB.setRole(mergeRoles(personDTOFromXLS, personDTOFromDB));
		copyIfNotNullOrEmpty(personDTOFromXLS, personDTOFromDB,
				"addedDate", "inactiveDate", "publicHealthNumber", "status");
		copyIfNotNullOrEmpty(personDTOFromXLS.getContactDetails(), personDTOFromDB.getContactDetails(),
				"surname", "forenames", "knownAs", "title", "telephoneNumber", "mobileNumber", "email", "address1", "address2", "address3", "postCode");
		copyIfNotNullOrEmpty(personDTOFromXLS.getPersonalDetails(), personDTOFromDB.getPersonalDetails(),
				"maritalStatus", "dateOfBirth", "disability", "disabilityDetails", "nationality", "gender", "ethnicOrigin", "sexualOrientation", "religiousBelief");
		copyIfNotNullOrEmpty(personDTOFromXLS.getGmcDetails(), personDTOFromDB.getGmcDetails(), "gmcNumber");
		copyIfNotNullOrEmpty(personDTOFromXLS.getGdcDetails(), personDTOFromDB.getGdcDetails(), "gdcNumber");
		copyIfNotNullOrEmpty(personDTOFromXLS.getRightToWork(), personDTOFromDB.getRightToWork(),
				"permitToWork", "settled", "visaDetails", "visaValidTo", "visaIssued", "eeaResident");
	}

	private String mergeRoles(PersonDTO personDTOFromXLS, PersonDTO personDTOFromDB) {
		Set<String> personDTOFromDBRoles = getRolesSet(personDTOFromDB.getRole());
		Set<String> personDTOFromXLSRoles = getRolesSet(personDTOFromXLS.getRole());
		personDTOFromXLSRoles.addAll(personDTOFromDBRoles);
		return org.apache.commons.lang3.StringUtils.join(personDTOFromXLSRoles, ',');
	}

	private Set<String> getRolesSet(String csvRoles) {
		return new HashSet<>(Arrays.asList(StringUtils.isEmpty(csvRoles) ? new String[0] : csvRoles.split(",")));
	}

	private void addPersons(Set<PersonXLS> personsInXLS) {
		if (!CollectionUtils.isEmpty(personsInXLS)) {
			for (PersonXLS personXLS : personsInXLS) {
				if (StringUtils.isEmpty(personXLS.getErrorMessage())) {
					PersonDTO personDTO = getPersonDTO(personXLS);
					if (personDTO != null) {
						try {
							PersonDTO savedPersonDTO = tcsServiceImpl.createPerson(personDTO);
							addQualificationsRotationsAndProgrammeMemberships(personXLS, personDTO, savedPersonDTO);
						} catch (ResourceAccessException rae) {
							new ErrorHandler().recordErrorMessageOnTemplateOrLogUnknown(personXLS, rae);
						}
					}
				}

				if (StringUtils.isEmpty(personXLS.getErrorMessage())) {
					personXLS.setSuccessfullyImported(true);
				}
			}
		}
	}

	private void addQualificationsRotationsAndProgrammeMemberships(PersonXLS personXLS, PersonDTO personDTO, PersonDTO savedPersonDTO) {
		QualificationDTO qualificationDTO = getQualificationDTO(personXLS);
		if(qualificationDTO != null) {
			qualificationDTO.setPerson(savedPersonDTO);
			if (!savedPersonDTO.getQualifications().contains(qualificationDTO)) {
				tcsServiceImpl.createQualification(qualificationDTO);
			}
		}

		if(!personDTO.getProgrammeMemberships().isEmpty()) {
			Optional<String> rotationNameOptional = creationOrUpdateRotation(personXLS, personDTO, savedPersonDTO);

			for (ProgrammeMembershipDTO programmeMembershipDTO : personDTO.getProgrammeMemberships()) {
				programmeMembershipDTO.setPerson(savedPersonDTO);
				rotationNameOptional.ifPresent(programmeMembershipDTO::setRotation);
				if (savedPersonDTO.getProgrammeMemberships().contains(programmeMembershipDTO)) {
					if(!StringUtils.isEmpty(programmeMembershipDTO.getRotation())) {
						savedPersonDTO.getProgrammeMemberships().stream()
								.filter(programmeMembershipDTO1 -> programmeMembershipDTO1.equals(programmeMembershipDTO))
								.findFirst()
								.ifPresent(savedProgrammeMembershipDTO -> {
									if (Objects.equals(programmeMembershipDTO.getRotation(), savedProgrammeMembershipDTO.getRotation())) {
										savedProgrammeMembershipDTO.setRotation(programmeMembershipDTO.getRotation());
										tcsServiceImpl.updateProgrammeMembership(savedProgrammeMembershipDTO);
									}
								});
					}
				} else {
					tcsServiceImpl.createProgrammeMembership(programmeMembershipDTO);
				}
			}
		}
	}

	private Optional<String> creationOrUpdateRotation(PersonXLS personXLS, PersonDTO personDTO, PersonDTO savedPersonDTO) {
		String rotationName = personXLS.getRotation1();
		if (!StringUtils.isEmpty(rotationName)) {
			if(personDTO.getProgrammeMemberships().isEmpty()) {
				personXLS.addErrorMessage(A_VALID_PROGRAMME_MEMBERSHIP_IS_NEEDED_TO_ADD_A_ROTATION);
			} else {
				Long programmeId = personDTO.getProgrammeMemberships().iterator().next().getProgrammeId();
				List<RotationDTO> rotationByProgrammeId = tcsServiceImpl.getRotationByProgrammeId(programmeId);
				Map<Long, Long> rotationByProgrammeIdMap = rotationByProgrammeId.stream().collect(
						Collectors.toMap(RotationDTO::getId, RotationDTO::getProgrammeId));

				RotationDTO rotationDTOWithRotationName = rotationByProgrammeId.stream()
						.filter(rotationDTO -> rotationDTO.getName().equalsIgnoreCase(rotationName))
						.findFirst()
						.orElse(null);

				if (rotationDTOWithRotationName != null) {
					List<RotationPersonDTO> rotationsForPerson = tcsServiceImpl.getRotationsForPerson(savedPersonDTO.getId());

					List<RotationPersonDTO> personRotationsForProgramme = rotationsForPerson.stream()
							.filter(rotationPersonDTO -> rotationDTOWithRotationName.getProgrammeId().equals(rotationByProgrammeIdMap.get(rotationPersonDTO.getRotationId())))
							.collect(Collectors.toList());

					if (personRotationsForProgramme.size() == 1) {
						RotationPersonDTO rotationPersonDTO = personRotationsForProgramme.get(0);
						rotationPersonDTO.setRotationId(rotationDTOWithRotationName.getId());
						tcsServiceImpl.updateRotationForPerson(rotationPersonDTO);
						return Optional.of(rotationDTOWithRotationName.getName());
					} else if (personRotationsForProgramme.size() == 0) { //add a Rotation
						RotationPersonDTO rotationPersonDTO = new RotationPersonDTO();
						rotationPersonDTO.setPersonId(savedPersonDTO.getId());
						rotationPersonDTO.setRotationId(rotationDTOWithRotationName.getId());
						tcsServiceImpl.createRotationForPerson(rotationPersonDTO);
						return Optional.of(rotationDTOWithRotationName.getName());
					} else {
						personXLS.addErrorMessage(MULTIPLE_ROTATIONS_EXIST_FOR_PERSON_CANNOT_IDENTIFY_A_ROTATION_TO_UPDATE);
					}
				} else {
					personXLS.addErrorMessage(CAN_ONLY_ADD_TO_A_ROTATION_LINKED_TO_THE_PROGRAMME_MEMBERSHIP_YOU_ARE_ADDING);
				}
			}
		}
		return Optional.empty();
	}

	private PersonDTO getPersonDTO(PersonXLS personXLS) {
		Set<CurriculumDTO> curricula = new HashSet<>();
		PersonDTO personDTO = null;
		try {
			CurriculumDTO curriculumDTO1 = getCurriculumDTO(personXLS.getCurriculum1());
			CurriculumDTO curriculumDTO2 = getCurriculumDTO(personXLS.getCurriculum2());
			CurriculumDTO curriculumDTO3 = getCurriculumDTO(personXLS.getCurriculum3());

			ProgrammeDTO programmeDTO = getProgrammeDTO(personXLS.getProgrammeName(), personXLS.getProgrammeNumber());
			if(programmeDTO != null) {
				programmeDTO.setCurricula(curricula);
			}
			personDTO = getPersonDTO(personXLS, curriculumDTO1, curriculumDTO2, curriculumDTO3, programmeDTO);
		} catch (IllegalArgumentException e) {
			personXLS.addErrorMessage(e.getMessage());
		}
		return personDTO;
	}

	private ProgrammeDTO getProgrammeDTO(String programmeName, String programmeNumber) throws IllegalArgumentException {
		return getProgrammeDTO(programmeName, programmeNumber, tcsServiceImpl::getProgrammeByNameAndNumber);
	}

	ProgrammeDTO getProgrammeDTO(String programmeName, String programmeNumber, BiFunction<String, String, List<ProgrammeDTO>> getProgrammeByNameAndNumber) throws IllegalArgumentException {
		ProgrammeDTO programmeDTO = null;
		if (!StringUtils.isEmpty(programmeName) && !StringUtils.isEmpty(programmeNumber)) {
			List<ProgrammeDTO> programmeDTOs = getProgrammeByNameAndNumber.apply(programmeName, programmeNumber);
			if (!CollectionUtils.isEmpty(programmeDTOs)) {
				if(programmeDTOs.size() == 1) {
					programmeDTO = programmeDTOs.get(0);
				} else {
					throw new IllegalArgumentException(String.format(MULTIPLE_PROGRAMME_FOUND_FOR, programmeName, programmeNumber));
				}
			} else if (CollectionUtils.isEmpty(programmeDTOs)) {
				throw new IllegalArgumentException(String.format(PROGRAMME_NOT_FOUND, programmeName, programmeNumber));
			}
		} else {
			if(!StringUtils.isEmpty(programmeName) || !StringUtils.isEmpty(programmeNumber)) {
				if (StringUtils.isEmpty(programmeName)) {
					throw new IllegalArgumentException(String.format(PROGRAMME_NAME_NOT_SPECIFIED, programmeName));
				} else if (StringUtils.isEmpty(programmeNumber)) {
					throw new IllegalArgumentException(String.format(PROGRAMME_NUMBER_NOT_SPECIFIED, programmeNumber));
				}
			}
		}
		return programmeDTO;
	}

	private CurriculumDTO getCurriculumDTO(String curriculumName) throws IllegalArgumentException {
		return getCurriculumDTO(curriculumName, tcsServiceImpl::getCurriculaByName);
	}

	CurriculumDTO getCurriculumDTO(String curriculumName, Function<String, List<CurriculumDTO>> getCurriculumByName) throws IllegalArgumentException {
		CurriculumDTO curriculumDTO = null;
		if (!StringUtils.isEmpty(curriculumName)) {
			List<CurriculumDTO> curriculumDTOs = getCurriculumByName.apply(curriculumName);
			if (!CollectionUtils.isEmpty(curriculumDTOs)) {
				if(curriculumDTOs.size() == 1) {
					curriculumDTO = curriculumDTOs.get(0);
				} else {
					throw new IllegalArgumentException(MULTIPLE_CURRICULA_FOUND_FOR + curriculumName);
				}
			} else if (CollectionUtils.isEmpty(curriculumDTOs)) {
				throw new IllegalArgumentException(CURRICULUM_NOT_FOUND + curriculumName);
			}
		}
		return curriculumDTO;
	}

	public PersonDTO getPersonDTO(PersonXLS personXLS, CurriculumDTO curriculumDTO1, CurriculumDTO curriculumDTO2, CurriculumDTO curriculumDTO3, ProgrammeDTO programmeDTO) {
		PersonDTO personDTO = new PersonDTO();
		personDTO.setAddedDate(LocalDateTime.now());
		personDTO.setInactiveDate(convertDateTime(personXLS.getInactiveDate()));
		personDTO.setPublicHealthNumber(personXLS.getPublicHealthNumber());
		if(!StringUtils.isEmpty(personXLS.getRecordStatus())) {
			personDTO.setStatus(Status.fromString(personXLS.getRecordStatus()));
		} else {
			personDTO.setStatus(Status.CURRENT);
		}
		personDTO.setRole(personXLS.getRole());

		personDTO.setContactDetails(getContactDetailsDTO(personXLS));
		personDTO.setPersonalDetails(getPersonalDetailsDTO(personXLS));

		GmcDetailsDTO gmcDetailsDTO = new GmcDetailsDTO();
		gmcDetailsDTO.setGmcNumber(personXLS.getGmcNumber());
		personDTO.setGmcDetails(gmcDetailsDTO);

		GdcDetailsDTO gdcDetailsDTO = new GdcDetailsDTO();
		gdcDetailsDTO.setGdcNumber(personXLS.getGdcNumber());
		personDTO.setGdcDetails(gdcDetailsDTO);

		personDTO.setRightToWork(getRightToWorkDTO(personXLS));

		if(programmeDTO != null) {
			LocalDate programmeEndDate = null;
			if (personXLS.getProgrammeEndDate() != null) {
				programmeEndDate = convertDate(personXLS.getProgrammeEndDate());
			}

			LocalDate curriculum1StartDateAsProgrammeStartDate = null;
			if (personXLS.getCurriculum1StartDate() != null) {
				curriculum1StartDateAsProgrammeStartDate = personXLS.getCurriculum1StartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}

			ProgrammeMembershipType programmeMembershipType = ProgrammeMembershipType.fromString(personXLS.getProgrammeMembership());

			TrainingNumberDTO trainingNumberDTO = null;
			if (!StringUtils.isEmpty(personXLS.getNtnProgramme())) {
				trainingNumberDTO = new TrainingNumberDTO();
				trainingNumberDTO.setTrainingNumber(personXLS.getNtnProgramme());
			}

			Set<ProgrammeMembershipDTO> programmeMembershipDTOS = new HashSet<>();
			if (curriculumDTO1 != null) {
				addOrUpdateCurriculumToProgrammeMemberships(programmeMembershipDTOS, trainingNumberDTO, curriculum1StartDateAsProgrammeStartDate, programmeEndDate, programmeDTO, curriculumDTO1, programmeMembershipType, personXLS.getCurriculum1StartDate(), personXLS.getCurriculum1EndDate());
			}
			if (curriculumDTO2 != null) {
				addOrUpdateCurriculumToProgrammeMemberships(programmeMembershipDTOS, trainingNumberDTO, curriculum1StartDateAsProgrammeStartDate, programmeEndDate, programmeDTO, curriculumDTO2, programmeMembershipType, personXLS.getCurriculum2StartDate(), personXLS.getCurriculum2EndDate());
			}
			if (curriculumDTO3 != null) {
				addOrUpdateCurriculumToProgrammeMemberships(programmeMembershipDTOS, trainingNumberDTO, curriculum1StartDateAsProgrammeStartDate, programmeEndDate, programmeDTO, curriculumDTO3, programmeMembershipType, personXLS.getCurriculum3StartDate(), personXLS.getCurriculum3EndDate());
			}

			if(programmeMembershipDTOS.isEmpty()) {
				personXLS.addErrorMessage(PROGRAMME_SHOULD_HAVE_AT_LEAST_ONE_CURRICULA);
			} else {
				personDTO.setProgrammeMemberships(programmeMembershipDTOS);
			}
		}
		return personDTO;
	}

	private QualificationDTO getQualificationDTO(PersonXLS personXLS) {
		QualificationDTO qualificationDTO = null;
		if(personXLS.getQualification() != null || personXLS.getCountryOfQualification() != null || personXLS.getMedicalSchool() != null || personXLS.getDateAttained() != null) {
			qualificationDTO = new QualificationDTO();
			qualificationDTO.setCountryOfQualification(personXLS.getCountryOfQualification());
			qualificationDTO.setQualification(personXLS.getQualification());
			qualificationDTO.setMedicalSchool(personXLS.getMedicalSchool());
			qualificationDTO.setQualificationAttainedDate(convertDate(personXLS.getDateAttained()));
		}
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
		personalDetailsDTO.setNationalInsuranceNumber(personXLS.getNiNumber());
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

	private void addOrUpdateCurriculumToProgrammeMemberships(Set<ProgrammeMembershipDTO> programmeMembershipDTOs,
	                                                         TrainingNumberDTO trainingNumberDTO,
	                                                         LocalDate programmeStartDate,
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
		programmeMembershipDTO.setProgrammeId(programmeDTO.getId());
		programmeMembershipDTO.setTrainingNumber(trainingNumberDTO);

		CurriculumMembershipDTO curriculumMembershipDTO = new CurriculumMembershipDTO();
		curriculumMembershipDTO.setCurriculumId(curriculumDTO.getId());
		curriculumMembershipDTO.setCurriculumStartDate(convertDate(curriculumStartDate));
		curriculumMembershipDTO.setCurriculumEndDate(convertDate(curriculumEndDate));

		if(programmeMembershipDTOs.contains(programmeMembershipDTO)) {
			programmeMembershipDTOs.stream()
					.filter(eachPmd -> Objects.equals(eachPmd, programmeMembershipDTO))
					.findFirst()
					.ifPresent(pmd -> pmd.getCurriculumMemberships().add(curriculumMembershipDTO));
		} else {
			programmeMembershipDTO.setCurriculumMemberships(Lists.newArrayList());
			programmeMembershipDTO.getCurriculumMemberships().add(curriculumMembershipDTO);
			programmeMembershipDTOs.add(programmeMembershipDTO);
		}
	}
}
