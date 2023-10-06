package com.transformuk.hee.tis.genericupload.service.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.PostCreateXls;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class PostCreateParameterizedTest {

  private final Status status;
  private final boolean isTrainingGrade;
  private final boolean isPostGrade;

  private PostCreateXls xls1;
  private PostCreateXls xls2;
  private List<PostCreateXls> xlsList;

  private PostCreateTransformerService service;
  private GradeDTO grade1, grade2;

  @Mock
  private ReferenceServiceImpl referenceService;

  @Mock
  private TcsServiceImpl tcsService;

  public PostCreateParameterizedTest(Status status, boolean isTrainingGrade, boolean isPostGrade) {
    this.status = status;
    this.isTrainingGrade = isTrainingGrade;
    this.isPostGrade = isPostGrade;
  }

  @Parameters
  public static Collection<Object[]> gradeTestCases() {
    return Arrays.asList(new Object[][]{
        {Status.INACTIVE, false, false},
        {Status.INACTIVE, true, true},
        {Status.INACTIVE, false, true},
        {Status.INACTIVE, true, false},
        {Status.CURRENT, false, true},
        {Status.CURRENT, true, false},
        {Status.CURRENT, false, false}
    });
  }

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    service = new PostCreateTransformerService(referenceService, tcsService);

    grade1 = new GradeDTO();

    xls1 = new PostCreateXls();
    xls1.setNationalPostNumber("npn1");
    xls1.setSpecialty("specialty1");
    xls1.setMainSite("site1");
    xls1.setTrainingBody("trainingBody1");
    xls1.setEmployingBody("employingBody1");
    xls1.setProgrammeTisId("1;2");
    xls1.setOwner("owner1");
    xls1.setApprovedGrade("grade1");

    xls2 = new PostCreateXls();
    xls2.setNationalPostNumber("npn2");
    xls2.setSpecialty("specialty2");
    xls2.setMainSite("site2");
    xls2.setTrainingBody("trainingBody2");
    xls2.setEmployingBody("employingBody2");
    xls2.setProgrammeTisId("3;4");
    xls2.setOwner("owner2");
    xls2.setApprovedGrade("grade2");

    grade2 = new GradeDTO();
    grade2.setId(2L);
    grade2.setName("grade2");
    grade2.setStatus(Status.CURRENT);
    grade2.setTrainingGrade(true);
    grade2.setPostGrade(true);

    xlsList = Arrays.asList(xls1, xls2);
  }

  @Test
  public void shouldThrowErrorMessageWhenApprovedGradeStatusIsNotCurrentWithPostGradeAndTrainingGradeValueNotTrue() {
    // Given.
    xls2.setOtherGrades("grade3;grade4");

    //Approved grades
    grade1.setStatus(status);
    grade1.setTrainingGrade(isTrainingGrade);
    grade1.setPostGrade(isPostGrade);

    //Other grades
    GradeDTO grade3 = new GradeDTO();
    grade3.setName("grade3");
    grade3.setStatus(Status.CURRENT);
    grade3.setPostGrade(true);
    grade3.setTrainingGrade(true);

    when(referenceService.findGradesByName(any()))
        .thenReturn(Arrays.asList(grade1, grade2, grade3));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("No current, post and training grade found for 'grade1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("No current, post and training grade found for 'grade4'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldThrowErrorMessageWhenOtherGradesStatusIsNotCurrentWithPostGradeAndTrainingGradeValueNotTrue() {
    // Given.
    // Approved grades
    GradeDTO grade1 = new GradeDTO();
    grade1.setName("grade1");
    grade1.setStatus(Status.CURRENT);
    grade1.setTrainingGrade(true);
    grade1.setPostGrade(true);
    xls1.setApprovedGrade(grade1.getName());

    GradeDTO grade2 = new GradeDTO();
    grade2.setName("grade2");
    grade2.setStatus(Status.CURRENT);
    grade2.setTrainingGrade(true);
    grade2.setPostGrade(true);
    xls2.setApprovedGrade(grade2.getName());

    // Other grades
    GradeDTO grade3 = new GradeDTO();
    grade3.setName("grade3");
    grade3.setStatus(status);
    grade3.setPostGrade(isPostGrade);
    grade3.setTrainingGrade(isTrainingGrade);

    xls1.setOtherGrades(grade3.getName());
    xls2.setOtherGrades("grade3");

    when(referenceService.findGradesByName(any()))
        .thenReturn(Arrays.asList(grade1, grade2, grade3));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("No current, post and training grade found for 'grade3'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("No current, post and training grade found for 'grade3'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
  }
}