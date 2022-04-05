package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static com.transformuk.hee.tis.genericupload.service.util.MultiValueUtil.splitMultiValueField;
import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementSupervisor;
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
import com.transformuk.hee.tis.reference.api.enums.Status;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonBasicDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonLiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementCommentDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSupervisorDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.CommentSource;
import com.transformuk.hee.tis.tcs.api.enumeration.LifecycleState;
import com.transformuk.hee.tis.tcs.api.enumeration.PlacementSiteType;
import com.transformuk.hee.tis.tcs.api.enumeration.PlacementStatus;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.math.BigDecimal;
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
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

@Component
public class PlacementTransformerService {

  public static final String CLINICAL_SUPERVISOR = "Clinical supervisor";
  public static final String EDUCATIONAL_SUPERVISOR = "Educational supervisor";
  private static final Logger logger = getLogger(PlacementTransformerService.class);
  private static final String AT_LEAST_ONE_OF_THE_3_REGISTRATION_NUMBERS_SHOULD_BE_PROVIDED_TO_IDENTIFY_A_PERSON = "At least one of the 3 registration numbers should be provided to identify a person";
  private static final String SURNAME_DOES_NOT_MATCH_LAST_NAME_OBTAINED_VIA_REGISTRATION_NUMBER = "Surname does not match last name obtained via registration number";
  private static final String NATIONAL_POST_NUMBER_IS_MANDATORY = "National Post number is mandatory";
  private static final String MULTIPLE_POSTS_FOUND_FOR_NATIONAL_POST_NUMBER = "Multiple posts found for National Post Number : ";
  private static final String COULD_NOT_FIND_POST_BY_NATIONAL_POST_NUMBER = "Could not find post by National Post Number : ";
  private static final String POST_STATUS_IS_SET_TO_DELETE_FOR_NATIONAL_POST_NUMBER = "POST status is set to DELETE for National Post Number : ";
  private static final String DID_NOT_FIND_A_PERSON_FOR_REGISTRATION_NUMBER = "Did not find a person for registration number : ";
  private static final String SPECIALTY1_IS_MANDATORY = "Specialty1 field is required";
  private static final String DID_NOT_FIND_SPECIALTY_FOR_NAME = "Did not find specialty for name : ";
  private static final String FOUND_MULTIPLE_SPECIALTIES_FOR_NAME = "Found multiple specialties for name : ";
  private static final String PLACEMENT_FROM_DATE_IS_MANDATORY = "Placement from date is mandatory";
  private static final String PLACEMENT_TO_DATE_IS_MANDATORY = "Placement to date is mandatory";
  private static final String MULTIPLE_OR_NO_GRADES_FOUND_FOR = "Multiple or no grades found for  : ";
  private static final String MULTIPLE_OR_NO_SITES_FOUND_FOR = "Multiple or no sites found for  : ";
  private static final String WHOLE_TIME_EQUIVALENT_WTE_IS_MANDATORY = "Whole Time Equivalent (WTE) is mandatory";
  private static final String PLACEMENT_TYPE_IS_MANDATORY = "Placement Type is mandatory";
  private static final String EXPECTED_A_PLACEMENT_GRADE_FOR = "Expected to find a placement grade for : %s";
  private static final String EXPECTED_TO_FIND_A_SINGLE_SITE_FOR = "Expected to find a single site for : %s";
  private static final String COULD_NOT_FIND_A_FOR_REGISTRATION_NUMBER = "Could not find a %1$s for registration number : %s";
  private static final String IS_NOT_A_ROLE_FOR_PERSON_WITH_REGISTRATION_NUMBER = "%1$s is not a role for person with registration number : %2$s";
  private static final String DID_NOT_FIND_OTHER_SITE_FOR_NAME = "Did not find other site for name \"%s\".";
  private static final String FOUND_MULTIPLE_OTHER_SITES_FOR_NAME = "Found multiple other sites for name \"%s\".";
  private static final String DID_NOT_FIND_OTHER_SITE_IN_PARENT_POST_FOR_NAME = "Did not find other site in parent post for name \"%s\".";
  private static final String END_DATE_IS_SET_BEFORE_START_DATE = "End date cannot be set before start date";
  protected static final String NO_TWO_SPECIALTIES_CAN_HAVE_SAME_VALUE =
          "No two of primary/other/sub specialty(ies) can be set with the same value.";

