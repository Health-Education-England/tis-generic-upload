package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static com.transformuk.hee.tis.genericupload.service.util.MultiValueUtil.splitMultiValueField;
import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementSupervisor;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementUpdateXLS;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PostFetcher;
import com.transformuk.hee.tis.genericupload.service.service.supervisor.PhnDTO;
import com.transformuk.hee.tis.genericupload.service.service.supervisor.RegNumberDTO;
import com.transformuk.hee.tis.genericupload.service.service.supervisor.RegNumberToDTOLookup;
import com.transformuk.hee.tis.genericupload.service.service.supervisor.RegNumberType;
import com.transformuk.hee.tis.genericupload.service.service.supervisor.SupervisorRegNumberIdService;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementCommentDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.CommentSource;
import com.transformuk.hee.tis.tcs.api.enumeration.LifecycleState;
import com.transformuk.hee.tis.tcs.api.enumeration.PlacementSiteType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

@Component
public class PlacementUpdateTransformerService {

  public static final String INTREPID_ID_IS_ALREADY_EXISTS_FOR_THIS_RECORD_AND_IT_CAN_NOT_BE_UPDATED = "INTREPID ID already exists for this record of placement and it can not be updated";
  public static final String CLINICAL_SUPERVISOR = "Clinical supervisor";
  public static final String EDUCATIONAL_SUPERVISOR = "Educational supervisor";
  private static final Logger logger = getLogger(PlacementUpdateTransformerService.class);
  private static final String MULTIPLE_POSTS_FOUND_FOR_NATIONAL_POST_NUMBER = "Multiple posts were found for National Post Number : ";
  private static final String COULD_NOT_FIND_POST_BY_NATIONAL_POST_NUMBER = "Could not find post by THIS National Post Number : ";
  private static final String POST_STATUS_IS_SET_TO_DELETE_FOR_NATIONAL_POST_NUMBER = "POST status is set to DELETE for National Post Number : ";
  private static final String DID_NOT_FIND_SPECIALTY_FOR_NAME = "Did not find specialty for name : ";
  private static final String FOUND_MULTIPLE_SPECIALTIES_FOR_NAME = "Found multiple specialties for name : ";
  private static final String MULTIPLE_OR_NO_GRADES_FOUND_FOR = "Multiple or no grades found for  : ";
  private static final String MULTIPLE_OR_NO_SITES_FOUND_FOR = "Multiple or no sites found for  : ";
  private static final String EXPECTED_A_PLACEMENT_GRADE_FOR = "Expected to find a placement grade for : %s";
  private static final String EXPECTED_TO_FIND_A_SINGLE_SITE_FOR = "Expected to find a single site for : %s";
  private static final String COULD_NOT_FIND_A_FOR_REGISTRATION_NUMBER = "Could not find a %1$s for Registration number : %s";
  private static final String IS_NOT_A_ROLE_FOR_PERSON_WITH_REGISTRATION_NUMBER = "%1$s is not a role for person with registration number : %2$s";
  private static final String DID_NOT_FIND_OTHER_SITE_FOR_NAME = "Did not find other site for name \"%s\".";
  private static final String FOUND_MULTIPLE_OTHER_SITES_FOR_NAME = "Found multiple other sites for name \"%s\".";
  private static final String DID_NOT_FIND_OTHER_SITE_IN_PARENT_POST_FOR_NAME = "Did not find other site in parent post for name \"%s\".";
  private static final String END_DATE_IS_SET_BEFORE_START_DATE = "End date cannot be set before start date";
  protected static final String NO_TWO_SPECIALTIES_CAN_HAVE_SAME_VALUE =
          "No two of primary/other/sub specialty(ies) can be set with the same value.";

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

