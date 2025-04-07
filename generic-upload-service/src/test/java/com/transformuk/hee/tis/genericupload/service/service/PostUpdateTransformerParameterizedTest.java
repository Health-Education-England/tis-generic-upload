package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.service.PostUpdateTransformerServiceTest.createSpecialtyDTO;
import static com.transformuk.hee.tis.genericupload.service.service.PostUpdateTransformerServiceTest.createTrustDTO;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class PostUpdateTransformerParameterizedTest {

  private PostUpdateXLS xls1;
  private List<PostUpdateXLS> xlsList;

  @InjectMocks
  private PostUpdateTransformerService service;
  private GradeDTO grade1, grade2;

  @Mock
  private ReferenceServiceImpl referenceService;

  @Mock
  private TcsServiceImpl tcsService;

  public static Stream<Arguments> gradeVariables() {
    return Stream.of(
        arguments(Status.INACTIVE, false, false),
        arguments(Status.INACTIVE, true, true),
        arguments(Status.INACTIVE, false, true),
        arguments(Status.INACTIVE, true, false),
        arguments(Status.CURRENT, false, true),
        arguments(Status.CURRENT, true, false),
        arguments(Status.CURRENT, false, false)
    );
  }

  @BeforeEach
  public void setUp() {
    service = new PostUpdateTransformerService();
    ReflectionTestUtils.setField(service, "referenceServiceImpl", referenceService);
    ReflectionTestUtils.setField(service, "tcsServiceImpl", tcsService);

    grade1 = new GradeDTO();
    grade1.setName("grade1");

    xls1 = new PostUpdateXLS();
    xls1.setPostTISId("1");
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

    SpecialtyDTO specialtyDTO = createSpecialtyDTO(12345L, "12345", "12345", "A MEDIA COLLEGE",
        "NHS_CODE", com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    TrustDTO trustDTO = createTrustDTO(42L, "TRAINING_BODY");
    PostDTO postDTO = new PostDTO();
    postDTO.setId(1L);
    when(tcsService.getPostById(1L)).thenReturn(postDTO);
    when(tcsService.getSpecialtyByName(any())).thenReturn(List.of(specialtyDTO));
    when(referenceService.findTrustByTrustKnownAs(any())).thenReturn(List.of(trustDTO));
    when(referenceService.findGradesByName("grade1")).thenReturn(Collections.singletonList(grade1));
    when(referenceService.findGradesByName("grade2")).thenReturn(Collections.singletonList(grade2));
  }

  @ParameterizedTest
  @MethodSource("gradeVariables")
  public void shouldThrowErrorMessageWhenApprovedGradeStatusIsNotCurrentWithPostGradeAndTrainingGradeValueNotTrue(
      Status status, boolean isTrainingGrade, boolean isPostGrade) {
    grade1.setStatus(status);
    grade1.setTrainingGrade(isTrainingGrade);
    grade1.setPostGrade(isPostGrade);
    xls1.setApprovedGrade(grade1.getName());
    xls1.setOtherGrades(grade2.getName());

    service.processPostUpdateUpload(xlsList, "foo");

    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        containsString("No current, post and training grade found for 'grade1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
  }

  @ParameterizedTest
  @MethodSource("gradeVariables")
  public void shouldThrowErrorMessageWhenOtherGradesStatusIsNotCurrentWithPostGradeAndTrainingGradeValueNotTrue(
      Status status, boolean isTrainingGrade, boolean isPostGrade) {
    grade1.setStatus(status);
    grade1.setTrainingGrade(isTrainingGrade);
    grade1.setPostGrade(isPostGrade);
    xls1.setApprovedGrade(grade2.getName());
    xls1.setOtherGrades(grade1.getName());

    service.processPostUpdateUpload(xlsList, "bar");

    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        containsString("No current, post and training grade found for 'grade1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
  }
}