package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.DTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GDCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GMCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PeopleByPHNFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PersonBasicDetailsDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PostFetcher;
import com.transformuk.hee.tis.genericupload.service.service.supervisor.PhnDTO;
import com.transformuk.hee.tis.genericupload.service.service.supervisor.RegNumberDTO;
import com.transformuk.hee.tis.genericupload.service.service.supervisor.RegNumberToDTOLookup;
import com.transformuk.hee.tis.genericupload.service.service.supervisor.RegNumberType;
import com.transformuk.hee.tis.genericupload.service.service.supervisor.SupervisorRegNumberIdService;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonBasicDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonLiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSupervisorDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PlacementStatus;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PlacementTransformerService {
	private static final Logger logger = getLogger(PlacementTransformerService.class);

	private static final String AT_LEAST_ONE_OF_THE_3_REGISTRATION_NUMBERS_SHOULD_BE_PROVIDED_TO_IDENTIFY_A_PERSON = "At least one of the 3 registration numbers should be provided to identify a person";
	private static final String FIRST_NAME_DOES_NOT_MATCH_FIRST_NAME_OBTAINED_VIA_REGISTRATION_NUMBER = "First name does not match first name obtained via registration number";
	private static final String SURNAME_DOES_NOT_MATCH_LAST_NAME_OBTAINED_VIA_REGISTRATION_NUMBER = "Surname does not match last name obtained via registration number";
	private static final String NATIONAL_POST_NUMBER_IS_MANDATORY = "National Post number is mandatory";
	private static final String MULTIPLE_POSTS_FOUND_FOR_NATIONAL_POST_NUMBER = "Multiple posts found for National Post Number : ";
	private static final String COULD_NOT_FIND_POST_BY_NATIONAL_POST_NUMBER = "Could not find post by National Post Number : ";
	private static final String POST_STATUS_IS_SET_TO_DELETE_FOR_NATIONAL_POST_NUMBER = "POST status is set to DELETE for National Post Number : ";
	private static final String DID_NOT_FIND_A_PERSON_FOR_REGISTRATION_NUMBER = "Did not find a person for registration number : ";
	private static final String DID_NOT_FIND_SPECIALTY_FOR_NAME = "Did not find specialty for name : ";
	private static final String FOUND_MULTIPLE_SPECIALTIES_FOR_NAME = "Found multiple specialties for name : ";
	private static final String PLACEMENT_FROM_DATE_IS_MANDATORY = "Placement from date is mandatory";
	private static final String PLACEMENT_TO_DATE_IS_MANDATORY = "Placement to date is mandatory";
	private static final String MULTIPLE_OR_NO_GRADES_FOUND_FOR = "Multiple or no grades found for  : ";
	private static final String MULTIPLE_OR_NO_SITES_FOUND_FOR = "Multiple or no sites found for  : ";
	private static final String WHOLE_TIME_EQUIVALENT_WTE_IS_MANDATORY = "Whole Time Equivalent (WTE) is mandatory";
	private static final String PLACEMENT_TYPE_IS_MANDATORY = "Placement Type is mandatory";
	private static final String EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR = "Expected to find a single grade for : {}";
	private static final String EXPECTED_TO_FIND_A_SINGLE_SITE_FOR = "Expected to find a single site for : {}";
	private static final String COULD_NOT_FIND_A_FOR_REGISTRATION_NUMBER = "Could not find a %1$s for registration number : %2$s";
	private static final String IS_NOT_A_ROLE_FOR_PERSON_WITH_REGISTRATION_NUMBER = "%1$s is not a role for person with registration number : %2$s";

	public static final String CLINICAL_SUPERVISOR = "Clinical supervisor";
	public static final String EDUCATIONAL_SUPERVISOR = "Educational supervisor";

	@Autowired
	private TcsServiceImpl tcsServiceImpl;
	@Autowired
	private ReferenceServiceImpl referenceServiceImpl;
	@Autowired
	private SupervisorRegNumberIdService supervisorRegNumberIdService;

	private GMCDTOFetcher gmcDtoFetcher;
	private GDCDTOFetcher gdcDtoFetcher;
	private PersonBasicDetailsDTOFetcher pbdDtoFetcher;
	private PeopleByPHNFetcher peopleByPHNFetcher;
	private PostFetcher postFetcher;
	Function<PlacementXLS, String> getPhNumber = PlacementXLS::getPublicHealthNumber;
	Function<PlacementXLS, String> getGdcNumber = PlacementXLS::getGdcNumber;
	Function<PlacementXLS, String> getGmcNumber = PlacementXLS::getGmcNumber;


	@PostConstruct
	public void initialiseFetchers() {
		this.gmcDtoFetcher = new GMCDTOFetcher(tcsServiceImpl);
		this.gdcDtoFetcher = new GDCDTOFetcher(tcsServiceImpl);
		this.pbdDtoFetcher = new PersonBasicDetailsDTOFetcher(tcsServiceImpl);
		this.peopleByPHNFetcher = new PeopleByPHNFetcher(tcsServiceImpl);
		this.postFetcher = new PostFetcher(tcsServiceImpl);
	}

	<DTO> Map<String, DTO> buildRegNumberDetailsMap(List<PlacementXLS> placementXLSS, Function<PlacementXLS, String> getRegNumberFunction, DTOFetcher<String, DTO> fetcher) {
		return fetcher.findWithKeys(
				collectRegNumbersForPlacements(
						getRowsWithRegistrationNumberForPlacements(placementXLSS, getRegNumberFunction),
						getRegNumberFunction));
	}

	<DTO> Map<Long, PersonBasicDetailsDTO> buildPersonBasicDetailsMapForRegNumber(Map<String, DTO> regNumberMap, DTOFetcher<String, DTO> idExtractingFetcher, Function<DTO, Long> getId) {
		return regNumberMap.isEmpty() ? null : pbdDtoFetcher.findWithKeys(idExtractingFetcher.extractIds(regNumberMap, getId));
	}

	void processPlacementsUpload(List<PlacementXLS> placementXLSS) {
		placementXLSS.forEach(PlacementXLS::initialiseSuccessfullyImported);
		RegNumberToDTOLookup regNumberToDTOLookup = supervisorRegNumberIdService.getRegNumbersForSheetOrMarkAsError(placementXLSS);

		if (!CollectionUtils.isEmpty(placementXLSS)) {
			Map<String, PersonDTO> phnDetailsMap = buildRegNumberDetailsMap(placementXLSS, getPhNumber, peopleByPHNFetcher);
			Map<Long, PersonBasicDetailsDTO> pbdMapByPH = buildPersonBasicDetailsMapForRegNumber(phnDetailsMap, peopleByPHNFetcher, PersonDTO::getId);

			Map<String, GdcDetailsDTO> gdcDetailsMap = buildRegNumberDetailsMap(placementXLSS, getGdcNumber, gdcDtoFetcher);
			Map<Long, PersonBasicDetailsDTO> pbdMapByGDC = buildPersonBasicDetailsMapForRegNumber(gdcDetailsMap, gdcDtoFetcher, GdcDetailsDTO::getId);

			Map<String, GmcDetailsDTO> gmcDetailsMap = buildRegNumberDetailsMap(placementXLSS, getGmcNumber, gmcDtoFetcher);
			Map<Long, PersonBasicDetailsDTO> pbdMapByGMC = buildPersonBasicDetailsMapForRegNumber(gmcDetailsMap, gmcDtoFetcher, GmcDetailsDTO::getId);

			Set<String> placementNPNs = placementXLSS.stream()
					.map(PlacementXLS::getNationalPostNumber)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
			Map<String, PostDTO> postsMappedByNPNs = !placementNPNs.isEmpty() ? postFetcher.findWithKeys(placementNPNs) : new HashMap<>();//TODO filter posts CURRENT/INACTIVE
			Set<String> duplicateNPNKeys = !placementNPNs.isEmpty() ? postFetcher.getDuplicateKeys() : new HashSet<>();

			Map<String, SiteDTO> siteMapByName = getSiteDTOMap(placementXLSS);
			Map<String, GradeDTO> gradeMapByName = getGradeDTOMap(placementXLSS);

			for (PlacementXLS placementXLS : placementXLSS) {
				Optional<PersonBasicDetailsDTO> personBasicDetailsDTOOptional = Optional.empty();
				if (!StringUtils.isEmpty(getGdcNumber.apply(placementXLS))) {
					personBasicDetailsDTOOptional = getPersonBasicDetailsDTO(getGdcNumber, gdcDetailsMap, pbdMapByGDC, placementXLS, GdcDetailsDTO::getId);
				} else if (!StringUtils.isEmpty(getGmcNumber.apply(placementXLS))) {
					personBasicDetailsDTOOptional = getPersonBasicDetailsDTO(getGmcNumber, gmcDetailsMap, pbdMapByGMC, placementXLS, GmcDetailsDTO::getId);
				} else if (!StringUtils.isEmpty(getPhNumber.apply(placementXLS))) {
					personBasicDetailsDTOOptional = getPersonBasicDetailsDTO(getPhNumber, phnDetailsMap, pbdMapByPH, placementXLS, PersonDTO::getId);
				} else {
					placementXLS.addErrorMessage(AT_LEAST_ONE_OF_THE_3_REGISTRATION_NUMBERS_SHOULD_BE_PROVIDED_TO_IDENTIFY_A_PERSON);
				}

				if (personBasicDetailsDTOOptional != null && personBasicDetailsDTOOptional.isPresent()) {
					PersonBasicDetailsDTO personBasicDetailsDTO = personBasicDetailsDTOOptional.get();
					if (!placementXLS.getForenames().equalsIgnoreCase(personBasicDetailsDTO.getFirstName())) {
						placementXLS.addErrorMessage(FIRST_NAME_DOES_NOT_MATCH_FIRST_NAME_OBTAINED_VIA_REGISTRATION_NUMBER);
					}

					if (!placementXLS.getSurname().equalsIgnoreCase(personBasicDetailsDTO.getLastName())) {
						placementXLS.addErrorMessage(SURNAME_DOES_NOT_MATCH_LAST_NAME_OBTAINED_VIA_REGISTRATION_NUMBER);
					}
					String nationalPostNumber = placementXLS.getNationalPostNumber();
					if (nationalPostNumber == null) {
						placementXLS.addErrorMessage(NATIONAL_POST_NUMBER_IS_MANDATORY);
					} else if (duplicateNPNKeys.contains(nationalPostNumber)) {
						placementXLS.addErrorMessage(MULTIPLE_POSTS_FOUND_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
					} else if (!postsMappedByNPNs.containsKey(nationalPostNumber)) {
						placementXLS.addErrorMessage(COULD_NOT_FIND_POST_BY_NATIONAL_POST_NUMBER + nationalPostNumber);
					} else {
						PostDTO postDTO = postsMappedByNPNs.get(nationalPostNumber);
						if (postDTO != null) {
							if ("DELETE".equalsIgnoreCase(postDTO.getStatus().toString())) {
								placementXLS.addErrorMessage(POST_STATUS_IS_SET_TO_DELETE_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
							} else {
								if (datesAreValid(placementXLS)) {
									List<PlacementDetailsDTO> placementsByPostIdAndPersonId = tcsServiceImpl.getPlacementsByPostIdAndPersonId(postDTO.getId(), personBasicDetailsDTO.getId());

									LocalDate dateFrom = convertDate(placementXLS.getDateFrom());
									LocalDate dateTo = convertDate(placementXLS.getDateTo());

									boolean existingPlacementUpdatedOrDeleted = false;
									if (!placementsByPostIdAndPersonId.isEmpty()) {
										for (PlacementDetailsDTO placementDTO : placementsByPostIdAndPersonId) {
											if (dateFrom.equals(placementDTO.getDateFrom()) && dateTo.equals(placementDTO.getDateTo())) {
												if ("DELETE".equalsIgnoreCase(placementXLS.getPlacementStatus())) {
													tcsServiceImpl.deletePlacement(placementDTO.getId());
													placementXLS.setSuccessfullyImported(true);
												} else {
													saveOrUpdatePlacement(siteMapByName, gradeMapByName, placementXLS, placementDTO, regNumberToDTOLookup, true);
												}
												existingPlacementUpdatedOrDeleted = true;
												break;
											}
										}
									}

									if (placementsByPostIdAndPersonId.isEmpty() || !existingPlacementUpdatedOrDeleted) {
										PlacementDetailsDTO placementDTO = new PlacementDetailsDTO();
										placementDTO.setTraineeId(personBasicDetailsDTO.getId());
										placementDTO.setPostId(postDTO.getId());
										placementDTO.setDateFrom(dateFrom);
										placementDTO.setDateTo(dateTo);
										saveOrUpdatePlacement(siteMapByName, gradeMapByName, placementXLS, placementDTO, regNumberToDTOLookup, false);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean datesAreValid(PlacementXLS placementXLS) {
		if (placementXLS.getDateFrom() == null || placementXLS.getDateTo() == null) {
			if (placementXLS.getDateFrom() == null) {
				placementXLS.addErrorMessage(PLACEMENT_FROM_DATE_IS_MANDATORY);
			}
			if (placementXLS.getDateTo() == null) {
				placementXLS.addErrorMessage(PLACEMENT_TO_DATE_IS_MANDATORY);
			}
			return false;
		}
		return true;
	}

	public void saveOrUpdatePlacement(Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName, PlacementXLS placementXLS, PlacementDetailsDTO placementDTO, RegNumberToDTOLookup regNumberToDTOLookup, boolean updatePlacement) {
		setOtherMandatoryFields(siteMapByName, gradeMapByName, placementXLS, placementDTO);
		setSpecialties(placementXLS, placementDTO, tcsServiceImpl::getSpecialtyByName); //NOTE : specialties won't have a placement Id here and relies on the api to assign the Id

		Set<String> clinicalSupervisorRoles = referenceServiceImpl.getRolesByCategory(1L).stream()
				.map(roleDTO -> roleDTO.getCode().toLowerCase().trim())
				.collect(Collectors.toSet());
		Set<String> educationalSupervisorRoles = referenceServiceImpl.getRolesByCategory(2L).stream()
				.map(roleDTO -> roleDTO.getCode().toLowerCase().trim())
				.collect(Collectors.toSet());
		addSupervisorsToPlacement(placementXLS, placementDTO, regNumberToDTOLookup, clinicalSupervisorRoles, educationalSupervisorRoles);

		if (!placementXLS.hasErrors()) {
			if (updatePlacement) {
				tcsServiceImpl.updatePlacement(placementDTO);
			} else {
				tcsServiceImpl.createPlacement(placementDTO);
			}
			placementXLS.setSuccessfullyImported(true);
		}
	}

	private void addSupervisorsToPlacement(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO, RegNumberToDTOLookup regNumberToDTOLookup, Set<String> clinicalSupervisorRoles, Set<String> educationalSupervisorRoles) {
		addSupervisorToPlacement(placementXLS, placementDTO, regNumberToDTOLookup, regNumberToDTOLookup::getDTOForClinicalSupervisor, PlacementXLS::getClinicalSupervisor, CLINICAL_SUPERVISOR, clinicalSupervisorRoles);
		addSupervisorToPlacement(placementXLS, placementDTO, regNumberToDTOLookup, regNumberToDTOLookup::getDTOForEducationalSupervisor, PlacementXLS::getEducationalSupervisor, EDUCATIONAL_SUPERVISOR, educationalSupervisorRoles);
	}

	private void addSupervisorToPlacement(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO, RegNumberToDTOLookup regNumberToDTOLookup, Function<String, Optional<RegNumberDTO>> getDTOForRegNumber, Function<PlacementXLS, String> getSupervisor, String supervisorType, Set<String> supervisorRoles) {
		if (!StringUtils.isEmpty(getSupervisor.apply(placementXLS))) {
			Optional<RegNumberDTO> dtoForSupervisor = getDTOForRegNumber.apply(getSupervisor.apply(placementXLS));
			if(dtoForSupervisor.isPresent()) {
				RegNumberDTO regNumberDTO = dtoForSupervisor.get();

				PersonDTO personDTO = regNumberDTO.getRegNumberType() == RegNumberType.PH
						? ((PhnDTO) regNumberDTO).getRegNumberDTO()
						: regNumberToDTOLookup.getPersonDetailsMapForSupervisorsByGmcAndGdc().get(regNumberDTO.getId());

				if(!supervisorHasRole(personDTO, supervisorRoles)) {
					placementXLS.addErrorMessage(String.format(IS_NOT_A_ROLE_FOR_PERSON_WITH_REGISTRATION_NUMBER, supervisorType, getSupervisor.apply(placementXLS)));
				} else {
					PersonLiteDTO personLiteDTO = new PersonLiteDTO();
					personLiteDTO.setId(regNumberDTO.getId());
					PlacementSupervisorDTO placementSupervisorDTO = new PlacementSupervisorDTO();
					placementSupervisorDTO.setPerson(personLiteDTO);

					switch (supervisorType) {
						case CLINICAL_SUPERVISOR : placementSupervisorDTO.setType(1); break;
						case EDUCATIONAL_SUPERVISOR : placementSupervisorDTO.setType(2); break;
						default: break;
					}
					if(placementDTO.getSupervisors() == null) {
						placementDTO.setSupervisors(new HashSet<>());
					}
					placementDTO.getSupervisors().add(placementSupervisorDTO);
				}
			} else {
				placementXLS.addErrorMessage(String.format(COULD_NOT_FIND_A_FOR_REGISTRATION_NUMBER, supervisorType, getSupervisor.apply(placementXLS)));
			}
		}
	}

	private boolean supervisorHasRole(PersonDTO personDTO, Set<String> supervisorRoles) {
		if(StringUtils.isEmpty(personDTO.getRole())) {
			return false;
		}
		Set<String> supervisorRolesAssignedToPerson = new HashSet<>(Arrays.asList(personDTO.getRole().split(",")));
		return supervisorRolesAssignedToPerson.stream()
				.anyMatch(roleAssignedToPerson -> supervisorRoles.contains(roleAssignedToPerson.toLowerCase().trim()));
	}


	<DTO> Optional<PersonBasicDetailsDTO> getPersonBasicDetailsDTO(Function<PlacementXLS, String> getRegNumber, Map<String, DTO> regNumberDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByRegNumber, PlacementXLS placementXLS, Function<DTO, Long> getId) {
		DTO regNumberDTO = regNumberDetailsMap.get(getRegNumber.apply(placementXLS));
		if (regNumberDTO != null) {
			return Optional.of(pbdMapByRegNumber.get(getId.apply(regNumberDTO)));
		} else {
			placementXLS.addErrorMessage(DID_NOT_FIND_A_PERSON_FOR_REGISTRATION_NUMBER + getRegNumber.apply(placementXLS));
			return Optional.empty();
		}
	}

	public void setSpecialties(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO, Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName) {
		Set<PlacementSpecialtyDTO> placementSpecialtyDTOS = placementDTO.getSpecialties();
		if (placementSpecialtyDTOS == null) {
			placementSpecialtyDTOS = initialiseNewPlacementSpecialtyDTOS(placementDTO);
		}

		Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional1 = buildPlacementSpecialtyDTO(placementXLS, placementDTO, getSpecialtyDTOsForName, placementXLS.getSpecialty1(), true);
		if (placementSpecialtyDTOOptional1.isPresent()) {
			placementSpecialtyDTOS = initialiseNewPlacementSpecialtyDTOS(placementDTO);

			PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional1.get();
			addDTOIfNotPresentAsPrimaryOrOther(placementSpecialtyDTOS, placementSpecialtyDTO);
		}

		Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional2 = buildPlacementSpecialtyDTO(placementXLS, placementDTO, getSpecialtyDTOsForName, placementXLS.getSpecialty2(), false);
		if (placementSpecialtyDTOOptional2.isPresent()) {
			PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional2.get();
			addDTOIfNotPresentAsPrimaryOrOther(placementSpecialtyDTOS, placementSpecialtyDTO);
		}

		Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional3 = buildPlacementSpecialtyDTO(placementXLS, placementDTO, getSpecialtyDTOsForName, placementXLS.getSpecialty3(), false);
		if (placementSpecialtyDTOOptional3.isPresent()) {
			PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional3.get();
			addDTOIfNotPresentAsPrimaryOrOther(placementSpecialtyDTOS, placementSpecialtyDTO);
		}
	}

	public Set<PlacementSpecialtyDTO> initialiseNewPlacementSpecialtyDTOS(PlacementDetailsDTO placementDTO) {
		Set<PlacementSpecialtyDTO> placementSpecialtyDTOS = new HashSet<>();
		placementDTO.setSpecialties(placementSpecialtyDTOS);
		return placementSpecialtyDTOS;
	}

	public void addDTOIfNotPresentAsPrimaryOrOther(Set<PlacementSpecialtyDTO> placementSpecialtyDTOS, PlacementSpecialtyDTO placementSpecialtyDTO) {
		if (placementSpecialtyDTOS.size() == 0) {
			placementSpecialtyDTOS.add(placementSpecialtyDTO);
		} else if (!placementSpecialtyDTOS.contains(placementSpecialtyDTO)) {
			placementSpecialtyDTO.setPlacementSpecialtyType(PostSpecialtyType.OTHER);
			placementSpecialtyDTOS.add(placementSpecialtyDTO);
		}
	}

	public Optional<PlacementSpecialtyDTO> buildPlacementSpecialtyDTO(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO, Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName, String specialtyName, boolean primary) {
		if (!StringUtils.isEmpty(specialtyName)) {
			List<SpecialtyDTO> specialtyByName = getSpecialtyDTOsForName.apply(specialtyName);
			if (specialtyByName != null) {
				if (specialtyByName.size() != 1) {
					if (specialtyByName.size() == 0) {
						placementXLS.addErrorMessage(DID_NOT_FIND_SPECIALTY_FOR_NAME + specialtyName);
					} else {
						placementXLS.addErrorMessage(FOUND_MULTIPLE_SPECIALTIES_FOR_NAME + specialtyName);
					}
				} else {
					SpecialtyDTO specialtyDTO = specialtyByName.get(0);
					PlacementSpecialtyDTO placementSpecialtyDTO = new PlacementSpecialtyDTO();
					placementSpecialtyDTO.setPlacementId(placementDTO.getId());
					placementSpecialtyDTO.setSpecialtyId(specialtyDTO.getId());

					placementSpecialtyDTO.setPlacementSpecialtyType(primary ? PostSpecialtyType.PRIMARY : PostSpecialtyType.OTHER);
					return Optional.of(placementSpecialtyDTO);
				}
			}
		}
		return Optional.empty();
	}

	public void setOtherMandatoryFields(Map<String, SiteDTO> siteMapByName,
	                                    Map<String, GradeDTO> gradeMapByName,
	                                    PlacementXLS placementXLS,
	                                    PlacementDetailsDTO placementDTO) {
		placementDTO.setStatus(PlacementStatus.CURRENT);
		setPlacementTypeOrRecordError(placementXLS, placementDTO);
		setWTEOrRecordError(placementXLS, placementDTO);
		setSiteOrRecordError(siteMapByName, placementXLS, placementDTO);
		setGradeOrRecordError(gradeMapByName, placementXLS, placementDTO);
	}

	private void setGradeOrRecordError(Map<String, GradeDTO> gradeMapByName, PlacementXLS placementXLS, PlacementDetailsDTO placementDTO) {
		String grade = placementXLS.getGrade();
		if (!StringUtils.isEmpty(grade) && !gradeMapByName.containsKey(grade)) {
			placementXLS.addErrorMessage(MULTIPLE_OR_NO_GRADES_FOUND_FOR + grade);
		} else {
			GradeDTO gradeDTO = gradeMapByName.get(grade);
			placementDTO.setGradeAbbreviation(gradeDTO.getAbbreviation());
			placementDTO.setGradeId(gradeDTO.getId());
		}
	}

	private void setSiteOrRecordError(Map<String, SiteDTO> siteMapByName, PlacementXLS placementXLS, PlacementDetailsDTO placementDTO) {
		String site = placementXLS.getSite();
		if (!StringUtils.isEmpty(site) && !siteMapByName.containsKey(site)) {
			placementXLS.addErrorMessage(MULTIPLE_OR_NO_SITES_FOUND_FOR + site);
		} else {
			SiteDTO siteDTO = siteMapByName.get(site);
			placementDTO.setSiteCode(siteDTO.getSiteCode());
			placementDTO.setSiteId(siteDTO.getId());
		}
	}

	private void setWTEOrRecordError(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO) {
		if (placementXLS.getWte() == null) {
			placementXLS.addErrorMessage(WHOLE_TIME_EQUIVALENT_WTE_IS_MANDATORY);
		} else {
			placementDTO.setWholeTimeEquivalent(new Double(placementXLS.getWte()));
		}
	}

	private void setPlacementTypeOrRecordError(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO) {
		if (StringUtils.isEmpty(placementXLS.getPlacementType())) {
			placementXLS.addErrorMessage(PLACEMENT_TYPE_IS_MANDATORY);
		} else {
			placementDTO.setPlacementType(placementXLS.getPlacementType());
		}
	}

	//TODO optimise these to be Fetcher like
	private Map<String, GradeDTO> getGradeDTOMap(List<PlacementXLS> placementXLSS) {
		Set<String> gradeNames = placementXLSS.stream()
				.map(PlacementXLS::getGrade)
				.collect(Collectors.toSet());
		Map<String, GradeDTO> gradeMapByName = new HashMap<>();
		for (String gradeName : gradeNames) {
			List<GradeDTO> gradesByName = referenceServiceImpl.findGradesByName(gradeName);
			if (!gradesByName.isEmpty() && gradesByName.size() == 1) {
				gradeMapByName.put(gradeName, gradesByName.get(0));
			} else {
				placementXLSS.stream().filter(placementXLS -> placementXLS.getGrade().equalsIgnoreCase(gradeName)).forEach(placementXLS -> {
					logger.error(EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR, gradeName);
					placementXLS.addErrorMessage(String.format(EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR, gradeName));
				});
			}
		}
		return gradeMapByName;
	}

	private Map<String, SiteDTO> getSiteDTOMap(List<PlacementXLS> placementXLSS) {
		Set<String> siteNames = placementXLSS.stream()
				.map(PlacementXLS::getSite)
				.collect(Collectors.toSet());
		Map<String, SiteDTO> siteMapByName = new HashMap<>();
		for (String siteName : siteNames) {
			List<SiteDTO> sitesByName = referenceServiceImpl.findSitesByName(siteName);
			if (!sitesByName.isEmpty() && sitesByName.size() == 1) {
				siteMapByName.put(siteName, sitesByName.get(0));
			} else {
				placementXLSS.stream().filter(placementXLS -> placementXLS.getSite().equalsIgnoreCase(siteName)).forEach(placementXLS -> {
					logger.error(EXPECTED_TO_FIND_A_SINGLE_SITE_FOR, siteName);
					placementXLS.addErrorMessage(String.format(EXPECTED_TO_FIND_A_SINGLE_SITE_FOR, siteName));
				});
			}
		}
		return siteMapByName;
	}

	private Set<String> collectRegNumbersForPlacements(List<PlacementXLS> placementXLSS, Function<PlacementXLS, String> extractRegistrationNumber) {
		return placementXLSS.stream()
				.map(extractRegistrationNumber::apply)
				.collect(Collectors.toSet());
	}

	private List<PlacementXLS> getRowsWithRegistrationNumberForPlacements(List<PlacementXLS> placementXLSS, Function<PlacementXLS, String> extractRegistrationNumber) {
		return placementXLSS.stream()
				.filter(placementXLS -> {
					String regNumber = extractRegistrationNumber.apply(placementXLS);
					return !"unknown".equalsIgnoreCase(regNumber) && !StringUtils.isEmpty(regNumber);
				})
				.collect(Collectors.toList());
	}
}