  void processPlacementsUpdateUpload(List<PlacementUpdateXLS> placementUpdateXLSS,
      String username) {
    placementUpdateXLSS.forEach(PlacementUpdateXLS::initialiseSuccessfullyImported);
    List<PlacementSupervisor> placementSupervisorList = placementUpdateXLSS.stream()
        .collect(Collectors.toList());
    RegNumberToDTOLookup regNumberToDTOLookup = supervisorRegNumberIdService
        .getRegNumbersForSheetOrMarkAsError(placementSupervisorList);
    Set<String> placementNPNs = placementUpdateXLSS.stream()
        .map(PlacementUpdateXLS::getNationalPostNumber)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Map<String, PostDTO> postsMappedByNPNs =
        !placementNPNs.isEmpty() ? postFetcher.findWithKeys(placementNPNs)
            : new HashMap<>();//TODO filter posts CURRENT/INACTIVE
    Set<String> duplicateNPNKeys =
        !placementNPNs.isEmpty() ? postFetcher.getDuplicateKeys() : new HashSet<>();
    Map<String, SiteDTO> siteMapByName = getSiteDTOMap(placementUpdateXLSS);
    Map<String, GradeDTO> gradeMapByName = getGradeDTOMap(placementUpdateXLSS);
    for (PlacementUpdateXLS placementXLS : placementUpdateXLSS) {
      useMatchingCriteriaToUpdatePlacement(regNumberToDTOLookup, postsMappedByNPNs,
          duplicateNPNKeys, siteMapByName, gradeMapByName, placementXLS, username);
    }
  }