  Function<PlacementXLS, String> getPhNumber = PlacementXLS::getPublicHealthNumber;
  Function<PlacementXLS, String> getGdcNumber = PlacementXLS::getGdcNumber;
  Function<PlacementXLS, String> getGmcNumber = PlacementXLS::getGmcNumber;
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

  public static void addNewSupervisorToPlacement(PlacementDetailsDTO placementDTO,
      String supervisorType, RegNumberDTO regNumberDTO) {
    PersonLiteDTO personLiteDTO = new PersonLiteDTO();
    personLiteDTO.setId(regNumberDTO.getId());
    PlacementSupervisorDTO placementSupervisorDTO = new PlacementSupervisorDTO();
    placementSupervisorDTO.setPerson(personLiteDTO);
    switch (supervisorType) {
      case CLINICAL_SUPERVISOR:
        placementSupervisorDTO.setType(1);
        break;
      case EDUCATIONAL_SUPERVISOR:
        placementSupervisorDTO.setType(2);
        break;
      default:
        break;
    }
    if (placementDTO.getSupervisors() == null) {
      placementDTO.setSupervisors(new HashSet<>());
    }
    placementDTO.getSupervisors().add(placementSupervisorDTO);
  }

  public static boolean supervisorHasRole(PersonDTO personDTO, Set<String> supervisorRoles) {
    if (StringUtils.isEmpty(personDTO.getRole())) {
      return false;
    }
    Set<String> supervisorRolesAssignedToPerson = new HashSet<>(
        Arrays.asList(personDTO.getRole().split(",")));
    return supervisorRolesAssignedToPerson.stream()
        .anyMatch(roleAssignedToPerson -> supervisorRoles
            .contains(roleAssignedToPerson.toLowerCase().trim()));
  }

  public static void setGradeOrRecordError(Map<String, GradeDTO> gradeMapByName,
      PlacementXLS placementXLS, PlacementDetailsDTO placementDTO) {
    String grade = placementXLS.getGrade();
    if (!StringUtils.isEmpty(grade) && !gradeMapByName.containsKey(grade)) {
      placementXLS.addErrorMessage(MULTIPLE_OR_NO_GRADES_FOUND_FOR + grade);
    } else {
      GradeDTO gradeDTO = gradeMapByName.get(grade);
      placementDTO.setGradeAbbreviation(gradeDTO.getAbbreviation());
      placementDTO.setGradeId(gradeDTO.getId());
    }
  }

  @PostConstruct
  public void initialiseFetchers() {
    this.gmcDtoFetcher = new GMCDTOFetcher(tcsServiceImpl);
    this.gdcDtoFetcher = new GDCDTOFetcher(tcsServiceImpl);
    this.pbdDtoFetcher = new PersonBasicDetailsDTOFetcher(tcsServiceImpl);
    this.peopleByPHNFetcher = new PeopleByPHNFetcher(tcsServiceImpl);
    this.postFetcher = new PostFetcher(tcsServiceImpl);
  }

  <DTO> Map<String, DTO> buildRegNumberDetailsMap(List<PlacementXLS> placementXLSS,
      Function<PlacementXLS, String> getRegNumberFunction, DTOFetcher<String, DTO> fetcher) {
    return fetcher.findWithKeys(
        collectRegNumbersForPlacements(
            getRowsWithRegistrationNumberForPlacements(placementXLSS, getRegNumberFunction),
            getRegNumberFunction));
  }

  <DTO> Map<Long, PersonBasicDetailsDTO> buildPersonBasicDetailsMapForRegNumber(
      Map<String, DTO> regNumberMap, DTOFetcher<String, DTO> idExtractingFetcher,
      Function<DTO, Long> getId) {
    return regNumberMap.isEmpty() ? null
        : pbdDtoFetcher.findWithKeys(idExtractingFetcher.extractIds(regNumberMap, getId));
  }

