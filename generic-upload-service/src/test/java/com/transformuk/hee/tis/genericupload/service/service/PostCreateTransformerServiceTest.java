package com.transformuk.hee.tis.genericupload.service.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.PostCreateXls;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.LocalOfficeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
import com.transformuk.hee.tis.reference.client.ReferenceService;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PostCreateTransformerServiceTest {

  private PostCreateTransformerService service;

  @Mock
  private ReferenceService referenceService;

  @Mock
  private TcsServiceImpl tcsService;

  private PostCreateXls xls1;
  private PostCreateXls xls2;
  private List<PostCreateXls> xlsList;

  private GradeDTO grade1;
  private GradeDTO grade2;

  private SpecialtyDTO specialty1;
  private SpecialtyDTO specialty2;

  private SiteDTO site1;
  private SiteDTO site2;

  private TrustDTO trainingBody1;
  private TrustDTO trainingBody2;
  private TrustDTO employingBody1;
  private TrustDTO employingBody2;

  private ProgrammeDTO programme1;
  private ProgrammeDTO programme2;
  private ProgrammeDTO programme3;
  private ProgrammeDTO programme4;

  private LocalOfficeDTO owner1;
  private LocalOfficeDTO owner2;

  @Before
  public void setUp() {
    service = new PostCreateTransformerService(referenceService, tcsService);

    xls1 = new PostCreateXls();
    xls1.setNationalPostNumber("npn1");
    xls1.setApprovedGrade("grade1");
    xls1.setSpecialty("specialty1");
    xls1.setMainSite("site1");
    xls1.setTrainingBody("trainingBody1");
    xls1.setEmployingBody("employingBody1");
    xls1.setProgrammeTisId("1;2");
    xls1.setOwner("owner1");

    xls2 = new PostCreateXls();
    xls2.setNationalPostNumber("npn2");
    xls2.setApprovedGrade("grade2");
    xls2.setSpecialty("specialty2");
    xls2.setMainSite("site2");
    xls2.setTrainingBody("trainingBody2");
    xls2.setEmployingBody("employingBody2");
    xls2.setProgrammeTisId("3;4");
    xls2.setOwner("owner2");

    xlsList = Arrays.asList(xls1, xls2);

    grade1 = new GradeDTO();
    grade1.setName("grade1");
    grade1.setStatus(Status.CURRENT);
    grade2 = new GradeDTO();
    grade2.setName("grade2");
    grade2.setStatus(Status.CURRENT);

    specialty1 = new SpecialtyDTO();
    specialty1.setName("specialty1");
    specialty1.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    specialty2 = new SpecialtyDTO();
    specialty2.setName("specialty2");
    specialty2.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);

    site1 = new SiteDTO();
    site1.setSiteKnownAs("site1");
    site1.setStatus(Status.CURRENT);
    site2 = new SiteDTO();
    site2.setSiteKnownAs("site2");
    site2.setStatus(Status.CURRENT);

    trainingBody1 = new TrustDTO();
    trainingBody1.setTrustKnownAs("trainingBody1");
    trainingBody1.setStatus(Status.CURRENT);
    trainingBody2 = new TrustDTO();
    trainingBody2.setTrustKnownAs("trainingBody2");
    trainingBody2.setStatus(Status.CURRENT);

    employingBody1 = new TrustDTO();
    employingBody1.setTrustKnownAs("employingBody1");
    employingBody1.setStatus(Status.CURRENT);
    employingBody2 = new TrustDTO();
    employingBody2.setTrustKnownAs("employingBody2");
    employingBody2.setStatus(Status.CURRENT);

    programme1 = new ProgrammeDTO();
    programme1.setId(1L);
    programme1.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    programme2 = new ProgrammeDTO();
    programme2.setId(2L);
    programme2.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    programme3 = new ProgrammeDTO();
    programme3.setId(3L);
    programme3.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    programme4 = new ProgrammeDTO();
    programme4.setId(4L);
    programme4.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);

    owner1 = new LocalOfficeDTO();
    owner1.setName("owner1");
    owner1.setStatus(Status.CURRENT);
    owner2 = new LocalOfficeDTO();
    owner2.setName("owner2");
    owner2.setStatus(Status.CURRENT);
  }

  @Test
  public void shouldFailValidationWhenNpnDuplicated() {
    // Given.
    xls2.setNationalPostNumber("npn1");

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("Duplicate NPN 'npn1' in upload."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("Duplicate NPN 'npn1' in upload."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldFailValidationWhenNpnAlreadyExists() {
    // Given.
    PostDTO post1 = new PostDTO();
    post1.setNationalPostNumber("npn1");
    PostDTO post2 = new PostDTO();
    post2.setNationalPostNumber("npn2");
    List<PostDTO> posts = Arrays.asList(post1, post2);

    when(tcsService.findPostsByNationalPostNumbersIn(any())).thenReturn(posts);

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("Post already exists with the NPN 'npn1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("Post already exists with the NPN 'npn2'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldFailValidationWhenCurrentGradeNotFound() {
    // Given.
    xls2.setOtherGrades("grade3;grade4");

    grade1.setStatus(Status.INACTIVE);
    GradeDTO grade3 = new GradeDTO();
    grade3.setName("grade3");
    grade3.setStatus(Status.CURRENT);

    when(referenceService.findGradesByName(any()))
        .thenReturn(Arrays.asList(grade1, grade2, grade3));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("Current grade not found with the name 'grade1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("Current grade not found with the name 'grade4'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldFailValidationWhenCurrentSpecialtyNotFound() {
    // Given.
    xls2.setOtherSpecialties("specialty3;specialty4");
    PostCreateXls xls3 = new PostCreateXls();
    xls3.setNationalPostNumber("npn3");
    xls3.setApprovedGrade("grade1");
    xls3.setSpecialty("specialty2");
    xls3.setSubSpecialties("specialty5;specialty6");

    specialty1.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.INACTIVE);
    SpecialtyDTO specialty3 = new SpecialtyDTO();
    specialty3.setName("specialty3");
    specialty3.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    SpecialtyDTO specialty5 = new SpecialtyDTO();
    specialty5.setName("specialty5");
    specialty5.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any()))
        .thenReturn(Arrays.asList(specialty1, specialty2, specialty3, specialty5));

    // When.
    service.processUpload(Arrays.asList(xls1, xls2, xls3));

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("Current specialty not found with the name 'specialty1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("Current specialty not found with the name 'specialty4'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls3.getErrorMessage(),
        is("Current specialty not found with the name 'specialty6'."));
    assertThat("The success flag did not match the expected value.", xls3.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldFailValidationWhenCurrentSiteNotFound() {
    // Given.
    xls2.setOtherSites("site3;site4");

    site1.setStatus(Status.INACTIVE);
    SiteDTO site3 = new SiteDTO();
    site3.setSiteKnownAs("site3");
    site3.setStatus(Status.CURRENT);

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialty1, specialty2));
    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2, site3));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("Current site not found with the name 'site1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("Current site not found with the name 'site4'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldFailValidationWhenCurrentTrainingBodyNotFound() {
    // Given.
    trainingBody1.setStatus(Status.INACTIVE);

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialty1, specialty2));
    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2));
    when(referenceService.findCurrentTrustsByTrustKnownAsIn(any()))
        .thenReturn(Arrays.asList(trainingBody1, employingBody1, employingBody2));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("Current training body not found with the name 'trainingBody1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("Current training body not found with the name 'trainingBody2'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldFailValidationWhenCurrentEmployingBodyNotFound() {
    // Given.
    employingBody1.setStatus(Status.INACTIVE);

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialty1, specialty2));
    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2));
    when(referenceService.findCurrentTrustsByTrustKnownAsIn(any()))
        .thenReturn(Arrays.asList(trainingBody1, trainingBody2, employingBody1));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("Current employing body not found with the name 'employingBody1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("Current employing body not found with the name 'employingBody2'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldFailValidationWhenCurrentProgrammeNotFound() {
    // Given.
    programme1.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.INACTIVE);

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialty1, specialty2));
    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2));
    when(referenceService.findCurrentTrustsByTrustKnownAsIn(any()))
        .thenReturn(Arrays.asList(trainingBody1, trainingBody2, employingBody1, employingBody2));
    when(tcsService.findProgrammesIn(any()))
        .thenReturn(Arrays.asList(programme1, programme2, programme3));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("Current programme not found with the ID '1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("Current programme not found with the ID '4'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldFailValidationWhenCurrentOwnerNotFound() {
    // Given.
    owner1.setStatus(Status.INACTIVE);
    owner2.setStatus(Status.INACTIVE);

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialty1, specialty2));
    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2));
    when(referenceService.findCurrentTrustsByTrustKnownAsIn(any()))
        .thenReturn(Arrays.asList(trainingBody1, trainingBody2, employingBody1, employingBody2));
    when(tcsService.findProgrammesIn(any()))
        .thenReturn(Arrays.asList(programme1, programme2, programme3, programme4));
    when(referenceService.findLocalOfficesByName(any())).thenReturn(Arrays.asList(owner1, owner2));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("Current owner not found with the name 'owner1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("Current owner not found with the name 'owner2'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldFailValidationWhenOldPostNotFound() {
    // Given.
    xls1.setOldPost("oldPost1");
    xls2.setOldPost("oldPost2");

    PostDTO post1 = new PostDTO();
    post1.setNationalPostNumber("oldPost1");
    post1.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.INACTIVE);

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialty1, specialty2));
    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2));
    when(referenceService.findCurrentTrustsByTrustKnownAsIn(any()))
        .thenReturn(Arrays.asList(trainingBody1, trainingBody2, employingBody1, employingBody2));
    when(tcsService.findProgrammesIn(any()))
        .thenReturn(Arrays.asList(programme1, programme2, programme3, programme4));
    when(referenceService.findLocalOfficesByName(any())).thenReturn(Arrays.asList(owner1, owner2));
    when(tcsService.findPostsByNationalPostNumbersIn(any()))
        .thenReturn(Collections.singletonList(post1));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(), nullValue());
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(true));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("Old post not found with the NPN 'oldPost2'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldCreatePostsWhenValidationPasses() {
    // Given.
    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialty1, specialty2));
    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2));
    when(referenceService.findCurrentTrustsByTrustKnownAsIn(any()))
        .thenReturn(Arrays.asList(trainingBody1, trainingBody2, employingBody1, employingBody2));
    when(tcsService.findProgrammesIn(any()))
        .thenReturn(Arrays.asList(programme1, programme2, programme3, programme4));
    when(referenceService.findLocalOfficesByName(any())).thenReturn(Arrays.asList(owner1, owner2));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(), nullValue());
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(true));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(), nullValue());
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(true));
  }
}