  private void useMatchingCriteriaToUpdatePlacement(RegNumberToDTOLookup regNumberToDTOLookup,
      Map<String, PostDTO> postsMappedByNPNs, Set<String> duplicateNPNKeys,
      Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
      PlacementUpdateXLS placementXLS, String username) {
    if (!StringUtils.isEmpty(placementXLS.getPlacementId())) {
      PlacementDetailsDTO dbPlacementDetailsDTO = tcsServiceImpl
          .getPlacementById(Long.valueOf(placementXLS.getPlacementId()));
      if (dbPlacementDetailsDTO != null) {
        updateIntrepidId(placementXLS, dbPlacementDetailsDTO);
        String nationalPostNumber = placementXLS.getNationalPostNumber();
        PostDTO postDTO = null;
        if (placementXLS.getNationalPostNumber() != null) {
          if (isNPNValid(placementXLS, nationalPostNumber, postsMappedByNPNs, duplicateNPNKeys)) {
            dbPlacementDetailsDTO.setNationalPostNumber(nationalPostNumber);
            postDTO = postsMappedByNPNs.get(nationalPostNumber);
            if (postDTO != null) {
              if ("DELETE".equalsIgnoreCase(postDTO.getStatus().toString())) {
                placementXLS.addErrorMessage(
                    POST_STATUS_IS_SET_TO_DELETE_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
              } else {
                updatePlacement(regNumberToDTOLookup, dbPlacementDetailsDTO, siteMapByName,
                    gradeMapByName, placementXLS, postDTO, username);
              }
            }
          }
        } else {
          updatePlacement(regNumberToDTOLookup, dbPlacementDetailsDTO, siteMapByName,
              gradeMapByName, placementXLS, postDTO, username);
        }
      }
    }
  }

  public void updateIntrepidId(PlacementUpdateXLS placementXLS,
      PlacementDetailsDTO dbPlacementDetailsDTO) {
    if (!StringUtils.isEmpty(placementXLS.getIntrepidId())) {
      if (!StringUtils.isEmpty(dbPlacementDetailsDTO.getIntrepidId())) {
        placementXLS.addErrorMessage(
            INTREPID_ID_IS_ALREADY_EXISTS_FOR_THIS_RECORD_AND_IT_CAN_NOT_BE_UPDATED);
      } else {
        dbPlacementDetailsDTO.setIntrepidId(placementXLS.getIntrepidId());
      }
    }
  }

  public boolean isNPNValid(PlacementUpdateXLS placementXLS, String nationalPostNumber,
      Map<String, PostDTO> postsMappedByNPNs, Set<String> duplicateNPNKeys) {
    if (!StringUtils.isEmpty(nationalPostNumber)) {
      if (duplicateNPNKeys.contains(nationalPostNumber)) {
        placementXLS
            .addErrorMessage(MULTIPLE_POSTS_FOUND_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
        return false;
      } else if (!postsMappedByNPNs.containsKey(nationalPostNumber)) {
        placementXLS
            .addErrorMessage(COULD_NOT_FIND_POST_BY_NATIONAL_POST_NUMBER + nationalPostNumber);
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if a grade is a valid placement grade.
   *
   * SIDE-EFFECT: if not valid, this is logged and the affected placement update XLS records have an
   * error message attached to them.
   *
   * @param placementXLSS   the list of placement update XLS records
   * @param gradeName       the grade to verify
   * @param placementGrades the list of valid placement grades
   * @return true if gradeName was a valid placement grade, false otherwise (note side-effect above)
   */
  public boolean isPlacementGradeValid(List<PlacementUpdateXLS> placementXLSS, String gradeName,
      List<String> placementGrades) {
    boolean gradeValid =
        placementGrades.stream().anyMatch(gradeName::equalsIgnoreCase);
    if (!gradeValid) {
      placementXLSS.stream()
          .filter(
              placementXLS -> Objects.toString(placementXLS.getGrade()).equalsIgnoreCase(gradeName))
          .forEach(placementXLS -> {
            logger.error(String.format(EXPECTED_A_PLACEMENT_GRADE_FOR, gradeName));
            placementXLS.addErrorMessage(String.format(EXPECTED_A_PLACEMENT_GRADE_FOR,
                gradeName));
          });
    }
    return gradeValid;
  }

  /**
   * Checks if the updated date range from the Excel document is acceptable.
   *
   * SIDE-EFFECT: if not valid, this is logged and the affected placement update XLS records have an
   * error message attached to them.
   * SIDE-EFFECT: the placementDTO is updated if the dates are acceptable.
   *
   * @param dbPlacementDetailsDTO the DTO of the placement
   * @param placementXLS          the placement update XLS record
   */
  public void validateDates(PlacementDetailsDTO dbPlacementDetailsDTO,
                             PlacementUpdateXLS placementXLS) {
    Date prevDateTo = java.sql.Date.valueOf(dbPlacementDetailsDTO.getDateTo());
    Date prevDateFrom = java.sql.Date.valueOf(dbPlacementDetailsDTO.getDateFrom());
    boolean dateError = true;

    if (placementXLS.getDateFrom() != null && placementXLS.getDateTo() != null) {
      if (placementXLS.getDateFrom().before(placementXLS.getDateTo())) {
        dbPlacementDetailsDTO.setDateFrom(convertDate(placementXLS.getDateFrom()));
        dbPlacementDetailsDTO.setDateTo(convertDate(placementXLS.getDateTo()));
        dateError = false;
      }
    } else if (placementXLS.getDateFrom() != null && placementXLS.getDateTo() == null) {
      if (placementXLS.getDateFrom().before(prevDateTo)) {
        dbPlacementDetailsDTO.setDateFrom(convertDate(placementXLS.getDateFrom()));
        dateError = false;
      }
    } else if (placementXLS.getDateTo() != null && placementXLS.getDateFrom() == null) {
      if (placementXLS.getDateTo().after(prevDateFrom)) {
        dbPlacementDetailsDTO.setDateTo(convertDate(placementXLS.getDateTo()));
        dateError = false;
      }
    } else if (placementXLS.getDateFrom() == null && placementXLS.getDateTo() == null) {
      dateError = false;
    }
    if (dateError) {
      placementXLS.addErrorMessage(END_DATE_IS_SET_BEFORE_START_DATE);
    }
  }

  private void updatePlacement(RegNumberToDTOLookup regNumberToDTOLookup,
      PlacementDetailsDTO dbPlacementDetailsDTO,
      Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
      PlacementUpdateXLS placementXLS, PostDTO postDTO, String username) {

    validateDates(dbPlacementDetailsDTO, placementXLS);

    setOtherMandatoryFields(siteMapByName, gradeMapByName, placementXLS, dbPlacementDetailsDTO);
    setSpecialties(placementXLS, dbPlacementDetailsDTO,
        tcsServiceImpl::getSpecialtyByName); //NOTE : specialties won't have a placement Id here
    setOtherSites(placementXLS, dbPlacementDetailsDTO, referenceServiceImpl::findSitesByName,
        postDTO);
    // and relies on the api to assign the Id
    Set<String> clinicalSupervisorRoles = referenceServiceImpl.getRolesByCategory(1L).stream()
        .map(roleDTO -> roleDTO.getCode().toLowerCase().trim())
        .collect(Collectors.toSet());
    Set<String> educationalSupervisorRoles = referenceServiceImpl.getRolesByCategory(2L).stream()
        .map(roleDTO -> roleDTO.getCode().toLowerCase().trim())
        .collect(Collectors.toSet());
    addSupervisorsToPlacement(placementXLS, dbPlacementDetailsDTO, regNumberToDTOLookup,
        clinicalSupervisorRoles, educationalSupervisorRoles);
    // update the post
    if (postDTO != null) {
      dbPlacementDetailsDTO.setPostId(postDTO.getId());
    }
    if (!placementXLS.hasErrors()) {
      dbPlacementDetailsDTO.setLifecycleState(LifecycleState.APPROVED);
      setCommentInPlacementDTO(dbPlacementDetailsDTO, placementXLS, username);
      logger.info("dbPlacementDetailsDTO => {}", dbPlacementDetailsDTO);
      try {
        tcsServiceImpl.updatePlacement(dbPlacementDetailsDTO);
        placementXLS.setSuccessfullyImported(true);
      } catch (ResourceAccessException rae) {
        new ErrorHandler().recordErrorMessageOnTemplateOrLogUnknown(placementXLS, rae);
      }
    }
  }

  private void setCommentInPlacementDTO(PlacementDetailsDTO placementDTO,
      PlacementUpdateXLS placementXLS, String username) {
    if (!StringUtils.isEmpty(placementXLS.getComments())) {
      HashSet<PlacementCommentDTO> comments = new HashSet<>();
      PlacementCommentDTO placementCommentDTO = new PlacementCommentDTO();
      placementCommentDTO.setBody(placementXLS.getComments());
      placementCommentDTO.setAuthor(username);
      placementCommentDTO.setSource(CommentSource.GENERIC_UPLOAD);
      comments.add(placementCommentDTO);
      placementDTO.setComments(comments);
    }
  }

  private void addSupervisorsToPlacement(PlacementUpdateXLS placementXLS,
      PlacementDetailsDTO placementDTO, RegNumberToDTOLookup regNumberToDTOLookup,
      Set<String> clinicalSupervisorRoles, Set<String> educationalSupervisorRoles) {
    addSupervisorToPlacement(placementXLS, placementDTO, regNumberToDTOLookup,
        regNumberToDTOLookup::getDTOForClinicalSupervisor,
        PlacementUpdateXLS::getClinicalSupervisor, CLINICAL_SUPERVISOR, clinicalSupervisorRoles);
    addSupervisorToPlacement(placementXLS, placementDTO, regNumberToDTOLookup,
        regNumberToDTOLookup::getDTOForEducationalSupervisor,
        PlacementUpdateXLS::getEducationalSupervisor, EDUCATIONAL_SUPERVISOR,
        educationalSupervisorRoles);
  }

  private void addSupervisorToPlacement(PlacementUpdateXLS placementXLS,
      PlacementDetailsDTO placementDTO, RegNumberToDTOLookup regNumberToDTOLookup,
      Function<String, Optional<RegNumberDTO>> getDTOForRegNumber,
      Function<PlacementUpdateXLS, String> getSupervisor, String supervisorType,
      Set<String> supervisorRoles) {
    if (!StringUtils.isEmpty(getSupervisor.apply(placementXLS))) {
      Optional<RegNumberDTO> dtoForSupervisor = getDTOForRegNumber
          .apply(getSupervisor.apply(placementXLS));
      if (dtoForSupervisor.isPresent()) {
        RegNumberDTO regNumberDTO = dtoForSupervisor.get();
        PersonDTO personDTO = regNumberDTO.getRegNumberType() == RegNumberType.PH
            ? ((PhnDTO) regNumberDTO).getRegNumberDTO()
            : regNumberToDTOLookup.getPersonDetailsMapForSupervisorsByGmcAndGdc()
                .get(regNumberDTO.getId());
        if (!PlacementTransformerService.supervisorHasRole(personDTO, supervisorRoles)) {
          placementXLS.addErrorMessage(String
              .format(IS_NOT_A_ROLE_FOR_PERSON_WITH_REGISTRATION_NUMBER, supervisorType,
                  getSupervisor.apply(placementXLS)));
        } else {
          PlacementTransformerService
              .addNewSupervisorToPlacement(placementDTO, supervisorType, regNumberDTO);
        }
      } else {
        placementXLS.addErrorMessage(String
            .format(COULD_NOT_FIND_A_FOR_REGISTRATION_NUMBER, supervisorType,
                getSupervisor.apply(placementXLS)));
      }
    }
  }

  public void setSpecialties(PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO,
      Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName) {
    Set<PlacementSpecialtyDTO> placementSpecialtyDTOS = placementDTO.getSpecialties();
    // If the primary specialty is populated in the template,
    // clean the existing specialty/other specialties/sub specialty.
    if (placementSpecialtyDTOS == null || !StringUtils.isEmpty(placementXLS.getSpecialty1())) {
      placementSpecialtyDTOS = initialiseNewPlacementSpecialtyDTOS(placementDTO);
    }
    // Primary Specialty
    Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional1 = buildPlacementSpecialtyDTO(
        placementXLS, placementDTO, getSpecialtyDTOsForName, placementXLS.getSpecialty1(),
        PostSpecialtyType.PRIMARY);
    if (placementSpecialtyDTOOptional1.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional1.get();
      addDTOIfNotPresentAsSubSpecialty(placementSpecialtyDTOS, placementSpecialtyDTO, placementXLS);
    }
    // Other specialties
    Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional2 = buildPlacementSpecialtyDTO(
        placementXLS, placementDTO, getSpecialtyDTOsForName, placementXLS.getSpecialty2(),
        PostSpecialtyType.OTHER);
    if (placementSpecialtyDTOOptional2.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional2.get();
      addDTOIfNotPresentAsSubSpecialty(placementSpecialtyDTOS, placementSpecialtyDTO, placementXLS);
    }
    Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional3 = buildPlacementSpecialtyDTO(
        placementXLS, placementDTO, getSpecialtyDTOsForName, placementXLS.getSpecialty3(),
        PostSpecialtyType.OTHER);
    if (placementSpecialtyDTOOptional3.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional3.get();
      addDTOIfNotPresentAsSubSpecialty(placementSpecialtyDTOS, placementSpecialtyDTO, placementXLS);
    }
    // Sub specialty
    Optional<PlacementSpecialtyDTO> placementSubSpecialtyDtoOptional = buildPlacementSpecialtyDTO(
        placementXLS, placementDTO, getSpecialtyDTOsForName, placementXLS.getSubSpecialty(),
        PostSpecialtyType.SUB_SPECIALTY);
    if (placementSubSpecialtyDtoOptional.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDto = placementSubSpecialtyDtoOptional.get();
      addDTOIfNotPresentAsSubSpecialty(placementSpecialtyDTOS, placementSpecialtyDto, placementXLS);
    }
  }

  public Set<PlacementSpecialtyDTO> initialiseNewPlacementSpecialtyDTOS(
      PlacementDetailsDTO placementDTO) {
    Set<PlacementSpecialtyDTO> placementSpecialtyDTOS = new HashSet<>();
    placementDTO.setSpecialties(placementSpecialtyDTOS);
    return placementSpecialtyDTOS;
  }

  void addDTOIfNotPresentAsSubSpecialty(Set<PlacementSpecialtyDTO> placementSpecialtyDtos,
                                               PlacementSpecialtyDTO placementSpecialtyDto,
                                               PlacementUpdateXLS placementXls) {
    if (placementSpecialtyDto.getPlacementSpecialtyType().equals(PostSpecialtyType.SUB_SPECIALTY)) {
      placementSpecialtyDtos.removeIf(
              ps -> ps.getPlacementSpecialtyType().equals(PostSpecialtyType.SUB_SPECIALTY));
    }

    if (placementSpecialtyDtos.contains(placementSpecialtyDto))  {
      placementXls.addErrorMessage(NO_TWO_SPECIALTIES_CAN_HAVE_SAME_VALUE);
    }
    placementSpecialtyDtos.add(placementSpecialtyDto);
  }

  public Optional<PlacementSpecialtyDTO> buildPlacementSpecialtyDTO(PlacementUpdateXLS placementXLS,
      PlacementDetailsDTO placementDTO,
      Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName,
      String specialtyName, PostSpecialtyType postSpecialtyType) {
    Optional<SpecialtyDTO> aSingleValidSpecialty = getASingleValidSpecialtyFromTheReferenceService(
        placementXLS, getSpecialtyDTOsForName, specialtyName);
    if (aSingleValidSpecialty.isPresent()) {
      SpecialtyDTO specialtyDTO = aSingleValidSpecialty.get();
      PlacementSpecialtyDTO placementSpecialtyDTO = new PlacementSpecialtyDTO();
      placementSpecialtyDTO.setPlacementId(placementDTO.getId());
      placementSpecialtyDTO.setSpecialtyId(specialtyDTO.getId());
      placementSpecialtyDTO.setSpecialtyName(specialtyName);
      placementSpecialtyDTO.setPlacementSpecialtyType(postSpecialtyType);
      return Optional.of(placementSpecialtyDTO);
    }
    return Optional.empty();
  }

  private Optional<SpecialtyDTO> getASingleValidSpecialtyFromTheReferenceService(
      PlacementUpdateXLS placementXLS, Function<String,
      List<SpecialtyDTO>> getSpecialtyDTOsForName, String specialtyName) {
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

  private void setGradeOrRecordError(Map<String, GradeDTO> gradeMapByName,
      PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO) {
    String grade = placementXLS.getGrade();
    if (!StringUtils.isEmpty(grade)) {
      if (!gradeMapByName.containsKey(grade)) {
        placementXLS.addErrorMessage(MULTIPLE_OR_NO_GRADES_FOUND_FOR + grade);
      } else {
        GradeDTO gradeDTO = gradeMapByName.get(grade);
        placementDTO.setGradeAbbreviation(gradeDTO.getAbbreviation());
        placementDTO.setGradeId(gradeDTO.getId());
      }
    }
  }

  private void setSiteOrRecordError(Map<String, SiteDTO> siteMapByName,
      PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO) {
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

  private void setWTEOrRecordError(PlacementUpdateXLS placementXLS,
      PlacementDetailsDTO placementDTO) {
    if (placementXLS.getWte() != null) {
      placementDTO.setWholeTimeEquivalent(new BigDecimal(placementXLS.getWte().toString()));
    }
  }

  private void setPlacementTypeOrRecordError(PlacementUpdateXLS placementXLS,
      PlacementDetailsDTO placementDTO) {
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
    List<String> gradesValidForPlacements = referenceServiceImpl
        .findGradesCurrentPlacementAndTrainingGrades().stream().map(GradeDTO::getName)
        .collect(Collectors.toList());
    for (String gradeName : gradeNames) {
      List<GradeDTO> gradesByName = referenceServiceImpl.findGradesByName(gradeName);
      if (isPlacementGradeValid(placementXLSS, gradeName, gradesValidForPlacements)) {
        gradeMapByName.put(gradeName, gradesByName.get(0));
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
        placementXLSS.stream()
            .filter(placementXLS -> placementXLS.getSite().equalsIgnoreCase(siteName))
            .forEach(placementXLS -> {
              logger.error(String.format(EXPECTED_TO_FIND_A_SINGLE_SITE_FOR, siteName));
              placementXLS
                  .addErrorMessage(String.format(EXPECTED_TO_FIND_A_SINGLE_SITE_FOR, siteName));
            });
      }
    }
    return siteMapByName;
  }

  // ***** Other Sites *****
  void setOtherSites(PlacementUpdateXLS placementXLS, PlacementDetailsDTO placementDTO,
      Function<String, List<SiteDTO>> getSiteDTOsForName, PostDTO postDTO) {
    Set<PlacementSiteDTO> placementSiteDTOS = placementDTO.getSites();
    if (placementSiteDTOS == null) {
      placementSiteDTOS = initialiseNewPlacementSiteDTOS(placementDTO);
    }
    String otherSitesStr = placementXLS.getOtherSites();
    if (otherSitesStr != null) {
      List<String> otherSites = splitMultiValueField(otherSitesStr);
      List<PlacementSiteDTO> newPlacementSiteDtos = new ArrayList<>();
      for (String otherSite : otherSites) {
        Optional<PlacementSiteDTO> placementSiteDTOOptional2 = buildPlacementSiteDTO(placementXLS,
            placementDTO, getSiteDTOsForName, otherSite, PlacementSiteType.OTHER, postDTO);
        if (placementSiteDTOOptional2.isPresent()) {
          newPlacementSiteDtos.add(placementSiteDTOOptional2.get());
        }
      }
      updatePlacementSitesForPlacementDto(placementSiteDTOS, newPlacementSiteDtos);
    }
  }

  private void updatePlacementSitesForPlacementDto(Set<PlacementSiteDTO> placementSiteDtos,
      List<PlacementSiteDTO> newPlacementSiteDtos) {
    if (newPlacementSiteDtos.isEmpty()) {
      return;
    }
    // all the placementSites are of type OTHER
    if (!placementSiteDtos.isEmpty()) {
      placementSiteDtos.removeIf(ps -> ps.getPlacementSiteType() == PlacementSiteType.OTHER);
    }
    placementSiteDtos.addAll(newPlacementSiteDtos);
  }

  private Set<PlacementSiteDTO> initialiseNewPlacementSiteDTOS(PlacementDetailsDTO placementDTO) {
    Set<PlacementSiteDTO> placmentSiteDTOS = new HashSet<>();
    placementDTO.setSites(placmentSiteDTOS);
    return placmentSiteDTOS;
  }

  private Optional<PlacementSiteDTO> buildPlacementSiteDTO(PlacementUpdateXLS placementXLS,
      PlacementDetailsDTO placementDTO,
      Function<String, List<SiteDTO>> getSiteDTOsForName,
      String siteName, PlacementSiteType siteType, PostDTO postDTO) {
    Optional<SiteDTO> aSingleValidSite = getASingleValidSiteFromTheReferenceService(placementXLS,
        getSiteDTOsForName, siteName, postDTO, placementDTO);
    if (aSingleValidSite.isPresent()) {
      SiteDTO siteDTO = aSingleValidSite.get();
      PlacementSiteDTO placementSiteDTO = new PlacementSiteDTO(placementDTO.getId(),
          siteDTO.getId(), siteType);
      return Optional.of(placementSiteDTO);
    }
    return Optional.empty();
  }

  private Optional<SiteDTO> getASingleValidSiteFromTheReferenceService(
      PlacementUpdateXLS placementXLS, Function<String,
      List<SiteDTO>> getSiteDTOsForName, String siteName, PostDTO postDTO,
      PlacementDetailsDTO placementDTO) {
    if (postDTO == null) {
      // get postDTO by postId
      long postId = placementDTO.getPostId();
      postDTO = tcsServiceImpl.getPostById(postId);
    }
    if (!StringUtils.isEmpty(siteName)) {
      List<SiteDTO> siteByName = getSiteDTOsForName.apply(siteName);
      if (siteByName != null) {
        siteByName = siteByName.stream().filter(site -> site.getStatus() == Status.CURRENT)
            .collect(Collectors.toList());
        if (siteByName.size() == 1) {
          // identify if the siteId exists in parent Post
          Set<PostSiteDTO> parentPostSites = postDTO.getSites();
          long siteId = siteByName.get(0).getId();
          long count = parentPostSites.stream()
              .filter(s -> s.getSiteId() == siteId)
              .count();
          if (count <= 0) {
            placementXLS.addErrorMessage(
                String.format(DID_NOT_FIND_OTHER_SITE_IN_PARENT_POST_FOR_NAME, siteName));
          } else {
            return Optional.of(siteByName.get(0));
          }
        } else {
          String errorMessage = siteByName.isEmpty() ? DID_NOT_FIND_OTHER_SITE_FOR_NAME
              : FOUND_MULTIPLE_OTHER_SITES_FOR_NAME;
          placementXLS.addErrorMessage(String.format(errorMessage, siteName));
        }
      }
    }
    return Optional.empty();
  }
}