  void processPlacementsUpload(List<PlacementXLS> placementXLSS, String username) {
    placementXLSS.forEach(PlacementXLS::initialiseSuccessfullyImported);
    List<PlacementSupervisor> placementSupervisorList = placementXLSS.stream()
        .collect(Collectors.toList());
    RegNumberToDTOLookup regNumberToDTOLookup = supervisorRegNumberIdService
        .getRegNumbersForSheetOrMarkAsError(placementSupervisorList);
    if (!CollectionUtils.isEmpty(placementXLSS)) {
      Map<String, PersonDTO> phnDetailsMap = buildRegNumberDetailsMap(placementXLSS, getPhNumber,
          peopleByPHNFetcher);
      Map<Long, PersonBasicDetailsDTO> pbdMapByPH = buildPersonBasicDetailsMapForRegNumber(
          phnDetailsMap, peopleByPHNFetcher, PersonDTO::getId);
      Map<String, GdcDetailsDTO> gdcDetailsMap = buildRegNumberDetailsMap(placementXLSS,
          getGdcNumber, gdcDtoFetcher);
      Map<Long, PersonBasicDetailsDTO> pbdMapByGDC = buildPersonBasicDetailsMapForRegNumber(
          gdcDetailsMap, gdcDtoFetcher, GdcDetailsDTO::getId);
      Map<String, GmcDetailsDTO> gmcDetailsMap = buildRegNumberDetailsMap(placementXLSS,
          getGmcNumber, gmcDtoFetcher);
      Map<Long, PersonBasicDetailsDTO> pbdMapByGMC = buildPersonBasicDetailsMapForRegNumber(
          gmcDetailsMap, gmcDtoFetcher, GmcDetailsDTO::getId);
      Set<String> placementNPNs = placementXLSS.stream()
          .map(PlacementXLS::getNationalPostNumber)
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
      Map<String, PostDTO> postsMappedByNPNs =
          !placementNPNs.isEmpty() ? postFetcher.findWithKeys(placementNPNs)
              : new HashMap<>();//TODO filter posts CURRENT/INACTIVE
      Set<String> duplicateNPNKeys =
          !placementNPNs.isEmpty() ? postFetcher.getDuplicateKeys() : new HashSet<>();
      Map<String, SiteDTO> siteMapByName = getSiteDTOMap(placementXLSS);
      Map<String, GradeDTO> gradeMapByName = getGradeDTOMap(placementXLSS);
      for (PlacementXLS placementXLS : placementXLSS) {
        useMatchingCriteriaToUpdatePlacement(regNumberToDTOLookup, phnDetailsMap, pbdMapByPH,
            gdcDetailsMap, pbdMapByGDC, gmcDetailsMap, pbdMapByGMC, postsMappedByNPNs,
            duplicateNPNKeys, siteMapByName, gradeMapByName, placementXLS, username);
      }
    }
  }

