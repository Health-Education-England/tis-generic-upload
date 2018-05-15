package com.transformuk.hee.tis.genericupload.service.service.identity;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
import com.transformuk.hee.tis.genericupload.service.service.PlacementTransformerService;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GDCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GMCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PeopleByPHNFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PersonBasicDetailsDTOFetcher;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SupervisorRegNumberIdService {
	private static final Logger logger = getLogger(PlacementTransformerService.class);

	@Autowired
	private TcsServiceImpl tcsServiceImpl;

	private GMCDTOFetcher gmcDtoFetcher;
	private GDCDTOFetcher gdcDtoFetcher;
	private PersonBasicDetailsDTOFetcher pbdDtoFetcher;
	private PeopleByPHNFetcher peopleByPHNFetcher;

	@PostConstruct
	public void initialiseFetchers() {
		this.gmcDtoFetcher = new GMCDTOFetcher(tcsServiceImpl);
		this.gdcDtoFetcher = new GDCDTOFetcher(tcsServiceImpl);
		this.pbdDtoFetcher = new PersonBasicDetailsDTOFetcher(tcsServiceImpl);
		this.peopleByPHNFetcher = new PeopleByPHNFetcher(tcsServiceImpl);
	}

	public RegNumberToDTOLookup getRegNumbersForSheetOrMarkAsError(List<PlacementXLS> placementXLSS){
		RegNumberToDTOLookup regNumberToDTOLookup = new RegNumberToDTOLookup();

		Function<PlacementXLS, String> getClinicalSupervisor = PlacementXLS::getClinicalSupervisor;
		List<PlacementXLS> clinicalSupervisorsForPlacements = getRowsWithRegistrationNumberForPlacements(placementXLSS, getClinicalSupervisor);
		Set<String> clinicalSupervisorsIds = collectRegNumbersForPlacements(clinicalSupervisorsForPlacements, getClinicalSupervisor);

		regNumberToDTOLookup.setGmcDetailsMapForClinicalSupervisors(gmcDtoFetcher.findWithKeys(clinicalSupervisorsIds));
		regNumberToDTOLookup.setGdcDetailsMapForClinicalSupervisors(gdcDtoFetcher.findWithKeys(clinicalSupervisorsIds));
		regNumberToDTOLookup.setPhnDetailsMapForClinicalSupervisors(peopleByPHNFetcher.findWithKeys(clinicalSupervisorsIds));

		Function<PlacementXLS, String> getEducationalSupervisor = PlacementXLS::getEducationalSupervisor;
		List<PlacementXLS> educationalSupervisorsForPlacements = getRowsWithRegistrationNumberForPlacements(placementXLSS, getEducationalSupervisor);
		Set<String> educationalSupervisorsIds = collectRegNumbersForPlacements(educationalSupervisorsForPlacements, getEducationalSupervisor);

		regNumberToDTOLookup.setGmcDetailsMapForEducationalSupervisors(gmcDtoFetcher.findWithKeys(educationalSupervisorsIds));
		regNumberToDTOLookup.setGdcDetailsMapForEducationalSupervisors(gdcDtoFetcher.findWithKeys(educationalSupervisorsIds));
		regNumberToDTOLookup.setPhnDetailsMapForEducationalSupervisors(peopleByPHNFetcher.findWithKeys(educationalSupervisorsIds));

		return regNumberToDTOLookup;
	}

	private Set<String> collectRegNumbersForPlacements(List<PlacementXLS> placementXLSS, Function<PlacementXLS, String> extractRegistrationNumber) {
		return placementXLSS.stream()
				.map(extractRegistrationNumber::apply)
				.collect(Collectors.toSet());
	}

	private List<PlacementXLS> getRowsWithRegistrationNumberForPlacements(List<PlacementXLS> placementXLSS, Function<PlacementXLS, String> extractRegistrationNumber) {
		return placementXLSS.stream()
				.filter(placementXLS -> !StringUtils.isEmpty(extractRegistrationNumber.apply(placementXLS)))
				.collect(Collectors.toList());
	}
}
