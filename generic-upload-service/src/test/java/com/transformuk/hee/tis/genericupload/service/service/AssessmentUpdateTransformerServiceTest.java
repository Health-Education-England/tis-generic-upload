package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertDate;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentDetailDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentOutcomeDTO;
import com.transformuk.hee.tis.assessment.api.dto.AssessmentOutcomeReasonDTO;
import com.transformuk.hee.tis.assessment.api.dto.RevalidationDTO;
import com.transformuk.hee.tis.assessment.client.service.impl.AssessmentServiceImpl;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentUpdateXLS;
import com.transformuk.hee.tis.genericupload.service.util.BooleanUtil;
import com.transformuk.hee.tis.reference.api.dto.AssessmentTypeDto;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipCurriculaDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.CurriculumSubType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class AssessmentUpdateTransformerServiceTest {

  @InjectMocks
  private AssessmentUpdateTransformerService assessmentUpdateTransformerService;
  @Mock
  private TcsServiceImpl tcsServiceMock;
  @Mock
  private ReferenceServiceImpl referenceServiceMock;
  @Mock
  private AssessmentServiceImpl assessmentServiceMock;
  @Mock
  private AssessmentTransformerService assessmentTransformerServiceMock;
  @Captor
  private ArgumentCaptor<List<AssessmentDTO>> assessmentDtoListCaptor;

  @Before
  public void setUp() {
    AssessmentTypeDto assessmentTypeDto1 = new AssessmentTypeDto();
    assessmentTypeDto1.setId(1L);
    assessmentTypeDto1.setLabel("assessmentType_1");
    AssessmentTypeDto assessmentTypeDto2 = new AssessmentTypeDto();
    assessmentTypeDto2.setId(2L);
    assessmentTypeDto2.setLabel("assessmentType_2");
    when(referenceServiceMock.findAllAssessmentTypes()).thenReturn(
        Lists.newArrayList(assessmentTypeDto1, assessmentTypeDto2));

    Outcome outcome1 = new Outcome();
    outcome1.setId(1L);
    outcome1.setLabel("outcome_1");
    Reason reason1 = new Reason(), reason2 = new Reason();
    reason1.setId(1L);
    reason1.setLabel("reason_1");
    reason2.setId(2L);
    reason2.setLabel("other");
    reason2.setRequireOther(true);
    Set<Reason> reasonSet1 = new HashSet<>();
    reasonSet1.add(reason1);
    reasonSet1.add(reason2);
    outcome1.setReasons(reasonSet1);

    Outcome outcome2 = new Outcome();
    outcome2.setId(2L);
    outcome2.setLabel("Outcome_2");
    Set<Outcome> outcomes = new HashSet<>();
    outcome2.setReasons(reasonSet1);

    Outcome outcome3 = new Outcome();
    outcome3.setId(3L);
    outcome3.setLabel("Outcome_3");

    outcomes.add(outcome1);
    outcomes.add(outcome2);
    outcomes.add(outcome3);
    when(assessmentTransformerServiceMock.getAllOutcomes()).thenReturn(outcomes);
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_duplicateIds() {
    String duplicateId = "1";
    AssessmentUpdateXLS xls1 = new AssessmentUpdateXLS();
    AssessmentUpdateXLS xls2 = new AssessmentUpdateXLS();
    xls1.setAssessmentId(duplicateId);
    xls2.setAssessmentId(duplicateId);

    List<AssessmentUpdateXLS> xlsList = Lists.newArrayList(xls1, xls2);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(String.format(AssessmentUpdateTransformerService.ASSESSMENT_ID_IS_DUPLICATE,
            duplicateId)));
    assertThat("Should get error", xlsList.get(1).getErrorMessage(),
        is(String.format(AssessmentUpdateTransformerService.ASSESSMENT_ID_IS_DUPLICATE,
            duplicateId)));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_NotExistingAssessmentId() {
    // Existing Assessment
    String id = "3";
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id)))
        .thenReturn(Collections.emptyList());

    // AssessmentUpdateXLS to update
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);

    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(AssessmentUpdateTransformerService.ASSESSMENT_ID_NOT_EXIST));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_TypeNotMatch() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id)))
        .thenReturn(Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    String type = "invalidType";
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setType(type);
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);

    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(AssessmentUpdateTransformerService.ASSESSMENT_TYPE_NOT_MATCH));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_GradesNotFound() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    String gradeAtTime = "invalidGradeAtTime";
    String gradeAtNextRotation = "invalidGradeAtNextRotation";
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setGradeAtTimeName(gradeAtTime);
    xls.setNextRotationGradeName(gradeAtNextRotation);
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);
    when(referenceServiceMock.findGradesByName(gradeAtTime)).thenReturn(Collections.emptyList());
    when(referenceServiceMock.findGradesByName(gradeAtNextRotation)).thenReturn(
        Collections.emptyList());

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);

    assertThat("Should get error", xlsList.get(0).getErrorMessage(), containsString(
        String.format(AssessmentUpdateTransformerService.NO_GRADE_AT_TIME_FOUND, gradeAtTime)));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(), containsString(
        String.format(AssessmentUpdateTransformerService.NO_GRADE_AT_NEXT_ROTATION_FOUND,
            gradeAtNextRotation)));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_DatesValidation() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    Calendar cal1 = Calendar.getInstance();
    cal1.set(1700, Calendar.SEPTEMBER, 1);
    Date date1 = cal1.getTime();
    Calendar cal2 = Calendar.getInstance();
    cal2.set(1700, Calendar.SEPTEMBER, 2);
    Date date2 = cal2.getTime();

    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setReviewDate(date1);
    xls.setPeriodCoveredFrom(date2);
    xls.setPeriodCoveredTo(date1);
    xls.setNextReviewDate(date1);
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);

    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(
            AssessmentUpdateTransformerService.PERIOD_COVERED_TO_CAN_NOT_BE_BEFORE_PERIOD_COVERED_FROM));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(AssessmentUpdateTransformerService.REVIEW_DATE_BEFORE_1753));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(AssessmentUpdateTransformerService.PERIOD_COVERED_FROM_DATE_BEFORE_1753));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(AssessmentUpdateTransformerService.PERIOD_COVERED_TO_DATE_BEFORE_1753));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(AssessmentUpdateTransformerService.NEXT_REVIEW_DATE_BEFORE_1753));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_NumericFieldsValidation() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id)))
        .thenReturn(Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    String invalidValue = "invalidNumberValue";
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setProgrammeMembershipId(invalidValue);
    xls.setMonthsCountedToTraining(invalidValue);
    xls.setDaysOutOfTraining(invalidValue);
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);

    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(
            AssessmentUpdateTransformerService.TIS_PROGRAMME_MEMBERSHIP_ID_SHOULD_BE_NUMERIC));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(), containsString(
        AssessmentUpdateTransformerService.MONTHS_OOPR_OOPT_COUNTED_TOWARDS_TRAINING_SHOULD_BE_NUMERIC));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(AssessmentUpdateTransformerService.DAYS_OUT_OF_TRAINING_SHOULD_BE_NUMERIC));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_BooleanFieldsValidation() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    String invalidValue = "InvalidBooleanValue";
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setPya(invalidValue);
    xls.setUnderAppeal(invalidValue);
    xls.setExternalTrainer(invalidValue);
    xls.setTenPercentAudit(invalidValue);
    xls.setKnownConcerns(invalidValue);
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);

    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(AssessmentUpdateTransformerService.PYA_SHOULD_BE_BOOLEAN));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(AssessmentUpdateTransformerService.UNDER_APPEAL_SHOULD_BE_BOOLEAN));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(AssessmentUpdateTransformerService.EXTERNAL_TRAINER_SHOULD_BE_BOOLEAN));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(AssessmentUpdateTransformerService.TEN_PERCENT_AUDIT_SHOULD_BE_BOOLEAN));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(AssessmentUpdateTransformerService.KNOWN_CONCERNS_SHOULD_BE_BOOLEAN));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_ProgrammeMembershipNotFound() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    Set<String> idSet = Collections.singleton(id);
    when(assessmentServiceMock.findAssessmentByIds(idSet)).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setProgrammeMembershipId("1");
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    when(tcsServiceMock.getProgrammeMembershipDetailsByIds(idSet)).thenReturn(
        Collections.emptyList());

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);

    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(AssessmentUpdateTransformerService.PROGRAMME_MEMBERSHIP_ID_NOT_MATCH));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_ProgrammeMembershipNotMatchPerson() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    Set<String> idSet = Collections.singleton(id);
    when(assessmentServiceMock.findAssessmentByIds(idSet)).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setProgrammeMembershipId("1");
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDto =
        new ProgrammeMembershipCurriculaDTO();
    programmeMembershipCurriculaDto.setId(1L);
    PersonDTO personDto = new PersonDTO();
    personDto.setId(2L);
    programmeMembershipCurriculaDto.setPerson(personDto);
    when(tcsServiceMock.getProgrammeMembershipDetailsByIds(idSet)).thenReturn(
        Collections.singletonList(programmeMembershipCurriculaDto));

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);
    assertThat("Should get error",
        xlsList.get(0).getErrorMessage(),
        is(AssessmentUpdateTransformerService.PROGRAMME_MEMBERSHIP_ID_NOT_MATCH));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_AcademicOutcomeIsRequired() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    Set<String> idSet = Collections.singleton(id);
    when(assessmentServiceMock.findAssessmentByIds(idSet)).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    Calendar cal1 = Calendar.getInstance();
    cal1.set(2020, Calendar.SEPTEMBER, 3);
    Date date1 = cal1.getTime();
    Calendar cal2 = Calendar.getInstance();
    cal2.set(2020, Calendar.SEPTEMBER, 15);
    Date date2 = cal2.getTime();

    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setProgrammeMembershipId("1");
    xls.setPeriodCoveredFrom(date1);
    xls.setPeriodCoveredTo(date2);
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDto =
        new ProgrammeMembershipCurriculaDTO();
    programmeMembershipCurriculaDto.setId(1L);
    PersonDTO personDto = new PersonDTO();
    personDto.setId(1L);
    programmeMembershipCurriculaDto.setPerson(personDto);
    CurriculumDTO curriculumDto = new CurriculumDTO();
    curriculumDto.setCurriculumSubType(CurriculumSubType.AFT);
    programmeMembershipCurriculaDto.setCurriculumDTO(curriculumDto);
    CurriculumMembershipDTO curriculumMembershipDto = new CurriculumMembershipDTO();
    curriculumMembershipDto.setCurriculumStartDate(
        LocalDate.of(2020, Month.SEPTEMBER, 1));
    curriculumMembershipDto.setCurriculumEndDate(
        LocalDate.of(2020, Month.OCTOBER, 30));
    programmeMembershipCurriculaDto.setCurriculumMemberships(
        Collections.singletonList(curriculumMembershipDto));
    when(tcsServiceMock.getProgrammeMembershipDetailsByIds(idSet)).thenReturn(
        Collections.singletonList(programmeMembershipCurriculaDto));

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(AssessmentUpdateTransformerService.ACADEMIC_OUTCOME_IS_REQUIRED));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_AcademicOutcomeNotExists() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    Set<String> idSet = Collections.singleton(id);
    when(assessmentServiceMock.findAssessmentByIds(idSet)).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    String academicOutcome = "invalidOutcome";
    Calendar cal1 = Calendar.getInstance();
    cal1.set(2020, Calendar.SEPTEMBER, 3);
    Date date1 = cal1.getTime();
    Calendar cal2 = Calendar.getInstance();
    cal2.set(2020, Calendar.SEPTEMBER, 15);
    Date date2 = cal2.getTime();
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setAcademicOutcome(academicOutcome);
    xls.setProgrammeMembershipId("1");
    xls.setPeriodCoveredFrom(date1);
    xls.setPeriodCoveredTo(date2);
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDto =
        new ProgrammeMembershipCurriculaDTO();
    programmeMembershipCurriculaDto.setId(1L);
    PersonDTO personDto = new PersonDTO();
    personDto.setId(1L);
    programmeMembershipCurriculaDto.setPerson(personDto);
    CurriculumDTO curriculumDto = new CurriculumDTO();
    curriculumDto.setCurriculumSubType(CurriculumSubType.AFT);
    programmeMembershipCurriculaDto.setCurriculumDTO(curriculumDto);
    CurriculumMembershipDTO curriculumMembershipDto = new CurriculumMembershipDTO();
    curriculumMembershipDto.setCurriculumStartDate(
        LocalDate.of(2020, Month.SEPTEMBER, 1));
    curriculumMembershipDto.setCurriculumEndDate(
        LocalDate.of(2020, Month.OCTOBER, 30));
    programmeMembershipCurriculaDto.setCurriculumMemberships(
        Collections.singletonList(curriculumMembershipDto));
    when(tcsServiceMock.getProgrammeMembershipDetailsByIds(idSet)).thenReturn(
        Collections.singletonList(programmeMembershipCurriculaDto));

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);

    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(AssessmentUpdateTransformerService.ACADEMIC_OUTCOME_NOT_EXISTS));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_outcomeNotValid() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setOutcome("X");
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(AssessmentUpdateTransformerService.OUTCOME_CANNOT_BE_IDENTIFIED));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_reasonsShouldNotBeInput() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setOutcome("Outcome_3");
    xls.setOutcomeNotAssessed("reason_1");
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(String.format(
            AssessmentUpdateTransformerService.NOT_ASSESSED_REASONS_SHOULD_BE_EMPTY_FOR_OUTCOME,
            xls.getOutcome())));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_outcomeReasonIsRequired() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setOutcome("outcome_1");
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(String.format(
            AssessmentUpdateTransformerService.OUTCOME_REASON_IS_REQUIRED_FOR_OUTCOME_S,
            xls.getOutcome())));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_notAssessedReasonIsRequiredForOtherReason() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setOutcome("outcome_1");
    xls.setOutcomeNotAssessedOther("otherReason");
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(AssessmentUpdateTransformerService.NOT_ASSESSED_REASON_IS_REQUIRED));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_otherReasonIsRequired() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setOutcome("outcome_1");
    xls.setOutcomeNotAssessed("reason_1;other");
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(AssessmentUpdateTransformerService.OTHER_REASON_IS_REQUIRED));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_otherReasonExceedLengthLimit() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setOutcome("outcome_1");
    xls.setOutcomeNotAssessed("reason_1;other");
    xls.setOutcomeNotAssessedOther("AAAAABBBBBCCCCCDDDDDAAAAABBBBBCCCCCDDDDDAAAAABBBBBCCCCCDDDDDA");
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        is(AssessmentUpdateTransformerService.OTHER_REASON_EXCEED_LENGTH_LIMIT));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_ReasonNotFound() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    AssessmentUpdateXLS xls = new AssessmentUpdateXLS();
    xls.setAssessmentId(id);
    xls.setOutcome("outcome_1");
    xls.setOutcomeNotAssessed("reason_X;reason_Y");
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);

    assertThat("Should get error", xlsList.get(0).getErrorMessage(), containsString(
        String.format(AssessmentUpdateTransformerService.GIVEN_ASSESSMENT_REASON_NOT_FOUND,
            "reason_X")));
    assertThat("Should get error", xlsList.get(0).getErrorMessage(),
        containsString(
            String.format(AssessmentUpdateTransformerService.GIVEN_ASSESSMENT_REASON_NOT_FOUND,
                "reason_Y")));
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_shouldUploadWhenValidationPasses()
      throws Exception {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    assessmentDto.setProgrammeMembershipId(1L);
    assessmentDto.setType("assessmentType_1");
    assessmentDto.setReviewDate(LocalDate.of(2020, 9, 2));
    AssessmentDetailDTO detailDto = new AssessmentDetailDTO();
    detailDto.id(1L)
        .curriculumId(1L)
        .gradeId(1L)
        .gradeName("gradeAtTime_1")
        .periodCoveredFrom(LocalDate.of(2020, 9, 1))
        .periodCoveredTo(LocalDate.of(2020, 9, 2))
        .daysOutOfTraining(1)
        .curriculumSpecialtyId("1")
        .pya(false)
        .curriculumStartDate(LocalDate.of(2020, 9, 1))
        .curriculumEndDate(LocalDate.of(2020, 9, 2))
        .curriculumSubType("subType_1");
    assessmentDto.setDetail(detailDto);
    AssessmentOutcomeDTO outcomeDto = new AssessmentOutcomeDTO();
    outcomeDto.id(1L)
        .outcome("outcome_1")
        .underAppeal(false)
        .nextReviewDate(LocalDate.of(2020, 9, 3))
        .comments("comments")
        .detailedReasons("reasons")
        .tenPercentAudit(false)
        .detailedReasons("aaa")
        .mitigatingCircumstances("aaa")
        .competencesToBeDeveloped("aaa")
        .otherRecommendedActions("aaa")
        .recommendedAdditionalTrainingTime("aaa")
        .additionalCommentsFromPanel("aaa")
        .nextRotationGradeId(3L)
        .nextRotationGradeName("gradeAtNextRotation_1");
    List<AssessmentOutcomeReasonDTO> reasons = new ArrayList<>();
    AssessmentOutcomeReasonDTO reason1 = new AssessmentOutcomeReasonDTO();
    reason1.id(1L).setReasonLabel("reason_1");
    reasons.add(reason1);
    AssessmentOutcomeReasonDTO reason2 = new AssessmentOutcomeReasonDTO();
    reason2.id(2L).setReasonLabel("Other");
    reason2.setRequireOther(true);
    reason2.setOther("Other reason");
    reasons.add(reason2);
    outcomeDto.setReasons(reasons);
    assessmentDto.setOutcome(outcomeDto);
    RevalidationDTO revalidationDto = new RevalidationDTO();
    revalidationDto.id(1L)
        .concernSummary("concernSummary")
        .setKnownConcerns(false);
    assessmentDto.setRevalidation(revalidationDto);
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    Calendar cal1 = Calendar.getInstance();
    cal1.set(2020, Calendar.JANUARY, 1);
    Date date1 = cal1.getTime();
    Calendar cal2 = Calendar.getInstance();
    cal2.set(2020, Calendar.OCTOBER, 2);
    Date date2 = cal2.getTime();

    AssessmentUpdateXLS xls = Mockito.spy(new AssessmentUpdateXLS());
    xls.setAssessmentId(id);
    xls.setProgrammeMembershipId("2");
    xls.setType("assessmentType_2");
    xls.setReviewDate(date2);
    xls.setPeriodCoveredFrom(date1);
    xls.setPeriodCoveredTo(date2);
    xls.setMonthsCountedToTraining("3");
    xls.setPya("Yes");
    xls.setGradeAtTimeName("gradeAtTime_2");
    xls.setDaysOutOfTraining("2");
    xls.setOutcome("outcome_2");
    xls.setOutcomeNotAssessed("other");
    xls.setOutcomeNotAssessedOther("other reason");
    xls.setUnderAppeal("Yes");
    xls.setExternalTrainer("Yes");
    xls.setNextReviewDate(date2);
    xls.setNextRotationGradeName("gradeAtNextRotation_2");
    xls.setComments("updatedComments");
    xls.setTenPercentAudit("Yes");
    xls.setAcademicOutcome("Successfully completed academic component");
    xls.setDetailedReasons("detailedReason");
    xls.setMitigatingCircumstances("mitigatingCircumstances");
    xls.setCompetencesToBeDeveloped("competencesToBeDeveloped");
    xls.setOtherRecommendedActions("otherActions");
    xls.setRecommendedAdditionalTrainingTime("additionalTraining");
    xls.setAdditionalCommentsFromPanel("additionalComments");
    xls.setKnownConcerns("Yes");
    xls.setConcernSummary("concernSummary");
    xls.setResponsibleOfficerComments("officerComments");
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    // Mock
    GradeDTO gradeAtTimeDto = new GradeDTO();
    gradeAtTimeDto.setId(2L);
    gradeAtTimeDto.setName("gradeAtTime_2");
    when(referenceServiceMock.findGradesByName("gradeAtTime_2"))
        .thenReturn(Collections.singletonList(gradeAtTimeDto));
    GradeDTO gradeAtNextRotationDto = new GradeDTO();
    gradeAtNextRotationDto.setId(4L);
    gradeAtNextRotationDto.setName("gradeAtNextRotation_2");
    when(referenceServiceMock.findGradesByName("gradeAtNextRotation_2"))
        .thenReturn(Collections.singletonList(gradeAtNextRotationDto));

    ProgrammeMembershipCurriculaDTO programmeMembershipCurriculaDto =
        new ProgrammeMembershipCurriculaDTO();
    programmeMembershipCurriculaDto.setId(2L);
    PersonDTO personDto = new PersonDTO();
    personDto.setId(1L);
    programmeMembershipCurriculaDto.setPerson(personDto);
    CurriculumDTO curriculumDto = new CurriculumDTO();
    curriculumDto.setCurriculumSubType(CurriculumSubType.AFT);
    programmeMembershipCurriculaDto.setCurriculumDTO(curriculumDto);
    CurriculumMembershipDTO curriculumMembershipDto = new CurriculumMembershipDTO();
    curriculumMembershipDto.setCurriculumStartDate(
        LocalDate.of(2020, Calendar.FEBRUARY, 1));
    curriculumMembershipDto.setCurriculumEndDate(
        LocalDate.of(2020, Calendar.AUGUST, 1));
    programmeMembershipCurriculaDto.setCurriculumMemberships(
        Collections.singletonList(curriculumMembershipDto));
    when(tcsServiceMock.getProgrammeMembershipDetailsByIds(Collections.singleton("2"))).thenReturn(
        Collections.singletonList(programmeMembershipCurriculaDto));
    AssessmentDTO updatedAssessmentDto = new AssessmentDTO();
    updatedAssessmentDto.id(1L);
    when(assessmentServiceMock.patchAssessments(any())).thenReturn(
        Collections.singletonList(updatedAssessmentDto));

    // When
    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);

    // Then
    verify(assessmentServiceMock).patchAssessments(assessmentDtoListCaptor.capture());
    List<AssessmentDTO> assessmentDtoList = assessmentDtoListCaptor.getValue();
    AssessmentDTO assessmentDtoToUpdate = assessmentDtoList.get(0);
    assertThat("The assessment Id does not match the expected value",
        assessmentDtoToUpdate.getId(), is(Long.valueOf(xls.getAssessmentId())));
    assertThat("The ProgrammeMembership Id does not match the expected value",
        assessmentDtoToUpdate.getProgrammeMembershipId(),
        is(Long.valueOf(xls.getProgrammeMembershipId())));
    assertThat("The type does not match the expected value", assessmentDtoToUpdate.getType(),
        is(xls.getType()));
    assertThat("The reviewDate does not match the expected value",
        assessmentDtoToUpdate.getReviewDate(), is(convertDate(xls.getReviewDate())));
    assertThat("The periodCoveredFrom does not match the expected value",
        assessmentDtoToUpdate.getDetail().getPeriodCoveredFrom(),
        is(convertDate(xls.getPeriodCoveredFrom())));
    assertThat("The periodCoveredTo does not match the expected value",
        assessmentDtoToUpdate.getDetail().getPeriodCoveredTo(),
        is(convertDate(xls.getPeriodCoveredTo())));
    assertThat("The monthsCountedToTraining does not match the expected value",
        assessmentDtoToUpdate.getDetail().getMonthsCountedToTraining(),
        is(Integer.valueOf(xls.getMonthsCountedToTraining())));
    assertThat("The pya does not match the expected value",
        assessmentDtoToUpdate.getDetail().getPya(),
        is(BooleanUtil.parseBoolean(xls.getPya())));
    assertThat("The gradeAtTime does not match the expected value",
        assessmentDtoToUpdate.getDetail().getGradeName(), is(xls.getGradeAtTimeName()));
    assertThat("The daysOutOfTraining does not match the expected value",
        assessmentDtoToUpdate.getDetail().getDaysOutOfTraining(),
        is(Integer.valueOf(xls.getDaysOutOfTraining())));
    assertThat("The outcome does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getOutcome(), is(xls.getOutcome()));
    assertThat("The size of reasons does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getReasons().size(), is(1));
    assertThat("The outcomeNotAssessed does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getReasons().get(0).getReasonLabel(),
        is(xls.getOutcomeNotAssessed()));
    assertThat("The outcomeNotAssessedOther does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getReasons().get(0).getOther(),
        is(xls.getOutcomeNotAssessedOther()));
    assertThat("The underAppeal does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getUnderAppeal(),
        is(BooleanUtil.parseBoolean(xls.getUnderAppeal())));
    assertThat("The externalTrainer does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getExternalTrainer(),
        is(BooleanUtil.parseBoolean(xls.getExternalTrainer())));
    assertThat("The nextReviewDate does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getNextReviewDate(),
        is(convertDate(xls.getNextReviewDate())));
    assertThat("The gradeAtNextRotation does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getNextRotationGradeName(),
        is(xls.getNextRotationGradeName()));
    assertThat("The comment does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getComments(), is(xls.getComments()));
    assertThat("The tenPercentAudit does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getTenPercentAudit(),
        is(BooleanUtil.parseBoolean(xls.getTenPercentAudit())));
    assertThat("The academicOutcome does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getAcademicOutcome(), is(xls.getAcademicOutcome()));
    assertThat("The detailedReason does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getDetailedReasons(), is(xls.getDetailedReasons()));
    assertThat("The mitigatingCircumstances does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getMitigatingCircumstances(),
        is(xls.getMitigatingCircumstances()));
    assertThat("The competencesToBeDeveloped does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getCompetencesToBeDeveloped(),
        is(xls.getCompetencesToBeDeveloped()));
    assertThat("The otherRecommendedActions does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getOtherRecommendedActions(),
        is(xls.getOtherRecommendedActions()));
    assertThat("The recommendedAdditionalTrainingTime does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getRecommendedAdditionalTrainingTime(),
        is(xls.getRecommendedAdditionalTrainingTime()));
    assertThat("The additionalCommentsFromPanel does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getAdditionalCommentsFromPanel(),
        is(xls.getAdditionalCommentsFromPanel()));
    assertThat("The knownConcerns does not match the expected value",
        assessmentDtoToUpdate.getRevalidation().getKnownConcerns(),
        is(BooleanUtil.parseBoolean(xls.getKnownConcerns())));
    assertThat("The concernSummary does not match the expected value",
        assessmentDtoToUpdate.getRevalidation().getConcernSummary(),
        is(xls.getConcernSummary()));
    assertThat("The responsibleOfficerComments does not match the expected value",
        assessmentDtoToUpdate.getRevalidation().getResponsibleOfficerComments(),
        is(xls.getResponsibleOfficerComments()));

    assertThat("Should not get any err messages",
        xlsList.get(0).getErrorMessage(),
        is(nullValue()));
    verify(xls).setSuccessfullyImported(true);
  }

  @Test
  public void testProcessAssessmentsUpdateUpload_shouldNotOverwriteWhenColumnIsEmpty() {
    // Existing Assessment
    String id = "1";
    AssessmentDTO assessmentDto = new AssessmentDTO();
    assessmentDto.id(Long.valueOf(id));
    assessmentDto.setTraineeId(1L);
    assessmentDto.setProgrammeMembershipId(1L);
    assessmentDto.setType("assessmentType_1");
    assessmentDto.setReviewDate(LocalDate.of(2020, 9, 2));
    AssessmentDetailDTO detailDto = new AssessmentDetailDTO();
    detailDto.id(1L)
        .curriculumId(1L)
        .gradeId(1L)
        .gradeName("gradeAtTime_1")
        .periodCoveredFrom(LocalDate.of(2020, 9, 1))
        .periodCoveredTo(LocalDate.of(2020, 9, 2))
        .monthsCountedToTraining(1)
        .daysOutOfTraining(1)
        .curriculumSpecialtyId("1")
        .pya(false)
        .curriculumStartDate(LocalDate.of(2020, 9, 1))
        .curriculumEndDate(LocalDate.of(2020, 9, 2))
        .curriculumSubType("subType_1");
    assessmentDto.setDetail(detailDto);
    AssessmentOutcomeDTO outcomeDto = new AssessmentOutcomeDTO();
    outcomeDto.id(1L)
        .outcome("outcome_1")
        .underAppeal(false)
        .nextReviewDate(LocalDate.of(2020, 9, 3))
        .comments("comments")
        .detailedReasons("reasons")
        .externalTrainer(false);
    List<AssessmentOutcomeReasonDTO> reasons = new ArrayList<>();
    AssessmentOutcomeReasonDTO reason1 = new AssessmentOutcomeReasonDTO();
    reason1.id(1L).setReasonLabel("reason_1");
    reasons.add(reason1);
    AssessmentOutcomeReasonDTO reason2 = new AssessmentOutcomeReasonDTO();
    reason2.id(2L).setReasonLabel("Other");
    reason2.setRequireOther(true);
    reason2.setOther("Other reason");
    reasons.add(reason2);
    outcomeDto.setReasons(reasons);
    assessmentDto.setOutcome(outcomeDto);
    RevalidationDTO revalidationDto = new RevalidationDTO();
    revalidationDto.id(1L)
        .concernSummary("concernSummary")
        .setKnownConcerns(false);
    assessmentDto.setRevalidation(revalidationDto);
    when(assessmentServiceMock.findAssessmentByIds(Collections.singleton(id))).thenReturn(
        Collections.singletonList(assessmentDto));

    // AssessmentUpdateXLS to update
    AssessmentUpdateXLS xls = Mockito.spy(new AssessmentUpdateXLS());
    xls.setAssessmentId(id);
    List<AssessmentUpdateXLS> xlsList = Collections.singletonList(xls);

    AssessmentDTO updatedAssessmentDto = new AssessmentDTO();
    updatedAssessmentDto.id(1L);
    when(assessmentServiceMock.patchAssessments(any())).thenReturn(
        Collections.singletonList(updatedAssessmentDto));
    // When
    assessmentUpdateTransformerService.processAssessmentsUpdateUpload(xlsList);
    // Then
    verify(assessmentServiceMock).patchAssessments(assessmentDtoListCaptor.capture());
    List<AssessmentDTO> assessmentDtoList = assessmentDtoListCaptor.getValue();
    AssessmentDTO assessmentDtoToUpdate = assessmentDtoList.get(0);
    assertThat("The assessment Id does not match the expected value",
        assessmentDtoToUpdate.getId(), is(Long.valueOf(xls.getAssessmentId())));
    assertThat("The ProgrammeMembership Id does not match the expected value",
        assessmentDtoToUpdate.getProgrammeMembershipId(),
        is(assessmentDto.getProgrammeMembershipId()));
    assertThat("The type does not match the expected value", assessmentDtoToUpdate.getType(),
        is(assessmentDto.getType()));
    assertThat("The reviewDate does not match the expected value",
        assessmentDtoToUpdate.getReviewDate(), is(assessmentDto.getReviewDate()));
    assertThat("The periodCoveredFrom does not match the expected value",
        assessmentDtoToUpdate.getDetail().getPeriodCoveredFrom(),
        is(assessmentDto.getDetail().getPeriodCoveredFrom()));
    assertThat("The periodCoveredTo does not match the expected value",
        assessmentDtoToUpdate.getDetail().getPeriodCoveredTo(),
        is(assessmentDto.getDetail().getPeriodCoveredTo()));
    assertThat("The pya does not match the expected value",
        assessmentDtoToUpdate.getDetail().getPya(),
        is(assessmentDto.getDetail().getPya()));
    assertThat("The curriculumStartDate does not match the expected value",
        assessmentDtoToUpdate.getDetail().getCurriculumStartDate(),
        is(assessmentDto.getDetail().getCurriculumStartDate()));
    assertThat("The curriculumEndDate does not match the expected value",
        assessmentDtoToUpdate.getDetail().getCurriculumEndDate(),
        is(assessmentDto.getDetail().getCurriculumEndDate()));
    assertThat("The gradeAtTime does not match the expected value",
        assessmentDtoToUpdate.getDetail().getGradeName(),
        is(assessmentDto.getDetail().getGradeName()));
    assertThat("The daysOutOfTraining does not match the expected value",
        assessmentDtoToUpdate.getDetail().getDaysOutOfTraining(),
        is(assessmentDto.getDetail().getDaysOutOfTraining()));
    assertThat("The outcome does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getOutcome(),
        is(assessmentDto.getOutcome().getOutcome()));
    assertThat("The underAppeal does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getUnderAppeal(),
        is(assessmentDto.getOutcome().getUnderAppeal()));
    assertThat("The nextReviewDate does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getNextReviewDate(),
        is(assessmentDto.getOutcome().getNextReviewDate()));
    assertThat("The comment does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getComments(),
        is(assessmentDto.getOutcome().getComments()));
    assertThat("The size of reasons does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getReasons().size(), is(2));
    assertThat("The otherReason does not match the expected value",
        assessmentDtoToUpdate.getOutcome().getReasons().get(0).getOther(),
        is(assessmentDto.getOutcome().getReasons().get(0).getOther()));
    assertThat("The concernSummary does not match the expected value",
        assessmentDtoToUpdate.getRevalidation().getConcernSummary(),
        is(assessmentDto.getRevalidation().getConcernSummary()));
    assertThat("The knownConcerns does not match the expected value",
        assessmentDtoToUpdate.getRevalidation().getKnownConcerns(),
        is(assessmentDto.getRevalidation().getKnownConcerns()));

    assertThat("Should not get any err messages",
        xlsList.get(0).getErrorMessage(),
        is(nullValue()));
    verify(xls).setSuccessfullyImported(true);
  }
}
