package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentDetailDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentListDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentOutcomeDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentOutcomeReasonDTO;
import com.transformuk.hee.tis.assessment.api.dto.RevalidationDTO;
import com.transformuk.hee.tis.assessment.client.service.impl.AssessmentServiceImpl;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentXLS;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.DTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GDCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.GMCDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PeopleByPHNFetcher;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.PersonBasicDetailsDTOFetcher;
import com.transformuk.hee.tis.genericupload.service.util.BooleanUtil;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonBasicDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSummaryDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipCurriculaDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class AssessmentTransformerService {

  public static final String SEMI_COLON = ";";
  public static final String ASSESSMENT_REASON_NOT_FOUND = "Given Assessment reason not found";
  public static final String ASSESSMENT_IS_DUPLICATE = "This assessment already exists: %s";
  protected static final String EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR =
      "Expected to find a single grade for: %s";
  private static final Logger logger = getLogger(AssessmentTransformerService.class);
  private static final String ONE_OF_3_REG_NUMBERS_SHOULD_BE_PROVIDED_TO_IDENTIFY_A_PERSON =
      "At least one of the 3 registration numbers should be provided to identify a person";
  private static final String SURNAME_DOES_NOT_MATCH_LAST_NAME_OBTAINED_VIA_REGISTRATION_NUMBER =
      "Surname does not match last name obtained via registration number";
  private static final String DID_NOT_FIND_A_PERSON_FOR_REGISTRATION_NUMBER =
      "Did not find a person for registration number : ";
  private static final String MULTIPLE_OR_NO_GRADES_FOUND_FOR = "Multiple or no grades found for: ";
  private static final String PROGRAMME_NAME_NOT_SPECIFIED =
      "Programme name (%s) has not been specified. Both programme name and number are needed to identify the programme";
  private static final String PROGRAMME_NUMBER_NOT_SPECIFIED =
      "Programme number (%s) has not been specified. Both programme name and number are needed to identify the programme";
  private static final String AT_LEAST_ONE_OF_THE_THREE_REGISTRATION_NUMBERS_NEEDS_TO_BE_SPECIFIED =
      "At least one of the three registration numbers needs to be specified";
  private static final String DID_NOT_FIND_PROGRAMME_CURRICULUM =
      "Did not find Programme curriculum";
  private static final String DAYS_OUT_OF_TRAINING_SHOULD_BE_NUMERIC =
      "Days out of training should be numeric";
  private static final String MONTHS_OOPR_OOPT_COUNTED_TOWARDS_TRAINING_SHOULD_BE_NUMERIC =
      "Months OOPR/OOPT counted towards training should be numeric";
  private static final String GIVEN_OUTCOME_IS_NOT_VALID = "Given outcome is not valid";
  private static final String OUTCOME_REASON_IS_REQUIRED_FOR_OUTCOME_S =
      "Outcome reason is required for outcome : %s";
  private static final String OTHER_REASON_IS_REQUIRED = "Other reason is required";
  private static final String PROGRAMME_CURRICULUM_INFO_NOT_FOUND =
      "Programme curriculum information not found for given trainee";
  private static final String TRAINEE_NOT_FOUND = "Trainee information not found";
  private static final String ASSESSMENT_TYPE_IS_REQUIRED = "Assessment type is required";
  private static final String REVIEW_DATE_BEFORE_1753 = "Review date is before year 1753";
  private static final String CURRICULUM_START_DATE_BEFORE_1753 =
      "Curriculum start date is below year 1753";
  private static final String CURRICULUM_END_DATE_BEFORE_1753 =
      "Curriculum end date is below year 1753";
  private static final String PERIOD_COVERED_FROM_DATE_BEFORE_1753 =
      "Period covered from date is below year 1753";
  private static final String PERIOD_COVERED_TO_DATE_BEFORE_1753 =
      "Period covered to date is below year 1753";
  private static final String NEXT_REVIEW_DATE_BEFORE_1753 = "Next review date is below year 1753";
  Function<AssessmentXLS, String> getPhNumber = AssessmentXLS::getPublicHealthNumber;
  Function<AssessmentXLS, String> getGdcNumber = AssessmentXLS::getGdcNumber;
  Function<AssessmentXLS, String> getGmcNumber = AssessmentXLS::getGmcNumber;
  @Autowired
  private TcsServiceImpl tcsServiceImpl;
  @Autowired
  private ReferenceServiceImpl referenceServiceImpl;
  @Autowired
  private AssessmentServiceImpl assessmentServiceImpl;
  private GMCDTOFetcher gmcDtoFetcher;
  private GDCDTOFetcher gdcDtoFetcher;
  private PersonBasicDetailsDTOFetcher pbdDtoFetcher;
  private PeopleByPHNFetcher peopleByPHNFetcher;
  private Set<Outcome> allOutcomes;
  private ObjectMapper objectMapper = new ObjectMapper();


  @PostConstruct
  public void initialiseFetchers() {
    this.gmcDtoFetcher = new GMCDTOFetcher(tcsServiceImpl);
    this.gdcDtoFetcher = new GDCDTOFetcher(tcsServiceImpl);
    this.pbdDtoFetcher = new PersonBasicDetailsDTOFetcher(tcsServiceImpl);
    this.peopleByPHNFetcher = new PeopleByPHNFetcher(tcsServiceImpl);
  }

  <DTO> Map<String, DTO> buildRegNumberDetailsMap(List<AssessmentXLS> assessmentXLS,
      Function<AssessmentXLS, String> getRegNumberFunction, DTOFetcher<String, DTO> fetcher) {
    return fetcher.findWithKeys(
        collectRegNumbersForAssessments(
            getRowsWithRegistrationNumberForAssessments(assessmentXLS, getRegNumberFunction),
            getRegNumberFunction));
  }

  <DTO> Map<Long, PersonBasicDetailsDTO> buildPersonBasicDetailsMapForRegNumber(
      Map<String, DTO> regNumberMap, DTOFetcher<String, DTO> idExtractingFetcher,
      Function<DTO, Long> getId) {
    return regNumberMap.isEmpty() ? null
        : pbdDtoFetcher.findWithKeys(idExtractingFetcher.extractIds(regNumberMap, getId));
  }

  /**
   * Checks for existing assessments that are duplicates of the assessment in the DTO.
   *
   * @param assessmentDto the assessment to check
   * @return String message listing duplicate assessment id's or an empty string if no duplicates
   */
  String getAnyDuplicateAssessmentsMessage(AssessmentDTO assessmentDto) {
    List<AssessmentListDTO> duplicateAssessments = assessmentServiceImpl.findAssessments(
        assessmentDto.getTraineeId(),
        assessmentDto.getCurriculumMembershipId(),
        assessmentDto.getReviewDate(),
        (assessmentDto.getOutcome() == null ? null : assessmentDto.getOutcome().getOutcome()));
    //now consider a possible null outcome value that is ignored by findAssessments()
    //giving false-positives that must be filtered out
    if (assessmentDto.getOutcome() == null || assessmentDto.getOutcome().getOutcome() == null) {
      duplicateAssessments = duplicateAssessments.stream()
          .filter(d -> d.getOutcome() == null)
          .collect(Collectors.toList());
    }

    if (!duplicateAssessments.isEmpty()) {
      return String.format(ASSESSMENT_IS_DUPLICATE, duplicateAssessments
          .stream()
          .map(d -> d.getId().toString())
          .collect(Collectors.joining(",")));
    }
    return ""; //no duplicates
  }

  void processAssessmentsUpload(List<AssessmentXLS> assessmentXLSList) {
    assessmentXLSList.forEach(AssessmentXLS::initialiseSuccessfullyImported);
    markRowsWithoutRegistrationNumbers(assessmentXLSList);

    if (!CollectionUtils.isEmpty(assessmentXLSList)) {
      Map<String, PersonDTO> phnDetailsMap = buildRegNumberDetailsMap(assessmentXLSList,
          getPhNumber, peopleByPHNFetcher);
      Map<Long, PersonBasicDetailsDTO> pbdMapByPH = buildPersonBasicDetailsMapForRegNumber(
          phnDetailsMap, peopleByPHNFetcher, PersonDTO::getId);

      Map<String, GdcDetailsDTO> gdcDetailsMap = buildRegNumberDetailsMap(assessmentXLSList,
          getGdcNumber, gdcDtoFetcher);
      Map<Long, PersonBasicDetailsDTO> pbdMapByGDC = buildPersonBasicDetailsMapForRegNumber(
          gdcDetailsMap, gdcDtoFetcher, GdcDetailsDTO::getId);

      Map<String, GmcDetailsDTO> gmcDetailsMap = buildRegNumberDetailsMap(assessmentXLSList,
          getGmcNumber, gmcDtoFetcher);
      Map<Long, PersonBasicDetailsDTO> pbdMapByGMC = buildPersonBasicDetailsMapForRegNumber(
          gmcDetailsMap, gmcDtoFetcher, GmcDetailsDTO::getId);

      Map<String, GradeDTO> gradeMapByName = getGradeDTOMap(assessmentXLSList);

      // initialise all the outcome reasons by calling assessment
      this.allOutcomes = getAllOutcomes();

      for (AssessmentXLS assessmentXLS : assessmentXLSList) {
        useMatchingCriteriaToCreateAssessment(phnDetailsMap, pbdMapByPH, gdcDetailsMap, pbdMapByGDC,
            gmcDetailsMap, pbdMapByGMC, gradeMapByName, assessmentXLS);
      }
    }
  }

  private void markRowsWithoutRegistrationNumbers(List<AssessmentXLS> assessmentXLSList) {
    assessmentXLSList.stream()
        .filter(xls ->
            xls.getGmcNumber() == null &&
                xls.getGdcNumber() == null &&
                xls.getPublicHealthNumber() == null)
        .forEach(xls -> xls
            .addErrorMessage(AT_LEAST_ONE_OF_THE_THREE_REGISTRATION_NUMBERS_NEEDS_TO_BE_SPECIFIED));
  }

  public Set<Outcome> getAllOutcomes() {
    Set<Outcome> allOutcomes = Sets.newHashSet();
    String jsonAllOutcome = assessmentServiceImpl.getAllOutcomes();
    logger.info("Outcome string: {}", jsonAllOutcome);
    try {
      if (StringUtils.isNotEmpty(jsonAllOutcome)) {
        allOutcomes = objectMapper.readValue(jsonAllOutcome, new TypeReference<Set<Outcome>>() {
        });
      }
    } catch (IOException e) {
      logger.error(" Could not convert Json string to Outcome");
    }
    return allOutcomes;
  }

  private void useMatchingCriteriaToCreateAssessment(Map<String, PersonDTO> phnDetailsMap,
      Map<Long, PersonBasicDetailsDTO> pbdMapByPH, Map<String, GdcDetailsDTO> gdcDetailsMap,
      Map<Long, PersonBasicDetailsDTO> pbdMapByGDC, Map<String, GmcDetailsDTO> gmcDetailsMap,
      Map<Long, PersonBasicDetailsDTO> pbdMapByGMC,
      Map<String, GradeDTO> gradeMapByName, AssessmentXLS assessmentXLS) {
    Optional<PersonBasicDetailsDTO> personBasicDetailsDTOOptional = getPersonBasicDetailsDTOFromRegNumber(
        phnDetailsMap, pbdMapByPH, gdcDetailsMap, pbdMapByGDC, gmcDetailsMap, pbdMapByGMC,
        assessmentXLS);

    if (personBasicDetailsDTOOptional.isPresent()) {
      PersonBasicDetailsDTO personBasicDetailsDTO = personBasicDetailsDTOOptional.get();

      if (!assessmentXLS.getSurname().equalsIgnoreCase(personBasicDetailsDTO.getLastName())) {
        assessmentXLS
            .addErrorMessage(SURNAME_DOES_NOT_MATCH_LAST_NAME_OBTAINED_VIA_REGISTRATION_NUMBER);
      }

      String grade = assessmentXLS.getNextRotationGradeName();
      if (!StringUtils.isEmpty(grade) && !gradeMapByName.containsKey(grade)) {
        assessmentXLS.addErrorMessage(MULTIPLE_OR_NO_GRADES_FOUND_FOR + grade);
      }

      if (StringUtils.isEmpty(assessmentXLS.getType())) {
        assessmentXLS.addErrorMessage(ASSESSMENT_TYPE_IS_REQUIRED);
      }

      ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDTO = getProgrammeMembershipCurriculaDTO(
          personBasicDetailsDTO.getId(),
          assessmentXLS, tcsServiceImpl::getProgrammeMembershipForTrainee);
      AssessmentDTO assessmentDTO = new AssessmentDTO();
      assessmentDTO.setFirstName(personBasicDetailsDTO.getFirstName());
      assessmentDTO.setLastName(personBasicDetailsDTO.getLastName());
      assessmentDTO.setTraineeId(personBasicDetailsDTO.getId());
      assessmentDTO.setGmcNumber(personBasicDetailsDTO.getGmcNumber());
      assessmentDTO.setGdcNumber(personBasicDetailsDTO.getGdcNumber());
      assessmentDTO.setPublicHealthNumber(personBasicDetailsDTO.getPublicHealthNumber());
      assessmentDTO.setType(assessmentXLS.getType());

      if (programmeMembershipCurriculaDTO != null
          && programmeMembershipCurriculaDTO.getCurriculumMemberships() != null) {
        assessmentDTO.setProgrammeName(programmeMembershipCurriculaDTO.getProgrammeName());
        assessmentDTO.setProgrammeId(programmeMembershipCurriculaDTO.getProgrammeId());
        assessmentDTO.setProgrammeNumber(programmeMembershipCurriculaDTO.getProgrammeNumber());
        assessmentDTO.setCurriculumMembershipId(
            programmeMembershipCurriculaDTO.getCurriculumMemberships().get(0).getId());
      } else {
        assessmentXLS.addErrorMessage(DID_NOT_FIND_PROGRAMME_CURRICULUM);
      }
      try {
        assessmentDTO.setReviewDate(convertDate(assessmentXLS.getReviewDate()));
      } catch (final IllegalArgumentException e) {
        assessmentXLS.addErrorMessage(REVIEW_DATE_BEFORE_1753);
      }

      // Assessment Details
      AssessmentDetailDTO assessmentDetailDTO = new AssessmentDetailDTO();
      if (programmeMembershipCurriculaDTO != null
          && programmeMembershipCurriculaDTO.getCurriculumMemberships() != null) {
        CurriculumDTO curriculumDTO = programmeMembershipCurriculaDTO.getCurriculumDTO();
        assessmentDetailDTO.setCurriculumId(curriculumDTO.getId());
        assessmentDetailDTO.setCurriculumName(curriculumDTO.getName());
        LocalDate curriculumStartDate = programmeMembershipCurriculaDTO.getCurriculumMemberships()
            .get(0).getCurriculumStartDate();
        if (curriculumStartDate.getYear() < 1753) {
          assessmentXLS.addErrorMessage(CURRICULUM_START_DATE_BEFORE_1753);
        }
        assessmentDetailDTO.setCurriculumStartDate(curriculumStartDate);
        LocalDate curriculumEndDate = programmeMembershipCurriculaDTO.getCurriculumMemberships()
            .get(0).getCurriculumEndDate();
        if (curriculumEndDate.getYear() < 1753) {
          assessmentXLS.addErrorMessage(CURRICULUM_END_DATE_BEFORE_1753);
        }
        assessmentDetailDTO.setCurriculumEndDate(curriculumEndDate);
        assessmentDetailDTO
            .setCurriculumSpecialtyId(String.valueOf(curriculumDTO.getSpecialty().getId()));
        assessmentDetailDTO
            .setCurriculumSpecialty(String.valueOf(curriculumDTO.getSpecialty().getName()));
        assessmentDetailDTO.setCurriculumSubType(curriculumDTO.getCurriculumSubType().name());
      }

      if (NumberUtils.isDigits(assessmentXLS.getDaysOutOfTraining())) {
        assessmentDetailDTO
            .setDaysOutOfTraining(Integer.parseInt(assessmentXLS.getDaysOutOfTraining()));
      } else if (!StringUtils.isEmpty(assessmentXLS.getDaysOutOfTraining())) {
        assessmentXLS.addErrorMessage(DAYS_OUT_OF_TRAINING_SHOULD_BE_NUMERIC);
      }

      try {
        assessmentDetailDTO.setPeriodCoveredFrom(convertDate(assessmentXLS.getPeriodCoveredFrom()));
      } catch (final IllegalArgumentException e) {
        assessmentXLS.addErrorMessage(PERIOD_COVERED_FROM_DATE_BEFORE_1753);
      }

      try {
        assessmentDetailDTO.setPeriodCoveredTo(convertDate(assessmentXLS.getPeriodCoveredTo()));
      } catch (final IllegalArgumentException e) {
        assessmentXLS.addErrorMessage(PERIOD_COVERED_TO_DATE_BEFORE_1753);
      }

      if (NumberUtils.isDigits(assessmentXLS.getMonthsCountedToTraining())) {
        assessmentDetailDTO.setMonthsCountedToTraining(
            Integer.parseInt(assessmentXLS.getMonthsCountedToTraining()));
      } else if (!StringUtils.isEmpty(assessmentXLS.getMonthsCountedToTraining())) {
        assessmentXLS.addErrorMessage(MONTHS_OOPR_OOPT_COUNTED_TOWARDS_TRAINING_SHOULD_BE_NUMERIC);
      }
      assessmentDetailDTO.setPya(BooleanUtil.parseBooleanObject(assessmentXLS.getPya()));

      // Get placement of trainee at time of assessment review
      List<PlacementSummaryDTO> placementForTrainee = tcsServiceImpl
          .getPlacementForTrainee(assessmentDTO.getTraineeId());
      Optional<PlacementSummaryDTO> placementAtTimeOfAssessmentReview = placementForTrainee.stream()
          .filter(p -> {
            LocalDate reviewDate = assessmentDTO.getReviewDate();
            if (reviewDate != null) {
              LocalDate dateFrom = p.getDateFrom()
                  .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
              LocalDate dateTo = p.getDateTo()
                  .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
              return dateFrom.isBefore(reviewDate) && dateTo.isAfter(reviewDate);
            }
            return false;
          }).findFirst();

      if (placementAtTimeOfAssessmentReview.isPresent()) {
        // Get grade information from the placementAtTimeOfAssessmentReview
        Long placementGradeId = placementAtTimeOfAssessmentReview.get().getGradeId();
        String placementGradeName = placementAtTimeOfAssessmentReview.get().getGradeName();
        String placementGradeAbbreviation = referenceServiceImpl
            .findGradesByName(placementGradeName).get(0).getAbbreviation();

        // Set assessmentDTO with newly set grade information
        assessmentDetailDTO.setGradeAbbreviation(placementGradeAbbreviation);
        assessmentDetailDTO.setGradeId(placementGradeId);
        assessmentDetailDTO.setGradeName(placementGradeName);
      } else {

        // Set assessmentDetail with newly set grade information
        assessmentDetailDTO.setGradeAbbreviation("NA");
        assessmentDetailDTO.setGradeId(null);
        assessmentDetailDTO.setGradeName("Not Available");

      }

      // Update assessmentDTO
      assessmentDTO.detail(assessmentDetailDTO);

      // Outcome
      AssessmentOutcomeDTO assessmentOutcomeDTO = null;
      if (!StringUtils.isEmpty(assessmentXLS.getOutcome())) {
        assessmentOutcomeDTO = new AssessmentOutcomeDTO();
        Outcome outcome = this.allOutcomes.stream()
            .filter(o -> o.getLabel().equalsIgnoreCase(assessmentXLS.getOutcome())).findAny()
            .orElse(null);
        if (outcome != null && outcome.getId() != null) {
          assessmentOutcomeDTO.setOutcomeId(outcome.getId());
          assessmentOutcomeDTO.setOutcome(outcome.getLabel());
        } else {
          assessmentXLS.addErrorMessage(GIVEN_OUTCOME_IS_NOT_VALID);
        }
        assessmentOutcomeDTO
            .setUnderAppeal(BooleanUtil.parseBooleanObject(assessmentXLS.getUnderAppeal()));
        assessmentOutcomeDTO.setAcademicOutcome(assessmentXLS.getAcademicOutcome());
        assessmentOutcomeDTO
            .setExternalTrainer(BooleanUtil.parseBooleanObject(assessmentXLS.getExternalTrainer()));
        // Assessment outcome reasons

        GradeDTO gradeDTO = gradeMapByName.get(assessmentXLS.getNextRotationGradeName());
        if (gradeDTO != null) {
          assessmentOutcomeDTO.setNextRotationGradeName(gradeDTO.getName());
          assessmentOutcomeDTO.setNextRotationGradeAbbr(gradeDTO.getAbbreviation());
          assessmentOutcomeDTO.setNextRotationGradeId(gradeDTO.getId());
        }

        try {
          assessmentOutcomeDTO.setNextReviewDate(convertDate(assessmentXLS.getNextReviewDate()));
        } catch (final IllegalArgumentException e) {
          assessmentXLS.addErrorMessage(NEXT_REVIEW_DATE_BEFORE_1753);
        }

        assessmentOutcomeDTO.setComments(assessmentXLS.getComments());
        assessmentOutcomeDTO
            .setTenPercentAudit(BooleanUtil.parseBooleanObject(assessmentXLS.getTenPercentAudit()));
        assessmentOutcomeDTO.setDetailedReasons(assessmentXLS.getDetailedReasons());
        assessmentOutcomeDTO.setMitigatingCircumstances(assessmentXLS.getMitigatingCircumstances());
        assessmentOutcomeDTO
            .setCompetencesToBeDeveloped(assessmentXLS.getCompetencesToBeDeveloped());
        assessmentOutcomeDTO.setOtherRecommendedActions(assessmentXLS.getOtherRecommendedActions());
        assessmentOutcomeDTO.setRecommendedAdditionalTrainingTime(
            assessmentXLS.getRecommendedAdditionalTrainingTime());
        assessmentOutcomeDTO
            .setAdditionalCommentsFromPanel(assessmentXLS.getAdditionalCommentsFromPanel());

        List<AssessmentOutcomeReasonDTO> assessmentOutcomeReasonDTOList = Lists.newArrayList();
        if (outcome != null) {
          Set<Reason> outcomeReasons = outcome.getReasons();
          // check if selected outcome has reasons and if outcome reason missing in excel then alert it
          if (!CollectionUtils.isEmpty(outcomeReasons) && StringUtils
              .isEmpty(assessmentXLS.getOutcomeNotAssessed())) {
            assessmentXLS.addErrorMessage(String
                .format(OUTCOME_REASON_IS_REQUIRED_FOR_OUTCOME_S, assessmentXLS.getOutcome()));
          } else if (!CollectionUtils.isEmpty(outcomeReasons)) {
            String[] notAssessedReasons = assessmentXLS.getOutcomeNotAssessed().split(SEMI_COLON);
            Arrays.stream(notAssessedReasons).forEach(notAssessedReason -> {
              Reason assessmentReason = outcomeReasons.stream().
                  filter(or -> or.getLabel().equalsIgnoreCase(notAssessedReason.trim())).findAny()
                  .orElse(null);
              if (assessmentReason != null) {
                AssessmentOutcomeReasonDTO assessmentOutcomeReasonDTO = new AssessmentOutcomeReasonDTO();
                assessmentOutcomeReasonDTO.setReasonLabel(assessmentReason.getLabel());
                assessmentOutcomeReasonDTO.setReasonId(assessmentReason.getId());
                assessmentOutcomeReasonDTO.setReasonCode(assessmentReason.getCode());
                assessmentOutcomeReasonDTO.setRequireOther(assessmentReason.isRequireOther());
                if (assessmentReason.isRequireOther()) {
                  if (!StringUtils.isEmpty(assessmentXLS.getOutcomeNotAssessedOther())) {
                    assessmentOutcomeReasonDTO.setOther(assessmentXLS.getOutcomeNotAssessedOther());
                  } else {
                    assessmentXLS.addErrorMessage(OTHER_REASON_IS_REQUIRED);
                  }
                }
                assessmentOutcomeReasonDTOList.add(assessmentOutcomeReasonDTO);
              } else {
                assessmentXLS.addErrorMessage(ASSESSMENT_REASON_NOT_FOUND);
              }
            });

          }
        }
        assessmentOutcomeDTO.setReasons(assessmentOutcomeReasonDTOList);

      }
      assessmentDTO.setOutcome(assessmentOutcomeDTO);

      String duplicateAssessments = getAnyDuplicateAssessmentsMessage(assessmentDTO);
      if (!duplicateAssessments.isEmpty()) {
        assessmentXLS.addErrorMessage(duplicateAssessments);
      }

      //Revalidation
      RevalidationDTO revalidationDTO = new RevalidationDTO();
      revalidationDTO
          .setKnownConcerns(BooleanUtil.parseBooleanObject(assessmentXLS.getKnownConcerns()));
      revalidationDTO.setConcernSummary(assessmentXLS.getConcernSummary());
      revalidationDTO.setResponsibleOfficerComments(assessmentXLS.getResponsibleOfficerComments());
      assessmentDTO.setRevalidation(revalidationDTO);

      saveAssessment(personBasicDetailsDTO, assessmentXLS, assessmentDTO);

    }
  }


  private Optional<PersonBasicDetailsDTO> getPersonBasicDetailsDTOFromRegNumber(
      Map<String, PersonDTO> phnDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByPH,
      Map<String, GdcDetailsDTO> gdcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGDC,
      Map<String, GmcDetailsDTO> gmcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGMC,
      AssessmentXLS assessmentXLS) {
    if (!StringUtils.isEmpty(getGdcNumber.apply(assessmentXLS))) {
      return getPersonBasicDetailsDTO(getGdcNumber, gdcDetailsMap, pbdMapByGDC, assessmentXLS,
          GdcDetailsDTO::getId);
    } else if (!StringUtils.isEmpty(getGmcNumber.apply(assessmentXLS))) {
      return getPersonBasicDetailsDTO(getGmcNumber, gmcDetailsMap, pbdMapByGMC, assessmentXLS,
          GmcDetailsDTO::getId);
    } else if (!StringUtils.isEmpty(getPhNumber.apply(assessmentXLS))) {
      return getPersonBasicDetailsDTO(getPhNumber, phnDetailsMap, pbdMapByPH, assessmentXLS,
          PersonDTO::getId);
    } else {
      assessmentXLS.addErrorMessage(
          ONE_OF_3_REG_NUMBERS_SHOULD_BE_PROVIDED_TO_IDENTIFY_A_PERSON);
      return Optional.empty();
    }
  }

  public void saveAssessment(PersonBasicDetailsDTO personBasicDetailsDTO,
      AssessmentXLS assessmentXLS, AssessmentDTO assessmentDTO) {

    if (!assessmentXLS.hasErrors() && personBasicDetailsDTO.getId() != null) {
      Long traineeId = personBasicDetailsDTO.getId();
      AssessmentDTO savedAssessment = assessmentServiceImpl
          .createTraineeAssessment(assessmentDTO, traineeId);
      if (savedAssessment != null && savedAssessment.getId() != null) {
        Long savedAssessmentId = savedAssessment.getId();
        // save Assessment Detail
        AssessmentDetailDTO assessmentDetailDTO = assessmentDTO.getDetail();
        assessmentServiceImpl
            .createTraineeAssessmentDetails(assessmentDetailDTO, traineeId, savedAssessmentId);

        // save Assessment Outcome
        AssessmentOutcomeDTO assessmentOutcomeDTO = assessmentDTO.getOutcome();
        if (assessmentOutcomeDTO != null) {
          assessmentServiceImpl
              .createTraineeAssessmentOutcome(assessmentOutcomeDTO, traineeId, savedAssessmentId);
        }

        // save Assessment Reason
        RevalidationDTO revalidationDTO = assessmentDTO.getRevalidation();
        assessmentServiceImpl
            .createTraineeAssessmentRevalidation(revalidationDTO, traineeId, savedAssessmentId);
      }
      assessmentXLS.setSuccessfullyImported(true);
    }
  }


  <DTO> Optional<PersonBasicDetailsDTO> getPersonBasicDetailsDTO(
      Function<AssessmentXLS, String> getRegNumber, Map<String, DTO> regNumberDetailsMap,
      Map<Long, PersonBasicDetailsDTO> pbdMapByRegNumber, AssessmentXLS assessmentXLS,
      Function<DTO, Long> getId) {
    DTO regNumberDTO = regNumberDetailsMap.get(getRegNumber.apply(assessmentXLS));
    if (regNumberDTO != null) {
      return Optional.of(pbdMapByRegNumber.get(getId.apply(regNumberDTO)));
    } else {
      assessmentXLS.addErrorMessage(
          DID_NOT_FIND_A_PERSON_FOR_REGISTRATION_NUMBER + getRegNumber.apply(assessmentXLS));
      return Optional.empty();
    }
  }


  //TODO optimise these to be Fetcher like
  private Map<String, GradeDTO> getGradeDTOMap(List<AssessmentXLS> assessmentXLSList) {
    Set<String> gradeNames = assessmentXLSList.stream()
        .map(AssessmentXLS::getNextRotationGradeName)
        .filter(StringUtils::isNotEmpty)
        .collect(Collectors.toSet());
    Map<String, GradeDTO> gradeMapByName = new HashMap<>();
    for (String gradeName : gradeNames) {
      List<GradeDTO> gradesByName = referenceServiceImpl.findGradesByName(gradeName);
      if (!gradesByName.isEmpty() && gradesByName.size() == 1) {
        gradeMapByName.put(gradeName, gradesByName.get(0));
      } else {
        assessmentXLSList.stream().filter(
            assessmentXLS -> {
              String nextRotationGradeName = assessmentXLS.getNextRotationGradeName();
              return nextRotationGradeName != null
                  && StringUtils.equalsIgnoreCase(nextRotationGradeName, gradeName);
            })
            .forEach(xls -> {
              logger.error(String.format(EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR, gradeName));
              xls.addErrorMessage(String.format(EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR, gradeName));
            });
      }
    }
    return gradeMapByName;
  }

  private Set<String> collectRegNumbersForAssessments(List<AssessmentXLS> assessmentXLS,
      Function<AssessmentXLS, String> extractRegistrationNumber) {
    return assessmentXLS.stream()
        .map(extractRegistrationNumber::apply)
        .collect(Collectors.toSet());
  }

  private List<AssessmentXLS> getRowsWithRegistrationNumberForAssessments(
      List<AssessmentXLS> assessmentXLS,
      Function<AssessmentXLS, String> extractRegistrationNumber) {
    return assessmentXLS.stream()
        .filter(xls -> {
          String regNumber = extractRegistrationNumber.apply(xls);
          return !"unknown".equalsIgnoreCase(regNumber) && !StringUtils.isEmpty(regNumber);
        })
        .collect(Collectors.toList());
  }

  private ProgrammeMembershipCurriculaDTO getProgrammeMembershipCurriculaDTO(Long traineeId,
      AssessmentXLS assessmentXLS,
      Function<Long, List<ProgrammeMembershipCurriculaDTO>> getProgrammeMembershipForTrainee) {
    ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDTO = null;
    String programmeName = assessmentXLS.getProgrammeName();
    String programmeNumber = assessmentXLS.getProgrammeNumber();
    String curriculumName = assessmentXLS.getCurriculumName();

    if (!StringUtils.isEmpty(programmeName) || !StringUtils.isEmpty(programmeNumber)) {
      if (StringUtils.isEmpty(programmeName)) {
        assessmentXLS.addErrorMessage(String.format(PROGRAMME_NAME_NOT_SPECIFIED, programmeName));
      } else if (StringUtils.isEmpty(programmeNumber)) {
        assessmentXLS
            .addErrorMessage(String.format(PROGRAMME_NUMBER_NOT_SPECIFIED, programmeNumber));
      }
    }
    if (traineeId != null) {
      List<ProgrammeMembershipCurriculaDTO> programmeMembershipCurriculaDTOList = getProgrammeMembershipForTrainee
          .apply(traineeId);
      if (!CollectionUtils.isEmpty(programmeMembershipCurriculaDTOList)) {
        //Match best possible programme membership
        for (ProgrammeMembershipCurriculaDTO dto : programmeMembershipCurriculaDTOList) {
          if (dto.getProgrammeName().equalsIgnoreCase(programmeName)
              && dto.getProgrammeNumber().equalsIgnoreCase(programmeNumber)
              && dto.getCurriculumDTO().getName().equalsIgnoreCase(curriculumName)) {
            programmeMembershipCurriculaDTO = dto;
          }
        }
      } else {
        assessmentXLS.addErrorMessage(PROGRAMME_CURRICULUM_INFO_NOT_FOUND);
      }
    } else {
      assessmentXLS.addErrorMessage(TRAINEE_NOT_FOUND);
    }
    return programmeMembershipCurriculaDTO;
  }

}
