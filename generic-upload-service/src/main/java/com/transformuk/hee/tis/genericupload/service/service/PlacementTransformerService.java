package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GDCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GMCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PeopleByPHNFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PersonBasicDetailsDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PostFetcher;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonBasicDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PlacementTransformerService {
	private static final Logger logger = getLogger(PlacementTransformerService.class);

	@Autowired
	private TcsServiceImpl tcsServiceImpl;
	@Autowired
	private ReferenceServiceImpl referenceServiceImpl;

	private GMCDTOFetcher gmcDtoFetcher;
	private GDCDTOFetcher gdcDtoFetcher;
	private PersonBasicDetailsDTOFetcher pbdDtoFetcher;
	private PeopleByPHNFetcher peopleByPHNFetcher;
	private PostFetcher postFetcher;

	@PostConstruct
	public void initialiseFetchers() {
		this.gmcDtoFetcher = new GMCDTOFetcher(tcsServiceImpl);
		this.gdcDtoFetcher = new GDCDTOFetcher(tcsServiceImpl);
		this.pbdDtoFetcher = new PersonBasicDetailsDTOFetcher(tcsServiceImpl);
		this.peopleByPHNFetcher = new PeopleByPHNFetcher(tcsServiceImpl);
		this.postFetcher = new PostFetcher(tcsServiceImpl);
	}

	public void processPlacementsUpload(List<PlacementXLS> placementXLSS) {
		placementXLSS.forEach(PlacementXLS::initialiseSuccessfullyImported);

		if (!CollectionUtils.isEmpty(placementXLSS)) {
			Function<PlacementXLS, String> getPhNumber = PlacementXLS::getPublicHealthNumber;
			Function<PlacementXLS, String> getGdcNumber = PlacementXLS::getGdcNumber;
			Function<PlacementXLS, String> getGmcNumber = PlacementXLS::getGmcNumber;

			List<PlacementXLS> rowsWithPHNumbers = getRowsWithRegistrationNumberForPlacements(placementXLSS, getPhNumber);
			List<PlacementXLS> rowsWithGDCNumbers = getRowsWithRegistrationNumberForPlacements(placementXLSS, getGdcNumber);
			List<PlacementXLS> rowsWithGMCNumbers = getRowsWithRegistrationNumberForPlacements(placementXLSS, getGmcNumber);

			Set<String> phNumbers = collectRegNumbersForPlacements(rowsWithPHNumbers, getPhNumber);
			Set<String> gdcNumbers = collectRegNumbersForPlacements(rowsWithGDCNumbers, getGdcNumber);
			Set<String> gmcNumbers = collectRegNumbersForPlacements(rowsWithGMCNumbers, getGmcNumber);

			Map<String, PersonDTO> phnDetailsMap = peopleByPHNFetcher.findWithKeys(phNumbers);
			Map<String, GdcDetailsDTO> gdcDetailsMap = gdcDtoFetcher.findWithKeys(gdcNumbers);
			Map<String, GmcDetailsDTO> gmcDetailsMap = gmcDtoFetcher.findWithKeys(gmcNumbers);

			Map<Long, PersonBasicDetailsDTO> pbdMapByGDC = null;
			if (!gdcDetailsMap.isEmpty()) {
				Set<Long> personIdsFromGDCDetailsTable = gdcDtoFetcher.extractIds(gdcDetailsMap, GdcDetailsDTO::getId);
				pbdMapByGDC = pbdDtoFetcher.findWithKeys(personIdsFromGDCDetailsTable);
			}

			Map<Long, PersonBasicDetailsDTO> pbdMapByGMC = null;
			if (!gmcDetailsMap.isEmpty()) {
				Set<Long> personIdsFromGMCDetailsTable = gmcDtoFetcher.extractIds(gmcDetailsMap, GmcDetailsDTO::getId);
				pbdMapByGMC = pbdDtoFetcher.findWithKeys(personIdsFromGMCDetailsTable);
			}

			Map<Long, PersonBasicDetailsDTO> pbdMapByPH = null;
			if (!phnDetailsMap.isEmpty()) {
				Set<Long> personIds = peopleByPHNFetcher.extractIds(phnDetailsMap, PersonDTO::getId);
				pbdMapByPH = pbdDtoFetcher.findWithKeys(personIds);
			}

			Set<String> placementNPNs = placementXLSS.stream() //TODO NPNs are blank here !
					.map(PlacementXLS::getNationalPostNumber)
					.collect(Collectors.toSet());
			Map<String, PostDTO> postsMappedByNPNs = postFetcher.findWithKeys(placementNPNs); //TODO filter posts CURRENT/INACTIVE
			Set<String> duplicateNPNKeys = postFetcher.getDuplicateKeys();


			Map<String, SiteDTO> siteMapByName = getSiteDTOMap(placementXLSS);
			Map<String, GradeDTO> gradeMapByName = getGradeDTOMap(placementXLSS);

			for(PlacementXLS placementXLS : placementXLSS) {
				PersonBasicDetailsDTO personBasicDetailsDTO = null;
				if(!StringUtils.isEmpty(getGdcNumber.apply(placementXLS))) {
					personBasicDetailsDTO = pbdMapByGDC.get(gdcDetailsMap.get(getGdcNumber.apply(placementXLS)).getId());
				} else if(!StringUtils.isEmpty(getGmcNumber.apply(placementXLS))) {
					personBasicDetailsDTO = pbdMapByGMC.get(gmcDetailsMap.get(getGmcNumber.apply(placementXLS)).getId());
				} else if(!StringUtils.isEmpty(getPhNumber.apply(placementXLS))) {
					personBasicDetailsDTO = pbdMapByPH.get(phnDetailsMap.get(getPhNumber.apply(placementXLS)).getId());
				}

				if(personBasicDetailsDTO == null) {
					placementXLS.addErrorMessage("Could not find person via registration number");
				} else {
					//validate that person exists
					if(!placementXLS.getForenames().equalsIgnoreCase(personBasicDetailsDTO.getFirstName())) {
						placementXLS.addErrorMessage("First name does not match first name obtained via registration number");
					}

					if(!placementXLS.getSurname().equalsIgnoreCase(personBasicDetailsDTO.getLastName())) {
						placementXLS.addErrorMessage("Surname does not match last name obtained via registration number");
					}
				}


				String nationalPostNumber = placementXLS.getNationalPostNumber();
				if(duplicateNPNKeys.contains(nationalPostNumber)) {
					placementXLS.addErrorMessage("Multiple posts found for National Post Number : " + nationalPostNumber);
				} else if(!postsMappedByNPNs.containsKey(nationalPostNumber)) {
					placementXLS.addErrorMessage("Could not find post by National Post Number : " + nationalPostNumber);
				} else {
					PostDTO postDTO = postsMappedByNPNs.get(nationalPostNumber);
					if(postDTO != null && personBasicDetailsDTO != null) {
						if(postDTO.getStatus().equals("DELETE")) {
							placementXLS.addErrorMessage("POST status is set to DELETE for National Post Number : " + nationalPostNumber);
						} else {
							List<PlacementDetailsDTO> placementsByPostIdAndPersonId = tcsServiceImpl.getPlacementsByPostIdAndPersonId(postDTO.getId(), personBasicDetailsDTO.getId());
							if(placementsByPostIdAndPersonId.isEmpty()) {
								PlacementDetailsDTO placementDTO = new PlacementDetailsDTO();
								placementDTO.setTraineeId(personBasicDetailsDTO.getId());
								placementDTO.setPostId(postDTO.getId());

								setDatesOrRecordError(placementXLS, placementDTO, false);
								setOtherMandatoryFields(siteMapByName, gradeMapByName, placementXLS, placementDTO);

								if(!placementXLS.hasErrors()) {
									tcsServiceImpl.createPlacement(placementDTO);
								}
							} else {
								if(placementsByPostIdAndPersonId.size() > 1) { //TODO validate this is ok - seem like we have to iterate and find at least one that matches dates - if not error
									placementXLS.addErrorMessage(String.format("Multiple placements found for post with id (%1$s) and person with id (%2$s)", postDTO.getId(), personBasicDetailsDTO.getId()));
								} else {
									PlacementDetailsDTO placementDTO = placementsByPostIdAndPersonId.get(0);

									setDatesOrRecordError(placementXLS, placementDTO, true);
									setOtherMandatoryFields(siteMapByName, gradeMapByName, placementXLS, placementDTO);

									if(!placementXLS.hasErrors()) {
										tcsServiceImpl.updatePlacement(placementDTO);
									}
								}
							}
						}
					} else {
						logger.error("Unexpected error. Expected to have a post with id {} and person with id {}", postDTO.getId(), personBasicDetailsDTO.getId());
						continue;
					}
				}
			}
		}
	}

	public void setOtherMandatoryFields(Map<String, SiteDTO> siteMapByName,
	                                    Map<String, GradeDTO> gradeMapByName,
	                                    PlacementXLS placementXLS,
	                                    PlacementDetailsDTO placementDTO) {
		setPlacementTypeOrRecordError(placementXLS, placementDTO);
		setWTEOrRecordError(placementXLS, placementDTO);
		setSiteOrRecordError(siteMapByName, placementXLS, placementDTO);
		setGradeOrRecordError(gradeMapByName, placementXLS, placementDTO);
	}

	public void setDatesOrRecordError(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO, boolean update) {
		if(placementXLS.getDateFrom() != null && placementXLS.getDateTo() != null) {
			LocalDate dateFrom = convertDate(placementXLS.getDateFrom());
			LocalDate dateTo = convertDate(placementXLS.getDateTo());

			if(update) {
				if (!dateFrom.equals(placementDTO.getDateFrom())) {
					placementXLS.addErrorMessage("From date does not match existing placement");
				}
				if (!dateTo.equals(placementDTO.getDateTo())) {
					placementXLS.addErrorMessage("To date does not match existing placement");
				}
			} else {
				placementDTO.setDateFrom(dateFrom);
				placementDTO.setDateTo(dateTo);
			}
		} else {
			if(placementXLS.getDateFrom() == null) {
				placementXLS.addErrorMessage("Placement from date is mandatory");
			}
			if(placementXLS.getDateTo() == null) {
				placementXLS.addErrorMessage("Placement to date is mandatory");
			}
		}
	}

	private void setGradeOrRecordError(Map<String, GradeDTO> gradeMapByName, PlacementXLS placementXLS, PlacementDetailsDTO placementDTO) {
		if(StringUtils.isEmpty(placementXLS.getGrade())) {
			placementXLS.addErrorMessage("Multiple or no grades found for  : " + placementXLS.getGrade());
		} else {
			GradeDTO gradeDTO = gradeMapByName.get(placementXLS.getGrade());
			placementDTO.setGradeAbbreviation(gradeDTO.getAbbreviation());
			placementDTO.setGradeId(gradeDTO.getId());
		}
	}

	private void setSiteOrRecordError(Map<String, SiteDTO> siteMapByName, PlacementXLS placementXLS, PlacementDetailsDTO placementDTO) {
		if(StringUtils.isEmpty(placementXLS.getSite())) {
			placementXLS.addErrorMessage("Multiple or no sites found for  : " + placementXLS.getSite());
		} else {
			SiteDTO siteDTO = siteMapByName.get(placementXLS.getSite());
			placementDTO.setSiteCode(siteDTO.getSiteCode());
			placementDTO.setSiteId(siteDTO.getId());
		}
	}

	private void setWTEOrRecordError(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO) {
		if(placementXLS.getWte() == null) {
			placementXLS.addErrorMessage("Whole Time Equivalent (WTE) is mandatory");
		} else {
			placementDTO.setWholeTimeEquivalent(new Double(placementXLS.getWte()));
		}
	}

	private void setPlacementTypeOrRecordError(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO) {
		if(StringUtils.isEmpty(placementXLS.getPlacementType())) {
			placementXLS.addErrorMessage("Placement Type is mandatory");
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
		for(String gradeName : gradeNames) {
			List<GradeDTO> gradesByName = referenceServiceImpl.findGradesByName(gradeName);
			if(!gradesByName.isEmpty() && gradesByName.size() == 1) {
				gradeMapByName.put(gradeName, gradesByName.get(0));
			} else {
				logger.error("Expected to find a single grade for : {}", gradeName);
			}
		}
		return gradeMapByName;
	}

	private Map<String, SiteDTO> getSiteDTOMap(List<PlacementXLS> placementXLSS) {
		Set<String> siteNames = placementXLSS.stream()
				.map(PlacementXLS::getSite)
				.collect(Collectors.toSet());
		Map<String, SiteDTO> siteMapByName = new HashMap<>();
		for(String siteName : siteNames) {
			List<SiteDTO> sitesByName = referenceServiceImpl.findSitesByName(siteName);
			if(!sitesByName.isEmpty() && sitesByName.size() == 1) {
				siteMapByName.put(siteName, sitesByName.get(0));
			} else {
				logger.error("Expected to find a single site for : {}", siteName);
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
