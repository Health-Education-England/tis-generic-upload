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

  /**
   * Appends a new PlacementSupervisor to the Placement supplied.
   *
   * @param placementDto   The placement to receive the additional supervisor record
   * @param supervisorType The type of the placement supervisor
   * @param regNumberDto   The DTO, containing the Person ID of the supervisor
   */
  public static void addNewSupervisorToPlacement(PlacementDetailsDTO placementDto,
      String supervisorType, RegNumberDTO<?> regNumberDto) {
    PersonLiteDTO personLiteDTO = new PersonLiteDTO();
    personLiteDTO.setId(regNumberDto.getId());
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
    if (placementDto.getSupervisors() == null) {
      placementDto.setSupervisors(new HashSet<>());
    }
    placementDto.getSupervisors().add(placementSupervisorDTO);
  }

  public static boolean supervisorHasRole(PersonDTO personDto, Set<String> supervisorRoles) {
    if (ObjectUtils.isEmpty(personDto.getRole())) {
      return false;
    }
    Set<String> supervisorRolesAssignedToPerson = new HashSet<>(
        Arrays.asList(personDto.getRole().split(",")));
    return supervisorRolesAssignedToPerson.stream()
        .anyMatch(roleAssignedToPerson -> supervisorRoles
            .contains(roleAssignedToPerson.toLowerCase().trim()));
  }

  public static void setGradeOrRecordError(Map<String, GradeDTO> gradeMapByName,
      PlacementXls placementXls, PlacementDetailsDTO placementDto) {
    String grade = placementXls.getGrade();
    if (!ObjectUtils.isEmpty(grade) && !gradeMapByName.containsKey(grade)) {
      placementXls.addErrorMessage(MULTIPLE_OR_NO_GRADES_FOUND_FOR + grade);
    } else {
      GradeDTO gradeDTO = gradeMapByName.get(grade);
      placementDto.setGradeAbbreviation(gradeDTO.getAbbreviation());
      placementDto.setGradeId(gradeDTO.getId());
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
            getRowsWithPopulatedIdentifier(xlsRows, getRegNumberFunction),
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
    RegNumberToDTOLookup regNumberToDtoLookup = supervisorRegNumberIdService
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
    Set<String> placementNpns = xlsRows.stream()
        .map(PlacementXls::getNationalPostNumber)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Map<String, PostDTO> postsMappedByNpns =
        !placementNpns.isEmpty() ? postFetcher.findWithKeys(placementNpns)
            : new HashMap<>();//TODO filter posts CURRENT/INACTIVE
    Set<String> duplicateNpnKeys =
        !placementNpns.isEmpty() ? postFetcher.getDuplicateKeys() : new HashSet<>();
    Map<String, SiteDTO> siteMapByName = getSiteDtoMap(xlsRows);
    Map<String, GradeDTO> gradeMapByName = getGradeDtoMap(xlsRows);
    for (PlacementXls placementXls : xlsRows) {
      useMatchingCriteriaToUpdatePlacement(regNumberToDtoLookup, phnDetailsMap, pbdMapByPH,
          gdcDetailsMap, pbdMapByGDC, gmcDetailsMap, pbdMapByGMC, personIdDetailsMap, pbdMapById,
          postsMappedByNpns, duplicateNpnKeys, siteMapByName, gradeMapByName, placementXls,
          username);
    }
  }

  private void useMatchingCriteriaToUpdatePlacement(RegNumberToDTOLookup regNumberToDtoLookup,
      Map<String, PersonDTO> phnDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByPH,
      Map<String, GdcDetailsDTO> gdcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGDC,
      Map<String, GmcDetailsDTO> gmcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGMC,
      Map<Long, PersonDTO> personIdDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapById,
      Map<String, PostDTO> postsMappedByNpns, Set<String> duplicateNpnKeys,
      Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
      PlacementXls placementXls, String username) {
    Optional<PersonBasicDetailsDTO> personBasicDetailsDtoOOptional = getPersonBasicDetailsDtoFromRegNumber(
        phnDetailsMap, pbdMapByPH, gdcDetailsMap, pbdMapByGDC, gmcDetailsMap, pbdMapByGMC,
        personIdDetailsMap, pbdMapById, placementXls);
    if (personBasicDetailsDtoOOptional.isPresent() && checkSpecialty1ExistsOrRecordError(
        placementXls)) {
      PersonBasicDetailsDTO personBasicDetailsDto = personBasicDetailsDtoOOptional.get();
      if (!placementXls.getSurname().equalsIgnoreCase(personBasicDetailsDto.getLastName())) {
        placementXls
            .addErrorMessage(SURNAME_DOES_NOT_MATCH_PERSON_OBTAINED_VIA_IDENTIFIER);
      }
      String nationalPostNumber = placementXls.getNationalPostNumber();
      if (isNpmValid(placementXls, nationalPostNumber, postsMappedByNpns, duplicateNpnKeys)) {
        PostDTO postDto = postsMappedByNpns.get(nationalPostNumber);
        if (postDto != null) {
          if ("INACTIVE".equalsIgnoreCase(postDto.getStatus().toString())) {
            placementXls.addErrorMessage(
                POST_STATUS_IS_INACTIVE_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
          } else {
            updatePlacement(regNumberToDtoLookup, siteMapByName, gradeMapByName, placementXls,
                personBasicDetailsDto, postDto, username);
          }
        }
      }
    }
  }

  /**
   * Validation method for National Post Number (NPN).  This method requires that all necessary
   * information is passed in.
   *
   * @param placementXls       The typed data from a spreadsheet row, used to add error messages
   *                           onto. The {@code nationalPostNumber} is typically from this row
   * @param nationalPostNumber The NPN to check
   * @param postsMappedByNpns  A map of valid NPNs
   * @param duplicateNpnKeys   A collection of NPNs which match multiple {@link PostDTO}s
   */
  public boolean isNpmValid(PlacementXls placementXls, String nationalPostNumber,
      Map<String, PostDTO> postsMappedByNpns, Set<String> duplicateNpnKeys) {
    if (nationalPostNumber == null) {
      placementXls.addErrorMessage(NATIONAL_POST_NUMBER_IS_MANDATORY);
      return false;
    } else if (duplicateNpnKeys.contains(nationalPostNumber)) {
      placementXls
          .addErrorMessage(MULTIPLE_POSTS_FOUND_FOR_NATIONAL_POST_NUMBER + nationalPostNumber);
      return false;
    } else if (!postsMappedByNpns.containsKey(nationalPostNumber)) {
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

  private Optional<PersonBasicDetailsDTO> getPersonBasicDetailsDtoFromRegNumber(
      Map<String, PersonDTO> phnDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByPH,
      Map<String, GdcDetailsDTO> gdcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGDC,
      Map<String, GmcDetailsDTO> gmcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGMC,
      Map<Long, PersonDTO> personIdDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapById,
      PlacementXls placementXls) {
    if (getPersonId.apply(placementXls) != null) {
      return getPersonBasicDetailsDto(getPersonId, personIdDetailsMap, pbdMapById, placementXls,
          PersonDTO::getId);
    } else if (!ObjectUtils.isEmpty(getGdcNumber.apply(placementXls))) {
      return getPersonBasicDetailsDto(getGdcNumber, gdcDetailsMap, pbdMapByGDC, placementXls,
          GdcDetailsDTO::getId);
    } else if (!ObjectUtils.isEmpty(getGmcNumber.apply(placementXls))) {
      return getPersonBasicDetailsDto(getGmcNumber, gmcDetailsMap, pbdMapByGMC, placementXls,
          GmcDetailsDTO::getId);
    } else if (!ObjectUtils.isEmpty(getPhNumber.apply(placementXls))) {
      return getPersonBasicDetailsDto(getPhNumber, phnDetailsMap, pbdMapByPH, placementXls,
          PersonDTO::getId);
    } else {
      placementXls.addErrorMessage(ONE_OF_4_ID_NUMBERS_SHOULD_BE_PROVIDED_TO_IDENTIFY_A_PERSON);
      return Optional.empty();
    }
  }

  private void updatePlacement(RegNumberToDTOLookup regNumberToDtoLookup,
      Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
      PlacementXls placementXls, PersonBasicDetailsDTO personBasicDetailsDto, PostDTO postDto,
      String username) {
    if (datesAreValid(placementXls)) {
      List<PlacementDetailsDTO> placementsByPostIdAndPersonId = tcsServiceImpl
          .getPlacementsByPostIdAndPersonId(postDto.getId(), personBasicDetailsDto.getId());
      LocalDate dateFrom = convertDate(placementXls.getDateFrom());
      LocalDate dateTo = convertDate(placementXls.getDateTo());
      boolean existingPlacementUpdatedOrDeleted = false;
      if (!placementsByPostIdAndPersonId.isEmpty()) {
        existingPlacementUpdatedOrDeleted = updateOrDeleteExistingPlacement(regNumberToDtoLookup,
            siteMapByName, gradeMapByName, placementXls, placementsByPostIdAndPersonId, dateFrom,
            dateTo, existingPlacementUpdatedOrDeleted, username, postDto);
      }
      if (placementsByPostIdAndPersonId.isEmpty() || !existingPlacementUpdatedOrDeleted) {
        PlacementDetailsDTO placementDto = new PlacementDetailsDTO();
        placementDto.setNationalPostNumber(postDto.getNationalPostNumber());
        placementDto.setTraineeId(personBasicDetailsDto.getId());
        placementDto.setPostId(postDto.getId());
        placementDto.setDateFrom(dateFrom);
        placementDto.setDateTo(dateTo);
        saveOrUpdatePlacement(siteMapByName, gradeMapByName, placementXls, placementDto,
            regNumberToDtoLookup, false, username, postDto);
      }
    }
  }

  private boolean updateOrDeleteExistingPlacement(RegNumberToDTOLookup regNumberToDtoLookup,
      Map<String, SiteDTO> siteMapByName, Map<String, GradeDTO> gradeMapByName,
      PlacementXls placementXls, List<PlacementDetailsDTO> placementsByPostIdAndPersonId,
      LocalDate dateFrom, LocalDate dateTo, boolean existingPlacementUpdatedOrDeleted,
      String username, PostDTO postDto) {
    for (PlacementDetailsDTO placementDto : placementsByPostIdAndPersonId) {
      if (dateFrom.equals(placementDto.getDateFrom()) && dateTo.equals(placementDto.getDateTo())) {
        if ("DELETE".equalsIgnoreCase(placementXls.getPlacementStatus())) {
          tcsServiceImpl.deletePlacement(placementDto.getId());
          placementXls.setSuccessfullyImported(true);
        } else {
          saveOrUpdatePlacement(siteMapByName, gradeMapByName, placementXls, placementDto,
              regNumberToDtoLookup, true, username, postDto);
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
      PlacementDetailsDTO placementDto, RegNumberToDTOLookup regNumberToDtoLookup,
      boolean updatePlacement, String username, PostDTO postDto) {
    setOtherMandatoryFields(siteMapByName, gradeMapByName, placementXls, placementDto);
    setSpecialties(placementXls, placementDto,
        tcsServiceImpl::getSpecialtyByName); //NOTE : specialties won't have a placement Id here and relies on the api to assign the Id
    setOtherSites(placementXls, placementDto, referenceServiceImpl::findSitesByName, postDto);
    Set<String> clinicalSupervisorRoles = referenceServiceImpl.getRolesByCategory(1L).stream()
        .map(roleDto -> roleDto.getCode().toLowerCase().trim())
        .collect(Collectors.toSet());
    Set<String> educationalSupervisorRoles = referenceServiceImpl.getRolesByCategory(2L).stream()
        .map(roleDto -> roleDto.getCode().toLowerCase().trim())
        .collect(Collectors.toSet());
    addSupervisorsToPlacement(placementXls, placementDto, regNumberToDtoLookup,
        clinicalSupervisorRoles, educationalSupervisorRoles);
    if (!placementXls.hasErrors()) {
      placementDto.setLifecycleState(LifecycleState.APPROVED);
      setCommentInPlacementDto(placementDto, placementXls, username);
      try {
        if (updatePlacement) {
          tcsServiceImpl.updatePlacement(placementDto);
        } else {
          tcsServiceImpl.createPlacement(placementDto);
        }
        placementXls.setSuccessfullyImported(true);
      } catch (ResourceAccessException rae) {
        new ErrorHandler().recordErrorMessageOnTemplateOrLogUnknown(placementXls, rae);
      }
    }
  }

  private void setCommentInPlacementDto(PlacementDetailsDTO placementDto, PlacementXls placementXls,
      String username) {
    if (!ObjectUtils.isEmpty(placementXls.getComments())) {
      if (placementDto.getComments() == null) {
        placementDto.setComments(new HashSet<>());
      }
      PlacementCommentDTO placementCommentDto;
      Optional<PlacementCommentDTO> commentsByGenericUpload = placementDto.getComments().stream()
          .filter(anExistingCommentForPlacement ->
              !ObjectUtils.isEmpty(anExistingCommentForPlacement.getSource())
                  && anExistingCommentForPlacement.getSource().equals(CommentSource.GENERIC_UPLOAD))
          .findAny();
      if (commentsByGenericUpload.isPresent()) {
        placementCommentDto = commentsByGenericUpload.get();
      } else {
        placementCommentDto = new PlacementCommentDTO();
        placementDto.getComments().add(placementCommentDto);
        placementCommentDto.setSource(CommentSource.GENERIC_UPLOAD);
      }
      placementCommentDto.setBody(placementXls.getComments());
      placementCommentDto.setAuthor(username);
    }
  }

  private void addSupervisorsToPlacement(PlacementXls placementXls,
      PlacementDetailsDTO placementDto, RegNumberToDTOLookup regNumberToDtoLookup,
      Set<String> clinicalSupervisorRoles, Set<String> educationalSupervisorRoles) {
    addSupervisorToPlacement(placementXls, placementDto, regNumberToDtoLookup,
        regNumberToDtoLookup::getDTOForClinicalSupervisor, PlacementXls::getClinicalSupervisor,
        CLINICAL_SUPERVISOR, clinicalSupervisorRoles);
    addSupervisorToPlacement(placementXls, placementDto, regNumberToDtoLookup,
        regNumberToDtoLookup::getDTOForEducationalSupervisor,
        PlacementXls::getEducationalSupervisor, EDUCATIONAL_SUPERVISOR, educationalSupervisorRoles);
  }

  private void addSupervisorToPlacement(PlacementXls placementXls, PlacementDetailsDTO placementDto,
      RegNumberToDTOLookup regNumberToDtoLookup,
      Function<String, Optional<RegNumberDTO>> getDtoForRegNumber,
      Function<PlacementXls, String> getSupervisor, String supervisorType,
      Set<String> supervisorRoles) {
    if (!ObjectUtils.isEmpty(getSupervisor.apply(placementXls))) {
      Optional<RegNumberDTO> dtoForSupervisor = getDtoForRegNumber
          .apply(getSupervisor.apply(placementXls));
      if (dtoForSupervisor.isPresent()) {
        RegNumberDTO regNumberDto = dtoForSupervisor.get();
        PersonDTO personDto = regNumberDto.getRegNumberType() == RegNumberType.PH
            ? ((PhnDTO) regNumberDto).getRegNumberDTO()
            : regNumberToDtoLookup.getPersonDetailsMapForSupervisorsByGmcAndGdc()
                .get(regNumberDto.getId());
        if (!supervisorHasRole(personDto, supervisorRoles)) {
          placementXls.addErrorMessage(String
              .format(IS_NOT_A_ROLE_FOR_PERSON_WITH_REGISTRATION_NUMBER, supervisorType,
                  getSupervisor.apply(placementXls)));
        } else {
          addNewSupervisorToPlacement(placementDto, supervisorType, regNumberDto);
        }
      } else {
        placementXls.addErrorMessage(String
            .format(COULD_NOT_FIND_A_FOR_REGISTRATION_NUMBER, supervisorType,
                getSupervisor.apply(placementXls)));
      }
    }
  }

  <DTO_KEY, DTO> Optional<PersonBasicDetailsDTO> getPersonBasicDetailsDto(
      Function<PlacementXls, DTO_KEY> getRegNumber, Map<DTO_KEY, DTO> regNumberDetailsMap,
      Map<Long, PersonBasicDetailsDTO> pbdMapByRegNumber, PlacementXls placementXls,
      Function<DTO, Long> getId) {
    DTO regNumberDto = regNumberDetailsMap.get(getRegNumber.apply(placementXls));
    if (regNumberDto != null) {
      return Optional.of(pbdMapByRegNumber.get(getId.apply(regNumberDto)));
    } else {
      placementXls.addErrorMessage(
          DID_NOT_FIND_A_PERSON_FOR_IDENTIFIER + getRegNumber.apply(placementXls));
      return Optional.empty();
    }
  }

  /**
   * Maps Specialty values from the {@link PlacementXls} to {@link PlacementDetailsDTO}.
   *
   * @param placementXls            The mapping source
   * @param placementDto            The mapping target
   * @param getSpecialtyDtosForName The function to supply {@link SpecialtyDTO}s for a name
   */
  public void setSpecialties(PlacementXls placementXls, PlacementDetailsDTO placementDto,
      Function<String, List<SpecialtyDTO>> getSpecialtyDtosForName) {
    // primary specialty is mandatory.
    // When it's populated in the template,
    // clean the existing specialty/other specialties/sub specialty.
    Set<PlacementSpecialtyDTO> placementSpecialtyDtos = initialiseNewPlacementSpecialtyDtos(
        placementDto);

    // Primary specialty
    Optional<PlacementSpecialtyDTO> placementSpecialtyDtoOptional1 = buildPlacementSpecialtyDto(
        placementXls, placementDto, getSpecialtyDtosForName, placementXls.getSpecialty1(),
        PostSpecialtyType.PRIMARY);
    if (placementSpecialtyDtoOptional1.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDto = placementSpecialtyDtoOptional1.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDto,
          placementXls);
    }
    // Other specialties
    Optional<PlacementSpecialtyDTO> placementSpecialtyDtoOptional2 = buildPlacementSpecialtyDto(
        placementXls, placementDto, getSpecialtyDtosForName, placementXls.getSpecialty2(),
        PostSpecialtyType.OTHER);
    if (placementSpecialtyDtoOptional2.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDto = placementSpecialtyDtoOptional2.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDto,
          placementXls);
    }
    Optional<PlacementSpecialtyDTO> placementSpecialtyDtoOptional3 = buildPlacementSpecialtyDto(
        placementXls, placementDto, getSpecialtyDtosForName, placementXls.getSpecialty3(),
        PostSpecialtyType.OTHER);
    if (placementSpecialtyDtoOptional3.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDto = placementSpecialtyDtoOptional3.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDto,
          placementXls);
    }
    // Sub specialty
    Optional<PlacementSpecialtyDTO> placementSubSpecialtyDtoOptional = buildPlacementSpecialtyDto(
        placementXls, placementDto, getSpecialtyDtosForName, placementXls.getSubSpecialty(),
        PostSpecialtyType.SUB_SPECIALTY);
    if (placementSubSpecialtyDtoOptional.isPresent()) {
      PlacementSpecialtyDTO placementSpecialtyDto = placementSubSpecialtyDtoOptional.get();
      addPlacementSpecialtyDtoIfUnique(placementSpecialtyDtos, placementSpecialtyDto,
          placementXls);
    }
  }

  public Set<PlacementSpecialtyDTO> initialiseNewPlacementSpecialtyDtos(
      PlacementDetailsDTO placementDto) {
    Set<PlacementSpecialtyDTO> placementSpecialtyDtos = new HashSet<>();
    placementDto.setSpecialties(placementSpecialtyDtos);
    return placementSpecialtyDtos;
  }

  void addPlacementSpecialtyDtoIfUnique(Set<PlacementSpecialtyDTO> placementSpecialtyDtos,
      PlacementSpecialtyDTO placementSpecialtyDto, PlacementXls placementXls) {
    if (placementSpecialtyDtos.contains(placementSpecialtyDto)) {
      placementXls.addErrorMessage(NO_TWO_SPECIALTIES_CAN_HAVE_SAME_VALUE);
    }
    placementSpecialtyDtos.add(placementSpecialtyDto);
  }

  /**
   * Using the provided information, creates a {@link PlacementSpecialtyDTO}.
   *
   * @param placementXls            The data provided by the user
   * @param placementDto            The persisted placement, the target will be linked to
   * @param getSpecialtyDtosForName A function for retrieving {@link SpecialtyDTO}s
   * @param specialtyName           The name of the Specialty to link to the Placement
   * @param specialtyType           The specialty type, enumerated in TCS
   * @return An optional of the build DTO, or empty
   */
  public Optional<PlacementSpecialtyDTO> buildPlacementSpecialtyDto(PlacementXls placementXls,
      PlacementDetailsDTO placementDto,
      Function<String, List<SpecialtyDTO>> getSpecialtyDtosForName, String specialtyName,
      PostSpecialtyType specialtyType) {
    Optional<SpecialtyDTO> singleValidSpecialty = getASingleValidSpecialtyFromTheReferenceService(
        placementXls, getSpecialtyDtosForName, specialtyName);
    if (singleValidSpecialty.isPresent()) {
      SpecialtyDTO specialtyDto = singleValidSpecialty.get();
      PlacementSpecialtyDTO placementSpecialtyDto = new PlacementSpecialtyDTO();
      placementSpecialtyDto.setPlacementId(placementDto.getId());
      placementSpecialtyDto.setSpecialtyId(specialtyDto.getId());
      placementSpecialtyDto.setSpecialtyName(specialtyName);
      placementSpecialtyDto.setPlacementSpecialtyType(specialtyType);
      return Optional.of(placementSpecialtyDto);
    }
    return Optional.empty();
  }

  private Optional<SpecialtyDTO> getASingleValidSpecialtyFromTheReferenceService(
      PlacementXls placementXls, Function<String, List<SpecialtyDTO>> getSpecialtyDtosForName,
      String specialtyName) {
    if (!ObjectUtils.isEmpty(specialtyName)) {
      List<SpecialtyDTO> specialtyByName = getSpecialtyDtosForName.apply(specialtyName);
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
      PlacementDetailsDTO placementDto) {
    placementDto.setStatus(PlacementStatus.CURRENT);
    setPlacementTypeOrRecordError(placementXls, placementDto);
    setWteOrRecordError(placementXls, placementDto);
    setSiteOrRecordError(siteMapByName, placementXls, placementDto);
    setGradeOrRecordError(gradeMapByName, placementXls, placementDto);
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
      PlacementDetailsDTO placementDto) {
    String site = placementXls.getSite();
    if (!ObjectUtils.isEmpty(site) && !siteMapByName.containsKey(site)) {
      placementXls.addErrorMessage(MULTIPLE_OR_NO_SITES_FOUND_FOR + site);
    } else {
      SiteDTO siteDto = siteMapByName.get(site);
      placementDto.setSiteCode(siteDto.getSiteCode());
      placementDto.setSiteId(siteDto.getId());
    }
  }

  private void setWteOrRecordError(PlacementXls placementXls, PlacementDetailsDTO placementDto) {
    if (placementXls.getWte() == null) {
      placementXls.addErrorMessage(WHOLE_TIME_EQUIVALENT_WTE_IS_MANDATORY);
    } else {
      placementDto.setWholeTimeEquivalent(new BigDecimal(placementXls.getWte().toString()));
    }
  }

  private void setPlacementTypeOrRecordError(PlacementXls placementXls,
      PlacementDetailsDTO placementDto) {
    if (ObjectUtils.isEmpty(placementXls.getPlacementType())) {
      placementXls.addErrorMessage(PLACEMENT_TYPE_IS_MANDATORY);
    } else {
      placementDto.setPlacementType(placementXls.getPlacementType());
    }
  }

  //TODO optimise these to be Fetcher like
  private Map<String, GradeDTO> getGradeDtoMap(List<PlacementXls> xlsRows) {
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

  private Map<String, SiteDTO> getSiteDtoMap(List<PlacementXls> xlsRows) {
    Set<String> siteNames = xlsRows.stream()
        .map(PlacementXls::getSite)
        .collect(Collectors.toSet());

    Map<String, SiteDTO> siteMapByName = new HashMap<>();
    for (String siteName : siteNames) {
      List<SiteDTO> sitesByName = referenceServiceImpl.findSitesByName(siteName);
      if (sitesByName.size() == 1) {
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
        .map(extractRegistrationNumber)
        .collect(Collectors.toSet());
  }

  private <DTO_KEY> List<PlacementXls> getRowsWithPopulatedIdentifier(
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
  void setOtherSites(PlacementXls placementXls, PlacementDetailsDTO placementDto,
      Function<String, List<SiteDTO>> getSiteDtosForName, PostDTO postDto) {
    Set<PlacementSiteDTO> placementSiteDtos = placementDto.getSites();
    if (placementSiteDtos == null) {
      placementSiteDtos = initialiseNewPlacementSiteDtos(placementDto);
    }
    String otherSitesStr = placementXls.getOtherSites();
    if (otherSitesStr != null) {
      List<String> otherSites = splitMultiValueField(otherSitesStr);
      for (String otherSite : otherSites) {
        Optional<PlacementSiteDTO> placementSiteDtoOOptional2 = buildPlacementSiteDto(placementXls,
            placementDto, getSiteDtosForName, otherSite, PlacementSiteType.OTHER, postDto);
        if (placementSiteDtoOOptional2.isPresent()) {
          PlacementSiteDTO placementSiteDto = placementSiteDtoOOptional2.get();
          addDtoIfNotPresentAsPrimaryOrOther1(placementSiteDtos, placementSiteDto);
        }
      }
    }
  }

  private Set<PlacementSiteDTO> initialiseNewPlacementSiteDtos(PlacementDetailsDTO placementDto) {
    Set<PlacementSiteDTO> placementSiteDtos = new HashSet<>();
    placementDto.setSites(placementSiteDtos);
    return placementSiteDtos;
  }

  private void addDtoIfNotPresentAsPrimaryOrOther1(Set<PlacementSiteDTO> placmentSiteDtos,
      PlacementSiteDTO placmentSiteDto) {
    if (placmentSiteDtos.isEmpty()) {
      placmentSiteDtos.add(placmentSiteDto);
    } else if (!placmentSiteDtos.contains(placmentSiteDto)) {
      placmentSiteDto.setPlacementSiteType(PlacementSiteType.OTHER);
      placmentSiteDtos.add(placmentSiteDto);
    }
  }

  private Optional<PlacementSiteDTO> buildPlacementSiteDto(PlacementXls placementXls,
      PlacementDetailsDTO placementDto,
      Function<String, List<SiteDTO>> getSiteDtosForName,
      String siteName, PlacementSiteType siteType, PostDTO postDto) {
    Optional<SiteDTO> singleValidSite = getSingleValidSite(placementXls,
        getSiteDtosForName, siteName, postDto);
    if (singleValidSite.isPresent()) {
      SiteDTO siteDTO = singleValidSite.get();
      PlacementSiteDTO placementSiteDTO = new PlacementSiteDTO(placementDto.getId(),
          siteDTO.getId(), siteType);
      return Optional.of(placementSiteDTO);
    }
    return Optional.empty();
  }

  private Optional<SiteDTO> getSingleValidSite(PlacementXls placementXls,
      Function<String, List<SiteDTO>> getSiteDtosForName, String siteName, PostDTO postDto) {
    if (ObjectUtils.isEmpty(siteName)) {
      return Optional.empty();
    }
    List<SiteDTO> siteByName = getSiteDtosForName.apply(siteName);
    if (siteByName == null) {
      return Optional.empty();
    }
    siteByName.removeIf(site -> site.getStatus() != Status.CURRENT);
    if (siteByName.size() == 1) {
      // identify if the siteId exists in parent Post
      long siteId = siteByName.get(0).getId();
      if (postDto.getSites().stream().anyMatch(s -> s.getSiteId() == siteId)) {
        return Optional.of(siteByName.get(0));
      } else {
        placementXls.addErrorMessage(
            String.format(DID_NOT_FIND_OTHER_SITE_IN_PARENT_POST_FOR_NAME, siteName));
      }
    } else {
      String errorMessage = siteByName.isEmpty() ? DID_NOT_FIND_OTHER_SITE_FOR_NAME
          : FOUND_MULTIPLE_OTHER_SITES_FOR_NAME;
      placementXls.addErrorMessage(String.format(errorMessage, siteName));
    }
    return Optional.empty();
  }
}
