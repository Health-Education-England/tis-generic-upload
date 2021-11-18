package com.transformuk.hee.tis.genericupload.service.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSiteType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

public class PlacementTransformerServiceTest {

  public static final String ANOTHER = "12345another";
  private static final String DID_NOT_FIND_OTHER_SITE_FOR_NAME = "Did not find other site for name";
  private static final String DID_NOT_FIND_OTHER_SITE_IN_PARENT_POST_FOR_NAME = "Did not find other site in parent post for name";
  private static final String FOUND_MULTIPLE_OTHER_SITES_FOR_NAME = "Found multiple other sites for name";
  private static final String EXPECTED_A_PLACEMENT_GRADE_FOR = "Expected to find a placement grade for";
  static Map<String, List<SpecialtyDTO>> specialtyByName, specialtyByNameWithDuplicate;
  static Map<String, List<SiteDTO>> siteByName;
  PlacementTransformerService placementTransformerService;
  PlacementDetailsDTO placementDTO;
  private PlacementXLS placementXLS;
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
    placementTransformerService = new PlacementTransformerService();
    initialiseData();
  }

  public void initialiseData() throws Exception {
    SpecialtyDTO specialtyDTO = createSpeciltyDTO(12345L, "12345", "12345", "A MEDIA COLLEGE",
        "NHS_CODE", Status.CURRENT);
    SpecialtyDTO specialtyDTOWithSameName = createSpeciltyDTO(123456L, "123456", "12345",
        "A MEDIA COLLEGE", "NHS_CODE", Status.CURRENT);
    SpecialtyDTO anotherSpecialtyDTO = createSpeciltyDTO(123453L, "123453", ANOTHER,
        "A MEDIA COLLEGE 3", "NHS_CODE 3", Status.CURRENT);

    specialtyByName = new HashMap<>();
    createSingleListWithSpecialty(specialtyByName, specialtyDTO);
    createSingleListWithSpecialty(specialtyByName, anotherSpecialtyDTO);

    specialtyByNameWithDuplicate = new HashMap<>();
    createSingleListWithSpecialty(specialtyByNameWithDuplicate, specialtyDTO);
    specialtyByNameWithDuplicate.get(specialtyDTO.getName()).add(specialtyDTOWithSameName);

    placementXLS = createPlacementXLS("forename", "surname", "7000010", "WMD/5AT01/085/ST1/001",
        specialtyDTO.getName());

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

  public PlacementXLS createPlacementXLS(String forename, String surname, String gmcNumber,
      String npn, String specialtyName) {
    PlacementXLS placementXLS = new PlacementXLS();
    placementXLS.setSurname(surname);
    placementXLS.setGmcNumber(gmcNumber);
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
    placementTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(0);
  }

  @Test
  public void canBuildSpecialtiesForPlacement() {
    placementTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(1);
    PlacementSpecialtyDTO placementSpecialtyDTO = placementDTO.getSpecialties().iterator().next();
    assertThat(placementSpecialtyDTO.getPlacementSpecialtyType()).isNotNull();
    assertThat(placementSpecialtyDTO.getPlacementSpecialtyType())
        .isEqualTo(PostSpecialtyType.PRIMARY);
  }

  @Test
  public void doesNotBuildSpecialtiesIfDuplicatesSpecialtiesExist() {
    placementTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSpecialtiesWithDuplicatesForSpecialtyName);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(0);
  }

  @Test
  public void canBuildMultipleSpecialtiesForPlacement() {
    placementXLS.setSpecialty2(ANOTHER);
    placementTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(2);
  }

  @Test
  public void handlesDuplicationOnOther() {
    placementXLS.setSpecialty2(ANOTHER);
    placementXLS.setSpecialty3(ANOTHER);

    placementTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(2);
  }

  @Test
  public void handlesDuplicationOnPrimary() {
    placementXLS.setSpecialty2(ANOTHER);
    placementXLS.setSpecialty3("12345");

    placementTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(2);
  }

  @Test
  public void overwritesSpecialtiesIfOneSpecialtyExistsOnUpload() {
    PlacementSpecialtyDTO placementSpecialtyDTO = new PlacementSpecialtyDTO();
    placementSpecialtyDTO.setSpecialtyId(10L);
    placementSpecialtyDTO.setPlacementId(10L);
    placementSpecialtyDTO.setPlacementSpecialtyType(PostSpecialtyType.PRIMARY);
    Set<PlacementSpecialtyDTO> placementSpecialtyDTOS = new HashSet<>();
    placementSpecialtyDTOS.add(placementSpecialtyDTO);
    placementDTO.setSpecialties(placementSpecialtyDTOS);

    placementTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(1);
    for (PlacementSpecialtyDTO placementSpecialtyDTOFromPlacement : placementDTO.getSpecialties()) {
      if (placementSpecialtyDTOFromPlacement.getSpecialtyId() == 12345L) {
        assertThat(placementSpecialtyDTOFromPlacement.getPlacementSpecialtyType())
            .isEqualTo(PostSpecialtyType.PRIMARY);
      }
    }
  }

  @Test
  public void doesNotOverwritesSpecialtiesIfNoSpecialtyExistsOnUpload() {
    PlacementSpecialtyDTO placementSpecialtyDTO = new PlacementSpecialtyDTO();
    placementSpecialtyDTO.setSpecialtyId(10L);
    placementSpecialtyDTO.setPlacementId(10L);
    placementSpecialtyDTO.setPlacementSpecialtyType(PostSpecialtyType.PRIMARY);
    Set<PlacementSpecialtyDTO> placementSpecialtyDTOS = new HashSet<>();
    placementSpecialtyDTOS.add(placementSpecialtyDTO);
    placementDTO.setSpecialties(placementSpecialtyDTOS);

    placementXLS.setSpecialty1("");

    placementTransformerService.setSpecialties(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSpecialtiesForString);
    assertThat(placementDTO.getSpecialties().size()).isEqualTo(1);
    for (PlacementSpecialtyDTO placementSpecialtyDTOFromPlacement : placementDTO.getSpecialties()) {
      if (placementSpecialtyDTOFromPlacement.getSpecialtyId() == 10L) {
        assertThat(placementSpecialtyDTOFromPlacement.getPlacementSpecialtyType())
            .isEqualTo(PostSpecialtyType.PRIMARY);
      }
    }
  }

  @Test
  public void shouldNotSetAnUnknownOtherSites() {
    placementXLS.setOtherSites("Unknown");
    placementTransformerService.setOtherSites(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSitesForString, postDTO);
    assertThat(placementDTO.getSites().size()).isEqualTo(0);
    assertThat(placementXLS.getErrorMessage()).contains(DID_NOT_FIND_OTHER_SITE_FOR_NAME);
  }

  @Test
  public void ShouldSetOtherSitesOnParentPost() {
    placementXLS.setOtherSites("mockedSite");
    placementTransformerService.setOtherSites(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSitesForString, postDTO);
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

    placementTransformerService.setOtherSites(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSitesForString, postDTO);
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
    placementTransformerService.setOtherSites(placementXLS, placementDTO,
        PlacementTransformerServiceTest::getSitesForString, postDTO);
    assertThat(placementDTO.getSites().size()).isEqualTo(0);
    assertThat(placementXLS.getErrorMessage()).contains(FOUND_MULTIPLE_OTHER_SITES_FOR_NAME);
  }


  @Test
  public void shouldAddErrorWithInvalidPlacementGrade() {
    //WHEN
    List<String> gradesValidForPlacements = Arrays.asList("Valid grade 1", "Valid grade 2");
    String gradeName = "Specialty Registrar - HENE";
    placementXLS.setGrade(gradeName); //not a placement grade
    List<PlacementXLS> placementXLSS = Collections.singletonList(placementXLS);
    placementTransformerService.isPlacementGradeValid(placementXLSS, gradeName,
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
    List<PlacementXLS> placementXLSS = Collections.singletonList(placementXLS);
    placementTransformerService.isPlacementGradeValid(placementXLSS, gradeName,
            gradesValidForPlacements);
    //THEN
    assertThat(placementXLS.getErrorMessage()).isNull();
  }
}
