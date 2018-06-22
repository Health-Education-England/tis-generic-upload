package com.transformuk.hee.tis.genericupload.service.service.supervisor;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
import com.transformuk.hee.tis.genericupload.service.service.PlacementTransformerService;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GDCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GMCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PeopleByPHNFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PeopleFetcher;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SupervisorRegNumberIdService {
	public static final String SUPERVISOR_CANNOT_BE_IDENTIFIED_IN_TIS = " cannot be identified in TIS";

	@Autowired
	private TcsServiceImpl tcsServiceImpl;

	private GMCDTOFetcher gmcDtoFetcher;
	private GDCDTOFetcher gdcDtoFetcher;
	private PeopleByPHNFetcher peopleByPHNFetcher;
	private PeopleFetcher peopleFetcher;

	@PostConstruct
	public void initialiseFetchers() {
		this.gmcDtoFetcher = new GMCDTOFetcher(tcsServiceImpl);
		this.gdcDtoFetcher = new GDCDTOFetcher(tcsServiceImpl);
		this.peopleByPHNFetcher = new PeopleByPHNFetcher(tcsServiceImpl);
		this.peopleFetcher = new PeopleFetcher(tcsServiceImpl);
	}

	public RegNumberToDTOLookup getRegNumbersForSheetOrMarkAsError(List<PlacementXLS> placementXLSS){
		RegNumberToDTOLookup regNumberToDTOLookup = new RegNumberToDTOLookup();

		Set<String> clinicalSupervisorsIds = getSupervisorIds(placementXLSS, PlacementXLS::getClinicalSupervisor);
		regNumberToDTOLookup.setGmcDetailsMapForClinicalSupervisors(gmcDtoFetcher.findWithKeys(clinicalSupervisorsIds));
		regNumberToDTOLookup.setGdcDetailsMapForClinicalSupervisors(gdcDtoFetcher.findWithKeys(clinicalSupervisorsIds));
		regNumberToDTOLookup.setPhnDetailsMapForClinicalSupervisors(peopleByPHNFetcher.findWithKeys(clinicalSupervisorsIds));

		Set<String> clinicalSupervisorKeysFoundInTIS = new HashSet<>();
		clinicalSupervisorKeysFoundInTIS.addAll(regNumberToDTOLookup.gmcDetailsMapForClinicalSupervisors.keySet());
		clinicalSupervisorKeysFoundInTIS.addAll(regNumberToDTOLookup.gdcDetailsMapForClinicalSupervisors.keySet());
		clinicalSupervisorKeysFoundInTIS.addAll(regNumberToDTOLookup.phnDetailsMapForClinicalSupervisors.keySet());
		reportSupervisorsNotFound(placementXLSS, clinicalSupervisorsIds, clinicalSupervisorKeysFoundInTIS, PlacementTransformerService.CLINICAL_SUPERVISOR, PlacementXLS::getClinicalSupervisor);

		Set<String> educationalSupervisorsIds = getSupervisorIds(placementXLSS, PlacementXLS::getEducationalSupervisor);
		regNumberToDTOLookup.setGmcDetailsMapForEducationalSupervisors(gmcDtoFetcher.findWithKeys(educationalSupervisorsIds));
		regNumberToDTOLookup.setGdcDetailsMapForEducationalSupervisors(gdcDtoFetcher.findWithKeys(educationalSupervisorsIds));
		regNumberToDTOLookup.setPhnDetailsMapForEducationalSupervisors(peopleByPHNFetcher.findWithKeys(educationalSupervisorsIds));

		Set<String> educationalSupervisorKeysFoundInTIS = new HashSet<>();
		educationalSupervisorKeysFoundInTIS.addAll(regNumberToDTOLookup.gmcDetailsMapForEducationalSupervisors.keySet());
		educationalSupervisorKeysFoundInTIS.addAll(regNumberToDTOLookup.gdcDetailsMapForEducationalSupervisors.keySet());
		educationalSupervisorKeysFoundInTIS.addAll(regNumberToDTOLookup.phnDetailsMapForEducationalSupervisors.keySet());
		reportSupervisorsNotFound(placementXLSS, educationalSupervisorsIds, educationalSupervisorKeysFoundInTIS, PlacementTransformerService.EDUCATIONAL_SUPERVISOR, PlacementXLS::getEducationalSupervisor);

		lookupPeopleForGmcGdcDetails(regNumberToDTOLookup);

		return regNumberToDTOLookup;
	}

	protected void lookupPeopleForGmcGdcDetails(RegNumberToDTOLookup regNumberToDTOLookup) {
		Set<Long> gmcPersonIds = getIdsFromRegNumberDTOsMap(regNumberToDTOLookup.gmcDetailsMapForClinicalSupervisors, GmcDetailsDTO::getId);
		gmcPersonIds.addAll(getIdsFromRegNumberDTOsMap(regNumberToDTOLookup.gmcDetailsMapForEducationalSupervisors, GmcDetailsDTO::getId));
		Function<PersonDTO, String> personDTOToGmcID = personDTO -> personDTO.getGmcDetails().getGmcNumber();
		Map<Long, PersonDTO> gmcPeople = peopleFetcher.setIdMappingFunction(personDTOToGmcID).findWithKeys(gmcPersonIds);

		Set<Long> gdcPersonIds = getIdsFromRegNumberDTOsMap(regNumberToDTOLookup.gdcDetailsMapForClinicalSupervisors, GdcDetailsDTO::getId);
		gdcPersonIds.addAll(getIdsFromRegNumberDTOsMap(regNumberToDTOLookup.gdcDetailsMapForEducationalSupervisors, GdcDetailsDTO::getId));
		Function<PersonDTO, String> personDTOToGdcID = personDTO -> personDTO.getGdcDetails().getGdcNumber();
		Map<Long, PersonDTO> gdcPeople = peopleFetcher.setIdMappingFunction(personDTOToGdcID).findWithKeys(gdcPersonIds);

		Map<Long, PersonDTO> combinedGMCAndGDCPeopleList = Stream.concat(gmcPeople.entrySet().stream(), gdcPeople.entrySet().stream())
				.collect(Collectors.toMap(
						longPersonDTOEntry -> longPersonDTOEntry.getValue().getId(),
						Map.Entry::getValue,
						(a, b) -> a)); //merge function set to use on duplicates

		regNumberToDTOLookup.setPersonDetailsMapForSupervisorsByGmcAndGdc(combinedGMCAndGDCPeopleList);
	}

	private void reportSupervisorsNotFound(List<PlacementXLS> placementXLSS, Set<String> supervisorsIds, Set<String> supervisorKeysFoundInTIS, String supervisorType, Function<PlacementXLS, String> extractRegistrationNumber) {
		Collection<String> regNumbersNotInTIS = CollectionUtils.subtract(supervisorsIds, supervisorKeysFoundInTIS);
		String errorMessage = supervisorType + SUPERVISOR_CANNOT_BE_IDENTIFIED_IN_TIS;
		placementXLSS.forEach(placementXLS -> {
			if(regNumbersNotInTIS.contains(extractRegistrationNumber.apply(placementXLS))) {
				placementXLS.addErrorMessage(errorMessage);
			}
		});
	}

	private Set<String> getSupervisorIds(List<PlacementXLS> placementXLSS, Function<PlacementXLS, String> extractRegistrationNumber) {
		return collectRegNumbersForPlacements(getRowsWithRegistrationNumberForPlacements(placementXLSS, extractRegistrationNumber), extractRegistrationNumber);
	}

	private <DTO> Set<Long> getIdsFromRegNumberDTOsMap(Map<String, DTO> regNumberMap, Function<DTO, Long> getId) {
		return regNumberMap.values().stream()
				.map(getId)
				.collect(Collectors.toSet());
	}

	private Set<String> collectRegNumbersForPlacements(List<PlacementXLS> placementXLSS, Function<PlacementXLS, String> extractRegistrationNumber) {
		return placementXLSS.stream()
				.map(extractRegistrationNumber)
				.collect(Collectors.toSet());
	}

	private List<PlacementXLS> getRowsWithRegistrationNumberForPlacements(List<PlacementXLS> placementXLSS, Function<PlacementXLS, String> extractRegistrationNumber) {
		return placementXLSS.stream()
				.filter(placementXLS -> !StringUtils.isEmpty(extractRegistrationNumber.apply(placementXLS)))
				.collect(Collectors.toList());
	}
}
