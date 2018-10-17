package com.transformuk.hee.tis.genericupload.service.service;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementSupervisor;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementUpdateXLS;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.*;
import com.transformuk.hee.tis.genericupload.service.service.supervisor.*;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.*;
import com.transformuk.hee.tis.tcs.api.enumeration.CommentSource;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.slf4j.LoggerFactory.getLogger;
@Component
public class PlacementUpdateTransformerService {
  public static final String INTREPID_ID_IS_ALREADY_EXISTS_FOR_THIS_RECORD_AND_IT_CAN_NOT_BE_UPDATED = "INTREPID ID already exists for this record of placement and it can not be updated";
  private static final Logger logger = getLogger(PlacementUpdateTransformerService.class);
  private static final String MULTIPLE_POSTS_FOUND_FOR_NATIONAL_POST_NUMBER = "Multiple posts were found for National Post Number : ";
  private static final String COULD_NOT_FIND_POST_BY_NATIONAL_POST_NUMBER = "Could not find post by THIS National Post Number : ";
  private static final String POST_STATUS_IS_SET_TO_DELETE_FOR_NATIONAL_POST_NUMBER = "POST status is set to DELETE for National Post Number : ";
  private static final String DID_NOT_FIND_SPECIALTY_FOR_NAME = "Did not find specialty for name : ";
  private static final String FOUND_MULTIPLE_SPECIALTIES_FOR_NAME = "Found multiple specialties for name : ";
  private static final String MULTIPLE_OR_NO_GRADES_FOUND_FOR = "Multiple or no grades found for  : ";
  private static final String MULTIPLE_OR_NO_SITES_FOUND_FOR = "Multiple or no sites found for  : ";
  private static final String EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR = "Expected to find a single grade for : %s";
  private static final String EXPECTED_TO_FIND_A_SINGLE_SITE_FOR = "Expected to find a single site for : %s";
  private static final String COULD_NOT_FIND_A_FOR_REGISTRATION_NUMBER = "Could not find a %1$s for Registration number : %s";
  private static final String IS_NOT_A_ROLE_FOR_PERSON_WITH_REGISTRATION_NUMBER = "%1$s is not a role for person with registration number : %2$s";
  public static final String CLINICAL_SUPERVISOR = "Clinical supervisor";
  public static final String EDUCATIONAL_SUPERVISOR = "Educational supervisor";
  @Autowired
  private TcsServiceImpl tcsServiceImpl;
  @Autowired
  private ReferenceServiceImpl referenceServiceImpl;
  @Autowired
  private SupervisorRegNumberIdService supervisorRegNumberIdService;
  private PostFetcher postFetcher;
  @PostConstruct
  public void initialiseFetchers() {
    this.postFetcher = new PostFetcher(tcsServiceImpl);
  }
  void processPlacementsUpdateUpload(List<PlacementUpdateXLS> placementUpdateXLSS, String username) {
    placementUpdateXLSS.forEach(PlacementUpdateXLS::initialiseSuccessfullyImported);
    List<PlacementSupervisor> placementSupervisorList = placementUpdateXLSS.stream().collect(Collectors.toList());
    RegNumberToDTOLookup regNumberToDTOLookup = supervisorRegNumberIdService.getRegNumbersForSheetOrMarkAsError(placementSupervisorList);
    Set<String> placementNPNs = placementUpdateXLSS.stream()
        .map(PlacementUpdateXLS::getNationalPostNumber)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Map<String, PostDTO> postsMappedByNPNs = !placementNPNs.isEmpty() ? postFetcher.findWithKeys(placementNPNs) : new HashMap<>();//TODO filter posts CURRENT/INACTIVE
    Set<String> duplicateNPNKeys = !placementNPNs.isEmpty() ? postFetcher.getDuplicateKeys() : new HashSet<>();
    Map<String, SiteDTO> siteMapByName = getSiteDTOMap(placementUpdateXLSS);
    Map<String, GradeDTO> gradeMapByName = getGradeDTOMap(placementUpdateXLSS);
    for (PlacementUpdateXLS placementXLS : placementUpdateXLSS) {
      useMatchingCriteriaToUpdatePlacement(regNumberToDTOLookup, postsMappedByNPNs, duplicateNPNKeys, siteMapByName, gradeMapByName, placementXLS, username);
    }
  }
  private void useMatchingCriteriaToUpdatePlacement(RegNumberToDTOLookup regNumberToDTOLookup, Map<String, PostDTO> postsMappedByNPNs, Set<String> duplicateNPNKeys,
                                                    Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
                                                    PlacementUpdateXLS placementXLS, String username) {
    if (!StringUtils.isEmpty(placementXLS.getPlacementId())) {
      PlacementDetailsDTO dbPlacementDetailsDTO = tcsServiceImpl.getPlacementById(Long.valueOf(placementXLS.getPlacementId()));
      if (dbPlacementDetailsDTO != null) {
        updateIntrepidId(placementXLS, dbPlacementDetailsDTO);
        String nationalPostNumber = placementXLS.getNationalPostNumber();
        PostDTO postDTO = null;
        if(placementXLS.getNationalPostNumber() != null) {
          if (isNPNValid(placementXLS, nationalPostNumber, postsMappedByNPNs, duplicateNPNKeys)) {
            postDTO = postsMappedByNPNs.get(nationalPostNumber);
            if (postDTO != null) {
              if ("DELETE".equalsIgnoreCase(postDTO.getStatus().toString())) {
                placementXLS.addErrorMessage(POST_STATUS_IS_SET_TO_DELETE_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
              } else {
                updatePlacement(regNumberToDTOLookup, dbPlacementDetailsDTO, siteMapByName, gradeMapByName, placementXLS, postDTO, username);
              }
            }
          }
        }
        else{
          updatePlacement(regNumberToDTOLookup, dbPlacementDetailsDTO, siteMapByName, gradeMapByName, placementXLS, postDTO, username);
        }
      }
    }
  }
  public void updateIntrepidId(PlacementUpdateXLS placementXLS, PlacementDetailsDTO dbPlacementDetailsDTO) {
    if(!StringUtils.isEmpty(placementXLS.getIntrepidId())){
      if (!StringUtils.isEmpty(dbPlacementDetailsDTO.getIntrepidId())) {
        placementXLS.addErrorMessage(INTREPID_ID_IS_ALREADY_EXISTS_FOR_THIS_RECORD_AND_IT_CAN_NOT_BE_UPDATED);
      } else {
        dbPlacementDetailsDTO.setIntrepidId(placementXLS.getIntrepidId());
      }
    }
  }
  public boolean isNPNValid(PlacementUpdateXLS placementXLS, String nationalPostNumber, Map<String, PostDTO> postsMappedByNPNs, Set<String> duplicateNPNKeys) {
    if (!StringUtils.isEmpty(nationalPostNumber)) {
      if (duplicateNPNKeys.contains(nationalPostNumber)) {
        placementXLS.addErrorMessage(MULTIPLE_POSTS_FOUND_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
        return false;
      } else if (!postsMappedByNPNs.containsKey(nationalPostNumber)) {
        placementXLS.addErrorMessage(COULD_NOT_FIND_POST_BY_NATIONAL_POST_NUMBER + nationalPostNumber);
        return false;
      }
    }
    return true;
  }
  private void updatePlacement(RegNumberToDTOLookup regNumberToDTOLookup, PlacementDetailsDTO dbPlacementDetailsDTO,
                               Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
                               PlacementUpdateXLS placementXLS, PostDTO postDTO, String username) {
    if(placementXLS.getDateFrom() != null){
      LocalDate dateFrom = convertDate(placementXLS.getDateFrom());
      dbPlacementDetailsDTO.setDateFrom(dateFrom);
    }
    if(placementXLS.getDateTo() != null){
      LocalDate dateTo = convertDate(placementXLS.getDateTo());
      dbPlacementDetailsDTO.setDateTo(dateTo);
    }
    setOtherMandatoryFields(siteMapByName, gradeMapByName, placementXLS, dbPlacementDetailsDTO);
    setSpecialties(placementXLS, dbPlacementDetailsDTO, tcsServiceImpl::getSpecialtyByName); //NOTE : specialties won't have a placement Id here and relies on the api to assign the Id
    Set<String> clinicalSupervisorRoles = referenceServiceImpl.getRolesByCategory(1L).stream()
        .map(roleDTO -> roleDTO.getCode().toLowerCase().trim())
        .collect(Collectors.toSet());
    Set<String> educationalSupervisorRoles = referenceServiceImpl.getRolesByCategory(2L).stream()
        .map(roleDTO -> roleDTO.getCode().toLowerCase().trim())
        .collect(Collectors.toSet());
    addSupervisorsToPlacement(placementXLS, dbPlacementDetailsDTO, regNumberToDTOLookup, clinicalSupervisorRoles, educationalSupervisorRoles);
    // update the post
    if(postDTO != null) {
      dbPlacementDetailsDTO.setPostId(postDTO.getId());
    }
    if (!placementXLS.hasErrors()) {
      setCommentInPlacementDTO(dbPlacementDetailsDTO, placementXLS, username);
      logger.info("dbPlacementDetailsDTO => {}",dbPlacementDetailsDTO);
      tcsServiceImpl.updatePlacement(dbPlacementDetailsDTO);
      placementXLS.setSuccessfullyImported(true);
    }
  }
  private void setCommentInPlacementDTO(PlacementDetailsDTO placementDTO, PlacementUpdateXLS placementXLS, String username) {
    if(!StringUtils.isEmpty(placementXLS.getComments())) {
      if(placementDTO.getComments() == null) {
        placementDTO.setComments(new HashSet<>());
      }
      PlacementCommentDTO placementCommentDTO;
      Optional<PlacementCommentDTO> commentsByGenericUpload = placementDTO.getComments().stream()
          .filter(anExistingCommentForPlacement -> !StringUtils.isEmpty(anExistingCommentForPlacement.getSource()) && anExistingCommentForPlacement.getSource().equals(CommentSource.GENERIC_UPLOAD))
          .findAny();
      if(commentsByGenericUpload.isPresent()) {
        placementCommentDTO = commentsByGenericUpload.get();
      } else {
        placementCommentDTO = new PlacementCommentDTO();
        placementDTO.getComments().add(placementCommentDTO);
        placementCommentDTO.setSource(CommentSource.GENERIC_UPLOAD);
      }
      placementCommentDTO.setBody(placementXLS.getComments());
      placementCommentDTO.setAuthor(username);
    }
  }
  private void addSupervisorsToPlacement(PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO, RegNumberToDTOLookup regNumberToDTOLookup, Set<String> clinicalSupervisorRoles, Set<String> educationalSupervisorRoles) {
    addSupervisorToPlacement(placementXLS, placementDTO, regNumberToDTOLookup, regNumberToDTOLookup::getDTOForClinicalSupervisor, PlacementUpdateXLS::getClinicalSupervisor, CLINICAL_SUPERVISOR, clinicalSupervisorRoles);
    addSupervisorToPlacement(placementXLS, placementDTO, regNumberToDTOLookup, regNumberToDTOLookup::getDTOForEducationalSupervisor, PlacementUpdateXLS::getEducationalSupervisor, EDUCATIONAL_SUPERVISOR, educationalSupervisorRoles);
  }
  private void addSupervisorToPlacement(PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO, RegNumberToDTOLookup regNumberToDTOLookup, Function<String, Optional<RegNumberDTO>> getDTOForRegNumber, Function<PlacementUpdateXLS, String> getSupervisor, String supervisorType, Set<String> supervisorRoles) {
    if (!StringUtils.isEmpty(getSupervisor.apply(placementXLS))) {
      Optional<RegNumberDTO> dtoForSupervisor = getDTOForRegNumber.apply(getSupervisor.apply(placementXLS));
      if(dtoForSupervisor.isPresent()) {
        RegNumberDTO regNumberDTO = dtoForSupervisor.get();
        PersonDTO personDTO = regNumberDTO.getRegNumberType() == RegNumberType.PH
            ? ((PhnDTO) regNumberDTO).getRegNumberDTO()
            : regNumberToDTOLookup.getPersonDetailsMapForSupervisorsByGmcAndGdc().get(regNumberDTO.getId());
        if(!PlacementTransformerService.supervisorHasRole(personDTO, supervisorRoles)) {
          placementXLS.addErrorMessage(String.format(IS_NOT_A_ROLE_FOR_PERSON_WITH_REGISTRATION_NUMBER, supervisorType, getSupervisor.apply(placementXLS)));
        } else {
          PlacementTransformerService.addNewSupervisorToPlacement(placementDTO, supervisorType, regNumberDTO);
        }
      } else {
        placementXLS.addErrorMessage(String.format(COULD_NOT_FIND_A_FOR_REGISTRATION_NUMBER, supervisorType, getSupervisor.apply(placementXLS)));
      }
    }
  }
  public void setSpecialties(PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO, Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName) {
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
      System.out.println();
    }
  }
  public Set<PlacementSpecialtyDTO> initialiseNewPlacementSpecialtyDTOS(PlacementDetailsDTO placementDTO) {
    Set<PlacementSpecialtyDTO> placementSpecialtyDTOS = new HashSet<>();
    placementDTO.setSpecialties(placementSpecialtyDTOS);
    return placementSpecialtyDTOS;
  }
  public void addDTOIfNotPresentAsPrimaryOrOther(Set<PlacementSpecialtyDTO> placementSpecialtyDTOS, PlacementSpecialtyDTO placementSpecialtyDTO) {
    if (placementSpecialtyDTOS.isEmpty()) {
      placementSpecialtyDTOS.add(placementSpecialtyDTO);
    } else if (!placementSpecialtyDTOS.contains(placementSpecialtyDTO)) {
      placementSpecialtyDTO.setPlacementSpecialtyType(PostSpecialtyType.OTHER);
      placementSpecialtyDTOS.add(placementSpecialtyDTO);
    }
  }
  public Optional<PlacementSpecialtyDTO> buildPlacementSpecialtyDTO(PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO, Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName, String specialtyName, boolean primary) {
    Optional<SpecialtyDTO> aSingleValidSpecialty = getASingleValidSpecialtyFromTheReferenceService(placementXLS, getSpecialtyDTOsForName, specialtyName);
    if (aSingleValidSpecialty.isPresent()) {
      SpecialtyDTO specialtyDTO = aSingleValidSpecialty.get();
      PlacementSpecialtyDTO placementSpecialtyDTO = new PlacementSpecialtyDTO();
      placementSpecialtyDTO.setPlacementId(placementDTO.getId());
      placementSpecialtyDTO.setSpecialtyId(specialtyDTO.getId());
      placementSpecialtyDTO.setPlacementSpecialtyType(primary ? PostSpecialtyType.PRIMARY : PostSpecialtyType.OTHER);
      return Optional.of(placementSpecialtyDTO);
    }
    return Optional.empty();
  }
  private Optional<SpecialtyDTO> getASingleValidSpecialtyFromTheReferenceService(PlacementUpdateXLS placementXLS, Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName, String specialtyName) {
    if (!StringUtils.isEmpty(specialtyName)) {
      List<SpecialtyDTO> specialtyByName = getSpecialtyDTOsForName.apply(specialtyName);
      if (specialtyByName != null) {
        if (specialtyByName.size() != 1) {
          if (specialtyByName.isEmpty()) {
            placementXLS.addErrorMessage(DID_NOT_FIND_SPECIALTY_FOR_NAME + specialtyName);
          } else {
            placementXLS.addErrorMessage(FOUND_MULTIPLE_SPECIALTIES_FOR_NAME + specialtyName);
          }
        } else {
          return Optional.of(specialtyByName.get(0));
        }
      }
    }
    return Optional.empty();
  }
  public void setOtherMandatoryFields(Map<String, SiteDTO> siteMapByName,
                                      Map<String, GradeDTO> gradeMapByName,
                                      PlacementUpdateXLS placementXLS,
                                      PlacementDetailsDTO placementDTO) {
    setPlacementTypeOrRecordError(placementXLS, placementDTO);
    setWTEOrRecordError(placementXLS, placementDTO);
    setSiteOrRecordError(siteMapByName, placementXLS, placementDTO);
    setGradeOrRecordError(gradeMapByName, placementXLS, placementDTO);
  }
  private void setGradeOrRecordError(Map<String, GradeDTO> gradeMapByName, PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO) {
    String grade = placementXLS.getGrade();
    if(!StringUtils.isEmpty(grade)) {
      if (!gradeMapByName.containsKey(grade)) {
        placementXLS.addErrorMessage(MULTIPLE_OR_NO_GRADES_FOUND_FOR + grade);
      } else {
        GradeDTO gradeDTO = gradeMapByName.get(grade);
        placementDTO.setGradeAbbreviation(gradeDTO.getAbbreviation());
        placementDTO.setGradeId(gradeDTO.getId());
      }
    }
  }
  private void setSiteOrRecordError(Map<String, SiteDTO> siteMapByName, PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO) {
    String site = placementXLS.getSite();
    if (!StringUtils.isEmpty(site)) {
      if (!siteMapByName.containsKey(site)) {
        placementXLS.addErrorMessage(MULTIPLE_OR_NO_SITES_FOUND_FOR + site);
      } else {
        SiteDTO siteDTO = siteMapByName.get(site);
        placementDTO.setSiteCode(siteDTO.getSiteCode());
        placementDTO.setSiteId(siteDTO.getId());
      }
    }
  }
  private void setWTEOrRecordError(PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO) {
    if (placementXLS.getWte() != null) {
      placementDTO.setWholeTimeEquivalent(new BigDecimal(placementXLS.getWte()));
    }
  }
  private void setPlacementTypeOrRecordError(PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO) {
    if (!StringUtils.isEmpty(placementXLS.getPlacementType())) {
      placementDTO.setPlacementType(placementXLS.getPlacementType());
    }
  }
  //TODO optimise these to be Fetcher like
  private Map<String, GradeDTO> getGradeDTOMap(List<PlacementUpdateXLS> placementXLSS) {
    Set<String> gradeNames = placementXLSS.stream()
        .filter(xls -> !StringUtils.isEmpty(xls.getGrade()))
        .map(PlacementUpdateXLS::getGrade)
        .collect(Collectors.toSet());
    Map<String, GradeDTO> gradeMapByName = new HashMap<>();
    for (String gradeName : gradeNames) {
      List<GradeDTO> gradesByName = referenceServiceImpl.findGradesByName(gradeName);
      if (!gradesByName.isEmpty() && gradesByName.size() == 1) {
        gradeMapByName.put(gradeName, gradesByName.get(0));
      } else {
        placementXLSS.stream().filter(placementXLS -> placementXLS.getGrade().equalsIgnoreCase(gradeName)).forEach(placementXLS -> {
          logger.error(String.format(EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR, gradeName));
          placementXLS.addErrorMessage(String.format(EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR, gradeName));
        });
      }
    }
    return gradeMapByName;
  }
  private Map<String, SiteDTO> getSiteDTOMap(List<PlacementUpdateXLS> placementXLSS) {
    Set<String> siteNames = placementXLSS.stream()
        .filter(xls -> !StringUtils.isEmpty(xls.getSite()))
        .map(PlacementUpdateXLS::getSite)
        .collect(Collectors.toSet());
    Map<String, SiteDTO> siteMapByName = new HashMap<>();
    for (String siteName : siteNames) {
      List<SiteDTO> sitesByName = referenceServiceImpl.findSitesByName(siteName);
      if (!sitesByName.isEmpty() && sitesByName.size() == 1) {
        siteMapByName.put(siteName, sitesByName.get(0));
      } else {
        placementXLSS.stream().filter(placementXLS -> placementXLS.getSite().equalsIgnoreCase(siteName)).forEach(placementXLS -> {
          logger.error(String.format(EXPECTED_TO_FIND_A_SINGLE_SITE_FOR, siteName));
          placementXLS.addErrorMessage(String.format(EXPECTED_TO_FIND_A_SINGLE_SITE_FOR, siteName));
        });
      }
    }
    return siteMapByName;
  }
}