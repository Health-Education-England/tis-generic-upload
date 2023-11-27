package com.transformuk.hee.tis.genericupload.service.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.api.dto.PostCreateXls;
import com.transformuk.hee.tis.reference.api.dto.FundingTypeDTO;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.LocalOfficeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
import com.transformuk.hee.tis.reference.client.ReferenceService;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostFundingDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostGradeDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostGradeType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSiteType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.SpecialtyType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PostCreateTransformerServiceTest {

  private static final long DAY_IN_MILLIS = 86400000L;
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

  private FundingTypeDTO fundingType1;

  @Captor
  private ArgumentCaptor<List<PostDTO>> dtoCaptor;

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
    xls1.setFundingType("funding1");
    xls1.setFundingStartDate(new Date(1L));

    xls2 = new PostCreateXls();
    xls2.setNationalPostNumber("npn2");
    xls2.setApprovedGrade("grade2");
    xls2.setSpecialty("specialty2");
    xls2.setMainSite("site2");
    xls2.setTrainingBody("trainingBody2");
    xls2.setEmployingBody("employingBody2");
    xls2.setProgrammeTisId("3;4");
    xls2.setOwner("owner2");
    xls2.setFundingType("funding1");
    xls2.setFundingStartDate(new Date(1L));

    xlsList = Arrays.asList(xls1, xls2);

    grade1 = new GradeDTO();
    grade1.setId(1L);
    grade1.setName("grade1");
    grade1.setStatus(Status.CURRENT);
    grade1.setTrainingGrade(true);
    grade1.setPostGrade(true);
    grade2 = new GradeDTO();
    grade2.setId(2L);
    grade2.setName("grade2");
    grade2.setStatus(Status.CURRENT);
    grade2.setTrainingGrade(true);
    grade2.setPostGrade(true);

    specialty1 = new SpecialtyDTO();
    specialty1.setName("specialty1");
    specialty1.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    specialty2 = new SpecialtyDTO();
    specialty2.setName("specialty2");
    specialty2.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);

    site1 = new SiteDTO();
    site1.setId(1L);
    site1.setSiteKnownAs("site1");
    site1.setStatus(Status.CURRENT);
    site2 = new SiteDTO();
    site2.setId(2L);
    site2.setSiteKnownAs("site2");
    site2.setStatus(Status.CURRENT);

    trainingBody1 = new TrustDTO();
    trainingBody1.setId(1L);
    trainingBody1.setTrustKnownAs("trainingBody1");
    trainingBody1.setStatus(Status.CURRENT);
    trainingBody2 = new TrustDTO();
    trainingBody2.setId(2L);
    trainingBody2.setTrustKnownAs("trainingBody2");
    trainingBody2.setStatus(Status.CURRENT);

    employingBody1 = new TrustDTO();
    employingBody1.setTrustKnownAs("employingBody1");
    employingBody1.setId(3L);
    employingBody1.setStatus(Status.CURRENT);
    employingBody2 = new TrustDTO();
    employingBody2.setId(4L);
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

    fundingType1 = new FundingTypeDTO();
    fundingType1.setStatus(Status.CURRENT);
    fundingType1.setLabel("funding1");
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
  public void shouldFailValidationWhenGradeNameNotFound() {
    // Given.
    xls2.setOtherGrades("grade3;grade4");

    //Approved grades
    grade1.setStatus(Status.INACTIVE);

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

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any()))
        .thenReturn(Arrays.asList(specialty1, specialty2, specialty3));

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
        is("One of the following Sub specialties is not a CURRENT specialty of type SUB_SPECIALTY: 'specialty5\",\"specialty6'."));
    assertThat("The success flag did not match the expected value.", xls3.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldFailValidationWhenTryingToUseSpecialtyNotOfTypeSubspecialtyAsPostSubSpecialty() {
    // Given.

    // Should be uploaded successfully
    SpecialtyDTO specialty3 = new SpecialtyDTO();
    specialty3.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    specialty3.setName("specialty3");
    specialty3.setSpecialtyTypes(
        new HashSet<>(Collections.singletonList(SpecialtyType.SUB_SPECIALTY)));
    xls1.setSubSpecialties("specialty3");

    // Should not be uploaded and have error
    SpecialtyDTO specialty4 = new SpecialtyDTO();
    specialty4.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    specialty4.setName("specialty4");
    specialty4.setSpecialtyTypes(new HashSet<>(Collections.singletonList(SpecialtyType.PLACEMENT)));
    xls2.setSubSpecialties("specialty4");

    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2));
    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(referenceService.findCurrentTrustsByTrustKnownAsIn(any()))
        .thenReturn(Arrays.asList(trainingBody1, trainingBody2, employingBody1, employingBody2));
    when(tcsService.findProgrammesIn(any()))
        .thenReturn(Arrays.asList(programme1, programme2, programme3, programme4));
    when(referenceService.findLocalOfficesByName(any())).thenReturn(Arrays.asList(owner1, owner2));
    when(tcsService.getSpecialtyByName("specialty1"))
        .thenReturn(Collections.singletonList((specialty1)));
    when(tcsService.getSpecialtyByName("specialty2"))
        .thenReturn(Collections.singletonList(specialty2));

    when(tcsService.getSpecialtyByName("specialty3", SpecialtyType.SUB_SPECIALTY))
        .thenReturn(Collections.singletonList(specialty3));
    when(tcsService.getSpecialtyByName("specialty4", SpecialtyType.SUB_SPECIALTY))
        .thenReturn(new ArrayList<>());
    when(referenceService.findCurrentFundingTypesByLabelIn(any()))
        .thenReturn(Collections.singletonList(fundingType1));

    // When.
    service.processUpload(Arrays.asList(xls1, xls2));

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is(nullValue()));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(true));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("One of the following Sub specialties is not a CURRENT specialty of type "
            + "SUB_SPECIALTY: 'specialty4'."));
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
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
    when(referenceService.findCurrentFundingTypesByLabelIn(any())).thenReturn(
        Collections.singletonList(fundingType1));

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
    when(referenceService.findCurrentFundingTypesByLabelIn(any())).thenReturn(
        Collections.singletonList(fundingType1));

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
  public void shouldFailValidationWhenProgrammeIdNotNumeric() {
    // Given.
    xls1.setProgrammeTisId("id1");
    xls2.setProgrammeTisId("id2");

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialty1, specialty2));
    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2));
    when(referenceService.findCurrentTrustsByTrustKnownAsIn(any()))
        .thenReturn(Arrays.asList(trainingBody1, trainingBody2, employingBody1, employingBody2));
    when(tcsService.findProgrammesIn(any()))
        .thenReturn(Arrays.asList(programme1, programme2, programme3));
    when(referenceService.findCurrentFundingTypesByLabelIn(any())).thenReturn(
        Collections.singletonList(fundingType1));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("Programme ID 'id1' is not a number."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(),
        is("Programme ID 'id2' is not a number."));
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
    when(referenceService.findCurrentFundingTypesByLabelIn(any())).thenReturn(
        Collections.singletonList(fundingType1));

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
    when(referenceService.findCurrentFundingTypesByLabelIn(any())).thenReturn(
        Collections.singletonList(fundingType1));

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
    when(referenceService.findCurrentFundingTypesByLabelIn(any())).thenReturn(
        Collections.singletonList(fundingType1));

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
  public void shouldFailValidationWhenPostFundingDatesInvalid() {
    // Given.
    xls1.setFundingStartDate(new Date(2 * DAY_IN_MILLIS));
    xls1.setFundingEndDate(new Date(DAY_IN_MILLIS));
    xls1.setFundingType("funding1");

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialty1, specialty2));
    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2));
    when(referenceService.findCurrentTrustsByTrustKnownAsIn(any()))
        .thenReturn(Arrays.asList(trainingBody1, trainingBody2, employingBody1, employingBody2));
    when(tcsService.findProgrammesIn(any()))
        .thenReturn(Arrays.asList(programme1, programme2, programme3, programme4));
    when(referenceService.findLocalOfficesByName(any())).thenReturn(Arrays.asList(owner1, owner2));
    when(referenceService.findCurrentFundingTypesByLabelIn(any())).thenReturn(
        Collections.singletonList(fundingType1));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("Funding End Date cannot be before Start Date if included."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldFailValidationWhenPostFundingBodyInvalid() {
    // Given.
    xls1.setFundingType("funding1");
    xls1.setFundingBody("funder1");

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialty1, specialty2));
    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2));
    when(referenceService.findCurrentTrustsByTrustKnownAsIn(any()))
        .thenReturn(Arrays.asList(trainingBody1, trainingBody2, employingBody1, employingBody2));
    when(tcsService.findProgrammesIn(any()))
        .thenReturn(Arrays.asList(programme1, programme2, programme3, programme4));
    when(referenceService.findLocalOfficesByName(any())).thenReturn(Arrays.asList(owner1, owner2));
    when(referenceService.findCurrentFundingTypesByLabelIn(any())).thenReturn(
        Collections.singletonList(fundingType1));

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(),
        is("No current match found for Funding Body 'funder1'."));
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(false));
  }

  @Test
  public void shouldCreatePostsWhenValidationPasses() {
    // Given.
    xls1.setTrainingDescription("trainingDescription1");
    xls1.setOtherGrades("grade1;grade2");
    xls1.setOtherSpecialties("specialty1");
    xls1.setSubSpecialties("specialty2");
    xls1.setOtherSites("site1;site2");
    xls1.setOldPost("oldPost1");

    SpecialtyDTO specialty3 = new SpecialtyDTO();
    specialty3.setName("specialty3");
    specialty3.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);

    PostDTO post1 = new PostDTO();
    post1.setNationalPostNumber("oldPost1");
    post1.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);

    when(referenceService.findGradesByName(any())).thenReturn(Arrays.asList(grade1, grade2));
    when(tcsService.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialty1, specialty3));
    when(tcsService.getSpecialtyByName("specialty2", SpecialtyType.SUB_SPECIALTY))
        .thenReturn(Collections.singletonList(specialty2));
    when(referenceService.findSitesByName(any())).thenReturn(Arrays.asList(site1, site2));
    when(referenceService.findCurrentTrustsByTrustKnownAsIn(any()))
        .thenReturn(Arrays.asList(trainingBody1, trainingBody2, employingBody1, employingBody2));
    when(tcsService.findProgrammesIn(any()))
        .thenReturn(Arrays.asList(programme1, programme2, programme3, programme4));
    when(referenceService.findLocalOfficesByName(any())).thenReturn(Arrays.asList(owner1, owner2));
    when(tcsService.findPostsByNationalPostNumbersIn(any()))
        .thenReturn(Collections.singletonList(post1));
    when(referenceService.findCurrentFundingTypesByLabelIn(any())).thenReturn(
        Collections.singletonList(fundingType1));

    when(tcsService.bulkCreateDto(dtoCaptor.capture(), any(), any()))
        .thenReturn(Collections.emptyList());

    PostDTO expected1 = new PostDTO();
    expected1.setNationalPostNumber("npn1");
    expected1.setTrainingBodyId(1L);
    expected1.setEmployingBodyId(3L);
    expected1.setOwner("owner1");
    expected1.setTrainingDescription("trainingDescription1");
    expected1.setOldPost(post1);
    expected1.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    expected1.setBypassNPNGeneration(true);
    expected1.setProgrammes(Stream.of(programme1, programme2).collect(Collectors.toSet()));
    expected1.setGrades(Stream.of(
        new PostGradeDTO(null, 1L, PostGradeType.APPROVED),
        new PostGradeDTO(null, 1L, PostGradeType.OTHER),
        new PostGradeDTO(null, 2L, PostGradeType.OTHER)
    ).collect(Collectors.toSet()));
    expected1.setSites(Stream.of(
        new PostSiteDTO(null, 1L, PostSiteType.PRIMARY),
        new PostSiteDTO(null, 1L, PostSiteType.OTHER),
        new PostSiteDTO(null, 2L, PostSiteType.OTHER)
    ).collect(Collectors.toSet()));
    expected1.specialties(Stream.of(
        new PostSpecialtyDTO(null, specialty1, PostSpecialtyType.PRIMARY),
        new PostSpecialtyDTO(null, specialty1, PostSpecialtyType.OTHER),
        new PostSpecialtyDTO(null, specialty2, PostSpecialtyType.SUB_SPECIALTY)
    ).collect(Collectors.toSet()));
    PostFundingDTO expectedFunding = new PostFundingDTO();
    expectedFunding.setFundingType("funding1");
    expectedFunding.setStartDate(LocalDate.of(1970, 1, 1));
    expected1.addFunding(expectedFunding);

    PostDTO expected2 = new PostDTO();
    expected2.setNationalPostNumber("npn2");
    expected2.setTrainingBodyId(2L);
    expected2.setEmployingBodyId(4L);
    expected2.setOwner("owner2");
    expected2.setStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    expected2.setBypassNPNGeneration(true);
    expected2.setProgrammes(Stream.of(programme3, programme4).collect(Collectors.toSet()));
    expected2.setGrades(Collections.singleton(new PostGradeDTO(null, 2L, PostGradeType.APPROVED)));
    expected2.setSites(Collections.singleton(new PostSiteDTO(null, 2L, PostSiteType.PRIMARY)));
    expected2.specialties(
        Collections.singleton(new PostSpecialtyDTO(null, specialty2, PostSpecialtyType.PRIMARY)));
    expected2.addFunding(expectedFunding);

    // When.
    service.processUpload(xlsList);

    // Then.
    assertThat("The error did not match the expected value.", xls1.getErrorMessage(), nullValue());
    assertThat("The success flag did not match the expected value.", xls1.isSuccessfullyImported(),
        is(true));
    assertThat("The error did not match the expected value.", xls2.getErrorMessage(), nullValue());
    assertThat("The success flag did not match the expected value.", xls2.isSuccessfullyImported(),
        is(true));

    List<PostDTO> postDtos = dtoCaptor.getValue();
    assertThat("The number of built DTOs did not match the expected value.", postDtos.size(),
        is(2));

    assertThat("The first DTO did not match the expected value.", postDtos.get(0),
        is(expected1));
    assertThat("The second DTO did not match the expected value.", postDtos.get(1),
        is(expected2));
  }
}
