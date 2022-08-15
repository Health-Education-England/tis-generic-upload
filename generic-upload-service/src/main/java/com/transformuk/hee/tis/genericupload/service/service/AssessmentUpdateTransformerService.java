package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.assessment.api.dto.AssessmentDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentDetailDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentOutcomeDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentOutcomeReasonDTO;
import com.transformuk.hee.tis.assessment.api.dto.RevalidationDTO;
import com.transformuk.hee.tis.assessment.client.service.impl.AssessmentServiceImpl;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.genericupload.service.util.BooleanUtil;
import com.transformuk.hee.tis.reference.api.dto.AssessmentTypeDto;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipCurriculaDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.CurriculumSubType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class AssessmentUpdateTransformerService {

  public static final String SEMI_COLON = ";";
  public static final int OTHER_REASON_LENGTH_LIMIT = 60;
  public static final String ASSESSMENT_ID_IS_DUPLICATE = "Duplicate TIS_Assessment_ID: %s.";
  public static final String ASSESSMENT_ID_NOT_EXIST = "TIS_Assessment_ID does not exist.";
  public static final String ASSESSMENT_TYPE_NOT_MATCH = "Type does not match a reference value.";
  public static final String MONTHS_OOPR_OOPT_COUNTED_TOWARDS_TRAINING_SHOULD_BE_NUMERIC =
      "Months OOPR/OOPT counted towards training should be numeric.";
  public static final String TIS_PROGRAMME_MEMBERSHIP_ID_SHOULD_BE_NUMERIC =
      "TIS ProgrammeMembership ID should be numeric.";
  public static final String DAYS_OUT_OF_TRAINING_SHOULD_BE_NUMERIC =
      "Days out of training should be numeric.";
  public static final String PROGRAMME_MEMBERSHIP_ID_NOT_MATCH =
      "ProgrammeMembership ID does not match to one of the ProgrammeMembership/s the trainee has.";
  public static final String NO_GRADE_AT_TIME_FOUND = "No grades at time found for: %s.";
  public static final String NO_GRADE_AT_NEXT_ROTATION_FOUND =
      "No grades at next rotation found for: %s.";
  public static final String PERIOD_COVERED_TO_CAN_NOT_BE_BEFORE_PERIOD_COVERED_FROM =
      "Period covered to can not be before Period covered from.";
  public static final String PERIOD_COVERED_FROM_MUST_BE_AFTER_CURRICULUM_START_DATE =
      "Period covered from must be after curriculum start date";
  public static final String PERIOD_COVERED_TO_MUST_BE_BEFORE_CURRICULUM_END_DATE =
      "Period covered to must be before curriculum end date";
  public static final String OUTCOME_CANNOT_BE_IDENTIFIED = "Outcome cannot be identified.";
  public static final String REVIEW_DATE_BEFORE_1753 = "Review date is before year 1753.";
  public static final String PERIOD_COVERED_FROM_DATE_BEFORE_1753 =
      "Period covered from date is below year 1753.";
  public static final String PERIOD_COVERED_TO_DATE_BEFORE_1753 =
      "Period covered to date is below year 1753.";
  public static final String NEXT_REVIEW_DATE_BEFORE_1753 = "Next review date is below year 1753.";
  public static final String OUTCOME_REASON_IS_REQUIRED_FOR_OUTCOME_S =
      "Outcome reason is required for outcome : %s.";
  public static final String OTHER_REASON_IS_REQUIRED = "Other reason is required.";
  public static final String OTHER_REASON_EXCEED_LENGTH_LIMIT =
      "Other reason must be less than 60 characters.";
  public static final String GIVEN_ASSESSMENT_REASON_NOT_FOUND =
      "Given Assessment reason not found for: %s.";
  public static final String NOT_ASSESSED_REASON_IS_REQUIRED =
      "Unsatisfactory Outcome/Not Assessed Reason is required when Other reason is updating";
  public static final String NOT_ASSESSED_REASONS_SHOULD_BE_EMPTY_FOR_OUTCOME =
      "Not assessed reason should be empty for outcome: %s";
  public static final String ACADEMIC_OUTCOME_IS_REQUIRED = "Academic outcome is required.";
  public static final String ACADEMIC_OUTCOME_NOT_EXISTS = "Academic outcome value does not exist.";
  public static final String PYA_SHOULD_BE_BOOLEAN = "Pya should be YES/NO.";
  public static final String UNDER_APPEAL_SHOULD_BE_BOOLEAN = "Under appeal should be YES/NO.";
  public static final String EXTERNAL_TRAINER_SHOULD_BE_BOOLEAN =
      "External trainer should be YES/NO.";
  public static final String TEN_PERCENT_AUDIT_SHOULD_BE_BOOLEAN =
      "Set 10% audit - lay member should be YES/NO.";
  public static final String KNOWN_CONCERNS_SHOULD_BE_BOOLEAN = "Known concerns should be YES/NO.";
  protected static final String[] academicCurricula = {"AFT", "ACLNIHR_FUNDING",
      "ACL_OTHER_FUNDING", "ACFNIHR_FUNDING", "ACF_OTHER_FUNDING"};
  protected static final String[] academicOutcomes = {"Continue on academic component",
      "Do not continue on academic component", "Successfully completed academic component"};

  private static final Logger logger = getLogger(AssessmentUpdateTransformerService.class);

  @Autowired
  private TcsServiceImpl tcsService;
  @Autowired
  private ReferenceServiceImpl referenceService;
  @Autowired
  private AssessmentServiceImpl assessmentService;
  @Autowired
  private AssessmentTransformerService assessmentTransformerService;

  /**
   * Validate the data from Excel and update the valid ones into the DB.
   *
   * @param xlsList The Xls list from the Excel user input
   */
  public void processAssessmentsUpdateUpload(List<AssessmentUpdateXLS> xlsList) {
    xlsList.forEach(TemplateXLS::initialiseSuccessfullyImported);

    if (!CollectionUtils.isEmpty(xlsList)) {

      List<AssessmentUpdateXLS> filteredList = handleDuplicateIds(xlsList);
      if (!filteredList.isEmpty()) {

        // Get Assessments from Assessment service
        Map<String, AssessmentDTO> assessmentsMap = getExistingAssessmentsMap(filteredList);

        // Get ProgrammeMemberships from Tcs service
        Map<String, ProgrammeMembershipCurriculaDTO> programmeMembershipsMap =
            getProgrammeMembershipsMap(filteredList);

        // Get Grades from Reference service
        Map<String, GradeDTO> gradeAtTimesMap = buildGradesMap(
            AssessmentUpdateXLS::getGradeAtTimeName, filteredList);
        Map<String, GradeDTO> gradeNextRotationsMap = buildGradesMap(
            AssessmentUpdateXLS::getNextRotationGradeName, filteredList);

        // Get types from Reference service
        List<AssessmentTypeDto> assessmentTypeDtoList = referenceService.findAllAssessmentTypes();

        // Get outcomes from Assessment service
        Set<Outcome> allOutcomes = assessmentTransformerService.getAllOutcomes();

        List<AssessmentDTO> assessmentDtoListToUpdate = useMatchingCriteriaToValidateAssessment(
            filteredList, assessmentsMap, programmeMembershipsMap,
            gradeAtTimesMap,
            gradeNextRotationsMap, assessmentTypeDtoList, allOutcomes);

        patchUpdateAssessments(xlsList, assessmentDtoListToUpdate);
      }
    }
  }

  private List<AssessmentDTO> useMatchingCriteriaToValidateAssessment(
      List<AssessmentUpdateXLS> xlsList,
      Map<String, AssessmentDTO> assessmentsMap,
      Map<String, ProgrammeMembershipCurriculaDTO> programmeMembershipsMap,
      Map<String, GradeDTO> gradeAtTimesMap,
      Map<String, GradeDTO> gradeNextRotationsMap, List<AssessmentTypeDto> assessmentTypeDtoList,
      Set<Outcome> allOutcomes) {

    List<AssessmentDTO> assessmentDtoListToUpdate = new ArrayList<>();

    for (AssessmentUpdateXLS xls : xlsList) {
      // Get AssessmentDto from DB and validate Assessment ID
      AssessmentDTO assessmentDto = assessmentsMap.get(xls.getAssessmentId());
      if (assessmentDto == null) {
        xls.addErrorMessage(ASSESSMENT_ID_NOT_EXIST);
        continue;
      }
      // when nested DTOs are null, initialise them
      Long assessmentId = assessmentDto.getId();

      if (assessmentDto.getDetail() == null) {
        AssessmentDetailDTO assessmentDetailDto = new AssessmentDetailDTO();
        assessmentDetailDto.setId(assessmentId);
        assessmentDto.setDetail(new AssessmentDetailDTO());
      }
      AssessmentDetailDTO assessmentDetailDto = assessmentDto.getDetail();

      if (assessmentDto.getOutcome() == null) {
        AssessmentOutcomeDTO assessmentOutcomeDto = new AssessmentOutcomeDTO();
        assessmentOutcomeDto.setId(assessmentId);
        assessmentDto.setOutcome(new AssessmentOutcomeDTO());
      }
      AssessmentOutcomeDTO assessmentOutcomeDto = assessmentDto.getOutcome();

      if (assessmentDto.getRevalidation() == null) {
        RevalidationDTO revalidationDto = new RevalidationDTO();
        revalidationDto.setId(assessmentId);
        assessmentDto.setRevalidation(new RevalidationDTO());
      }
      RevalidationDTO revalidationDto = assessmentDto.getRevalidation();

      updateBooleanFields(assessmentDetailDto, assessmentOutcomeDto, revalidationDto,
          xls);
      validateAndUpdateNumericFields(assessmentDetailDto, xls);

      validateAndUpdateProgrammeMembership(programmeMembershipsMap, assessmentDto,
          assessmentDetailDto, xls);
      // Validation on dates should be after validation on programmeMembership
      validateAndUpdateDates(assessmentDto, assessmentDetailDto, assessmentOutcomeDto, xls);
      validateAndUpdateOutcome(allOutcomes, assessmentOutcomeDto, xls);
      validateAndUpdateType(assessmentTypeDtoList, assessmentDto, xls);
      validateAndUpdateGrades(gradeAtTimesMap, gradeNextRotationsMap, assessmentDetailDto,
          assessmentOutcomeDto, xls);
      // Validation on Academic outcome should be after validation on dates and outcome
      validateAndUpdateAcademicOutcome(assessmentDetailDto, assessmentOutcomeDto, xls);
      updateOtherFields(assessmentOutcomeDto, revalidationDto, xls);

      if (!xls.hasErrors()) {
        assessmentDtoListToUpdate.add(assessmentDto);
      } else {
        logger.error("{}: {}", xls.getAssessmentId(), xls.getErrorMessage());
      }
    }
    return assessmentDtoListToUpdate;
  }

  private void validateAndUpdateAcademicOutcome(AssessmentDetailDTO assessmentDetailDto,
      AssessmentOutcomeDTO assessmentOutcomeDto, AssessmentUpdateXLS xls) {
    // Academic curriculum assessed
    LocalDate periodCoveredFrom = assessmentDetailDto.getPeriodCoveredFrom();
    LocalDate periodCoveredTo = assessmentDetailDto.getPeriodCoveredTo();
    LocalDate curriculumStartDate = assessmentDetailDto.getCurriculumStartDate();
    LocalDate curriculumEndDate = assessmentDetailDto.getCurriculumEndDate();
    String curriculumSubType = assessmentDetailDto.getCurriculumSubType();
    boolean academicCurriculumAssessed = false;

    if (!StringUtils.isEmpty(curriculumSubType)) {
      String academicType = Arrays.stream(academicCurricula)
          .filter(c -> StringUtils.equals(c, curriculumSubType)).findAny().orElse(null);
      if (!StringUtils.isEmpty(academicType)) {
        boolean academicCurriculumHasOverlaps = academicCurriculumOverlaps(curriculumStartDate,
            curriculumEndDate, periodCoveredFrom, periodCoveredTo);
        if (academicCurriculumHasOverlaps) {
          assessmentOutcomeDto.setAcademicCurriculumAssessed(
              assessmentDetailDto.getCurriculumName());
          academicCurriculumAssessed = true;
        }
      }
    }

    String academicOutcome = xls.getAcademicOutcome();
    if (academicCurriculumAssessed) {
      if (StringUtils.isEmpty(academicOutcome)) {
        xls.addErrorMessage(ACADEMIC_OUTCOME_IS_REQUIRED);
      } else {
        String outcome = Arrays.stream(academicOutcomes)
            .filter(o -> StringUtils.equals(o, academicOutcome)).findAny().orElse(null);
        if (StringUtils.isEmpty(outcome)) {
          xls.addErrorMessage(ACADEMIC_OUTCOME_NOT_EXISTS);
        } else {
          assessmentOutcomeDto.setAcademicOutcome(academicOutcome);
        }
      }
    }
  }

  private void validateAndUpdateType(List<AssessmentTypeDto> assessmentTypeDtoList,
      AssessmentDTO assessmentDto, AssessmentUpdateXLS xls) {
    // Set Type
    String type = xls.getType();
    if (!StringUtils.isEmpty(type)) {
      AssessmentTypeDto assessmentTypeDto = assessmentTypeDtoList.stream()
          .filter(t -> t.getLabel().equalsIgnoreCase(type)).findAny().orElse(null);
      if (assessmentTypeDto != null && assessmentTypeDto.getLabel() != null) {
        assessmentDto.setType(assessmentTypeDto.getLabel());
      } else {
        xls.addErrorMessage(ASSESSMENT_TYPE_NOT_MATCH);
      }
    }
  }

  private void validateAndUpdateOutcome(Set<Outcome> allOutcomes,
      AssessmentOutcomeDTO assessmentOutcomeDto,
      AssessmentUpdateXLS xls) {
    boolean outcomeFromXls = !StringUtils.isEmpty(xls.getOutcome());
    boolean notAssessedReasonFromXls = !StringUtils.isEmpty(xls.getOutcomeNotAssessed());
    boolean otherReasonFromXls = !StringUtils.isEmpty(xls.getOutcomeNotAssessedOther());
    boolean outcomeExists = false;

    if (!outcomeFromXls && !notAssessedReasonFromXls && !otherReasonFromXls) {
      return;
    }

    Outcome outcome = null;
    List<AssessmentOutcomeReasonDTO> assessmentOutcomeReasonDtoList = new ArrayList<>();

    // Set outcome: if outcome doesn't exist in Xls, use the DB one.
    String outcomeLabel;
    if (outcomeFromXls) {
      outcomeLabel = xls.getOutcome();
    } else {
      outcomeLabel = assessmentOutcomeDto.getOutcome();
    }

    if (!StringUtils.isEmpty(outcomeLabel)) {
      outcome = allOutcomes.stream()
          .filter(o -> o.getLabel().equalsIgnoreCase(outcomeLabel)).findAny().orElse(null);
      outcomeExists = outcome != null && outcome.getId() != null;
    }

    if (outcomeExists) {
      assessmentOutcomeDto.setOutcomeId(outcome.getId());
      assessmentOutcomeDto.setOutcome(outcomeLabel);
      assessmentOutcomeDto.setReasons(assessmentOutcomeReasonDtoList);
    } else {
      xls.addErrorMessage(OUTCOME_CANNOT_BE_IDENTIFIED);
      return; // if outcome doesn't exist either in the Xls or in the DB
    }

    // Reasons
    Set<Reason> outcomeReasons = outcome.getReasons();
    // When outcome does not need a reason, but reasons are input.
    if (CollectionUtils.isEmpty(outcomeReasons) && (notAssessedReasonFromXls
        || otherReasonFromXls)) {
      xls.addErrorMessage(
          String.format(NOT_ASSESSED_REASONS_SHOULD_BE_EMPTY_FOR_OUTCOME, outcomeLabel));
      return;
    }
    // When otherReason is input, the notAssessedReason is required.
    if (!notAssessedReasonFromXls && otherReasonFromXls) {
      xls.addErrorMessage(NOT_ASSESSED_REASON_IS_REQUIRED);
      return;
    }
    // When outcome needs a reason, check if reason is input.
    if (!CollectionUtils.isEmpty(outcomeReasons)) {
      if (!notAssessedReasonFromXls) {
        xls.addErrorMessage(
            String.format(OUTCOME_REASON_IS_REQUIRED_FOR_OUTCOME_S, outcomeLabel));
      } else {
        List<String> notAssessedReasons = Arrays.asList(
            xls.getOutcomeNotAssessed().split(SEMI_COLON));
        notAssessedReasons.forEach(notAssessedReason -> {
          Reason assessmentReason = outcomeReasons.stream()
              .filter(r -> r.getLabel().equalsIgnoreCase(notAssessedReason.trim())).findAny()
              .orElse(null);
          if (assessmentReason == null) {
            xls.addErrorMessage(
                String.format(GIVEN_ASSESSMENT_REASON_NOT_FOUND, notAssessedReason));
          } else {
            AssessmentOutcomeReasonDTO assessmentOutcomeReasonDto =
                new AssessmentOutcomeReasonDTO();
            assessmentOutcomeReasonDto.setReasonLabel(assessmentReason.getLabel());
            assessmentOutcomeReasonDto.setReasonId(assessmentReason.getId());
            assessmentOutcomeReasonDto.setReasonCode(assessmentReason.getCode());
            assessmentOutcomeReasonDto.setRequireOther(assessmentReason.isRequireOther());
            if (assessmentReason.isRequireOther()) {
              if (otherReasonFromXls) {
                String otherReason = xls.getOutcomeNotAssessedOther();
                if (otherReason.length() <= OTHER_REASON_LENGTH_LIMIT) {
                  assessmentOutcomeReasonDto.setOther(otherReason);
                } else {
                  xls.addErrorMessage(OTHER_REASON_EXCEED_LENGTH_LIMIT);
                }
              } else {
                xls.addErrorMessage(OTHER_REASON_IS_REQUIRED);
              }
            }
            assessmentOutcomeReasonDtoList.add(assessmentOutcomeReasonDto);
          }
        });
        assessmentOutcomeDto.setReasons(assessmentOutcomeReasonDtoList);
      }
    }
  }

  private void validateAndUpdateProgrammeMembership(
      Map<String, ProgrammeMembershipCurriculaDTO> programmeMembershipsMap,
      AssessmentDTO assessmentDto, AssessmentDetailDTO assessmentDetailDto,
      AssessmentUpdateXLS xls) {
    // Set ProgrammeMembership & curriculum

    String programmeMembershipId = xls.getCurriculumMembershipId();
    if (StringUtils.isEmpty(programmeMembershipId)) {
      return;
    }

    boolean programmeMembershipIdIsValid = false;

    ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDto = programmeMembershipsMap.get(
        programmeMembershipId);

    if (programmeMembershipCurriculaDto != null) {
      Long existingTraineeId = assessmentDto.getTraineeId();
      PersonDTO person = programmeMembershipCurriculaDto.getPerson();
      if (existingTraineeId != null && person != null) {
        Long personIdFromProgrammeMembership = person.getId();
        if (existingTraineeId.equals(personIdFromProgrammeMembership)) {
          programmeMembershipIdIsValid = true;
        }
      }
    }

    if (!programmeMembershipIdIsValid) {
      xls.addErrorMessage(PROGRAMME_MEMBERSHIP_ID_NOT_MATCH);
      return;
    }

    assessmentDto.setCurriculumMembershipId(programmeMembershipCurriculaDto.getId());
    assessmentDto.setProgrammeId(programmeMembershipCurriculaDto.getProgrammeId());
    assessmentDto.setProgrammeName(programmeMembershipCurriculaDto.getProgrammeName());
    assessmentDto.setProgrammeNumber(programmeMembershipCurriculaDto.getProgrammeNumber());

    // set curriculum
    CurriculumDTO curriculumDto = programmeMembershipCurriculaDto.getCurriculumDTO();
    CurriculumMembershipDTO curriculumMembershipDto;

    if (curriculumDto == null) {
      curriculumDto = new CurriculumDTO();
    }
    if (programmeMembershipCurriculaDto.getCurriculumMemberships() == null
        || programmeMembershipCurriculaDto.getCurriculumMemberships().isEmpty()) {
      curriculumMembershipDto = new CurriculumMembershipDTO();
    } else {
      curriculumMembershipDto = programmeMembershipCurriculaDto.getCurriculumMemberships()
          .get(0);
    }
    assessmentDetailDto.setCurriculumId(curriculumDto.getId());
    assessmentDetailDto.setCurriculumName(curriculumDto.getName());
    SpecialtyDTO specialtyDto = curriculumDto.getSpecialty();
    if (specialtyDto != null) {
      assessmentDetailDto.setCurriculumSpecialty(specialtyDto.getName());
      assessmentDetailDto.setCurriculumSpecialtyId(String.valueOf(specialtyDto.getId()));
    } else {
      assessmentDetailDto.setCurriculumSpecialty(null);
      assessmentDetailDto.setCurriculumSpecialtyId(null);
    }
    CurriculumSubType curriculumSubType = curriculumDto.getCurriculumSubType();
    if (curriculumSubType != null) {
      assessmentDetailDto.setCurriculumSubType(curriculumSubType.name());
    } else {
      assessmentDetailDto.setCurriculumSubType(null);
    }
    assessmentDetailDto.setCurriculumStartDate(curriculumMembershipDto.getCurriculumStartDate());
    assessmentDetailDto.setCurriculumEndDate(curriculumMembershipDto.getCurriculumEndDate());
  }

  private boolean academicCurriculumOverlaps(LocalDate curriculumStartDate,
      LocalDate curriculumEndDate, LocalDate periodCoveredFrom, LocalDate periodCoveredTo) {
    if (curriculumStartDate != null && curriculumEndDate != null && periodCoveredFrom != null
        && periodCoveredTo != null) {
      return (curriculumStartDate.compareTo(periodCoveredFrom) >= 0
          && curriculumStartDate.compareTo(periodCoveredTo) <= 0) || (
          periodCoveredFrom.compareTo(curriculumStartDate) >= 0
              && periodCoveredFrom.compareTo(curriculumEndDate) <= 0);
    }
    return false;
  }

  private void validateAndUpdateGrades(Map<String, GradeDTO> gradeAtTimesMap,
      Map<String, GradeDTO> gradeNextRotationsMap, AssessmentDetailDTO assessmentDetailDto,
      AssessmentOutcomeDTO assessmentOutcomeDto, AssessmentUpdateXLS xls) {

    // Set Grade at time
    String gradeAtTime = xls.getGradeAtTimeName();
    if (!StringUtils.isEmpty(gradeAtTime)) {
      if (!gradeAtTimesMap.containsKey(gradeAtTime)) {
        xls.addErrorMessage(String.format(NO_GRADE_AT_TIME_FOUND, gradeAtTime));
      } else {
        GradeDTO gradeDto = gradeAtTimesMap.get(gradeAtTime);
        assessmentDetailDto.setGradeId(gradeDto.getId());
        assessmentDetailDto.setGradeAbbreviation(gradeDto.getAbbreviation());
        assessmentDetailDto.setGradeName(gradeDto.getName());
      }
    }

    // Set grade at next rotation
    String gradeAtNextRotation = xls.getNextRotationGradeName();
    if (!StringUtils.isEmpty(gradeAtNextRotation)) {
      if (!gradeNextRotationsMap.containsKey(gradeAtNextRotation)) {
        xls.addErrorMessage(String.format(NO_GRADE_AT_NEXT_ROTATION_FOUND, gradeAtNextRotation));
      } else {
        GradeDTO gradeDto = gradeNextRotationsMap.get(gradeAtNextRotation);
        assessmentOutcomeDto.setNextRotationGradeId(gradeDto.getId());
        assessmentOutcomeDto.setNextRotationGradeAbbr(gradeDto.getAbbreviation());
        assessmentOutcomeDto.setNextRotationGradeName(gradeDto.getName());
      }
    }
  }

  private void validateAndUpdateDates(AssessmentDTO assessmentDto,
      AssessmentDetailDTO assessmentDetailDto, AssessmentOutcomeDTO assessmentOutcomeDto,
      AssessmentUpdateXLS xls) {
    // Set Review date
    Date reviewDate = xls.getReviewDate();
    if (reviewDate != null) {
      try {
        assessmentDto.setReviewDate(convertDate(reviewDate));
      } catch (final IllegalArgumentException e) {
        xls.addErrorMessage(REVIEW_DATE_BEFORE_1753);
      }
    }

    // Set Next review date
    Date nextReviewDate = xls.getNextReviewDate();
    if (nextReviewDate != null) {
      try {
        assessmentOutcomeDto.setNextReviewDate(convertDate(nextReviewDate));
      } catch (final IllegalArgumentException e) {
        xls.addErrorMessage(NEXT_REVIEW_DATE_BEFORE_1753);
      }
    }

    // Set Period covered from & Period covered to
    // Validate Period cover from is before Period cover to
    Date periodCoveredFrom = xls.getPeriodCoveredFrom();
    Date periodCoveredTo = xls.getPeriodCoveredTo();
    boolean dateCheckNext = true;
    if (periodCoveredFrom != null) {
      try {
        assessmentDetailDto.setPeriodCoveredFrom(convertDate(periodCoveredFrom));
      } catch (final IllegalArgumentException e) {
        xls.addErrorMessage(PERIOD_COVERED_FROM_DATE_BEFORE_1753);
        dateCheckNext = false;
      }
    }
    if (periodCoveredTo != null) {
      try {
        assessmentDetailDto.setPeriodCoveredTo(convertDate(periodCoveredTo));
      } catch (final IllegalArgumentException e) {
        xls.addErrorMessage(PERIOD_COVERED_TO_DATE_BEFORE_1753);
        dateCheckNext = false;
      }
    }
    // If date conversion fails, other checks are not needed.
    if (!dateCheckNext) {
      return;
    }
    // Below converted dates could be from xls or from the DB.
    LocalDate periodCoveredFromConverted = assessmentDetailDto.getPeriodCoveredFrom();
    LocalDate periodCoveredToConverted = assessmentDetailDto.getPeriodCoveredTo();

    if (periodCoveredFromConverted != null && periodCoveredToConverted != null
        && periodCoveredToConverted.isBefore(periodCoveredFromConverted)) {
      xls.addErrorMessage(PERIOD_COVERED_TO_CAN_NOT_BE_BEFORE_PERIOD_COVERED_FROM);
    }

    // Validate periodCovered dates with curriculum dates
    LocalDate curriculumStartDate = assessmentDetailDto.getCurriculumStartDate();
    LocalDate curriculumEndDate = assessmentDetailDto.getCurriculumEndDate();
    if (periodCoveredFromConverted != null && curriculumStartDate != null
        && periodCoveredFromConverted.isBefore(curriculumStartDate)) {
      xls.addErrorMessage(PERIOD_COVERED_FROM_MUST_BE_AFTER_CURRICULUM_START_DATE);
    }
    if (periodCoveredToConverted != null && curriculumEndDate != null
        && periodCoveredToConverted.isAfter(curriculumEndDate)) {
      xls.addErrorMessage(PERIOD_COVERED_TO_MUST_BE_BEFORE_CURRICULUM_END_DATE);
    }
  }

  private void updateBooleanFields(AssessmentDetailDTO assessmentDetailDto,
      AssessmentOutcomeDTO assessmentOutcomeDto, RevalidationDTO revalidationDto,
      AssessmentUpdateXLS xls) {
    // Set Pya
    if (!StringUtils.isEmpty(xls.getPya())) {
      try {
        assessmentDetailDto.setPya(BooleanUtil.parseBoolean(xls.getPya()));
      } catch (ParseException e) {
        xls.addErrorMessage(PYA_SHOULD_BE_BOOLEAN);
      }
    }
    // Set Under appeal
    if (!StringUtils.isEmpty(xls.getUnderAppeal())) {
      try {
        assessmentOutcomeDto.setUnderAppeal(BooleanUtil.parseBoolean(xls.getUnderAppeal()));
      } catch (ParseException e) {
        xls.addErrorMessage(UNDER_APPEAL_SHOULD_BE_BOOLEAN);
      }
    }
    // Set External trainer
    if (!StringUtils.isEmpty(xls.getExternalTrainer())) {
      try {
        assessmentOutcomeDto.setExternalTrainer(
            BooleanUtil.parseBoolean(xls.getExternalTrainer()));
      } catch (ParseException e) {
        xls.addErrorMessage(EXTERNAL_TRAINER_SHOULD_BE_BOOLEAN);
      }
    }
    // Set 10% audit - lay member
    if (!StringUtils.isEmpty(xls.getTenPercentAudit())) {
      try {
        assessmentOutcomeDto.setTenPercentAudit(
            BooleanUtil.parseBoolean(xls.getTenPercentAudit()));
      } catch (ParseException e) {
        xls.addErrorMessage(TEN_PERCENT_AUDIT_SHOULD_BE_BOOLEAN);
      }
    }
    // Set Known concerns
    if (!StringUtils.isEmpty(xls.getKnownConcerns())) {
      try {
        revalidationDto.setKnownConcerns(BooleanUtil.parseBoolean(xls.getKnownConcerns()));
      } catch (ParseException e) {
        xls.addErrorMessage(KNOWN_CONCERNS_SHOULD_BE_BOOLEAN);
      }
    }
  }

  private void validateAndUpdateNumericFields(AssessmentDetailDTO assessmentDetailDto,
      AssessmentUpdateXLS xls) {

    String programmeMembershipId = xls.getCurriculumMembershipId();
    if (!StringUtils.isEmpty(programmeMembershipId)
        && !NumberUtils.isDigits(programmeMembershipId)) {
      xls.addErrorMessage(TIS_PROGRAMME_MEMBERSHIP_ID_SHOULD_BE_NUMERIC);
    }

    // Set Months OOPR/OOPT counted towards training
    String monthsCounted = xls.getMonthsCountedToTraining();
    if (!StringUtils.isEmpty(monthsCounted)) {
      if (!NumberUtils.isDigits(monthsCounted)) {
        xls.addErrorMessage(MONTHS_OOPR_OOPT_COUNTED_TOWARDS_TRAINING_SHOULD_BE_NUMERIC);
      } else {
        assessmentDetailDto.setMonthsCountedToTraining(Integer.valueOf(monthsCounted));
      }
    }

    // Set Days out of training
    String daysOutOfTraining = xls.getDaysOutOfTraining();
    if (!StringUtils.isEmpty(daysOutOfTraining)) {
      if (!NumberUtils.isDigits(daysOutOfTraining)) {
        xls.addErrorMessage(DAYS_OUT_OF_TRAINING_SHOULD_BE_NUMERIC);
      } else {
        assessmentDetailDto.setDaysOutOfTraining(Integer.valueOf(daysOutOfTraining));
      }
    }
  }

  private void updateOtherFields(AssessmentOutcomeDTO assessmentOutcomeDto,
      RevalidationDTO revalidationDto, AssessmentUpdateXLS xls) {
    // Set Comments
    if (!StringUtils.isEmpty(xls.getComments())) {
      assessmentOutcomeDto.setComments(xls.getComments());
    }
    // Set Detailed reason
    if (!StringUtils.isEmpty(xls.getDetailedReasons())) {
      assessmentOutcomeDto.setDetailedReasons(xls.getDetailedReasons());
    }
    // Set Mitigating circumstances
    if (!StringUtils.isEmpty(xls.getMitigatingCircumstances())) {
      assessmentOutcomeDto.setMitigatingCircumstances(xls.getMitigatingCircumstances());
    }
    //Set Competences which need to be developed by next ARCP
    if (!StringUtils.isEmpty(xls.getCompetencesToBeDeveloped())) {
      assessmentOutcomeDto.setCompetencesToBeDeveloped(xls.getCompetencesToBeDeveloped());
    }
    // Set Other recommended actions
    if (!StringUtils.isEmpty(xls.getOtherRecommendedActions())) {
      assessmentOutcomeDto.setOtherRecommendedActions(xls.getOtherRecommendedActions());
    }
    // Set Recommended additional training time (if required)
    if (!StringUtils.isEmpty(xls.getRecommendedAdditionalTrainingTime())) {
      assessmentOutcomeDto.setRecommendedAdditionalTrainingTime(
          xls.getRecommendedAdditionalTrainingTime());
    }
    // Set Additional comments from the panel
    if (!StringUtils.isEmpty(xls.getAdditionalCommentsFromPanel())) {
      assessmentOutcomeDto.setAdditionalCommentsFromPanel(xls.getAdditionalCommentsFromPanel());
    }
    // Set Concern summary
    if (!StringUtils.isEmpty(xls.getConcernSummary())) {
      revalidationDto.setConcernSummary(xls.getConcernSummary());
    }
    // Set Responsible Officers Comments
    if (!StringUtils.isEmpty(xls.getResponsibleOfficerComments())) {
      revalidationDto.setResponsibleOfficerComments(xls.getResponsibleOfficerComments());
    }
  }

  /**
   * Patch all the Assessment data which pass the validation and updated.
   *
   * @param xlsList The Xls linked with the data users input
   * @param assessmentDtoList The updated DTO list
   */
  public void patchUpdateAssessments(List<AssessmentUpdateXLS> xlsList,
      List<AssessmentDTO> assessmentDtoList) {
    Map<String, AssessmentUpdateXLS> assessmentIdToXls = new HashMap<>();
    xlsList.forEach(r -> assessmentIdToXls.put(r.getAssessmentId(), r));

    List<AssessmentDTO> patchedAssessmentDtoList = assessmentService.patchAssessments(
        assessmentDtoList);

    for (AssessmentDTO dto : patchedAssessmentDtoList) {
      // Get the source XLS for the DTO and add error messages or success.
      AssessmentUpdateXLS xls = assessmentIdToXls.get(String.valueOf(dto.getId()));
      List<String> errMessages = dto.getMessageList();
      if (errMessages.isEmpty()) {
        xls.setSuccessfullyImported(true);
      } else {
        xls.addErrorMessages(errMessages);
      }
    }
  }

  private List<AssessmentUpdateXLS> handleDuplicateIds(List<AssessmentUpdateXLS> xlsList) {
    List<AssessmentUpdateXLS> filteredList = new ArrayList<>();
    // Use a HashMap to store all the numbers of each AssessmentId
    Map<String, Integer> numberOfIds = new HashMap<>();

    for (AssessmentUpdateXLS xls : xlsList) {
      String assessmentId = xls.getAssessmentId();
      if (!numberOfIds.containsKey(assessmentId)) {
        numberOfIds.put(assessmentId, 1);
      } else {
        numberOfIds.put(assessmentId, numberOfIds.get(assessmentId) + 1);
      }
    }
    for (AssessmentUpdateXLS xls : xlsList) {
      if (numberOfIds.get(xls.getAssessmentId()) > 1) {
        xls.addErrorMessage(String.format(ASSESSMENT_ID_IS_DUPLICATE, xls.getAssessmentId()));
      } else {
        filteredList.add(xls);
      }
    }
    return filteredList;
  }

  private Map<String, GradeDTO> buildGradesMap(Function<AssessmentUpdateXLS, String> getGradeName,
      List<AssessmentUpdateXLS> xlsList) {
    Map<String, GradeDTO> gradesMap = new HashMap<>();
    if (!xlsList.isEmpty()) {
      Set<String> gradeNames = xlsList.stream().map(getGradeName::apply)
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());

      if (!gradeNames.isEmpty()) {
        String gradeNamesStr = String.join("\",\"", gradeNames);
        List<GradeDTO> gradeDtoList = referenceService.findGradesByName(gradeNamesStr);
        for (GradeDTO dto : gradeDtoList) {
          gradesMap.put(dto.getName(), dto);
        }
      }
    }
    return gradesMap;
  }

  private Map<String, AssessmentDTO> getExistingAssessmentsMap(
      List<AssessmentUpdateXLS> xlsList) {
    Map<String, AssessmentDTO> assessmentsMap = new HashMap<>();
    if (!xlsList.isEmpty()) {
      Set<String> assessmentIds = xlsList.stream()
          .map(AssessmentUpdateXLS::getAssessmentId)
          .filter(NumberUtils::isDigits)
          .collect(
              Collectors.toSet());
      List<AssessmentDTO> assessmentDtoList = assessmentService.findAssessmentByIds(assessmentIds);

      for (AssessmentDTO dto : assessmentDtoList) {
        assessmentsMap.put(dto.getId().toString(), dto);
      }
    }
    return assessmentsMap;
  }

  private Map<String, ProgrammeMembershipCurriculaDTO> getProgrammeMembershipsMap(
      List<AssessmentUpdateXLS> xlsList) {
    // Get all ProgrammeMemberships from tcs service
    Map<String, ProgrammeMembershipCurriculaDTO> programmeMembershipsMap = new HashMap<>();
    if (!xlsList.isEmpty()) {
      Set<String> programmeMembershipIds = xlsList.stream()
          .map(AssessmentUpdateXLS::getCurriculumMembershipId)
          .filter(NumberUtils::isDigits).collect(
              Collectors.toSet());
      if (!programmeMembershipIds.isEmpty()) {
        List<ProgrammeMembershipCurriculaDTO> programmeMembershipCurriculaDtoList =
            tcsService.getProgrammeMembershipDetailsByIds(programmeMembershipIds);

        for (ProgrammeMembershipCurriculaDTO dto : programmeMembershipCurriculaDtoList) {
          programmeMembershipsMap.put(dto.getId().toString(), dto);
        }
      }
    }
    return programmeMembershipsMap;
  }
}