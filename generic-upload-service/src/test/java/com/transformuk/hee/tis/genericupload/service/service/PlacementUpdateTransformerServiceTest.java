package com.transformuk.hee.tis.genericupload.service.service;

import static com.transformuk.hee.tis.genericupload.service.util.MultiValueUtil.MULTI_VALUE_SEPARATOR;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PlacementSiteType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSiteType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class PlacementUpdateTransformerServiceTest {

  public static final String ANOTHER = "12345another";
  private static final String DID_NOT_FIND_OTHER_SITE_FOR_NAME = "Did not find other site for name";
  private static final String DID_NOT_FIND_OTHER_SITE_IN_PARENT_POST_FOR_NAME = "Did not find other site in parent post for name";
  private static final String FOUND_MULTIPLE_OTHER_SITES_FOR_NAME = "Found multiple other sites for name";
  private static final String EXPECTED_A_PLACEMENT_GRADE_FOR = "Expected to find a placement grade for";
  static Map<String, List<SpecialtyDTO>> specialtyByName, specialtyByNameWithDuplicate;
  static Map<String, List<SiteDTO>> siteByName;
  PlacementUpdateTransformerService placementUpdateTransformerService;
  PlacementDetailsDTO placementDTO;
  private PlacementUpdateXLS placementXLS;
  private PostDTO postDTO;

  private static SpecialtyDTO createSpeciltyDTO(Long id, String intrepidId, String name,
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

  private static SiteDTO createSiteDTO(Long id, String siteKnownAs,
      com.transformuk.hee.tis.reference.api.enums.Status status) {
    SiteDTO siteDTO = new SiteDTO();
    siteDTO.setId(id);
    siteDTO.setSiteKnownAs(siteKnownAs);
    siteDTO.setStatus(status);
    return siteDTO;
  }

  public static List<SpecialtyDTO> getSpecialtiesForString(String specialtyName) {
    return specialtyByName.get(specialtyName);
  }

  public static List<SpecialtyDTO> getSpecialtiesWithDuplicatesForSpecialtyName(
      String specialtyName) {
    return specialtyByNameWithDuplicate.get(specialtyName);
  }

  public static List<SiteDTO> getSitesForString(String siteName) {
    List<SiteDTO> ret = siteByName.get(siteName);
    if (ret == null) {
      return new ArrayList<SiteDTO>();
    } else {
      return ret;
    }
  }

  @Before
  public void initialise() throws Exception {
    placementUpdateTransformerService = new PlacementUpdateTransformerService();
    initialiseData();
  }

  public void initialiseData() throws Exception {
    SpecialtyDTO specialtyDTO = createSpeciltyDTO(12345L, "12345", "12345", "A MEDIA COLLEGE",
        "NHS_CODE", Status.CURRENT);
    SpecialtyDTO specialtyDTOWithSameName = createSpeciltyDTO(123456L, "123456", "12345",
        "A MEDIA COLLEGE", "NHS_CODE", Status.CURRENT);
    SpecialtyDTO anotherSpecialtyDTO = createSpeciltyDTO(123453L, "123453", ANOTHER,
        "A MEDIA COLLEGE 3", "NHS_CODE 3", Status.CURRENT);
    SpecialtyDTO specialtyDTO1 = createSpeciltyDTO(11111L, "11111", "11111", "A MEIDA COLLEGE",
        "NHS_CODE", Status.CURRENT);

    specialtyByName = new HashMap<>();
    createSingleListWithSpecialty(specialtyByName, specialtyDTO);
    createSingleListWithSpecialty(specialtyByName, anotherSpecialtyDTO);
    createSingleListWithSpecialty(specialtyByName, specialtyDTO1);

    specialtyByNameWithDuplicate = new HashMap<>();
    createSingleListWithSpecialty(specialtyByNameWithDuplicate, specialtyDTO);
    specialtyByNameWithDuplicate.get(specialtyDTO.getName()).add(specialtyDTOWithSameName);

    placementXLS = createPlacementXLS("1", "100", "WMD/5AT01/085/ST1/001", specialtyDTO.getName());

    placementDTO = new PlacementDetailsDTO();

    SiteDTO siteDTO = createSiteDTO(1L, "mockedSite",
        com.transformuk.hee.tis.reference.api.enums.Status.CURRENT);
    siteByName = new HashMap<>();
    addSitesList(siteByName, siteDTO);

    // initialise postDTO
    Set<PostSiteDTO> sites = new HashSet<>();
    sites.add(new PostSiteDTO(1L, 1L, PostSiteType.OTHER));
    postDTO = new PostDTO();
    postDTO.setSites(sites);
  }

  public PlacementUpdateXLS createPlacementXLS(String placementId, String intrepidId, String npn,
      String specialtyName) {
    PlacementUpdateXLS placementXLS = new PlacementUpdateXLS();
    placementXLS.setPlacementId(placementId);
    placementXLS.setIntrepidId(intrepidId);
    placementXLS.setNationalPostNumber(npn);
    placementXLS.setSpecialty1(specialtyName);
    return placementXLS;
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

  public void addSitesList(Map<String, List<SiteDTO>> siteByName, SiteDTO siteDTO) {
    if (siteByName.get(siteDTO.getSiteKnownAs()) == null) {
      siteByName.put(siteDTO.getSiteKnownAs(), new ArrayList<>());
    }
    siteByName.get(siteDTO.getSiteKnownAs()).add(siteDTO);
  }

  @Test
  public void canHandleAnUnknownSpecialty() {
    placementXLS.setSpecialty1("Unknown");
    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(0);
  }

  @Test
  public void canBuildSpecialtiesForPlacement() {
    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(1);
    PlacementSpecialtyDTO placementSpecialtyDTO = placementDTO.getSpecialties().iterator().next();
    assertThat(placementSpecialtyDTO.getPlacementSpecialtyType()).isNotNull();
    assertThat(placementSpecialtyDTO.getPlacementSpecialtyType())
        .isEqualTo(PostSpecialtyType.PRIMARY);
  }

  @Test
  public void canUpdateIntrepidIdForPlacementWhenDbIntrepidIdIsNull() {
    placementUpdateTransformerService.updateIntrepidId(placementXLS, placementDTO);
    assertThat(placementDTO.getIntrepidId()).isEqualToIgnoringCase(placementXLS.getIntrepidId());
  }

  @Test
  public void canUpdateIntrepidIdForPlacementWhenDbIntrepidIdIsNotNull() {
    String expectedIntrepidId = "222";
    placementDTO.setIntrepidId(expectedIntrepidId);
    placementUpdateTransformerService.updateIntrepidId(placementXLS, placementDTO);
    assertThat(placementDTO.getIntrepidId()).isEqualToIgnoringCase(expectedIntrepidId);
  }

  @Test
  public void doesNotBuildSpecialtiesIfDuplicatesSpecialtiesExist() {
    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesWithDuplicatesForSpecialtyName);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(0);
  }

  @Test
  public void canBuildAllSpecialtiesWhenPrimarySpecialtyPopulatedForPlacement() {
    placementXLS.setSpecialty2(ANOTHER);
    placementXLS.setSubSpecialty("11111");
    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(3);
    long countOfOtherSpecialties = placementDTO.getSpecialties().stream()
        .filter(s -> s.getPlacementSpecialtyType().equals(PostSpecialtyType.OTHER)).count();
    long countOfSubSpecialties = placementDTO.getSpecialties().stream()
        .filter(s -> s.getPlacementSpecialtyType().equals(PostSpecialtyType.SUB_SPECIALTY)).count();
    assertThat(countOfOtherSpecialties).isEqualTo(1);
    assertThat(countOfSubSpecialties).isEqualTo(1);
  }

  @Test
  public void handlesDuplicationOnOther() {
    placementXLS.setSpecialty2(ANOTHER);
    placementXLS.setSpecialty3(ANOTHER);

    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(2);
  }

  @Test
  public void shouldSkipOtherSpecialtyWhenDuplicationOnPrimary() {
    placementXLS.setSpecialty2(ANOTHER);
    placementXLS.setSpecialty3("12345");

    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(2);
    long countOfOtherSpecialties = placementDTO.getSpecialties().stream()
        .filter(s -> s.getPlacementSpecialtyType().equals(PostSpecialtyType.OTHER)).count();
    assertThat(countOfOtherSpecialties).isEqualTo(1);
  }

  @Test
  public void shouldSkipSubSpecialtyWhenDuplicationOnPrimary() {
    placementXLS.setSubSpecialty("12345");

    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesForString);

    assertThat(placementDTO.getSpecialties().size()).isEqualTo(1);
    PlacementSpecialtyDTO placementSpecialtyDto = placementDTO.getSpecialties().iterator().next();
    assertThat(placementSpecialtyDto.getPlacementSpecialtyType()).isEqualTo(
        PostSpecialtyType.PRIMARY);
    assertThat(placementSpecialtyDto.getSpecialtyName()).isEqualTo("12345");
  }

  @Test
  public void shouldSkipSubSpecialtyWhenDuplicationOnOtherSpecialties() {
    placementXLS.setSpecialty2(ANOTHER);
    placementXLS.setSpecialty3("11111");
    placementXLS.setSubSpecialty(ANOTHER);

    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesForString);

    assertThat(placementDTO.getSpecialties().size()).isEqualTo(3);
    long countOfOtherSpecialties = placementDTO.getSpecialties().stream()
        .filter(s -> s.getPlacementSpecialtyType().equals(PostSpecialtyType.OTHER)).count();
    long countOfSubSpecialty = placementDTO.getSpecialties().stream()
        .filter(s -> s.getPlacementSpecialtyType().equals(PostSpecialtyType.SUB_SPECIALTY)).count();
    assertThat(countOfOtherSpecialties).isEqualTo(2);
    assertThat(countOfSubSpecialty).isEqualTo(0);
  }

  @Test
  public void shouldOverwriteAllSpecialtiesIfPrimaryExistsOnUpload() {
    PlacementSpecialtyDTO primaryPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    primaryPlacementSpecialtyDto.setSpecialtyId(10L);
    primaryPlacementSpecialtyDto.setPlacementId(10L);
    primaryPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.PRIMARY);

    PlacementSpecialtyDTO otherPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    otherPlacementSpecialtyDto.setSpecialtyId(20L);
    otherPlacementSpecialtyDto.setPlacementId(10L);
    otherPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.OTHER);

    PlacementSpecialtyDTO subPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    subPlacementSpecialtyDto.setSpecialtyId(30L);
    subPlacementSpecialtyDto.setPlacementId(10L);
    subPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.SUB_SPECIALTY);

    placementDTO.setSpecialties(
        Sets.newHashSet(primaryPlacementSpecialtyDto, otherPlacementSpecialtyDto,
            subPlacementSpecialtyDto));

    placementXLS.setSpecialty2(ANOTHER);
    placementXLS.setSubSpecialty("11111");

    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(3);
    assertThat(
        placementDTO.getSpecialties().stream().map(ps -> ps.getPlacementSpecialtyType())).contains(
        PostSpecialtyType.PRIMARY, PostSpecialtyType.OTHER, PostSpecialtyType.SUB_SPECIALTY);
    assertThat(placementDTO.getSpecialties().stream().map(ps -> ps.getSpecialtyId())).contains(
        12345L, 123453L, 11111L);
  }

  @Test
  public void shouldNotOverwriteAnySpecialtiesIfNoSpecialtyExistsOnUpload() {
    PlacementSpecialtyDTO primaryPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    primaryPlacementSpecialtyDto.setSpecialtyId(10L);
    primaryPlacementSpecialtyDto.setPlacementId(10L);
    primaryPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.PRIMARY);

    PlacementSpecialtyDTO otherPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    otherPlacementSpecialtyDto.setSpecialtyId(20L);
    otherPlacementSpecialtyDto.setPlacementId(10L);
    otherPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.OTHER);

    PlacementSpecialtyDTO subPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    subPlacementSpecialtyDto.setSpecialtyId(30L);
    subPlacementSpecialtyDto.setPlacementId(10L);
    subPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.SUB_SPECIALTY);

    placementDTO.setSpecialties(
        Sets.newHashSet(primaryPlacementSpecialtyDto, otherPlacementSpecialtyDto,
            subPlacementSpecialtyDto));

    placementXLS.setSpecialty1("");

    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(3);
    assertThat(
        placementDTO.getSpecialties().stream().map(ps -> ps.getPlacementSpecialtyType())).contains(
        PostSpecialtyType.PRIMARY, PostSpecialtyType.OTHER, PostSpecialtyType.SUB_SPECIALTY);
    assertThat(placementDTO.getSpecialties().stream().map(ps -> ps.getSpecialtyId())).contains(10L,
        20L, 30L);
  }

  @Test
  public void shouldAddOtherSpecialtiesIfPrimaryNotFromXls() {
    PlacementSpecialtyDTO primaryPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    primaryPlacementSpecialtyDto.setSpecialtyId(10L);
    primaryPlacementSpecialtyDto.setPlacementId(10L);
    primaryPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.PRIMARY);

    PlacementSpecialtyDTO otherPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    otherPlacementSpecialtyDto.setSpecialtyId(20L);
    otherPlacementSpecialtyDto.setPlacementId(10L);
    otherPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.OTHER);

    PlacementSpecialtyDTO subPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    subPlacementSpecialtyDto.setSpecialtyId(30L);
    subPlacementSpecialtyDto.setPlacementId(10L);
    subPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.SUB_SPECIALTY);

    placementDTO.setSpecialties(
        Sets.newHashSet(primaryPlacementSpecialtyDto, otherPlacementSpecialtyDto,
            subPlacementSpecialtyDto));

    placementXLS.setSpecialty1(null);
    placementXLS.setSpecialty2("12345");
    placementXLS.setSpecialty3(ANOTHER);

    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesForString);

    assertThat(placementDTO.getSpecialties().size()).isEqualTo(5);
    List<PlacementSpecialtyDTO> otherPlacementSpecialtyDtos = placementDTO.getSpecialties().stream()
        .filter(ps -> ps.getPlacementSpecialtyType().equals(PostSpecialtyType.OTHER)).collect(
            Collectors.toList());
    assertThat(otherPlacementSpecialtyDtos.size()).isEqualTo(3);
    assertThat(otherPlacementSpecialtyDtos.stream().map(ps -> ps.getSpecialtyId())).contains(20L,
        12345L, 123453L);
  }

  @Test
  public void shouldUpdateSubSpecialtiesIfPrimaryNotFromXls() {
    PlacementSpecialtyDTO primaryPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    primaryPlacementSpecialtyDto.setSpecialtyId(10L);
    primaryPlacementSpecialtyDto.setPlacementId(10L);
    primaryPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.PRIMARY);

    PlacementSpecialtyDTO otherPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    otherPlacementSpecialtyDto.setSpecialtyId(20L);
    otherPlacementSpecialtyDto.setPlacementId(10L);
    otherPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.OTHER);

    PlacementSpecialtyDTO subPlacementSpecialtyDto = new PlacementSpecialtyDTO();
    subPlacementSpecialtyDto.setSpecialtyId(30L);
    subPlacementSpecialtyDto.setPlacementId(10L);
    subPlacementSpecialtyDto.setPlacementSpecialtyType(PostSpecialtyType.SUB_SPECIALTY);

    placementDTO.setSpecialties(
        Sets.newHashSet(primaryPlacementSpecialtyDto, otherPlacementSpecialtyDto,
            subPlacementSpecialtyDto));

    placementXLS.setSpecialty1(null);
    placementXLS.setSpecialty2("12345");
    placementXLS.setSubSpecialty(ANOTHER);

    placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSpecialtiesForString);

    assertThat(placementDTO.getSpecialties().size()).isEqualTo(4);
    assertThat(placementDTO.getSpecialties().stream()
        .filter(ps -> ps.getPlacementSpecialtyType().equals(PostSpecialtyType.OTHER))
        .count()).isEqualTo(2);
    List<PlacementSpecialtyDTO> subSpecialtyDtoList = placementDTO.getSpecialties().stream()
        .filter(ps -> ps.getPlacementSpecialtyType().equals(PostSpecialtyType.SUB_SPECIALTY))
        .collect(Collectors.toList());
    assertThat(subSpecialtyDtoList.size()).isEqualTo(1);
    assertThat(subSpecialtyDtoList.get(0).getSpecialtyName()).isEqualTo(ANOTHER);
  }

  @Test
  public void shouldNotUpdateAnUnknownOtherSites() {
    placementXLS.setOtherSites("Unknown");
    placementUpdateTransformerService.setOtherSites(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSitesForString, postDTO);
    assertThat(placementDTO.getSites().size()).isEqualTo(0);
    assertThat(placementXLS.getErrorMessage()).contains(DID_NOT_FIND_OTHER_SITE_FOR_NAME);
  }

  @Test
  public void ShouldUpdateOtherSitesOnParentPost() {
    placementXLS.setOtherSites("mockedSite");
    placementUpdateTransformerService.setOtherSites(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSitesForString, postDTO);
    assertThat(placementDTO.getSites().size()).isEqualTo(1);
    assertThat(placementXLS.getErrorMessage()).isNull();
  }

  @Test
  public void ShouldNotSetOtherSitesNotOnParentPost() {
    placementXLS.setOtherSites("mockedSite");

    Set<PostSiteDTO> sites = new HashSet<>();
    sites.add(new PostSiteDTO(1L, 2L, PostSiteType.OTHER));
    postDTO = new PostDTO();
    postDTO.setSites(sites);

    placementUpdateTransformerService.setOtherSites(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSitesForString, postDTO);
    assertThat(placementDTO.getSites().size()).isEqualTo(0);
    assertThat(placementXLS.getErrorMessage())
        .contains(DID_NOT_FIND_OTHER_SITE_IN_PARENT_POST_FOR_NAME);
  }

  @Test
  public void ShouldNotSetOtherSitesWhenMultipleFound() {
    placementXLS.setOtherSites("mockedSite");

    SiteDTO siteDTO = createSiteDTO(2L, "mockedSite",
        com.transformuk.hee.tis.reference.api.enums.Status.CURRENT);
    addSitesList(siteByName, siteDTO);
    placementUpdateTransformerService.setOtherSites(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSitesForString, postDTO);
    assertThat(placementDTO.getSites().size()).isEqualTo(0);
    assertThat(placementXLS.getErrorMessage()).contains(FOUND_MULTIPLE_OTHER_SITES_FOR_NAME);
  }

  @Test
  public void shouldUpdateMultipleOtherSites() {
    String mockedSite1 = "mockedSite1";
    String mockedSite2 = "mockedSite2";
    placementXLS.setOtherSites(mockedSite1 + MULTI_VALUE_SEPARATOR + mockedSite2);

    SiteDTO siteDto1 = createSiteDTO(1L, mockedSite1,
        com.transformuk.hee.tis.reference.api.enums.Status.CURRENT);
    SiteDTO siteDto2 = createSiteDTO(2L, mockedSite2,
        com.transformuk.hee.tis.reference.api.enums.Status.CURRENT);
    addSitesList(siteByName, siteDto1);
    addSitesList(siteByName, siteDto2);
    postDTO.getSites().add(new PostSiteDTO(1L, 2L, PostSiteType.OTHER));

    // set existing other sites
    PlacementSiteDTO placementSiteDto = new PlacementSiteDTO();
    placementSiteDto.setPlacementId(1L);
    placementSiteDto.setId(1L);
    placementSiteDto.setSiteId(3L);
    placementSiteDto.setPlacementSiteType(PlacementSiteType.OTHER);
    placementDTO.setSites(Sets.newHashSet(placementSiteDto));

    placementUpdateTransformerService.setOtherSites(placementXLS, placementDTO,
        PlacementUpdateTransformerServiceTest::getSitesForString, postDTO);

    assertThat(placementXLS.getErrorMessage()).isNull();
    assertThat(placementDTO.getSites().size()).isEqualTo(2);
    assertThat(placementDTO.getSites().stream().map(PlacementSiteDTO::getSiteId)
        .collect(Collectors.toSet())).contains(1L, 2L);
  }

  @Test
  public void shouldAddErrorWithInvalidPlacementGrade() {
    //WHEN
    List<String> gradesValidForPlacements = Arrays.asList("Valid grade 1", "Valid grade 2");
    String gradeName = "Specialty Registrar - HENE";
    placementXLS.setGrade(gradeName); //not a placement grade
    List<PlacementUpdateXLS> placementUpdateXLSS = Collections.singletonList(placementXLS);
    placementUpdateTransformerService.isPlacementGradeValid(placementUpdateXLSS, gradeName,
        gradesValidForPlacements);
    //THEN
    assertThat(placementXLS.getErrorMessage()).contains(EXPECTED_A_PLACEMENT_GRADE_FOR);
  }

  @Test
  public void shouldNotAddErrorWithValidPlacementGrade() {
    //WHEN
    List<String> gradesValidForPlacements = Arrays.asList("Valid grade 1", "Valid grade 2");
    String gradeName = "Valid grade 1";
    placementXLS.setGrade(gradeName); //placement grade
    List<PlacementUpdateXLS> placementUpdateXLSS = Collections.singletonList(placementXLS);
    placementUpdateTransformerService.isPlacementGradeValid(placementUpdateXLSS, gradeName,
        gradesValidForPlacements);
    //THEN
    assertThat(placementXLS.getErrorMessage()).isNull();
  }
}
