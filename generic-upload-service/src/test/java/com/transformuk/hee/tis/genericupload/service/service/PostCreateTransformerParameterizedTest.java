package com.transformuk.hee.tis.genericupload.service.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.PostCreateXls;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class PostCreateTransformerParameterizedTest {

  private final Status status;
  private final boolean isTrainingGrade;
  private final boolean isPostGrade;

  private PostCreateXls xls1;
  private List<PostCreateXls> xlsList;

  private PostCreateTransformerService service;
  private GradeDTO grade1, grade2;

  @Mock
  private ReferenceServiceImpl referenceService;

  @Mock
  private TcsServiceImpl tcsService;

  public PostCreateTransformerParameterizedTest(Status status, boolean isTrainingGrade,
      boolean isPostGrade) {
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
    grade1.setName("grade1");
    grade1.setStatus(status);
    grade1.setTrainingGrade(isTrainingGrade);
    grade1.setPostGrade(isPostGrade);

    xls1 = new PostCreateXls();
    xls1.setNationalPostNumber("npn1");
    xls1.setSpecialty("specialty1");
    xls1.setMainSite("site1");
    xls1.setTrainingBody("trainingBody1");
    xls1.setEmployingBody("employingBody1");
    xls1.setProgrammeTisId("1;2");
    xls1.setOwner("owner1");

    grade2 = new GradeDTO();
    grade2.setId(2L);
    grade2.setName("grade2");
    grade2.setStatus(Status.CURRENT);
    grade2.setTrainingGrade(true);
    grade2.setPostGrade(true);

    xlsList = Collections.singletonList(xls1);
  }

  @Test
  public void shouldThrowErrorMessageWhenApprovedGradeStatusIsNotCurrentWithPostGradeAndTrainingGradeValueNotTrue() {
    // Given.
    //Approved grade
    xls1.setApprovedGrade("grade1");
    //Other grades
    xls1.setOtherGrades("grade2");

    when(referenceService.findGradesByName(any()))
        .thenReturn(Arrays.asList(grade1, grade2));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("No current, post and training grade found for 'grade1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldThrowErrorMessageWhenOtherGradesStatusIsNotCurrentWithPostGradeAndTrainingGradeValueNotTrue() {
    // Given.
    // Approved grade
    xls1.setApprovedGrade(grade2.getName());

    // Other grades
    xls1.setOtherGrades(grade1.getName());

    when(referenceService.findGradesByName(any()))
        .thenReturn(Arrays.asList(grade2, grade1));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("No current, post and training grade found for 'grade1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
  }
}