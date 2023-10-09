package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.util.MultiValueUtil.MULTI_VALUE_SEPARATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostGradeDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.RotationDTO;
import com.transformuk.hee.tis.tcs.api.dto.RotationPostDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostGradeType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSiteType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PostUpdateTransformerServiceTest {

  private static final String ANOTHER = "12345another";
  private static final String SUB_SPECIALTY = "12345sub";
  private static final String TRAINING_DESCRIPTION = "12345training";
  private static final Long TRAINING_BODY_ID = 1234L;
  private static final String TRAINING_BODY = "1234body";
  private static final Long APPROVED_GRADE_ID = 1L;
  private static final String APPROVED_GRADE_NAME = "1234grade";
  private static final Long SITE_ID = 123L;
  private static final String SITE_NAME = "1234site";

  private static Map<String, List<SpecialtyDTO>> specialtyByName, specialtyByNameWithDuplicate;
  private static Map<String, List<GradeDTO>> gradeByName;
  private static Map<String, List<SiteDTO>> siteByName;
  private static Map<String, List<TrustDTO>> trustByTrustKnownAs;

  @InjectMocks
  private PostUpdateTransformerService postUpdateTransformerService;

  @Mock
  private TcsServiceImpl tcsServiceImpl;
  @Mock
  private ReferenceServiceImpl referenceServiceImpl;

  private PostUpdateXLS postXLS;
  private PostDTO postDTO;

  @Captor
  private ArgumentCaptor<List<RotationPostDTO>> rotationPostDtosCaptor;
  @Captor
  private ArgumentCaptor<PostDTO> postDtoCaptor;

  static SpecialtyDTO createSpecialtyDTO(Long id, String intrepidId, String name,
      String college, String specialtyCode, Status status) {
    SpecialtyDTO specialtyDTO = new SpecialtyDTO();
    specialtyDTO.setId(id);
    specialtyDTO.setIntrepidId(intrepidId);
    specialtyDTO.setSpecialtyCode(specialtyCode);
    specialtyDTO.setName(name);
    specialtyDTO.setCollege(college);
    specialtyDTO.setStatus(status);
    return specialtyDTO;
  }

  public static List<SpecialtyDTO> getSpecialtiesForString(String name) {
    return specialtyByName.get(name);
  }

  public static List<SpecialtyDTO> getSpecialtiesWithDuplicatesForSpecialtyName(
      String specialtyName) {
    return specialtyByNameWithDuplicate.get(specialtyName);
  }

  private static GradeDTO createGradeDTO(Long id, String name) {
    GradeDTO result = new GradeDTO();
    result.setName(name);
    result.setId(id);
    return result;
  }

  public static List<GradeDTO> getGradeDTOsForName(String name) {
    return gradeByName.get(name);
  }

  private static SiteDTO createSiteDTO(Long id, String name) {
    SiteDTO result = new SiteDTO();
    result.setSiteName(name);
    result.setId(id);
    return result;
  }

  public static List<SiteDTO> getSiteDTOsForName(String name) {
    return siteByName.get(name);
  }

  public static TrustDTO createTrustDTO(Long id, String name) {
    TrustDTO result = new TrustDTO();
    result.setTrustKnownAs(name);
    result.setId(id);
    return result;
  }

  public static List<TrustDTO> getTrustsByTrustKnownAs(String name) {
    return trustByTrustKnownAs.get(name);
  }

  @Before
  public void initialise() throws Exception {
    initialiseData();
  }

  public void initialiseData() throws Exception {
    // mock specialties
    SpecialtyDTO specialtyDTO = createSpecialtyDTO(12345L, "12345", "12345", "A MEDIA COLLEGE",
        "NHS_CODE", Status.CURRENT);
    SpecialtyDTO specialtyDTOWithSameName = createSpecialtyDTO(123456L, "123456", "12345",
        "A MEDIA COLLEGE", "NHS_CODE", Status.CURRENT);
    SpecialtyDTO anotherSpecialtyDTO = createSpecialtyDTO(123457L, "123457", ANOTHER,
        "A MEDIA COLLEGE 3", "NHS_CODE 3", Status.CURRENT);
    SpecialtyDTO subSpecialtyDTO = createSpecialtyDTO(123458L, "123458", SUB_SPECIALTY,
        "A MEDIA COLLEGE 3", "NHS_CODE 3", Status.CURRENT);
    specialtyByName = new HashMap<>();
    createSingleListWithSpecialty(specialtyByName, specialtyDTO);
    createSingleListWithSpecialty(specialtyByName, anotherSpecialtyDTO);
    createSingleListWithSpecialty(specialtyByName, subSpecialtyDTO);
    specialtyByNameWithDuplicate = new HashMap<>();
    createSingleListWithSpecialty(specialtyByNameWithDuplicate, specialtyDTO);
    specialtyByNameWithDuplicate.get(specialtyDTO.getName()).add(specialtyDTOWithSameName);

    // mock grades
    GradeDTO gradeDTO = createGradeDTO(APPROVED_GRADE_ID, APPROVED_GRADE_NAME);
    gradeByName = new HashMap<>();
    gradeDTO.setStatus(com.transformuk.hee.tis.reference.api.enums.Status.CURRENT);
    gradeDTO.setPostGrade(true);
    gradeDTO.setTrainingGrade(true);
    createSingleListWithGrade(gradeByName, gradeDTO);

    // mock sites
    siteByName = new HashMap<>();
    SiteDTO siteDTO = createSiteDTO(SITE_ID, SITE_NAME);
    createSingleListWithSite(siteByName, siteDTO);

    // mock trusts
    trustByTrustKnownAs = new HashMap<>();
    TrustDTO trustDTO = createTrustDTO(TRAINING_BODY_ID, TRAINING_BODY);
    createSingleListWithTrust(trustByTrustKnownAs, trustDTO);

    // mock XLS and postDTO
    postXLS = createPostXLS("1", specialtyDTO.getName(), anotherSpecialtyDTO.getName(),
        subSpecialtyDTO.getName(), TRAINING_BODY);
    postDTO = new PostDTO();
  }

  public PostUpdateXLS createPostXLS(String postId, String specialtyName, String otherSpecialtyName,
      String subSpecialtyName, String trainingBody) {
    PostUpdateXLS postXLS = new PostUpdateXLS();
    postXLS.setPostTISId(postId);
    postXLS.setSpecialty(specialtyName);
    postXLS.setOtherSpecialties(otherSpecialtyName);
    postXLS.setSubSpecialties(subSpecialtyName);
    postXLS.setTrainingBody(trainingBody);
    return postXLS;
  }

  public void createSingleListWithSpecialty(Map<String, List<SpecialtyDTO>> specialtyByName,
      SpecialtyDTO specialtyDTO) throws Exception {
    if (specialtyByName.get(specialtyDTO.getName()) == null) {
      specialtyByName.put(specialtyDTO.getName(), new ArrayList<>());
      specialtyByName.get(specialtyDTO.getName()).add(specialtyDTO);
    } else {
      throw new Exception("Duplicated specialtyDTO : " + specialtyDTO.getName());
    }
  }

  public void createSingleListWithGrade(Map<String, List<GradeDTO>> gradeByName, GradeDTO gradeDTO)
      throws Exception {
    if (gradeByName.get(gradeDTO.getName()) == null) {
      gradeByName.put(gradeDTO.getName(), new ArrayList<>());
      gradeByName.get(gradeDTO.getName()).add(gradeDTO);
    } else {
      throw new Exception("Duplicated gradeDTO : " + gradeDTO.getName());
    }
  }

  public void createSingleListWithSite(Map<String, List<SiteDTO>> siteByName, SiteDTO siteDTO)
      throws Exception {
    if (siteByName.get(siteDTO.getSiteName()) == null) {
      siteByName.put(siteDTO.getSiteName(), new ArrayList<>());
      siteByName.get(siteDTO.getSiteName()).add(siteDTO);
    } else {
      throw new Exception("Duplicated siteDTO : " + siteDTO.getSiteName());
    }
  }

  public void createSingleListWithTrust(Map<String, List<TrustDTO>> trustByTrustKnownAs,
      TrustDTO trustDTO) throws Exception {
    if (trustByTrustKnownAs.get(trustDTO.getTrustKnownAs()) == null) {
      trustByTrustKnownAs.put(trustDTO.getTrustKnownAs(), new ArrayList<>());
      trustByTrustKnownAs.get(trustDTO.getTrustKnownAs()).add(trustDTO);
    } else {
      throw new Exception("Duplicated siteDTO : " + trustDTO.getTrustKnownAs());
    }
  }

  @Test
  public void canHandleAnUnknownSpecialty() {
    postXLS.setSpecialty("Unknown");
    postUpdateTransformerService.setSpecialties(postXLS, postDTO,
        PostUpdateTransformerServiceTest::getSpecialtiesForString);
    assertThat(postDTO.getSpecialties().size()).isEqualTo(2);
  }

  @Test
  public void canBuildSpecialtiesForPost() {
    postUpdateTransformerService.setSpecialties(postXLS, postDTO,
        PostUpdateTransformerServiceTest::getSpecialtiesForString);
    assertThat(postDTO.getSpecialties().size()).isEqualTo(3);
    Iterator<PostSpecialtyDTO> postSpecialtyDTO = postDTO.getSpecialties().iterator();
    while (postSpecialtyDTO.hasNext()) {
      assertThat(postSpecialtyDTO.next().getPostSpecialtyType()).isNotNull();
    }
  }

  @Test
  public void doesNotBuildSpecialtiesIfDuplicatesSpecialtiesExist() {
    postUpdateTransformerService.setSpecialties(postXLS, postDTO,
        PostUpdateTransformerServiceTest::getSpecialtiesWithDuplicatesForSpecialtyName);
    assertThat(postDTO.getSpecialties().size()).isEqualTo(0);
  }

  @Test
  public void doesNotUpdateTrainingDescriptionIfNull() {
    postXLS.setTrainingDescription(TRAINING_DESCRIPTION);
    postUpdateTransformerService.updateTrainingDescription(postXLS, postDTO);
    postXLS.setTrainingDescription(null);
    postUpdateTransformerService.updateTrainingDescription(postXLS, postDTO);
    assertThat(postDTO.getTrainingDescription()).isEqualTo(TRAINING_DESCRIPTION);
  }

  @Test
  public void canUpdateTrainingDescription() {
    postXLS.setTrainingDescription(TRAINING_DESCRIPTION);
    postUpdateTransformerService.updateTrainingDescription(postXLS, postDTO);
    assertThat(postDTO.getTrainingDescription()).isEqualTo(TRAINING_DESCRIPTION);
  }

  @Test
  public void canUpdateTrainingBody() {
    postXLS.setTrainingBody(TRAINING_BODY);
    postUpdateTransformerService.updateTrustReferences(postXLS, postDTO,
        PostUpdateTransformerServiceTest::getTrustsByTrustKnownAs);
    assertThat(postDTO.getTrainingBodyId()).isEqualTo(TRAINING_BODY_ID);
  }

  @Test
  public void canUpdateMainSite() {
    postXLS.setMainSite(SITE_NAME);
    postUpdateTransformerService
        .updateSites(postXLS, postDTO, PostUpdateTransformerServiceTest::getSiteDTOsForName);
    assertThat(postDTO.getSites().size()).isEqualTo(1);
    assertThat(postDTO.getSites().iterator().next().getSiteId()).isEqualTo(SITE_ID);
  }

  @Test
  public void canUpdateApprovedGrade() {
    postXLS.setApprovedGrade(APPROVED_GRADE_NAME);

    // set existing other grades
    PostGradeDTO postGradeDTO = new PostGradeDTO(1l, 2L, PostGradeType.APPROVED);
    postDTO.setGrades(Sets.newHashSet(postGradeDTO));

    postUpdateTransformerService
        .updateGrades(postXLS, postDTO, PostUpdateTransformerServiceTest::getGradeDTOsForName);
    assertThat(postDTO.getGrades().size()).isEqualTo(1);
    assertThat(postDTO.getGrades().iterator().next().getGradeId()).isEqualTo(APPROVED_GRADE_ID);
  }

  /**
   * Test that a post rotation is are created when a single rotation is given.
   */
  @Test
  public void testProcessPostUpdateUpload_singleRotation_createRotationPost() {
    // Set up test scenario.
    postXLS.setPostTISId("1");
    postXLS.setRotations("rotation1");

    postDTO.setId(1L);
    ProgrammeDTO programmeDto = new ProgrammeDTO();
    programmeDto.setId(2L);
    postDTO.setProgrammes(Collections.singleton(programmeDto));

    RotationDTO rotation1 = new RotationDTO();
    rotation1.setId(3L);
    rotation1.setName("rotation1");

    // Record expectations.
    when(tcsServiceImpl.getPostById(1L)).thenReturn(postDTO);
    when(tcsServiceImpl.getRotationByProgrammeIdsIn(Collections.singletonList(2L)))
        .thenReturn(Collections.singletonList(rotation1));

    // Call the code under test.
    postUpdateTransformerService.processPostUpdateUpload(Collections.singletonList(postXLS), "");

    // Verify expectations.
    verify(tcsServiceImpl).deleteRotationsForPostId(1L);
    verify(tcsServiceImpl).createRotationsForPost(rotationPostDtosCaptor.capture());

    // Perform assertions.
    MatcherAssert.assertThat("The XLS error messages contained an unexpected value.",
        postXLS.getErrorMessage(), CoreMatchers.not(containsString("rotation")));

    List<RotationPostDTO> rotationPostDtos = rotationPostDtosCaptor.getValue();
    MatcherAssert.assertThat("The number of RotationPostDTOs did not match the expected value.",
        rotationPostDtos.size(), CoreMatchers.is(1));

    RotationPostDTO rotationPostDto = rotationPostDtos.get(0);
    MatcherAssert.assertThat("The RotationPostDTO's ID did not match the expected value.",
        rotationPostDto.getId(), CoreMatchers.nullValue());
    MatcherAssert.assertThat("The RotationPostDTO's post ID did not match the expected value.",
        rotationPostDto.getPostId(), CoreMatchers.is(1L));
    MatcherAssert.assertThat("The RotationPostDTO's rotation ID did not match the expected value.",
        rotationPostDto.getRotationId(), CoreMatchers.is(3L));
    MatcherAssert.assertThat("The RotationPostDTO's programme ID did not match the expected value.",
        rotationPostDto.getProgrammeId(), CoreMatchers.nullValue());
  }

  /**
   * Test that post rotations are created when multiple rotation is given.
   */
  @Test
  public void testProcessPostUpdateUpload_multipleRotations_createRotationPosts() {
    // Set up test scenario.
    postXLS.setPostTISId("1");
    postXLS.setRotations("rotation1;rotation2;rotation3");

    postDTO.setId(1L);
    ProgrammeDTO programmeDto1 = new ProgrammeDTO();
    programmeDto1.setId(2L);
    ProgrammeDTO programmeDto2 = new ProgrammeDTO();
    programmeDto2.setId(3L);
    postDTO.setProgrammes(Sets.newHashSet(programmeDto1, programmeDto2));

    RotationDTO rotation1 = new RotationDTO();
    rotation1.setId(4L);
    rotation1.setName("rotation1");

    RotationDTO rotation2 = new RotationDTO();
    rotation2.setId(5L);
    rotation2.setName("rotation2");

    RotationDTO rotation3 = new RotationDTO();
    rotation3.setId(6L);
    rotation3.setName("rotation3");

    // Record expectations.
    when(tcsServiceImpl.getPostById(1L)).thenReturn(postDTO);
    when(tcsServiceImpl.getRotationByProgrammeIdsIn(Arrays.asList(2L, 3L)))
        .thenReturn(Arrays.asList(rotation1, rotation2, rotation3));

    // Call the code under test.
    postUpdateTransformerService.processPostUpdateUpload(Collections.singletonList(postXLS), "");

    // Verify expectations.
    verify(tcsServiceImpl).deleteRotationsForPostId(1L);
    verify(tcsServiceImpl).createRotationsForPost(rotationPostDtosCaptor.capture());

    // Perform assertions.
    MatcherAssert.assertThat("The XLS error messages contained an unexpected value.",
        postXLS.getErrorMessage(), CoreMatchers.not(containsString("rotation")));

    List<RotationPostDTO> rotationPostDtos = rotationPostDtosCaptor.getValue();
    MatcherAssert.assertThat("The number of RotationPostDTOs did not match the expected value.",
        rotationPostDtos.size(), CoreMatchers.is(3));

    RotationPostDTO rotationPostDto = rotationPostDtos.get(0);
    MatcherAssert.assertThat("The RotationPostDTO's ID did not match the expected value.",
        rotationPostDto.getId(), CoreMatchers.nullValue());
    MatcherAssert.assertThat("The RotationPostDTO's post ID did not match the expected value.",
        rotationPostDto.getPostId(), CoreMatchers.is(1L));
    MatcherAssert.assertThat("The RotationPostDTO's rotation ID did not match the expected value.",
        rotationPostDto.getRotationId(), CoreMatchers.is(4L));
    MatcherAssert.assertThat("The RotationPostDTO's programme ID did not match the expected value.",
        rotationPostDto.getProgrammeId(), CoreMatchers.nullValue());

    rotationPostDto = rotationPostDtos.get(1);
    MatcherAssert.assertThat("The RotationPostDTO's ID did not match the expected value.",
        rotationPostDto.getId(), CoreMatchers.nullValue());
    MatcherAssert.assertThat("The RotationPostDTO's post ID did not match the expected value.",
        rotationPostDto.getPostId(), CoreMatchers.is(1L));
    MatcherAssert.assertThat("The RotationPostDTO's rotation ID did not match the expected value.",
        rotationPostDto.getRotationId(), CoreMatchers.is(5L));
    MatcherAssert.assertThat("The RotationPostDTO's programme ID did not match the expected value.",
        rotationPostDto.getProgrammeId(), CoreMatchers.nullValue());

    rotationPostDto = rotationPostDtos.get(2);
    MatcherAssert.assertThat("The RotationPostDTO's ID did not match the expected value.",
        rotationPostDto.getId(), CoreMatchers.nullValue());
    MatcherAssert.assertThat("The RotationPostDTO's post ID did not match the expected value.",
        rotationPostDto.getPostId(), CoreMatchers.is(1L));
    MatcherAssert.assertThat("The RotationPostDTO's rotation ID did not match the expected value.",
        rotationPostDto.getRotationId(), CoreMatchers.is(6L));
    MatcherAssert.assertThat("The RotationPostDTO's programme ID did not match the expected value.",
        rotationPostDto.getProgrammeId(), CoreMatchers.nullValue());
  }

  /**
   * Test that error messages are added when the given rotations could not be found by name.
   */
  @Test
  public void testProcessPostUpdateUpload_rotationNotFound_errorMessage() {
    // Set up test scenario.
    postXLS.setPostTISId("1");
    postXLS.setRotations("rotation1;rotation2;rotation3");

    postDTO.setId(1L);
    ProgrammeDTO programmeDto1 = new ProgrammeDTO();
    programmeDto1.setId(2L);
    ProgrammeDTO programmeDto2 = new ProgrammeDTO();
    programmeDto2.setId(3L);
    postDTO.setProgrammes(Sets.newHashSet(programmeDto1, programmeDto2));

    RotationDTO rotation1 = new RotationDTO();
    rotation1.setId(4L);
    rotation1.setName("rotation1");

    // Record expectations.
    when(tcsServiceImpl.getPostById(1L)).thenReturn(postDTO);
    when(tcsServiceImpl.getRotationByProgrammeIdsIn(Arrays.asList(2L, 3L)))
        .thenReturn(Collections.singletonList(rotation1));

    // Call the code under test.
    postUpdateTransformerService.processPostUpdateUpload(Collections.singletonList(postXLS), "");

    // Verify expectations.
    verify(tcsServiceImpl, never()).deleteRotationsForPostId(any());
    verify(tcsServiceImpl, never()).createRotationsForPost(any());

    // Perform assertions.
    MatcherAssert.assertThat("The XLS error messages did not contain the expected value.",
        postXLS.getErrorMessage(),
        containsString("Did not find rotation for name \"rotation2\"."));
    MatcherAssert.assertThat("The XLS error messages did not contain the expected value.",
        postXLS.getErrorMessage(),
        containsString("Did not find rotation for name \"rotation3\"."));
  }

  /**
   * Test that error messages are added when the given rotations are found multiple times by name.
   */
  @Test
  public void testProcessPostUpdateUpload_rotationFoundMultiple_errorMessage() {
    // Set up test scenario.
    postXLS.setPostTISId("1");
    postXLS.setRotations("rotation");

    postDTO.setId(1L);
    ProgrammeDTO programmeDto1 = new ProgrammeDTO();
    programmeDto1.setId(2L);
    ProgrammeDTO programmeDto2 = new ProgrammeDTO();
    programmeDto2.setId(3L);
    postDTO.setProgrammes(Sets.newHashSet(programmeDto1, programmeDto2));

    RotationDTO rotation1 = new RotationDTO();
    rotation1.setId(4L);
    rotation1.setName("rotation");

    RotationDTO rotation2 = new RotationDTO();
    rotation2.setId(5L);
    rotation2.setName("rotation");

    // Record expectations.
    when(tcsServiceImpl.getPostById(1L)).thenReturn(postDTO);
    when(tcsServiceImpl.getRotationByProgrammeIdsIn(Arrays.asList(2L, 3L)))
        .thenReturn(Arrays.asList(rotation1, rotation2));

    // Call the code under test.
    postUpdateTransformerService.processPostUpdateUpload(Collections.singletonList(postXLS), "");

    // Verify expectations.
    verify(tcsServiceImpl, never()).deleteRotationsForPostId(any());
    verify(tcsServiceImpl, never()).createRotationsForPost(any());

    // Perform assertions.
    MatcherAssert.assertThat("The XLS error messages did not contain the expected value.",
        postXLS.getErrorMessage(),
        containsString("Found multiple rotations for name \"rotation\"."));
    MatcherAssert.assertThat("The XLS error messages contained an unexpected value.",
        postXLS.getErrorMessage(), CoreMatchers
            .not(containsString("Did not find rotation for name \"rotation\".")));
  }

  @Test
  public void shouldUpdateMultipleOtherGrades() throws Exception {
    String mockedGrade1 = "mockedGrade1";
    String mockedGrade2 = "mockedGrade2";
    postXLS.setOtherGrades(mockedGrade1 + MULTI_VALUE_SEPARATOR + mockedGrade2);

    GradeDTO gradeDto1 = createGradeDTO(2L, mockedGrade1);
    gradeDto1.setStatus(com.transformuk.hee.tis.reference.api.enums.Status.CURRENT);
    gradeDto1.setPostGrade(true);
    gradeDto1.setTrainingGrade(true);

    GradeDTO gradeDto2 = createGradeDTO(3L, mockedGrade2);
    gradeDto2.setStatus(com.transformuk.hee.tis.reference.api.enums.Status.CURRENT);
    gradeDto2.setPostGrade(true);
    gradeDto2.setTrainingGrade(true);

    createSingleListWithGrade(gradeByName, gradeDto1);
    createSingleListWithGrade(gradeByName, gradeDto2);

    // set existing other grades
    PostGradeDTO postGradeDto = new PostGradeDTO(1l, 4L, PostGradeType.OTHER);
    postDTO.setGrades(Sets.newHashSet(postGradeDto));

    postUpdateTransformerService.updateGrades(postXLS, postDTO,
        PostUpdateTransformerServiceTest::getGradeDTOsForName);

    assertThat(postXLS.getErrorMessage()).isNull();
    Set<PostGradeDTO> newPostGradeDTOSet = postDTO.getGrades().stream()
        .filter(g -> g.getPostGradeType() == PostGradeType.OTHER).collect(Collectors.toSet());
    assertThat(newPostGradeDTOSet.size()).isEqualTo(2);
    assertThat(newPostGradeDTOSet.stream().map(PostGradeDTO::getGradeId)
        .collect(Collectors.toSet())).contains(2L, 3L);
  }

  @Test
  public void shouldUpdateMultipleOtherSpecialties() throws Exception {
    String mockedSpecialty1 = "mockedSpecialty1";
    String mockedSpecialty2 = "mockedSpecialty2";
    String mockedSpecialty3 = "mockedSpecialty3";
    postXLS.setSpecialty(null);
    postXLS.setOtherSpecialties(mockedSpecialty2 + MULTI_VALUE_SEPARATOR + mockedSpecialty3);

    SpecialtyDTO specialtyDto2 = createSpecialtyDTO(2L, "intrepid_2",
        mockedSpecialty2, "college", "code2", Status.CURRENT);
    SpecialtyDTO specialtyDto3 = createSpecialtyDTO(3L, "intrepid_3",
        mockedSpecialty3, "college", "code3", Status.CURRENT);
    createSingleListWithSpecialty(specialtyByName, specialtyDto2);
    createSingleListWithSpecialty(specialtyByName, specialtyDto3);

    // set existing other specialties
    SpecialtyDTO specialtyDto1 = createSpecialtyDTO(1L, "intrepid_1",
        mockedSpecialty1, "college", "code1", Status.CURRENT);
    PostSpecialtyDTO postSpecialtyDto = new PostSpecialtyDTO(1l, specialtyDto1,
        PostSpecialtyType.OTHER);
    postDTO.setSpecialties(Sets.newHashSet(postSpecialtyDto));

    postUpdateTransformerService.setSpecialties(postXLS, postDTO,
        PostUpdateTransformerServiceTest::getSpecialtiesForString);

    assertThat(postXLS.getErrorMessage()).isNull();
    Set<PostSpecialtyDTO> newPostSpecialtyDtoSet = postDTO.getSpecialties().stream()
        .filter(s -> s.getPostSpecialtyType() == PostSpecialtyType.OTHER)
        .collect(Collectors.toSet());
    assertThat(newPostSpecialtyDtoSet.size()).isEqualTo(2);
    assertThat(newPostSpecialtyDtoSet.stream().map(PostSpecialtyDTO::getSpecialty)
        .collect(Collectors.toSet())).contains(specialtyDto2, specialtyDto3);
  }

  @Test
  public void shouldUpdateMultipleSubSpecialties() throws Exception {
    String mockedSpecialty1 = "mockedSpecialty1";
    String mockedSpecialty2 = "mockedSpecialty2";
    String mockedSpecialty3 = "mockedSpecialty3";
    postXLS.setSpecialty(null);
    postXLS.setSubSpecialties(mockedSpecialty2 + MULTI_VALUE_SEPARATOR + mockedSpecialty3);

    SpecialtyDTO specialtyDto2 = createSpecialtyDTO(2L, "intrepid_2",
        mockedSpecialty2, "college", "code2", Status.CURRENT);
    SpecialtyDTO specialtyDto3 = createSpecialtyDTO(3L, "intrepid_3",
        mockedSpecialty3, "college", "code3", Status.CURRENT);
    createSingleListWithSpecialty(specialtyByName, specialtyDto2);
    createSingleListWithSpecialty(specialtyByName, specialtyDto3);

    // set existing sub specialties
    SpecialtyDTO specialtyDto1 = createSpecialtyDTO(1L, "intrepid_1",
        mockedSpecialty1, "college", "code1", Status.CURRENT);
    PostSpecialtyDTO postSpecialtyDto = new PostSpecialtyDTO(1l, specialtyDto1,
        PostSpecialtyType.SUB_SPECIALTY);
    postDTO.setSpecialties(Sets.newHashSet(postSpecialtyDto));

    postUpdateTransformerService.setSpecialties(postXLS, postDTO,
        PostUpdateTransformerServiceTest::getSpecialtiesForString);

    assertThat(postXLS.getErrorMessage()).isNull();
    Set<PostSpecialtyDTO> newPostSpecialtyDtoSet = postDTO.getSpecialties().stream()
        .filter(s -> s.getPostSpecialtyType() == PostSpecialtyType.SUB_SPECIALTY)
        .collect(Collectors.toSet());
    assertThat(newPostSpecialtyDtoSet.size()).isEqualTo(2);
    assertThat(newPostSpecialtyDtoSet.stream().map(PostSpecialtyDTO::getSpecialty)
        .collect(Collectors.toSet())).contains(specialtyDto2, specialtyDto3);
  }

  @Test
  public void shouldUpdateMultipleOtherSites() throws Exception {
    String mockedSite1 = "mockedSite1";
    String mockedSite2 = "mockedSite2";
    postXLS.setMainSite(null);
    postXLS.setOtherSites(mockedSite1 + MULTI_VALUE_SEPARATOR + mockedSite2);

    SiteDTO siteDto1 = createSiteDTO(1L, mockedSite1);
    SiteDTO siteDto2 = createSiteDTO(2L, mockedSite2);
    createSingleListWithSite(siteByName, siteDto1);
    createSingleListWithSite(siteByName, siteDto2);

    // set existing other sites
    PostSiteDTO postSiteDto = new PostSiteDTO(1L, 3L, PostSiteType.OTHER);
    postDTO.setSites(Sets.newHashSet(postSiteDto));

    postUpdateTransformerService.updateSites(postXLS, postDTO,
        PostUpdateTransformerServiceTest::getSiteDTOsForName);

    assertThat(postXLS.getErrorMessage()).isNull();
    Set<PostSiteDTO> newPostSiteDtoSet = postDTO.getSites().stream()
        .filter(s -> s.getPostSiteType() == PostSiteType.OTHER)
        .collect(Collectors.toSet());
    assertThat(newPostSiteDtoSet.size()).isEqualTo(2);
    assertThat(newPostSiteDtoSet.stream().map(PostSiteDTO::getSiteId)
        .collect(Collectors.toSet())).contains(1L, 2L);
  }

  @Test
  public void shouldUpdateMultipleProgrammes() {
    PostUpdateXLS postUpdateXls = new PostUpdateXLS();
    postUpdateXls.setPostTISId("1");
    postUpdateXls.setProgrammeTisId("2;3");

    ProgrammeDTO programmeDto2 = new ProgrammeDTO();
    programmeDto2.setId(2L);
    programmeDto2.setProgrammeName("programme2");
    programmeDto2.setStatus(Status.CURRENT);

    ProgrammeDTO programmeDto3 = new ProgrammeDTO();
    programmeDto3.setId(3L);
    programmeDto3.setProgrammeName("programme3");
    programmeDto3.setStatus(Status.CURRENT);

    // set existing programmes
    postDTO.setId(1L);
    ProgrammeDTO programmeDto1 = new ProgrammeDTO();
    programmeDto1.setId(1L);
    postDTO.setProgrammes(Sets.newHashSet(programmeDto1));

    when(tcsServiceImpl.getPostById(1L)).thenReturn(postDTO);
    when(tcsServiceImpl.findProgrammesIn(Arrays.asList("2", "3")))
        .thenReturn(Arrays.asList(programmeDto2, programmeDto3));

    postUpdateTransformerService.processPostUpdateUpload(
        Collections.singletonList(postUpdateXls), "");
    verify(tcsServiceImpl).updatePost(postDtoCaptor.capture());

    assertThat(postXLS.getErrorMessage()).isNull();
    PostDTO updatedPostDto = postDtoCaptor.getValue();
    Set<ProgrammeDTO> updatedProgrammes = updatedPostDto.getProgrammes();
    assertThat(updatedProgrammes.size()).isEqualTo(2);
    assertThat(updatedProgrammes).contains(programmeDto2, programmeDto3);
  }

  @Test
  public void shouldPassValidationWhenCurrentGradesAreOfPostGradeAndTrainingGradeValueTrue() {
    // Approved grade
    GradeDTO approvedGrade = new GradeDTO();
    approvedGrade.setName("Approved grade");
    approvedGrade.setStatus(com.transformuk.hee.tis.reference.api.enums.Status.CURRENT);
    approvedGrade.setPostGrade(true);
    approvedGrade.setTrainingGrade(true);

    //Other grades
    GradeDTO otherGrade1 = new GradeDTO();
    otherGrade1.setName("Other grade 1");
    otherGrade1.setStatus(com.transformuk.hee.tis.reference.api.enums.Status.CURRENT);
    otherGrade1.setPostGrade(true);
    otherGrade1.setTrainingGrade(true);

    GradeDTO otherGrade3 = new GradeDTO();
    otherGrade3.setName("Other grade 3");
    otherGrade3.setStatus(com.transformuk.hee.tis.reference.api.enums.Status.CURRENT);
    otherGrade3.setPostGrade(true);
    otherGrade3.setTrainingGrade(true);

    //Mock specialty and trusts
    SpecialtyDTO specialtyDTO = createSpecialtyDTO(12345L, "12345", "12345", "A MEDIA COLLEGE",
        "NHS_CODE", Status.CURRENT);
    TrustDTO trustDTO = createTrustDTO(TRAINING_BODY_ID, TRAINING_BODY);

    postXLS.setApprovedGrade(approvedGrade.getName());
    postXLS.setOtherGrades(otherGrade1.getName() + ";" + otherGrade3.getName());

    postDTO.setId(1L);

    when(tcsServiceImpl.getPostById(1L)).thenReturn(postDTO);
    when(referenceServiceImpl.findGradesByName("Approved grade"))
        .thenReturn(Arrays.asList(approvedGrade));
    when(referenceServiceImpl.findGradesByName("Other grade 1"))
        .thenReturn(Arrays.asList(otherGrade1));
    when(referenceServiceImpl.findGradesByName("Other grade 3"))
        .thenReturn(Arrays.asList(otherGrade3));
    when(tcsServiceImpl.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialtyDTO));
    when(referenceServiceImpl.findTrustByTrustKnownAs(any())).thenReturn(Arrays.asList(trustDTO));

    //Code under test
    postUpdateTransformerService.processPostUpdateUpload(Collections.singletonList(postXLS), "");

    //Assertions
    String message = postXLS.getErrorMessage();

    MatcherAssert.assertThat(message, nullValue());
    MatcherAssert.assertThat(postXLS.isSuccessfullyImported(), is(true));
  }

  @Test
  public void shouldFailGradeValidationWhenNoGradeMatches() {
    //Inactive Approved grade
    GradeDTO approvedGrade = new GradeDTO();
    approvedGrade.setName("Approved grade");
    approvedGrade.setStatus(com.transformuk.hee.tis.reference.api.enums.Status.INACTIVE);
    approvedGrade.setPostGrade(true);
    approvedGrade.setTrainingGrade(true);

    //Inactive Other grades
    GradeDTO otherGrade1 = new GradeDTO();
    otherGrade1.setName("Other grade 1");
    otherGrade1.setStatus(com.transformuk.hee.tis.reference.api.enums.Status.INACTIVE);
    otherGrade1.setPostGrade(true);
    otherGrade1.setTrainingGrade(true);

    //Mock specialty and trusts
    SpecialtyDTO specialtyDTO = createSpecialtyDTO(12345L, "12345", "12345", "A MEDIA COLLEGE",
        "NHS_CODE", Status.CURRENT);
    TrustDTO trustDTO = createTrustDTO(TRAINING_BODY_ID, TRAINING_BODY);

    postXLS.setApprovedGrade(approvedGrade.getName());
    postXLS.setOtherGrades(
        otherGrade1.getName() + ";Other grade 2;");//Multiple other grades are allowed

    postDTO.setId(1L);

    when(tcsServiceImpl.getPostById(1L)).thenReturn(postDTO);
    when(referenceServiceImpl.findGradesByName("Approved grade"))
        .thenReturn(Collections.emptyList());
    when(referenceServiceImpl.findGradesByName("Other grade 1"))
        .thenReturn(Collections.emptyList());
    when(tcsServiceImpl.getSpecialtyByName(any())).thenReturn(Arrays.asList(specialtyDTO));
    when(referenceServiceImpl.findTrustByTrustKnownAs(any())).thenReturn(Arrays.asList(trustDTO));

    // Code under test
    postUpdateTransformerService.processPostUpdateUpload(Collections.singletonList(postXLS), "");

    // assertions
    String message = postXLS.getErrorMessage();
    MatcherAssert.assertThat(
        "No current, post and training grade found for",
        message, containsString("No current, post and training grade found for 'Approved grade'."));

    MatcherAssert.assertThat("No current, post and training grade found for", message,
        containsString("No current, post and training grade found for 'Other grade 1'"));

    MatcherAssert.assertThat(
        "No current, post and training grade found for",
        message, containsString("No current, post and training grade found for 'Other grade 2'"));
  }
}
