package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static com.transformuk.hee.tis.genericupload.service.util.MultiValueUtil.splitMultiValueField;
import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementSupervisor;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementXls;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.DTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GDCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GMCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PeopleByIdFetcher;
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
import java.util.ArrayList;
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
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.ResourceAccessException;

@Component
public class PlacementTransformerService {

  public static final String CLINICAL_SUPERVISOR = "Clinical supervisor";
  public static final String EDUCATIONAL_SUPERVISOR = "Educational supervisor";
  protected static final String NO_TWO_SPECIALTIES_CAN_HAVE_SAME_VALUE =
      "No two of primary/other/sub specialty(ies) can be set with the same value.";
  private static final Logger logger = getLogger(PlacementTransformerService.class);
  private static final String ONE_OF_4_ID_NUMBERS_SHOULD_BE_PROVIDED_TO_IDENTIFY_A_PERSON =
      "At least one of the 4 identifying numbers should be provided to identify a person";
  private static final String SURNAME_DOES_NOT_MATCH_PERSON_OBTAINED_VIA_IDENTIFIER =
      "Surname does not match person obtained via identifier";
  private static final String NATIONAL_POST_NUMBER_IS_MANDATORY =
      "National Post number is mandatory";
  private static final String MULTIPLE_POSTS_FOUND_FOR_NATIONAL_POST_NUMBER =
      "Multiple posts found for National Post Number: ";
  private static final String COULD_NOT_FIND_POST_BY_NATIONAL_POST_NUMBER =
      "Could not find post by National Post Number: ";
  private static final String POST_STATUS_IS_INACTIVE_FOR_NATIONAL_POST_NUMBER =
      "POST status is INACTIVE for National Post Number: ";
  private static final String DID_NOT_FIND_A_PERSON_FOR_REGISTRATION_NUMBER =
      "Did not find a person for registration number: ";
  private static final String POST_STATUS_IS_SET_TO_DELETE_FOR_NATIONAL_POST_NUMBER =
      "POST status is set to DELETE for National Post Number: ";
  private static final String DID_NOT_FIND_A_PERSON_FOR_IDENTIFIER =
      "Did not find a person for identifier: ";
  private static final String SPECIALTY1_IS_MANDATORY =
      "Specialty1 field is required";
  private static final String DID_NOT_FIND_SPECIALTY_FOR_NAME =
      "Did not find specialty for name: ";
  private static final String FOUND_MULTIPLE_SPECIALTIES_FOR_NAME =
      "Found multiple specialties for name: ";
  private static final String PLACEMENT_FROM_DATE_IS_MANDATORY =
      "Placement from date is mandatory";
  private static final String PLACEMENT_TO_DATE_IS_MANDATORY =
      "Placement to date is mandatory";
  private static final String MULTIPLE_OR_NO_GRADES_FOUND_FOR =
      "Multiple or no grades found for: ";
  private static final String MULTIPLE_OR_NO_SITES_FOUND_FOR =
      "Multiple or no sites found for: ";
  private static final String WHOLE_TIME_EQUIVALENT_WTE_IS_MANDATORY =
      "Whole Time Equivalent (WTE) is mandatory";
  private static final String PLACEMENT_TYPE_IS_MANDATORY =
      "Placement Type is mandatory";
  private static final String EXPECTED_A_PLACEMENT_GRADE_FOR =
      "Expected to find a placement grade for: %s";
  private static final String EXPECTED_TO_FIND_A_SINGLE_SITE_FOR =
      "Expected to find a single site for: %s";
  private static final String COULD_NOT_FIND_A_FOR_REGISTRATION_NUMBER =
      "Could not find a %1$s for registration number: %s";
  private static final String IS_NOT_A_ROLE_FOR_PERSON_WITH_REGISTRATION_NUMBER =
      "%1$s is not a role for person with registration number: %2$s";
  private static final String DID_NOT_FIND_OTHER_SITE_FOR_NAME =
      "Did not find other site for name \"%s\".";
  private static final String FOUND_MULTIPLE_OTHER_SITES_FOR_NAME =
      "Found multiple other sites for name \"%s\".";
  private static final String DID_NOT_FIND_OTHER_SITE_IN_PARENT_POST_FOR_NAME =
      "Did not find other site in parent post for name \"%s\".";
  private static final String END_DATE_IS_SET_BEFORE_START_DATE =
      "End date cannot be set before start date";
  Function<PlacementXls, String> getPhNumber = PlacementXls::getPublicHealthNumber;
  Function<PlacementXls, String> getGdcNumber = PlacementXls::getGdcNumber;
  Function<PlacementXls, String> getGmcNumber = PlacementXls::getGmcNumber;
  Function<PlacementXls, Long> getPersonId = PlacementXls::getPersonId;
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
  private PeopleByIdFetcher peopleByIdFetcher;
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
    if (ObjectUtils.isEmpty(personDTO.getRole())) {
      return false;
    }
    Set<String> supervisorRolesAssignedToPerson = new HashSet<>(
        Arrays.asList(personDTO.getRole().split(",")));
    return supervisorRolesAssignedToPerson.stream()
        .anyMatch(roleAssignedToPerson -> supervisorRoles
            .contains(roleAssignedToPerson.toLowerCase().trim()));
  }

  public static void setGradeOrRecordError(Map<String, GradeDTO> gradeMapByName,
      PlacementXls placementXls, PlacementDetailsDTO placementDTO) {
    String grade = placementXls.getGrade();
    if (!ObjectUtils.isEmpty(grade) && !gradeMapByName.containsKey(grade)) {
      placementXls.addErrorMessage(MULTIPLE_OR_NO_GRADES_FOUND_FOR + grade);
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
    this.peopleByIdFetcher = new PeopleByIdFetcher(tcsServiceImpl);
    this.postFetcher = new PostFetcher(tcsServiceImpl);
  }

  <DTO_KEY, DTO> Map<DTO_KEY, DTO> buildRegNumberDetailsMap(List<PlacementXls> xlsRows,
      Function<PlacementXls, DTO_KEY> getRegNumberFunction, DTOFetcher<DTO_KEY, DTO> fetcher) {
    return fetcher.findWithKeys(
        collectRegNumbersForPlacements(
            getRowsWithRegistrationNumberForPlacements(xlsRows, getRegNumberFunction),
            getRegNumberFunction));
  }

  <DTO_KEY, DTO> Map<Long, PersonBasicDetailsDTO> buildPersonBasicDetailsMapForRegNumber(
      Map<DTO_KEY, DTO> regNumberMap, DTOFetcher<DTO_KEY, DTO> idExtractingFetcher,
      Function<DTO, Long> getId) {
    return regNumberMap.isEmpty() ? null
        : pbdDtoFetcher.findWithKeys(idExtractingFetcher.extractIds(regNumberMap, getId));
  }

  void processPlacementsUpload(List<PlacementXls> xlsRows, String username) {
    if (CollectionUtils.isEmpty(xlsRows)) {
      return;
    }
    xlsRows.forEach(PlacementXls::initialiseSuccessfullyImported);
    List<PlacementSupervisor> placementSupervisorList = new ArrayList<>(xlsRows);
    RegNumberToDTOLookup regNumberToDTOLookup = supervisorRegNumberIdService
        .getRegNumbersForSheetOrMarkAsError(placementSupervisorList);
    Map<Long, PersonDTO> personIdDetailsMap = buildRegNumberDetailsMap(xlsRows, getPersonId,
        peopleByIdFetcher);
    Map<Long, PersonBasicDetailsDTO> pbdMapById = buildPersonBasicDetailsMapForRegNumber(
        personIdDetailsMap, peopleByIdFetcher, PersonDTO::getId);
    Map<String, PersonDTO> phnDetailsMap = buildRegNumberDetailsMap(xlsRows, getPhNumber,
        peopleByPHNFetcher);
    Map<Long, PersonBasicDetailsDTO> pbdMapByPH = buildPersonBasicDetailsMapForRegNumber(
        phnDetailsMap, peopleByPHNFetcher, PersonDTO::getId);
    Map<String, GdcDetailsDTO> gdcDetailsMap = buildRegNumberDetailsMap(xlsRows,
        getGdcNumber, gdcDtoFetcher);
    Map<Long, PersonBasicDetailsDTO> pbdMapByGDC = buildPersonBasicDetailsMapForRegNumber(
        gdcDetailsMap, gdcDtoFetcher, GdcDetailsDTO::getId);
    Map<String, GmcDetailsDTO> gmcDetailsMap = buildRegNumberDetailsMap(xlsRows,
        getGmcNumber, gmcDtoFetcher);
    Map<Long, PersonBasicDetailsDTO> pbdMapByGMC = buildPersonBasicDetailsMapForRegNumber(
        gmcDetailsMap, gmcDtoFetcher, GmcDetailsDTO::getId);
    Set<String> placementNPNs = xlsRows.stream()
        .map(PlacementXls::getNationalPostNumber)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Map<String, PostDTO> postsMappedByNPNs =
        !placementNPNs.isEmpty() ? postFetcher.findWithKeys(placementNPNs)
            : new HashMap<>();//TODO filter posts CURRENT/INACTIVE
    Set<String> duplicateNPNKeys =
        !placementNPNs.isEmpty() ? postFetcher.getDuplicateKeys() : new HashSet<>();
    Map<String, SiteDTO> siteMapByName = getSiteDTOMap(xlsRows);
    Map<String, GradeDTO> gradeMapByName = getGradeDTOMap(xlsRows);
    for (PlacementXls placementXls : xlsRows) {
      useMatchingCriteriaToUpdatePlacement(regNumberToDTOLookup, phnDetailsMap, pbdMapByPH,
          gdcDetailsMap, pbdMapByGDC, gmcDetailsMap, pbdMapByGMC, personIdDetailsMap, pbdMapById,
          postsMappedByNPNs, duplicateNPNKeys, siteMapByName, gradeMapByName, placementXls,
          username);
    }
  }

  private void useMatchingCriteriaToUpdatePlacement(RegNumberToDTOLookup regNumberToDTOLookup,
      Map<String, PersonDTO> phnDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByPH,
      Map<String, GdcDetailsDTO> gdcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGDC,
      Map<String, GmcDetailsDTO> gmcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGMC,
      Map<Long, PersonDTO> personIdDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapById,
      Map<String, PostDTO> postsMappedByNPNs, Set<String> duplicateNPNKeys,
      Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
      PlacementXls placementXls, String username) {
    Optional<PersonBasicDetailsDTO> personBasicDetailsDTOOptional = getPersonBasicDetailsDTOFromRegNumber(
        phnDetailsMap, pbdMapByPH, gdcDetailsMap, pbdMapByGDC, gmcDetailsMap, pbdMapByGMC,
        personIdDetailsMap, pbdMapById, placementXls);
    if (personBasicDetailsDTOOptional.isPresent() && checkSpecialty1ExistsOrRecordError(
        placementXls)) {
      PersonBasicDetailsDTO personBasicDetailsDTO = personBasicDetailsDTOOptional.get();
      if (!placementXls.getSurname().equalsIgnoreCase(personBasicDetailsDTO.getLastName())) {
        placementXls
            .addErrorMessage(SURNAME_DOES_NOT_MATCH_PERSON_OBTAINED_VIA_IDENTIFIER);
      }
      String nationalPostNumber = placementXls.getNationalPostNumber();
      if (isNPNValid(placementXls, nationalPostNumber, postsMappedByNPNs, duplicateNPNKeys)) {
        PostDTO postDTO = postsMappedByNPNs.get(nationalPostNumber);
        if (postDTO != null) {
          if ("INACTIVE".equalsIgnoreCase(postDTO.getStatus().toString())) {
            placementXls.addErrorMessage(
                POST_STATUS_IS_INACTIVE_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
          } else {
            updatePlacement(regNumberToDTOLookup, siteMapByName, gradeMapByName, placementXls,
                personBasicDetailsDTO, postDTO, username);
          }
        }
      }
    }
  }

  public boolean isNPNValid(PlacementXls placementXls, String nationalPostNumber,
      Map<String, PostDTO> postsMappedByNPNs, Set<String> duplicateNPNKeys) {
    if (nationalPostNumber == null) {
      placementXls.addErrorMessage(NATIONAL_POST_NUMBER_IS_MANDATORY);
      return false;
    } else if (duplicateNPNKeys.contains(nationalPostNumber)) {
      placementXls
          .addErrorMessage(MULTIPLE_POSTS_FOUND_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
      return false;
    } else if (!postsMappedByNPNs.containsKey(nationalPostNumber)) {
      placementXls
          .addErrorMessage(COULD_NOT_FIND_POST_BY_NATIONAL_POST_NUMBER + nationalPostNumber);
      return false;
    } else {
      return true;
    }
  }


  /**
   * Checks if a grade is a valid placement grade.
   * <p>
   * SIDE-EFFECT: if not valid, this is logged and the affected placement XLS records have an error
   * message attached to them.
   * </p>
   *
   * @param xlsRows         the list of placement XLS records
   * @param gradeName       the grade to verify
   * @param placementGrades the list of valid placement grades
   * @return true if gradeName was a valid placement grade, false otherwise (note side-effect above)
   */
  public boolean isPlacementGradeValid(List<PlacementXls> xlsRows, String gradeName,
      List<String> placementGrades) {
    boolean gradeValid =
        placementGrades.stream().anyMatch(gradeName::equalsIgnoreCase);
    if (!gradeValid) {
      xlsRows.stream()
          .filter(placementXls -> placementXls.getGrade().equalsIgnoreCase(gradeName))
          .forEach(placementXls -> {
            logger.error(String.format(EXPECTED_A_PLACEMENT_GRADE_FOR, gradeName));
            placementXls.addErrorMessage(String.format(EXPECTED_A_PLACEMENT_GRADE_FOR,
                gradeName));
          });
    }
    return gradeValid;
  }

  private Optional<PersonBasicDetailsDTO> getPersonBasicDetailsDTOFromRegNumber(
      Map<String, PersonDTO> phnDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByPH,
      Map<String, GdcDetailsDTO> gdcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGDC,
      Map<String, GmcDetailsDTO> gmcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGMC,
      Map<Long, PersonDTO> personIdDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapById,
      PlacementXls placementXls) {
    if (getPersonId.apply(placementXls) != null) {
      return getPersonBasicDetailsDTO(getPersonId, personIdDetailsMap, pbdMapById, placementXls,
          PersonDTO::getId);
    } else if (!ObjectUtils.isEmpty(getGdcNumber.apply(placementXls))) {
      return getPersonBasicDetailsDTO(getGdcNumber, gdcDetailsMap, pbdMapByGDC, placementXls,
          GdcDetailsDTO::getId);
    } else if (!ObjectUtils.isEmpty(getGmcNumber.apply(placementXls))) {
      return getPersonBasicDetailsDTO(getGmcNumber, gmcDetailsMap, pbdMapByGMC, placementXls,
          GmcDetailsDTO::getId);
    } else if (!ObjectUtils.isEmpty(getPhNumber.apply(placementXls))) {
      return getPersonBasicDetailsDTO(getPhNumber, phnDetailsMap, pbdMapByPH, placementXls,
          PersonDTO::getId);
    } else {
      placementXls.addErrorMessage(ONE_OF_4_ID_NUMBERS_SHOULD_BE_PROVIDED_TO_IDENTIFY_A_PERSON);
      return Optional.empty();
    }
  }

  private void updatePlacement(RegNumberToDTOLookup regNumberToDTOLookup,
      Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
      PlacementXls placementXls, PersonBasicDetailsDTO personBasicDetailsDTO, PostDTO postDTO,
      String username) {
    if (datesAreValid(placementXls)) {
      List<PlacementDetailsDTO> placementsByPostIdAndPersonId = tcsServiceImpl
          .getPlacementsByPostIdAndPersonId(postDTO.getId(), personBasicDetailsDTO.getId());
      LocalDate dateFrom = convertDate(placementXls.getDateFrom());
      LocalDate dateTo = convertDate(placementXls.getDateTo());
      boolean existingPlacementUpdatedOrDeleted = false;
      if (!placementsByPostIdAndPersonId.isEmpty()) {
        existingPlacementUpdatedOrDeleted = updateOrDeleteExistingPlacement(regNumberToDTOLookup,
            siteMapByName, gradeMapByName, placementXls, placementsByPostIdAndPersonId, dateFrom,
            dateTo, existingPlacementUpdatedOrDeleted, username, postDTO);
      }
      if (placementsByPostIdAndPersonId.isEmpty() || !existingPlacementUpdatedOrDeleted) {
        PlacementDetailsDTO placementDTO = new PlacementDetailsDTO();
        placementDTO.setNationalPostNumber(postDTO.getNationalPostNumber());
        placementDTO.setTraineeId(personBasicDetailsDTO.getId());
        placementDTO.setPostId(postDTO.getId());
        placementDTO.setDateFrom(dateFrom);
        placementDTO.setDateTo(dateTo);
        saveOrUpdatePlacement(siteMapByName, gradeMapByName, placementXls, placementDTO,
            regNumberToDTOLookup, false, username, postDTO);
      }
    }
  }

  private boolean updateOrDeleteExistingPlacement(RegNumberToDTOLookup regNumberToDTOLookup,
      Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
      PlacementXls placementXls, List<PlacementDetailsDTO> placementsByPostIdAndPersonId,
      LocalDate dateFrom, LocalDate dateTo, boolean existingPlacementUpdatedOrDeleted,
      String username, PostDTO postDTO) {
    for (PlacementDetailsDTO placementDTO : placementsByPostIdAndPersonId) {
      if (dateFrom.equals(placementDTO.getDateFrom()) && dateTo.equals(placementDTO.getDateTo())) {
        if ("DELETE".equalsIgnoreCase(placementXls.getPlacementStatus())) {
          tcsServiceImpl.deletePlacement(placementDTO.getId());
          placementXls.setSuccessfullyImported(true);
        } else {
          saveOrUpdatePlacement(siteMapByName, gradeMapByName, placementXls, placementDTO,
              regNumberToDTOLookup, true, username, postDTO);
        }
        existingPlacementUpdatedOrDeleted = true;
        break;
      }
    }
    return existingPlacementUpdatedOrDeleted;
  }

  private boolean datesAreValid(PlacementXls placementXls) {
    if (placementXls.getDateFrom() == null || placementXls.getDateTo() == null) {
      if (placementXls.getDateFrom() == null) {
        placementXls.addErrorMessage(PLACEMENT_FROM_DATE_IS_MANDATORY);
      }
      if (placementXls.getDateTo() == null) {
        placementXls.addErrorMessage(PLACEMENT_TO_DATE_IS_MANDATORY);
      }
      return false;
    } else if (placementXls.getDateFrom().after(placementXls.getDateTo())) {
      placementXls.addErrorMessage(END_DATE_IS_SET_BEFORE_START_DATE);
      return false;
    }
    return true;
  }

  private void saveOrUpdatePlacement(Map<String, SiteDTO> siteMapByName,
      Map<String, GradeDTO> gradeMapByName, PlacementXls placementXls,
      PlacementDetailsDTO placementDTO, RegNumberToDTOLookup regNumberToDTOLookup,
      boolean updatePlacement, String username, PostDTO postDTO) {
    setOtherMandatoryFields(siteMapByName, gradeMapByName, placementXls, placementDTO);
    setSpecialties(placementXls, placementDTO,
        tcsServiceImpl::getSpecialtyByName); //NOTE : specialties won't have a placement Id here and relies on the api to assign the Id
    setOtherSites(placementXls, placementDTO, referenceServiceImpl::findSitesByName, postDTO);
    Set<String> clinicalSupervisorRoles = referenceServiceImpl.getRolesByCategory(1L).stream()
        .map(roleDTO -> roleDTO.getCode().toLowerCase().trim())
        .collect(Collectors.toSet());
    Set<String> educationalSupervisorRoles = referenceServiceImpl.getRolesByCategory(2L).stream()
        .map(roleDTO -> roleDTO.getCode().toLowerCase().trim())
        .collect(Collectors.toSet());
    addSupervisorsToPlacement(placementXls, placementDTO, regNumberToDTOLookup,
        clinicalSupervisorRoles, educationalSupervisorRoles);
    if (!placementXls.hasErrors()) {
      placementDTO.setLifecycleState(LifecycleState.APPROVED);
      setCommentInPlacementDTO(placementDTO, placementXls, username);
      try {
        if (updatePlacement) {
          tcsServiceImpl.updatePlacement(placementDTO);
        } else {
          tcsServiceImpl.createPlacement(placementDTO);
        }
        placementXls.setSuccessfullyImported(true);
      } catch (ResourceAccessException rae) {
        new ErrorHandler().recordErrorMessageOnTemplateOrLogUnknown(placementXls, rae);
      }
    }
  }

  private void setCommentInPlacementDTO(PlacementDetailsDTO placementDTO, PlacementXls placementXls,
      String username) {
    if (!ObjectUtils.isEmpty(placementXls.getComments())) {
      if (placementDTO.getComments() == null) {
        placementDTO.setComments(new HashSet<>());
      }
      PlacementCommentDTO placementCommentDTO;
      Optional<PlacementCommentDTO> commentsByGenericUpload = placementDTO.getComments().stream()
          .filter(anExistingCommentForPlacement ->
              !ObjectUtils.isEmpty(anExistingCommentForPlacement.getSource())
                  && anExistingCommentForPlacement.getSource().equals(CommentSource.GENERIC_UPLOAD))
          .findAny();
      if (commentsByGenericUpload.isPresent()) {
        placementCommentDTO = commentsByGenericUpload.get();
      } else {
        placementCommentDTO = new PlacementCommentDTO();
        placementDTO.getComments().add(placementCommentDTO);
        placementCommentDTO.setSource(CommentSource.GENERIC_UPLOAD);
      }
      placementCommentDTO.setBody(placementXls.getComments());
      placementCommentDTO.setAuthor(username);
    }
  }

  private void addSupervisorsToPlacement(PlacementXls placementXls,
      PlacementDetailsDTO placementDTO, RegNumberToDTOLookup regNumberToDTOLookup,
      Set<String> clinicalSupervisorRoles, Set<String> educationalSupervisorRoles) {
    addSupervisorToPlacement(placementXls, placementDTO, regNumberToDTOLookup,
        regNumberToDTOLookup::getDTOForClinicalSupervisor, PlacementXls::getClinicalSupervisor,
        CLINICAL_SUPERVISOR, clinicalSupervisorRoles);
    addSupervisorToPlacement(placementXls, placementDTO, regNumberToDTOLookup,
        regNumberToDTOLookup::getDTOForEducationalSupervisor,
        PlacementXls::getEducationalSupervisor, EDUCATIONAL_SUPERVISOR, educationalSupervisorRoles);
  }

  private void addSupervisorToPlacement(PlacementXls placementXls, PlacementDetailsDTO placementDTO,
      RegNumberToDTOLookup regNumberToDTOLookup,
      Function<String, Optional<RegNumberDTO>> getDTOForRegNumber,
      Function<PlacementXls, String> getSupervisor, String supervisorType,
      Set<String> supervisorRoles) {
    if (!ObjectUtils.isEmpty(getSupervisor.apply(placementXls))) {
      Optional<RegNumberDTO> dtoForSupervisor = getDTOForRegNumber
          .apply(getSupervisor.apply(placementXls));
      if (dtoForSupervisor.isPresent()) {
        RegNumberDTO regNumberDTO = dtoForSupervisor.get();
        PersonDTO personDTO = regNumberDTO.getRegNumberType() == RegNumberType.PH
            ? ((PhnDTO) regNumberDTO).getRegNumberDTO()
            : regNumberToDTOLookup.getPersonDetailsMapForSupervisorsByGmcAndGdc()
                .get(regNumberDTO.getId());
        if (!supervisorHasRole(personDTO, supervisorRoles)) {
          placementXls.addErrorMessage(String
              .format(IS_NOT_A_ROLE_FOR_PERSON_WITH_REGISTRATION_NUMBER, supervisorType,
                  getSupervisor.apply(placementXls)));
        } else {
          addNewSupervisorToPlacement(placementDTO, supervisorType, regNumberDTO);
        }
      } else {
        placementXls.addErrorMessage(String
            .format(COULD_NOT_FIND_A_FOR_REGISTRATION_NUMBER, supervisorType,
                getSupervisor.apply(placementXls)));
      }
    }
  }

  <DTO_KEY, DTO> Optional<PersonBasicDetailsDTO> getPersonBasicDetailsDTO(
      Function<PlacementXls, DTO_KEY> getRegNumber, Map<DTO_KEY, DTO> regNumberDetailsMap,
      Map<Long, PersonBasicDetailsDTO> pbdMapByRegNumber, PlacementXls placementXls,
      Function<DTO, Long> getId) {
    DTO regNumberDTO = regNumberDetailsMap.get(getRegNumber.apply(placementXls));
    if (regNumberDTO != null) {
      return Optional.of(pbdMapByRegNumber.get(getId.apply(regNumberDTO)));
    } else {
      placementXls.addErrorMessage(
          DID_NOT_FIND_A_PERSON_FOR_IDENTIFIER + getRegNumber.apply(placementXls));
      return Optional.empty();
    }
  }

  public void setSpecialties(PlacementXls placementXls, PlacementDetailsDTO placementDTO,
      Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName) {
    // primary specialty is mandatory.
    // When it's populated in the template,
    // clean the existing specialty/other specialties/sub specialty.
    Set<PlacementSpecialtyDTO> placementSpecialtyDtos = initialiseNewPlacementSpecialtyDTOS(
        placementDTO);

    // Primary specialty
    Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional1 = buildPlacementSpecialtyDTO(
        placementXls, placementDTO, getSpecialtyDTOsForName, placementXls.getSpecialty1(),
        PostSpecialtyType.PRIMARY);
    if (placementSpecialtyDTOOptional1.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional1.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDTO,
          placementXls);
    }
    // Other specialties
    Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional2 = buildPlacementSpecialtyDTO(
        placementXls, placementDTO, getSpecialtyDTOsForName, placementXls.getSpecialty2(),
        PostSpecialtyType.OTHER);
    if (placementSpecialtyDTOOptional2.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional2.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDTO,
          placementXls);
    }
    Optional<PlacementSpecialtyDTO> placementSpecialtyDTOOptional3 = buildPlacementSpecialtyDTO(
        placementXls, placementDTO, getSpecialtyDTOsForName, placementXls.getSpecialty3(),
        PostSpecialtyType.OTHER);
    if (placementSpecialtyDTOOptional3.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDTO = placementSpecialtyDTOOptional3.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDTO,
          placementXls);
    }
    // Sub specialty
    Optional<PlacementSpecialtyDTO> placementSubSpecialtyDtoOptional = buildPlacementSpecialtyDTO(
        placementXls, placementDTO, getSpecialtyDTOsForName, placementXls.getSubSpecialty(),
        PostSpecialtyType.SUB_SPECIALTY);
    if (placementSubSpecialtyDtoOptional.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDto = placementSubSpecialtyDtoOptional.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDto,
          placementXls);
    }
  }

  public Set<PlacementSpecialtyDTO> initialiseNewPlacementSpecialtyDTOS(
      PlacementDetailsDTO placementDTO) {
    Set<PlacementSpecialtyDTO> placementSpecialtyDTOS = new HashSet<>();
    placementDTO.setSpecialties(placementSpecialtyDTOS);
    return placementSpecialtyDTOS;
  }

  void addPlacementSpecialtyDtoIfUnique(Set<PlacementSpecialtyDTO> placementSpecialtyDtos,
      PlacementSpecialtyDTO placementSpecialtyDto, PlacementXls placementXls) {
    if (placementSpecialtyDtos.contains(placementSpecialtyDto)) {
      placementXls.addErrorMessage(NO_TWO_SPECIALTIES_CAN_HAVE_SAME_VALUE);
    }
    placementSpecialtyDtos.add(placementSpecialtyDto);
  }

  public Optional<PlacementSpecialtyDTO> buildPlacementSpecialtyDTO(PlacementXls placementXls,
      PlacementDetailsDTO placementDTO,
      Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName, String specialtyName,
      PostSpecialtyType specialtyType) {
    Optional<SpecialtyDTO> aSingleValidSpecialty = getASingleValidSpecialtyFromTheReferenceService(
        placementXls, getSpecialtyDTOsForName, specialtyName);
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
      PlacementXls placementXls, Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName,
      String specialtyName) {
    if (!ObjectUtils.isEmpty(specialtyName)) {
      List<SpecialtyDTO> specialtyByName = getSpecialtyDTOsForName.apply(specialtyName);
      if (specialtyByName != null) {
        if (specialtyByName.size() != 1) {
          if (specialtyByName.isEmpty()) {
            placementXls.addErrorMessage(DID_NOT_FIND_SPECIALTY_FOR_NAME + specialtyName);
          } else {
            placementXls.addErrorMessage(FOUND_MULTIPLE_SPECIALTIES_FOR_NAME + specialtyName);
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
      PlacementXls placementXls,
      PlacementDetailsDTO placementDTO) {
    placementDTO.setStatus(PlacementStatus.CURRENT);
    setPlacementTypeOrRecordError(placementXls, placementDTO);
    setWTEOrRecordError(placementXls, placementDTO);
    setSiteOrRecordError(siteMapByName, placementXls, placementDTO);
    setGradeOrRecordError(gradeMapByName, placementXls, placementDTO);
  }

  private boolean checkSpecialty1ExistsOrRecordError(PlacementXls placementXls) {
    if (ObjectUtils.isEmpty(placementXls.getSpecialty1())) {
      placementXls.addErrorMessage(SPECIALTY1_IS_MANDATORY);
      return false;
    } else {
      return true;
    }
  }

  private void setSiteOrRecordError(Map<String, SiteDTO> siteMapByName, PlacementXls placementXls,
      PlacementDetailsDTO placementDTO) {
    String site = placementXls.getSite();
    if (!ObjectUtils.isEmpty(site) && !siteMapByName.containsKey(site)) {
      placementXls.addErrorMessage(MULTIPLE_OR_NO_SITES_FOUND_FOR + site);
    } else {
      SiteDTO siteDTO = siteMapByName.get(site);
      placementDTO.setSiteCode(siteDTO.getSiteCode());
      placementDTO.setSiteId(siteDTO.getId());
    }
  }

  private void setWTEOrRecordError(PlacementXls placementXls, PlacementDetailsDTO placementDTO) {
    if (placementXls.getWte() == null) {
      placementXls.addErrorMessage(WHOLE_TIME_EQUIVALENT_WTE_IS_MANDATORY);
    } else {
      placementDTO.setWholeTimeEquivalent(new BigDecimal(placementXls.getWte().toString()));
    }
  }

  private void setPlacementTypeOrRecordError(PlacementXls placementXls,
      PlacementDetailsDTO placementDTO) {
    if (ObjectUtils.isEmpty(placementXls.getPlacementType())) {
      placementXls.addErrorMessage(PLACEMENT_TYPE_IS_MANDATORY);
    } else {
      placementDTO.setPlacementType(placementXls.getPlacementType());
    }
  }

  //TODO optimise these to be Fetcher like
  private Map<String, GradeDTO> getGradeDTOMap(List<PlacementXls> xlsRows) {
    Set<String> gradeNames = xlsRows.stream()
        .map(PlacementXls::getGrade)
        .collect(Collectors.toSet());
    Map<String, GradeDTO> gradeMapByName = new HashMap<>();
    List<String> gradesValidForPlacements = referenceServiceImpl
        .findGradesCurrentPlacementAndTrainingGrades().stream().map(GradeDTO::getName)
        .collect(Collectors.toList());
    for (String gradeName : gradeNames) {
      List<GradeDTO> gradesByName = referenceServiceImpl.findGradesByName(gradeName);
      if (isPlacementGradeValid(xlsRows, gradeName, gradesValidForPlacements)) {
        gradeMapByName.put(gradeName, gradesByName.get(0));
      }
    }
    return gradeMapByName;
  }

  private Map<String, SiteDTO> getSiteDTOMap(List<PlacementXls> xlsRows) {
    Set<String> siteNames = xlsRows.stream()
        .map(PlacementXls::getSite)
        .collect(Collectors.toSet());

    Map<String, SiteDTO> siteMapByName = new HashMap<>();
    for (String siteName : siteNames) {
      List<SiteDTO> sitesByName = referenceServiceImpl.findSitesByName(siteName);
      if (!sitesByName.isEmpty() && sitesByName.size() == 1) {
        siteMapByName.put(siteName, sitesByName.get(0));
      } else {
        xlsRows.stream()
            .filter(placementXls -> placementXls.getSite().equalsIgnoreCase(siteName))
            .forEach(placementXls -> {
              logger.error(String.format(EXPECTED_TO_FIND_A_SINGLE_SITE_FOR, siteName));
              placementXls
                  .addErrorMessage(String.format(EXPECTED_TO_FIND_A_SINGLE_SITE_FOR, siteName));
            });
      }
    }
    return siteMapByName;
  }

  private <DTO_KEY> Set<DTO_KEY> collectRegNumbersForPlacements(List<PlacementXls> xlsRows,
      Function<PlacementXls, DTO_KEY> extractRegistrationNumber) {
    return xlsRows.stream()
        .map(extractRegistrationNumber::apply)
        .collect(Collectors.toSet());
  }

  private <DTO_KEY> List<PlacementXls> getRowsWithRegistrationNumberForPlacements(
      List<PlacementXls> xlsRows, Function<PlacementXls, DTO_KEY> extractRegistrationNumber) {
    return xlsRows.stream()
        .filter(placementXls -> {
          DTO_KEY regNumber = extractRegistrationNumber.apply(placementXls);
          return !(regNumber instanceof String && "unknown".equalsIgnoreCase((String) regNumber))
              && !ObjectUtils.isEmpty(regNumber);
        })
        .collect(Collectors.toList());
  }

  // ***** Other Sites *****
  void setOtherSites(PlacementXls placementXls, PlacementDetailsDTO placementDTO,
      Function<String, List<SiteDTO>> getSiteDTOsForName, PostDTO postDTO) {
    Set<PlacementSiteDTO> placementSiteDTOS = placementDTO.getSites();
    if (placementSiteDTOS == null) {
      placementSiteDTOS = initialiseNewPlacementSiteDTOS(placementDTO);
    }
    String otherSitesStr = placementXls.getOtherSites();
    if (otherSitesStr != null) {
      List<String> otherSites = splitMultiValueField(otherSitesStr);
      for (String otherSite : otherSites) {
        Optional<PlacementSiteDTO> placementSiteDTOOptional2 = buildPlacementSiteDTO(placementXls,
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

  private Optional<PlacementSiteDTO> buildPlacementSiteDTO(PlacementXls placementXls,
      PlacementDetailsDTO placementDTO,
      Function<String, List<SiteDTO>> getSiteDTOsForName,
      String siteName, PlacementSiteType siteType, PostDTO postDTO) {
    Optional<SiteDTO> aSingleValidSite = getASingleValidSiteFromTheReferenceService(placementXls,
        getSiteDTOsForName, siteName, postDTO);
    if (aSingleValidSite.isPresent()) {
      SiteDTO siteDTO = aSingleValidSite.get();
      PlacementSiteDTO placementSiteDTO = new PlacementSiteDTO(placementDTO.getId(),
          siteDTO.getId(), siteType);
      return Optional.of(placementSiteDTO);
    }
    return Optional.empty();
  }

  private Optional<SiteDTO> getASingleValidSiteFromTheReferenceService(PlacementXls placementXls,
      Function<String,
          List<SiteDTO>> getSiteDTOsForName, String siteName, PostDTO postDTO) {
    if (!ObjectUtils.isEmpty(siteName)) {
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
            placementXls.addErrorMessage(
                String.format(DID_NOT_FIND_OTHER_SITE_IN_PARENT_POST_FOR_NAME, siteName));
          } else {
            return Optional.of(siteByName.get(0));
          }
        } else {
          String errorMessage = siteByName.isEmpty() ? DID_NOT_FIND_OTHER_SITE_FOR_NAME
              : FOUND_MULTIPLE_OTHER_SITES_FOR_NAME;
          placementXls.addErrorMessage(String.format(errorMessage, siteName));
        }
      }
    }
    return Optional.empty();
  }
}