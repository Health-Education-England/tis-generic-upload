package com.transformuk.hee.tis.genericupload.service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import java.io.IOException;
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
  private static final String OUTCOME_REASON_IS_REQUIRED_FOR_OUTCOME_S = "Outcome reason is required for outcome : %s";
  private static final String OTHER_REASON_IS_REQUIRED = "Other reason is required";
  private static final String PROGRAMME_CURRICULUM_INFO_NOT_FOUND = "Programme curriculum information not found for given trainee";
  private static final String TRAINEE_NOT_FOUND = "Trainee information not found";
  private static final String ASSESSMENT_TYPE_IS_REQUIRED = "Assessment type is required";

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
  Function<AssessmentXLS, String> getPhNumber = AssessmentXLS::getPublicHealthNumber;
  Function<AssessmentXLS, String> getGdcNumber = AssessmentXLS::getGdcNumber;
  Function<AssessmentXLS, String> getGmcNumber = AssessmentXLS::getGmcNumber;
  private ObjectMapper objectMapper = new ObjectMapper();


  @PostConstruct
  public void initialiseFetchers() {
    this.gmcDtoFetcher = new GMCDTOFetcher(tcsServiceImpl);
    this.gdcDtoFetcher = new GDCDTOFetcher(tcsServiceImpl);
    this.pbdDtoFetcher = new PersonBasicDetailsDTOFetcher(tcsServiceImpl);
    this.peopleByPHNFetcher = new PeopleByPHNFetcher(tcsServiceImpl);
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

      // initialise all the outcome reasons by calling assessment
      this.allOutcomes = getAllOutcomes();

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

  public Set<Outcome> getAllOutcomes() {
    Set<Outcome> allOutcomes = Sets.newHashSet();
    String jsonAllOutcome = assessmentServiceImpl.getAllOutcomes();
    logger.info("Outcome string: {}", jsonAllOutcome);
    try {
      if (!StringUtils.isEmpty(allOutcomes)) {
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

      if(StringUtils.isEmpty(assessmentXLS.getType())){
        assessmentXLS.addErrorMessage(ASSESSMENT_TYPE_IS_REQUIRED);
      }

      ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDTO = getProgrammeMembershipCurriculaDTO(personBasicDetailsDTO.getId(),
              assessmentXLS, tcsServiceImpl::getProgrammeMembershipForTrainee);
      AssessmentDTO assessmentDTO = new AssessmentDTO();
      assessmentDTO.setFirstName(personBasicDetailsDTO.getFirstName());
      assessmentDTO.setLastName(personBasicDetailsDTO.getLastName());
      assessmentDTO.setTraineeId(personBasicDetailsDTO.getId());
      assessmentDTO.setType(assessmentXLS.getType());
      if (!StringUtils.isEmpty(assessmentXLS.getStatus())) {
        assessmentDTO.setEventStatus(EventStatus.valueOf(StringUtils.capitalize(assessmentXLS.getStatus())));
      }

      if (programmeMembershipCurriculaDTO != null && programmeMembershipCurriculaDTO.getCurriculumMemberships() != null) {
        assessmentDTO.setProgrammeName(programmeMembershipCurriculaDTO.getProgrammeName());
        assessmentDTO.setProgrammeId(programmeMembershipCurriculaDTO.getProgrammeId());
        assessmentDTO.setProgrammeNumber(programmeMembershipCurriculaDTO.getProgrammeNumber());
        assessmentDTO.setProgrammeMembershipId(programmeMembershipCurriculaDTO.getCurriculumMemberships().get(0).getId());
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
      } else if (!StringUtils.isEmpty(assessmentXLS.getDaysOutOfTraining())) {
        assessmentXLS.addErrorMessage(DAYS_OUT_OF_TRAINING_SHOULD_BE_NUMERIC);
      }
      assessmentDetailDTO.setPeriodCoveredFrom(convertDate(assessmentXLS.getPeriodCoveredFrom()));
      assessmentDetailDTO.setPeriodCoveredTo(convertDate(assessmentXLS.getPeriodCoveredTo()));
      if (NumberUtils.isDigits(assessmentXLS.getMonthsCountedToTraining())) {
        assessmentDetailDTO.setMonthsCountedToTraining(Integer.parseInt(assessmentXLS.getMonthsCountedToTraining()));
      } else if (!StringUtils.isEmpty(assessmentXLS.getMonthsCountedToTraining())) {
        assessmentXLS.addErrorMessage(MONTHS_OOPR_OOPT_COUNTED_TOWARDS_TRAINING_SHOULD_BE_NUMERIC);
      }
      assessmentDetailDTO.setPya(BooleanUtil.parseBooleanObject(assessmentXLS.getPya()));
      assessmentDTO.detail(assessmentDetailDTO);

      // Outcome
      AssessmentOutcomeDTO assessmentOutcomeDTO = null;
      if (!StringUtils.isEmpty(assessmentXLS.getOutcome())) {
        assessmentOutcomeDTO = new AssessmentOutcomeDTO();
        Outcome outcome = this.allOutcomes.stream().filter(o -> o.getLabel().equalsIgnoreCase(assessmentXLS.getOutcome())).findAny().orElse(null);
        if (outcome != null && outcome.getId() != null) {
          assessmentOutcomeDTO.setOutcomeId(outcome.getId());
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

        List<AssessmentOutcomeReasonDTO> assessmentOutcomeReasonDTOList = Lists.newArrayList();
        if (outcome != null) {
          Set<Reason> outcomeReasons = outcome.getReasons();

          if (outcomeReasons != null && StringUtils.isEmpty(assessmentXLS.getOutcomeNotAssessed())) {
            assessmentXLS.addErrorMessage(String.format(OUTCOME_REASON_IS_REQUIRED_FOR_OUTCOME_S, assessmentXLS.getOutcome()));
          } else if (outcomeReasons != null) {
            Reason assessmentReason = outcomeReasons.stream().
                    filter(or -> or.getLabel().equalsIgnoreCase(assessmentXLS.getOutcomeNotAssessed())).findAny().orElse(null);
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
        if (assessmentOutcomeDTO != null) {
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

  private ProgrammeMembershipCurriculaDTO getProgrammeMembershipCurriculaDTO(Long traineeId, AssessmentXLS assessmentXLS, Function<Long, List<ProgrammeMembershipCurriculaDTO>> getProgrammeMembershipForTrainee) {
    ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDTO = null;
    String programmeName = assessmentXLS.getProgrammeName();
    String programmeNumber = assessmentXLS.getProgrammeNumber();
    String curriculumName = assessmentXLS.getCurriculumName();

    if (!StringUtils.isEmpty(programmeName) || !StringUtils.isEmpty(programmeNumber)) {
      if (StringUtils.isEmpty(programmeName)) {
        assessmentXLS.addErrorMessage(String.format(PROGRAMME_NAME_NOT_SPECIFIED, programmeName));
      } else if (StringUtils.isEmpty(programmeNumber)) {
        assessmentXLS.addErrorMessage(String.format(PROGRAMME_NUMBER_NOT_SPECIFIED, programmeNumber));
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
        assessmentXLS.addErrorMessage(PROGRAMME_CURRICULUM_INFO_NOT_FOUND);
      }
    } else {
      assessmentXLS.addErrorMessage(TRAINEE_NOT_FOUND);
    }
    return programmeMembershipCurriculaDTO;
  }

}
