package com.transformuk.hee.tis.genericupload.service.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transformuk.hee.tis.assessment.api.dto.*;
import com.transformuk.hee.tis.assessment.client.service.impl.AssessmentServiceImpl;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentXLS;
import com.transformuk.hee.tis.genericupload.service.service.fetcher.*;
import com.transformuk.hee.tis.genericupload.service.util.BooleanUtil;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.*;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class AssessmentTransformerService {
  private static final Logger logger = getLogger(AssessmentTransformerService.class);

  private static final String AT_LEAST_ONE_OF_THE_3_REGISTRATION_NUMBERS_SHOULD_BE_PROVIDED_TO_IDENTIFY_A_PERSON = "At least one of the 3 registration numbers should be provided to identify a person";
  private static final String SURNAME_DOES_NOT_MATCH_LAST_NAME_OBTAINED_VIA_REGISTRATION_NUMBER = "Surname does not match last name obtained via registration number";
  private static final String DID_NOT_FIND_A_PERSON_FOR_REGISTRATION_NUMBER = "Did not find a person for registration number : ";
  private static final String MULTIPLE_OR_NO_GRADES_FOUND_FOR = "Multiple or no grades found for  : ";
  private static final String EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR = "Expected to find a single grade for : %s";
  private static final String PROGRAMME_NAME_NOT_SPECIFIED = "Programme name (%s) has not been specified. Both programme name and number are needed to identify the programme";
  private static final String PROGRAMME_NUMBER_NOT_SPECIFIED = "Programme number (%s) has not been specified. Both programme name and number are needed to identify the programme";

  private static final String AT_LEAST_ONE_OF_THE_THREE_REGISTRATION_NUMBERS_NEEDS_TO_BE_SPECIFIED = "At least one of the three registration numbers needs to be specified";

  private static final String DID_NOT_FIND_PROGRAMME_CURRICULUM = "Did not find Programme curriculum";
  private static final String DAYS_OUT_OF_TRAINING_SHOULD_BE_NUMERIC = "Days out of training should be numeric";
  private static final String MONTHS_OOPR_OOPT_COUNTED_TOWARDS_TRAINING_SHOULD_BE_NUMERIC = "Months OOPR/OOPT counted towards training should be numeric";
  private static final String GIVEN_OUTCOME_IS_NOT_VALID = "Given outcome is not valid";
  public static final String OUTCOME_REASON_IS_REQUIRED_FOR_OUTCOME_S = "Outcome reason is required for outcome : %s";
  public static final String OTHER_REASON_IS_REQUIRED = "Other reason is required";

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
  private Map<String, Long> outcomeMap;
  private Map<Long, Map<String, AssessmentReason>> outcomeReasonMap;
  Function<AssessmentXLS, String> getPhNumber = AssessmentXLS::getPublicHealthNumber;
  Function<AssessmentXLS, String> getGdcNumber = AssessmentXLS::getGdcNumber;
  Function<AssessmentXLS, String> getGmcNumber = AssessmentXLS::getGmcNumber;


  @PostConstruct
  public void initialiseFetchers() {
    this.gmcDtoFetcher = new GMCDTOFetcher(tcsServiceImpl);
    this.gdcDtoFetcher = new GDCDTOFetcher(tcsServiceImpl);
    this.pbdDtoFetcher = new PersonBasicDetailsDTOFetcher(tcsServiceImpl);
    this.peopleByPHNFetcher = new PeopleByPHNFetcher(tcsServiceImpl);
    this.outcomeMap = getOutcomeMap();
    this.outcomeReasonMap = getOutcomeReasonMap();
  }

  //TODO: remove hard coding, as its require assessment BE and FE changes because Outcome model is not in API
  private Map<String, Long> getOutcomeMap() {
    Map<String, Long> outcome = Maps.newHashMap();
    outcome.put("Not Assessed", 1L);
    outcome.put("1", 2L);
    outcome.put("2", 3L);
    outcome.put("3", 4L);
    outcome.put("4", 5L);
    outcome.put("5", 6L);
    outcome.put("6", 7L);
    outcome.put("6R", 8L);
    outcome.put("7", 9L);
    outcome.put("8", 10L);
    outcome.put("9", 11L);
    return outcome;
  }

  //TODO: remove hard coding, as its require assessment BE and FE changes because Reason model is not in API
  private Map<Long, Map<String, AssessmentReason>> getOutcomeReasonMap() {
    Map<Long, Map<String, AssessmentReason>> outcomeReason = Maps.newHashMap();
    Map<String, AssessmentReason> reason1 = Maps.newHashMap();
    reason1.put("Trainee sick leave", new AssessmentReason(11L, "N1", "Trainee sick leave", false, false));
    reason1.put("Trainee maternity/paternity leave", new AssessmentReason(12L, "N2", "Trainee maternity/paternity leave", false, false));
    reason1.put("Trainee not in post long enough", new AssessmentReason(13L, "N3", "Trainee not in post long enough", false, false));
    reason1.put("Trainee fell outside annual reporting period", new AssessmentReason(14L, "N4", "Trainee fell outside annual reporting period", false, false));
    reason1.put("Trainee post-CCT", new AssessmentReason(15L, "N5", "Trainee post-CCT", false, false));
    reason1.put("Trainee missed review", new AssessmentReason(16L, "N6", "Trainee missed review", false, false));
    reason1.put("Trainee inter-Deanery transfer", new AssessmentReason(17L, "N7", "Trainee inter-Deanery transfer", false, false));
    reason1.put("Trainee reviewed in other Deanery", new AssessmentReason(18L, "N8", "Trainee reviewed in other Deanery", false, false));
    reason1.put("Trainee contract termination", new AssessmentReason(19L, "N9", "Trainee contract termination", false, false));
    reason1.put("Trainee gross misconduct", new AssessmentReason(20L, "N10", "Trainee gross misconduct", false, false));
    reason1.put("Trainee suspension", new AssessmentReason(21L, "N11", "Trainee suspension", false, false));
    reason1.put("Other reason (please specify)", new AssessmentReason(22L, "N13", "Other reason (please specify)", true, false));
    reason1.put("LTFT achieving progress at the expected rate", new AssessmentReason(23L, "N14", "LTFT achieving progress at the expected rate", false, false));
    reason1.put("LTFT not achieving progress at the expected rate", new AssessmentReason(24L, "N15", "LTFT not achieving progress at the expected rate", false, false));
    reason1.put("Dismissed", new AssessmentReason(25L, "N16", "Dismissed", false, false));
    reason1.put("Dismissed no remedial training", new AssessmentReason(26L, "N17", "Dismissed no remedial training", false, false));
    reason1.put("Dismissed received remedial training", new AssessmentReason(27L, "N18", "Dismissed received remedial training", false, false));
    reason1.put("Dismissed no GMC referral", new AssessmentReason(28L, "N19", "Dismissed no GMC referral", false, false));
    reason1.put("Dismissed - following GMC referral", new AssessmentReason(29L, "N20", "Dismissed - following GMC referral", false, false));
    reason1.put("Resignation no remedial training undertaken", new AssessmentReason(30L, "N21", "Resignation no remedial training undertaken", false, false));
    reason1.put("Resignation received remedial training", new AssessmentReason(31L, "N22", "Resignation received remedial training", false, false));
    outcomeReason.put(1L, reason1);

    outcomeReason.put(2L, null);

    Map<String, AssessmentReason> reason3 = Maps.newHashMap();
    reason3.put("Record keeping and evidence", new AssessmentReason(1L, "U1", "Record keeping and evidence", false, false));
    reason3.put("Inadequate experience", new AssessmentReason(2L, "U2", "Inadequate experience", false, false));
    reason3.put("No engagement with supervisor", new AssessmentReason(3L, "U3", "No engagement with supervisor", false, false));
    reason3.put("Trainer absence", new AssessmentReason(4L, "U4", "Trainer absence", false, false));
    reason3.put("Single exam failure", new AssessmentReason(5L, "U5", "Single exam failure", false, false));
    reason3.put("Continual exam failure", new AssessmentReason(6L, "U6", "Continual exam failure", false, false));
    reason3.put("Trainee requires Deanery support", new AssessmentReason(7L, "U7", "Trainee requires Deanery support", false, false));
    reason3.put("Other reason (please specify)", new AssessmentReason(8L, "U8", "Other reason (please specify)", true, true));
    reason3.put("Inadequate attendance", new AssessmentReason(9L, "U9", "Inadequate attendance", false, false));
    reason3.put("Assessment/Curriculum outcomes not achieved", new AssessmentReason(10L, "U10", "Assessment/Curriculum outcomes not achieved", false, false));

    outcomeReason.put(3L, reason3);

    outcomeReason.put(4L, reason3);

    outcomeReason.put(5L, reason3);

    outcomeReason.put(6L, null);

    outcomeReason.put(7L, null);

    outcomeReason.put(8L, null);
    outcomeReason.put(9L, null);
    outcomeReason.put(10L, null);
    outcomeReason.put(11L, null);
    return outcomeReason;
  }


  <DTO> Map<String, DTO> buildRegNumberDetailsMap(List<AssessmentXLS> assessmentXLS, Function<AssessmentXLS, String> getRegNumberFunction, DTOFetcher<String, DTO> fetcher) {
    return fetcher.findWithKeys(
            collectRegNumbersForAssessments(
                    getRowsWithRegistrationNumberForAssessments(assessmentXLS, getRegNumberFunction),
                    getRegNumberFunction));
  }

  <DTO> Map<Long, PersonBasicDetailsDTO> buildPersonBasicDetailsMapForRegNumber(Map<String, DTO> regNumberMap, DTOFetcher<String, DTO> idExtractingFetcher, Function<DTO, Long> getId) {
    return regNumberMap.isEmpty() ? null : pbdDtoFetcher.findWithKeys(idExtractingFetcher.extractIds(regNumberMap, getId));
  }

  void processAssessmentsUpload(List<AssessmentXLS> assessmentXLSList) {
    assessmentXLSList.forEach(AssessmentXLS::initialiseSuccessfullyImported);
    markRowsWithoutRegistrationNumbers(assessmentXLSList);

    if (!CollectionUtils.isEmpty(assessmentXLSList)) {
      Map<String, PersonDTO> phnDetailsMap = buildRegNumberDetailsMap(assessmentXLSList, getPhNumber, peopleByPHNFetcher);
      Map<Long, PersonBasicDetailsDTO> pbdMapByPH = buildPersonBasicDetailsMapForRegNumber(phnDetailsMap, peopleByPHNFetcher, PersonDTO::getId);

      Map<String, GdcDetailsDTO> gdcDetailsMap = buildRegNumberDetailsMap(assessmentXLSList, getGdcNumber, gdcDtoFetcher);
      Map<Long, PersonBasicDetailsDTO> pbdMapByGDC = buildPersonBasicDetailsMapForRegNumber(gdcDetailsMap, gdcDtoFetcher, GdcDetailsDTO::getId);

      Map<String, GmcDetailsDTO> gmcDetailsMap = buildRegNumberDetailsMap(assessmentXLSList, getGmcNumber, gmcDtoFetcher);
      Map<Long, PersonBasicDetailsDTO> pbdMapByGMC = buildPersonBasicDetailsMapForRegNumber(gmcDetailsMap, gmcDtoFetcher, GmcDetailsDTO::getId);

      Map<String, GradeDTO> gradeMapByName = getGradeDTOMap(assessmentXLSList);

      for (AssessmentXLS assessmentXLS : assessmentXLSList) {
        useMatchingCriteriaToCreateAssessment(phnDetailsMap, pbdMapByPH, gdcDetailsMap, pbdMapByGDC, gmcDetailsMap, pbdMapByGMC, gradeMapByName, assessmentXLS);
      }
    }
  }

  private void markRowsWithoutRegistrationNumbers(List<AssessmentXLS> assessmentXLSList) {
    assessmentXLSList.stream()
            .filter(xls ->
                    xls.getGmcNumber() == null &&
                            xls.getGdcNumber() == null &&
                            xls.getPublicHealthNumber() == null)
            .forEach(xls -> xls.addErrorMessage(AT_LEAST_ONE_OF_THE_THREE_REGISTRATION_NUMBERS_NEEDS_TO_BE_SPECIFIED));
  }


  private void useMatchingCriteriaToCreateAssessment(Map<String, PersonDTO> phnDetailsMap,
                                                     Map<Long, PersonBasicDetailsDTO> pbdMapByPH, Map<String, GdcDetailsDTO> gdcDetailsMap,
                                                     Map<Long, PersonBasicDetailsDTO> pbdMapByGDC, Map<String, GmcDetailsDTO> gmcDetailsMap,
                                                     Map<Long, PersonBasicDetailsDTO> pbdMapByGMC,
                                                     Map<String, GradeDTO> gradeMapByName, AssessmentXLS assessmentXLS) {
    Optional<PersonBasicDetailsDTO> personBasicDetailsDTOOptional = getPersonBasicDetailsDTOFromRegNumber(phnDetailsMap, pbdMapByPH, gdcDetailsMap, pbdMapByGDC, gmcDetailsMap, pbdMapByGMC, assessmentXLS);

    if (personBasicDetailsDTOOptional.isPresent()) {
      PersonBasicDetailsDTO personBasicDetailsDTO = personBasicDetailsDTOOptional.get();

      if (!assessmentXLS.getSurname().equalsIgnoreCase(personBasicDetailsDTO.getLastName())) {
        assessmentXLS.addErrorMessage(SURNAME_DOES_NOT_MATCH_LAST_NAME_OBTAINED_VIA_REGISTRATION_NUMBER);
      }

      String grade = assessmentXLS.getNextRotationGradeName();
      if (!StringUtils.isEmpty(grade) && !gradeMapByName.containsKey(grade)) {
        assessmentXLS.addErrorMessage(MULTIPLE_OR_NO_GRADES_FOUND_FOR + grade);
      }

      ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDTO = getProgrammeMembershipCurriculaDTO(personBasicDetailsDTO.getId(),
              assessmentXLS.getProgrammeName(), assessmentXLS.getProgrammeNumber(), assessmentXLS.getCurriculumName(), tcsServiceImpl::getProgrammeMembershipForTrainee);
      AssessmentDTO assessmentDTO = new AssessmentDTO();
      assessmentDTO.setFirstName(personBasicDetailsDTO.getFirstName());
      assessmentDTO.setLastName(personBasicDetailsDTO.getLastName());
      assessmentDTO.setTraineeId(personBasicDetailsDTO.getId());
      assessmentDTO.setType(assessmentXLS.getType());
      if (!StringUtils.isEmpty(assessmentXLS.getStatus())) {
        assessmentDTO.setEventStatus(EventStatus.valueOf(assessmentXLS.getStatus()));
      }

      if (programmeMembershipCurriculaDTO != null) {
        assessmentDTO.setProgrammeName(programmeMembershipCurriculaDTO.getProgrammeName());
        assessmentDTO.setProgrammeId(programmeMembershipCurriculaDTO.getProgrammeId());
        assessmentDTO.setProgrammeNumber(programmeMembershipCurriculaDTO.getProgrammeNumber());
      } else {
        assessmentXLS.addErrorMessage(DID_NOT_FIND_PROGRAMME_CURRICULUM);
      }
      assessmentDTO.setReviewDate(convertDate(assessmentXLS.getReviewDate()));

      // Assessment Details
      AssessmentDetailDTO assessmentDetailDTO = new AssessmentDetailDTO();
      if (programmeMembershipCurriculaDTO != null && programmeMembershipCurriculaDTO.getCurriculumMemberships() != null) {
        CurriculumDTO curriculumDTO = programmeMembershipCurriculaDTO.getCurriculumDTO();
        assessmentDetailDTO.setCurriculumId(curriculumDTO.getId());
        assessmentDetailDTO.setCurriculumName(curriculumDTO.getName());
        assessmentDetailDTO.setCurriculumStartDate(programmeMembershipCurriculaDTO.getCurriculumMemberships().get(0).getCurriculumStartDate());
        assessmentDetailDTO.setCurriculumEndDate(programmeMembershipCurriculaDTO.getCurriculumMemberships().get(0).getCurriculumEndDate());
        assessmentDetailDTO.setCurriculumSpecialtyId(String.valueOf(curriculumDTO.getSpecialty().getId()));
        assessmentDetailDTO.setCurriculumSpecialty(String.valueOf(curriculumDTO.getSpecialty().getName()));
        assessmentDetailDTO.setCurriculumSubType(curriculumDTO.getCurriculumSubType().name());
      }
      assessmentDetailDTO.setPortfolioReviewDate(convertDate(assessmentXLS.getPortfolioReviewDate()));
      if (NumberUtils.isDigits(assessmentXLS.getDaysOutOfTraining())) {
        assessmentDetailDTO.setDaysOutOfTraining(Integer.parseInt(assessmentXLS.getDaysOutOfTraining()));
      } else {
        assessmentXLS.addErrorMessage(DAYS_OUT_OF_TRAINING_SHOULD_BE_NUMERIC);
      }
      assessmentDetailDTO.setPeriodCoveredFrom(convertDate(assessmentXLS.getPeriodCoveredFrom()));
      assessmentDetailDTO.setPeriodCoveredTo(convertDate(assessmentXLS.getPeriodCoveredTo()));
      if (NumberUtils.isDigits(assessmentXLS.getMonthsCountedToTraining())) {
        assessmentDetailDTO.setMonthsCountedToTraining(Integer.parseInt(assessmentXLS.getMonthsCountedToTraining()));
      } else {
        assessmentXLS.addErrorMessage(MONTHS_OOPR_OOPT_COUNTED_TOWARDS_TRAINING_SHOULD_BE_NUMERIC);
      }
      assessmentDetailDTO.setPya(BooleanUtil.parseBooleanObject(assessmentXLS.getPya()));
      assessmentDTO.detail(assessmentDetailDTO);

      // Outcome
      AssessmentOutcomeDTO assessmentOutcomeDTO = null;
      if (!StringUtils.isEmpty(assessmentXLS.getOutcome())) {
        assessmentOutcomeDTO = new AssessmentOutcomeDTO();
        Long outcomeId = this.outcomeMap.get(assessmentXLS.getOutcome());
        if (outcomeId != null) {
          assessmentOutcomeDTO.setOutcomeId(this.outcomeMap.get(assessmentXLS.getOutcome()));
        } else {
          assessmentXLS.addErrorMessage(GIVEN_OUTCOME_IS_NOT_VALID);
        }
        assessmentOutcomeDTO.setOutcome(assessmentXLS.getOutcome());
        assessmentOutcomeDTO.setUnderAppeal(BooleanUtil.parseBooleanObject(assessmentXLS.getUnderAppeal()));
        assessmentOutcomeDTO.setAcademicOutcome(assessmentXLS.getAcademicOutcome());
        assessmentOutcomeDTO.setExternalTrainer(BooleanUtil.parseBooleanObject(assessmentXLS.getExternalTrainer()));
        // Assessment outcome reasons

        GradeDTO gradeDTO = gradeMapByName.get(assessmentXLS.getNextRotationGradeName());
        if (gradeDTO != null) {
          assessmentOutcomeDTO.setNextRotationGradeName(gradeDTO.getName());
          assessmentOutcomeDTO.setNextRotationGradeAbbr(gradeDTO.getAbbreviation());
          assessmentOutcomeDTO.setNextRotationGradeId(gradeDTO.getId());
        }
        assessmentOutcomeDTO.setTraineeNotifiedOfOutcome(BooleanUtil.parseBooleanObject(assessmentXLS.getTraineeNotifiedOfOutcome()));
        assessmentOutcomeDTO.setNextReviewDate(convertDate(assessmentXLS.getNextReviewDate()));
        assessmentOutcomeDTO.setComments(assessmentXLS.getComments());
        assessmentOutcomeDTO.setTenPercentAudit(BooleanUtil.parseBooleanObject(assessmentXLS.getTenPercentAudit()));
        assessmentOutcomeDTO.setDetailedReasons(assessmentXLS.getDetailedReasons());
        assessmentOutcomeDTO.setMitigatingCircumstances(assessmentXLS.getMitigatingCircumstances());
        assessmentOutcomeDTO.setCompetencesToBeDeveloped(assessmentXLS.getCompetencesToBeDeveloped());
        assessmentOutcomeDTO.setOtherRecommendedActions(assessmentXLS.getOtherRecommendedActions());
        assessmentOutcomeDTO.setRecommendedAdditionalTrainingTime(assessmentXLS.getRecommendedAdditionalTrainingTime());
        assessmentOutcomeDTO.setAdditionalCommentsFromPanel(assessmentXLS.getAdditionalCommentsFromPanel());
        Map<String, AssessmentReason> reason = outcomeReasonMap.get(outcomeId);
        List<AssessmentOutcomeReasonDTO> assessmentOutcomeReasonDTOList = Lists.newArrayList();

        if (reason != null && StringUtils.isEmpty(assessmentXLS.getOutcomeNotAssessed())) {
          assessmentXLS.addErrorMessage(String.format(OUTCOME_REASON_IS_REQUIRED_FOR_OUTCOME_S, assessmentXLS.getOutcome()));
        } else if (reason != null) {
          AssessmentReason assessmentReason = reason.get(assessmentXLS.getOutcomeNotAssessed());
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
          }
        }
        assessmentOutcomeDTO.setReasons(assessmentOutcomeReasonDTOList);

      }
      assessmentDTO.setOutcome(assessmentOutcomeDTO);

      //Revalidation
      RevalidationDTO revalidationDTO = new RevalidationDTO();
      revalidationDTO.setKnownConcerns(BooleanUtil.parseBooleanObject(assessmentXLS.getKnownConcerns()));
      revalidationDTO.setConcernSummary(assessmentXLS.getConcernSummary());
      revalidationDTO.setResponsibleOfficerComments(assessmentXLS.getResponsibleOfficerComments());
      assessmentDTO.setRevalidation(revalidationDTO);

      saveAssessment(personBasicDetailsDTO, assessmentXLS, assessmentDTO);

    }
  }


  private Optional<PersonBasicDetailsDTO> getPersonBasicDetailsDTOFromRegNumber(Map<String, PersonDTO> phnDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByPH, Map<String, GdcDetailsDTO> gdcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGDC, Map<String, GmcDetailsDTO> gmcDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByGMC, AssessmentXLS assessmentXLS) {
    if (!StringUtils.isEmpty(getGdcNumber.apply(assessmentXLS))) {
      return getPersonBasicDetailsDTO(getGdcNumber, gdcDetailsMap, pbdMapByGDC, assessmentXLS, GdcDetailsDTO::getId);
    } else if (!StringUtils.isEmpty(getGmcNumber.apply(assessmentXLS))) {
      return getPersonBasicDetailsDTO(getGmcNumber, gmcDetailsMap, pbdMapByGMC, assessmentXLS, GmcDetailsDTO::getId);
    } else if (!StringUtils.isEmpty(getPhNumber.apply(assessmentXLS))) {
      return getPersonBasicDetailsDTO(getPhNumber, phnDetailsMap, pbdMapByPH, assessmentXLS, PersonDTO::getId);
    } else {
      assessmentXLS.addErrorMessage(AT_LEAST_ONE_OF_THE_3_REGISTRATION_NUMBERS_SHOULD_BE_PROVIDED_TO_IDENTIFY_A_PERSON);
      return Optional.empty();
    }
  }

  public void saveAssessment(PersonBasicDetailsDTO personBasicDetailsDTO, AssessmentXLS assessmentXLS, AssessmentDTO assessmentDTO) {

    if (!assessmentXLS.hasErrors() && personBasicDetailsDTO.getId() != null) {
      Long traineeId = personBasicDetailsDTO.getId();
      AssessmentDTO savedAssessment = assessmentServiceImpl.createTraineeAssessment(assessmentDTO, traineeId);
      if (savedAssessment != null && savedAssessment.getId() != null) {
        Long savedAssessmentId = savedAssessment.getId();
        // save Assessment Detail
        AssessmentDetailDTO assessmentDetailDTO = assessmentDTO.getDetail();
        assessmentServiceImpl.createTraineeAssessmentDetails(assessmentDetailDTO, traineeId, savedAssessmentId);

        // save Assessment Outcome
        AssessmentOutcomeDTO assessmentOutcomeDTO = assessmentDTO.getOutcome();
        if(assessmentOutcomeDTO != null) {
          assessmentServiceImpl.createTraineeAssessmentOutcome(assessmentOutcomeDTO, traineeId, savedAssessmentId);
        }

        // save Assessment Reason
        RevalidationDTO revalidationDTO = assessmentDTO.getRevalidation();
        assessmentServiceImpl.createTraineeAssessmentRevalidation(revalidationDTO, traineeId, savedAssessmentId);
      }
      assessmentXLS.setSuccessfullyImported(true);
    }
  }


  <DTO> Optional<PersonBasicDetailsDTO> getPersonBasicDetailsDTO(Function<AssessmentXLS, String> getRegNumber, Map<String, DTO> regNumberDetailsMap, Map<Long, PersonBasicDetailsDTO> pbdMapByRegNumber, AssessmentXLS assessmentXLS, Function<DTO, Long> getId) {
    DTO regNumberDTO = regNumberDetailsMap.get(getRegNumber.apply(assessmentXLS));
    if (regNumberDTO != null) {
      return Optional.of(pbdMapByRegNumber.get(getId.apply(regNumberDTO)));
    } else {
      assessmentXLS.addErrorMessage(DID_NOT_FIND_A_PERSON_FOR_REGISTRATION_NUMBER + getRegNumber.apply(assessmentXLS));
      return Optional.empty();
    }
  }


  //TODO optimise these to be Fetcher like
  private Map<String, GradeDTO> getGradeDTOMap(List<AssessmentXLS> assessmentXLSList) {
    Set<String> gradeNames = assessmentXLSList.stream()
            .filter(assessmentXLS -> !StringUtils.isEmpty(assessmentXLS.getNextRotationGradeName()))
            .map(AssessmentXLS::getNextRotationGradeName)
            .collect(Collectors.toSet());
    Map<String, GradeDTO> gradeMapByName = new HashMap<>();
    for (String gradeName : gradeNames) {
      List<GradeDTO> gradesByName = referenceServiceImpl.findGradesByName(gradeName);
      if (!gradesByName.isEmpty() && gradesByName.size() == 1) {
        gradeMapByName.put(gradeName, gradesByName.get(0));
      } else {
        assessmentXLSList.stream().filter(assessmentXLS -> assessmentXLS.getNextRotationGradeName().equalsIgnoreCase(gradeName)).forEach(xls -> {
          logger.error(String.format(EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR, gradeName));
          xls.addErrorMessage(String.format(EXPECTED_TO_FIND_A_SINGLE_GRADE_FOR, gradeName));
        });
      }
    }
    return gradeMapByName;
  }


  private Set<String> collectRegNumbersForAssessments(List<AssessmentXLS> assessmentXLS, Function<AssessmentXLS, String> extractRegistrationNumber) {
    return assessmentXLS.stream()
            .map(extractRegistrationNumber::apply)
            .collect(Collectors.toSet());
  }

  private List<AssessmentXLS> getRowsWithRegistrationNumberForAssessments(List<AssessmentXLS> assessmentXLS, Function<AssessmentXLS, String> extractRegistrationNumber) {
    return assessmentXLS.stream()
            .filter(xls -> {
              String regNumber = extractRegistrationNumber.apply(xls);
              return !"unknown".equalsIgnoreCase(regNumber) && !StringUtils.isEmpty(regNumber);
            })
            .collect(Collectors.toList());
  }

  private ProgrammeMembershipCurriculaDTO getProgrammeMembershipCurriculaDTO(Long traineeId, String programmeName, String programmeNumber,
                                                                             String curriculumName, Function<Long, List<ProgrammeMembershipCurriculaDTO>> getProgrammeMembershipForTrainee) {
    ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDTO = null;
    if (!StringUtils.isEmpty(programmeName) || !StringUtils.isEmpty(programmeNumber)) {
      if (StringUtils.isEmpty(programmeName)) {
        throw new IllegalArgumentException(String.format(PROGRAMME_NAME_NOT_SPECIFIED, programmeName));
      } else if (StringUtils.isEmpty(programmeNumber)) {
        throw new IllegalArgumentException(String.format(PROGRAMME_NUMBER_NOT_SPECIFIED, programmeNumber));
      }
    }
    if (traineeId != null) {
      List<ProgrammeMembershipCurriculaDTO> programmeMembershipCurriculaDTOList = getProgrammeMembershipForTrainee.apply(traineeId);
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
        throw new IllegalArgumentException("Programme curriculum information not found for given trainee");
      }
    } else {
      throw new IllegalArgumentException("Trainee not found");
    }
    return programmeMembershipCurriculaDTO;
  }

}