  private void useMatchingCriteriaToUpdatePlacement(RegNumberToDTOLookup regNumberToDTOLookup,
      Map<String, PersonDTO> phnDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByPH,
      Map<String, GdcDetailsDTO> gdcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGDC,
      Map<String, GmcDetailsDTO> gmcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGMC,
      Map<String, PostDTO> postsMappedByNPNs, Set<String> duplicateNPNKeys,
      Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
      PlacementXLS placementXLS, String username) {
    Optional<PersonBasicDetailsDTO> personBasicDetailsDTOOptional = getPersonBasicDetailsDTOFromRegNumber(
        phnDetailsMap, pbdMapByPH, gdcDetailsMap, pbdMapByGDC, gmcDetailsMap, pbdMapByGMC,
        placementXLS);
    if (personBasicDetailsDTOOptional.isPresent() && checkSpecialty1ExistsOrRecordError(
        placementXLS)) {
      PersonBasicDetailsDTO personBasicDetailsDTO = personBasicDetailsDTOOptional.get();
      if (!placementXLS.getSurname().equalsIgnoreCase(personBasicDetailsDTO.getLastName())) {
        placementXLS
            .addErrorMessage(SURNAME_DOES_NOT_MATCH_LAST_NAME_OBTAINED_VIA_REGISTRATION_NUMBER);
      }
      String nationalPostNumber = placementXLS.getNationalPostNumber();
      if (isNPNValid(placementXLS, nationalPostNumber, postsMappedByNPNs, duplicateNPNKeys)) {
        PostDTO postDTO = postsMappedByNPNs.get(nationalPostNumber);
        if (postDTO != null) {
          if ("DELETE".equalsIgnoreCase(postDTO.getStatus().toString())) {
            placementXLS.addErrorMessage(
                POST_STATUS_IS_SET_TO_DELETE_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
          } else {
            updatePlacement(regNumberToDTOLookup, siteMapByName, gradeMapByName, placementXLS,
                personBasicDetailsDTO, postDTO, username);
          }
        }
      }
    }
  }

  public boolean isNPNValid(PlacementXLS placementXLS, String nationalPostNumber,
      Map<String, PostDTO> postsMappedByNPNs, Set<String> duplicateNPNKeys) {
    if (nationalPostNumber == null) {
      placementXLS.addErrorMessage(NATIONAL_POST_NUMBER_IS_MANDATORY);
      return false;
    } else if (duplicateNPNKeys.contains(nationalPostNumber)) {
      placementXLS
          .addErrorMessage(MULTIPLE_POSTS_FOUND_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
      return false;
    } else if (!postsMappedByNPNs.containsKey(nationalPostNumber)) {
      placementXLS
          .addErrorMessage(COULD_NOT_FIND_POST_BY_NATIONAL_POST_NUMBER + nationalPostNumber);
      return false;
    } else {
      return true;
    }
  }


  /**
   * Checks if a grade is a valid placement grade.
   *
   * SIDE-EFFECT: if not valid, this is logged and the affected placement XLS records have an error
   * message attached to them.
   *
   * @param placementXLSS   the list of placement XLS records
   * @param gradeName       the grade to verify
   * @param placementGrades the list of valid placement grades
   * @return true if gradeName was a valid placement grade, false otherwise (note side-effect above)
   */
  public boolean isPlacementGradeValid(List<PlacementXLS> placementXLSS, String gradeName,
      List<String> placementGrades) {
    boolean gradeValid =
        placementGrades.stream().anyMatch(gradeName::equalsIgnoreCase);
    if (!gradeValid) {
      placementXLSS.stream()
          .filter(placementXLS -> placementXLS.getGrade().equalsIgnoreCase(gradeName))
          .forEach(placementXLS -> {
            logger.error(String.format(EXPECTED_A_PLACEMENT_GRADE_FOR, gradeName));
            placementXLS.addErrorMessage(String.format(EXPECTED_A_PLACEMENT_GRADE_FOR,
                gradeName));
          });
    }
    return gradeValid;
  }

  private Optional<PersonBasicDetailsDTO> getPersonBasicDetailsDTOFromRegNumber(
      Map<String, PersonDTO> phnDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByPH,
      Map<String, GdcDetailsDTO> gdcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGDC,
      Map<String, GmcDetailsDTO> gmcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGMC,
      PlacementXLS placementXLS) {
    if (!StringUtils.isEmpty(getGdcNumber.apply(placementXLS))) {
      return getPersonBasicDetailsDTO(getGdcNumber, gdcDetailsMap, pbdMapByGDC, placementXLS,
          GdcDetailsDTO::getId);
    } else if (!StringUtils.isEmpty(getGmcNumber.apply(placementXLS))) {
      return getPersonBasicDetailsDTO(getGmcNumber, gmcDetailsMap, pbdMapByGMC, placementXLS,
          GmcDetailsDTO::getId);
    } else if (!StringUtils.isEmpty(getPhNumber.apply(placementXLS))) {
      return getPersonBasicDetailsDTO(getPhNumber, phnDetailsMap, pbdMapByPH, placementXLS,
          PersonDTO::getId);
    } else {
      placementXLS.addErrorMessage(
          AT_LEAST_ONE_OF_THE_3_REGISTRATION_NUMBERS_SHOULD_BE_PROVIDED_TO_IDENTIFY_A_PERSON);
      return Optional.empty();
    }
  }

  private void updatePlacement(RegNumberToDTOLookup regNumberToDTOLookup,
      Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
      PlacementXLS placementXLS, PersonBasicDetailsDTO personBasicDetailsDTO, PostDTO postDTO,
      String username) {
    if (datesAreValid(placementXLS)) {
      List<PlacementDetailsDTO> placementsByPostIdAndPersonId = tcsServiceImpl
          .getPlacementsByPostIdAndPersonId(postDTO.getId(), personBasicDetailsDTO.getId());
      LocalDate dateFrom = convertDate(placementXLS.getDateFrom());
      LocalDate dateTo = convertDate(placementXLS.getDateTo());
      boolean existingPlacementUpdatedOrDeleted = false;
      if (!placementsByPostIdAndPersonId.isEmpty()) {
        existingPlacementUpdatedOrDeleted = updateOrDeleteExistingPlacement(regNumberToDTOLookup,
            siteMapByName, gradeMapByName, placementXLS, placementsByPostIdAndPersonId, dateFrom,
            dateTo, existingPlacementUpdatedOrDeleted, username, postDTO);
      }
      if (placementsByPostIdAndPersonId.isEmpty() || !existingPlacementUpdatedOrDeleted) {
        PlacementDetailsDTO placementDTO = new PlacementDetailsDTO();
        placementDTO.setNationalPostNumber(postDTO.getNationalPostNumber());
        placementDTO.setTraineeId(personBasicDetailsDTO.getId());
        placementDTO.setPostId(postDTO.getId());
        placementDTO.setDateFrom(dateFrom);
        placementDTO.setDateTo(dateTo);
        saveOrUpdatePlacement(siteMapByName, gradeMapByName, placementXLS, placementDTO,
            regNumberToDTOLookup, false, username, postDTO);
      }
    }
  }

  private boolean updateOrDeleteExistingPlacement(RegNumberToDTOLookup regNumberToDTOLookup,
      Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
      PlacementXLS placementXLS, List<PlacementDetailsDTO> placementsByPostIdAndPersonId,
      LocalDate dateFrom, LocalDate dateTo, boolean existingPlacementUpdatedOrDeleted,
      String username, PostDTO postDTO) {
    for (PlacementDetailsDTO placementDTO : placementsByPostIdAndPersonId) {
      if (dateFrom.equals(placementDTO.getDateFrom()) && dateTo.equals(placementDTO.getDateTo())) {
        if ("DELETE".equalsIgnoreCase(placementXLS.getPlacementStatus())) {
          tcsServiceImpl.deletePlacement(placementDTO.getId());
          placementXLS.setSuccessfullyImported(true);
        } else {
          saveOrUpdatePlacement(siteMapByName, gradeMapByName, placementXLS, placementDTO,
              regNumberToDTOLookup, true, username, postDTO);
        }
        existingPlacementUpdatedOrDeleted = true;
        break;
      }
    }
    return existingPlacementUpdatedOrDeleted;
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
    } else if (placementXLS.getDateFrom().after(placementXLS.getDateTo())) {
      placementXLS.addErrorMessage(END_DATE_IS_SET_BEFORE_START_DATE);
      return false;
    }
    return true;
  }

  private void saveOrUpdatePlacement(Map<String, SiteDTO> siteMapByName,
      Map<String, GradeDTO> gradeMapByName, PlacementXLS placementXLS,
      PlacementDetailsDTO placementDTO, RegNumberToDTOLookup regNumberToDTOLookup,
      boolean updatePlacement, String username, PostDTO postDTO) {
    setOtherMandatoryFields(siteMapByName, gradeMapByName, placementXLS, placementDTO);
    setSpecialties(placementXLS, placementDTO,
        tcsServiceImpl::getSpecialtyByName); //NOTE : specialties won't have a placement Id here and relies on the api to assign the Id
    setOtherSites(placementXLS, placementDTO, referenceServiceImpl::findSitesByName, postDTO);
    Set<String> clinicalSupervisorRoles = referenceServiceImpl.getRolesByCategory(1L).stream()
        .map(roleDTO -> roleDTO.getCode().toLowerCase().trim())
        .collect(Collectors.toSet());
    Set<String> educationalSupervisorRoles = referenceServiceImpl.getRolesByCategory(2L).stream()
        .map(roleDTO -> roleDTO.getCode().toLowerCase().trim())
        .collect(Collectors.toSet());
    addSupervisorsToPlacement(placementXLS, placementDTO, regNumberToDTOLookup,
        clinicalSupervisorRoles, educationalSupervisorRoles);
    if (!placementXLS.hasErrors()) {
      placementDTO.setLifecycleState(LifecycleState.APPROVED);
      setCommentInPlacementDTO(placementDTO, placementXLS, username);
      try {
        if (updatePlacement) {
          tcsServiceImpl.updatePlacement(placementDTO);
        } else {
          tcsServiceImpl.createPlacement(placementDTO);
        }
        placementXLS.setSuccessfullyImported(true);
      } catch (ResourceAccessException rae) {
        new ErrorHandler().recordErrorMessageOnTemplateOrLogUnknown(placementXLS, rae);
      }
    }
  }

  private void setCommentInPlacementDTO(PlacementDetailsDTO placementDTO, PlacementXLS placementXLS,
      String username) {
    if (!StringUtils.isEmpty(placementXLS.getComments())) {
      if (placementDTO.getComments() == null) {
        placementDTO.setComments(new HashSet<>());
      }
      PlacementCommentDTO placementCommentDTO;
      Optional<PlacementCommentDTO> commentsByGenericUpload = placementDTO.getComments().stream()
          .filter(anExistingCommentForPlacement ->
              !StringUtils.isEmpty(anExistingCommentForPlacement.getSource())
                  && anExistingCommentForPlacement.getSource().equals(CommentSource.GENERIC_UPLOAD))
          .findAny();
      if (commentsByGenericUpload.isPresent()) {
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

  private void addSupervisorsToPlacement(PlacementXLS placementXLS,
      PlacementDetailsDTO placementDTO, RegNumberToDTOLookup regNumberToDTOLookup,
      Set<String> clinicalSupervisorRoles, Set<String> educationalSupervisorRoles) {
    addSupervisorToPlacement(placementXLS, placementDTO, regNumberToDTOLookup,
        regNumberToDTOLookup::getDTOForClinicalSupervisor, PlacementXLS::getClinicalSupervisor,
        CLINICAL_SUPERVISOR, clinicalSupervisorRoles);
    addSupervisorToPlacement(placementXLS, placementDTO, regNumberToDTOLookup,
        regNumberToDTOLookup::getDTOForEducationalSupervisor,
        PlacementXLS::getEducationalSupervisor, EDUCATIONAL_SUPERVISOR, educationalSupervisorRoles);
  }

  private void addSupervisorToPlacement(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO,
      RegNumberToDTOLookup regNumberToDTOLookup,
      Function<String, Optional<RegNumberDTO>> getDTOForRegNumber,
      Function<PlacementXLS, String> getSupervisor, String supervisorType,
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
        if (!supervisorHasRole(personDTO, supervisorRoles)) {
          placementXLS.addErrorMessage(String
              .format(IS_NOT_A_ROLE_FOR_PERSON_WITH_REGISTRATION_NUMBER, supervisorType,
                  getSupervisor.apply(placementXLS)));
        } else {
          addNewSupervisorToPlacement(placementDTO, supervisorType, regNumberDTO);
        }
      } else {
        placementXLS.addErrorMessage(String
            .format(COULD_NOT_FIND_A_FOR_REGISTRATION_NUMBER, supervisorType,
                getSupervisor.apply(placementXLS)));
      }
    }
  }

  <DTO> Optional<PersonBasicDetailsDTO> getPersonBasicDetailsDTO(
      Function<PlacementXLS, String> getRegNumber, Map<String, DTO> regNumberDetailsMap,
      Map<Long, PersonBasicDetailsDTO> pbdMapByRegNumber, PlacementXLS placementXLS,
      Function<DTO, Long> getId) {
    DTO regNumberDTO = regNumberDetailsMap.get(getRegNumber.apply(placementXLS));
    if (regNumberDTO != null) {
      return Optional.of(pbdMapByRegNumber.get(getId.apply(regNumberDTO)));
    } else {
      placementXLS.addErrorMessage(
          DID_NOT_FIND_A_PERSON_FOR_REGISTRATION_NUMBER + getRegNumber.apply(placementXLS));
      return Optional.empty();
    }
  }

  public void setSpecialties(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO,
      Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName) {
    // primary specialty is mandatory.
    // When it's populated in the template,
    // clean the existing specialty/other specialties/sub specialty.
    Set<PlacementSpecialtyDTO> placementSpecialtyDtos = initialiseNewPlacementSpecialtyDTOS(
        placementDTO);

    // Primary specialty
    Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional1 = buildPlacementSpecialtyDTO(
        placementXLS, placementDTO, getSpecialtyDTOsForName, placementXLS.getSpecialty1(),
        PostSpecialtyType.PRIMARY);
    if (placementSpecialtyDTOOptional1.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional1.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDTO,
              placementXLS);
    }
    // Other specialties
    Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional2 = buildPlacementSpecialtyDTO(
        placementXLS, placementDTO, getSpecialtyDTOsForName, placementXLS.getSpecialty2(),
        PostSpecialtyType.OTHER);
    if (placementSpecialtyDTOOptional2.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional2.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDTO,
              placementXLS);
    }
    Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional3 = buildPlacementSpecialtyDTO(
        placementXLS, placementDTO, getSpecialtyDTOsForName, placementXLS.getSpecialty3(),
        PostSpecialtyType.OTHER);
    if (placementSpecialtyDTOOptional3.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional3.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDTO,
              placementXLS);
    }
    // Sub specialty
    Optional<PlacementSpecialtyDTO> placementSubSpecialtyDtoOptional = buildPlacementSpecialtyDTO(
        placementXLS, placementDTO, getSpecialtyDTOsForName, placementXLS.getSubSpecialty(),
        PostSpecialtyType.SUB_SPECIALTY);
    if (placementSubSpecialtyDtoOptional.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDto = placementSubSpecialtyDtoOptional.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDto,
              placementXLS);
    }
  }

  public Set<PlacementSpecialtyDTO> initialiseNewPlacementSpecialtyDTOS(
      PlacementDetailsDTO placementDTO) {
    Set<PlacementSpecialtyDTO> placementSpecialtyDTOS = new HashSet<>();
    placementDTO.setSpecialties(placementSpecialtyDTOS);
    return placementSpecialtyDTOS;
  }

  void addPlacementSpecialtyDtoIfUnique(Set<PlacementSpecialtyDTO> placementSpecialtyDtos,
      PlacementSpecialtyDTO placementSpecialtyDto, PlacementXLS placementXls) {
    if (placementSpecialtyDtos.contains(placementSpecialtyDto)) {
      placementXls.addErrorMessage(NO_TWO_SPECIALTIES_CAN_HAVE_SAME_VALUE);
    }
    placementSpecialtyDtos.add(placementSpecialtyDto);
  }

  public Optional<PlacementSpecialtyDTO> buildPlacementSpecialtyDTO(PlacementXLS placementXLS,
      PlacementDetailsDTO placementDTO,
      Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName, String specialtyName,
      PostSpecialtyType specialtyType) {
    Optional<SpecialtyDTO> aSingleValidSpecialty = getASingleValidSpecialtyFromTheReferenceService(
        placementXLS, getSpecialtyDTOsForName, specialtyName);
    if (aSingleValidSpecialty.isPresent()) {
      SpecialtyDTO specialtyDTO = aSingleValidSpecialty.get();
      PlacementSpecialtyDTO placementSpecialtyDTO = new PlacementSpecialtyDTO();
      placementSpecialtyDTO.setPlacementId(placementDTO.getId());
      placementSpecialtyDTO.setSpecialtyId(specialtyDTO.getId());
      placementSpecialtyDTO.setSpecialtyName(specialtyName);
      placementSpecialtyDTO.setPlacementSpecialtyType(specialtyType);
      return Optional.of(placementSpecialtyDTO);
    }
    return Optional.empty();
  }

  private Optional<SpecialtyDTO> getASingleValidSpecialtyFromTheReferenceService(
      PlacementXLS placementXLS, Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName,
      String specialtyName) {
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
      PlacementXLS placementXLS,
      PlacementDetailsDTO placementDTO) {
    placementDTO.setStatus(PlacementStatus.CURRENT);
    setPlacementTypeOrRecordError(placementXLS, placementDTO);
    setWTEOrRecordError(placementXLS, placementDTO);
    setSiteOrRecordError(siteMapByName, placementXLS, placementDTO);
    setGradeOrRecordError(gradeMapByName, placementXLS, placementDTO);
  }

  private boolean checkSpecialty1ExistsOrRecordError(PlacementXLS placementXLS) {
    if (StringUtils.isEmpty(placementXLS.getSpecialty1())) {
      placementXLS.addErrorMessage(SPECIALTY1_IS_MANDATORY);
      return false;
    } else {
      return true;
    }
  }

  private void setSiteOrRecordError(Map<String, SiteDTO> siteMapByName, PlacementXLS placementXLS,
      PlacementDetailsDTO placementDTO) {
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
      placementDTO.setWholeTimeEquivalent(new BigDecimal(placementXLS.getWte().toString()));
    }
  }

  private void setPlacementTypeOrRecordError(PlacementXLS placementXLS,
      PlacementDetailsDTO placementDTO) {
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

  private Set<String> collectRegNumbersForPlacements(List<PlacementXLS> placementXLSS,
      Function<PlacementXLS, String> extractRegistrationNumber) {
    return placementXLSS.stream()
        .map(extractRegistrationNumber::apply)
        .collect(Collectors.toSet());
  }

  private List<PlacementXLS> getRowsWithRegistrationNumberForPlacements(
      List<PlacementXLS> placementXLSS, Function<PlacementXLS, String> extractRegistrationNumber) {
    return placementXLSS.stream()
        .filter(placementXLS -> {
          String regNumber = extractRegistrationNumber.apply(placementXLS);
          return !"unknown".equalsIgnoreCase(regNumber) && !StringUtils.isEmpty(regNumber);
        })
        .collect(Collectors.toList());
  }

  // ***** Other Sites *****
  void setOtherSites(PlacementXLS placementXLS, PlacementDetailsDTO placementDTO,
      Function<String, List<SiteDTO>> getSiteDTOsForName, PostDTO postDTO) {
    Set<PlacementSiteDTO> placementSiteDTOS = placementDTO.getSites();
    if (placementSiteDTOS == null) {
      placementSiteDTOS = initialiseNewPlacementSiteDTOS(placementDTO);
    }
    String otherSitesStr = placementXLS.getOtherSites();
    if (otherSitesStr != null) {
      List<String> otherSites = splitMultiValueField(otherSitesStr);
      for (String otherSite : otherSites) {
        Optional<PlacementSiteDTO> placementSiteDTOOptional2 = buildPlacementSiteDTO(placementXLS,
            placementDTO, getSiteDTOsForName, otherSite, PlacementSiteType.OTHER, postDTO);
        if (placementSiteDTOOptional2.isPresent()) {
          PlacementSiteDTO placementSiteDTO = placementSiteDTOOptional2.get();
          addDTOIfNotPresentAsPrimaryOrOther1(placementSiteDTOS, placementSiteDTO);
        }
      }
    }
  }

  private Set<PlacementSiteDTO> initialiseNewPlacementSiteDTOS(PlacementDetailsDTO placementDTO) {
    Set<PlacementSiteDTO> placmentSiteDTOS = new HashSet<>();
    placementDTO.setSites(placmentSiteDTOS);
    return placmentSiteDTOS;
  }

  private void addDTOIfNotPresentAsPrimaryOrOther1(Set<PlacementSiteDTO> placmentSiteDTOS,
      PlacementSiteDTO placmentSiteDTO) {
    if (placmentSiteDTOS.isEmpty()) {
      placmentSiteDTOS.add(placmentSiteDTO);
    } else if (!placmentSiteDTOS.contains(placmentSiteDTO)) {
      placmentSiteDTO.setPlacementSiteType(PlacementSiteType.OTHER);
      placmentSiteDTOS.add(placmentSiteDTO);
    }
  }

  private Optional<PlacementSiteDTO> buildPlacementSiteDTO(PlacementXLS placementXLS,
      PlacementDetailsDTO placementDTO,
      Function<String, List<SiteDTO>> getSiteDTOsForName,
      String siteName, PlacementSiteType siteType, PostDTO postDTO) {
    Optional<SiteDTO> aSingleValidSite = getASingleValidSiteFromTheReferenceService(placementXLS,
        getSiteDTOsForName, siteName, postDTO);
    if (aSingleValidSite.isPresent()) {
      SiteDTO siteDTO = aSingleValidSite.get();
      PlacementSiteDTO placementSiteDTO = new PlacementSiteDTO(placementDTO.getId(),
          siteDTO.getId(), siteType);
      return Optional.of(placementSiteDTO);
    }
    return Optional.empty();
  }

  private Optional<SiteDTO> getASingleValidSiteFromTheReferenceService(PlacementXLS placementXLS,
      Function<String,
          List<SiteDTO>> getSiteDTOsForName, String siteName, PostDTO postDTO) {
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