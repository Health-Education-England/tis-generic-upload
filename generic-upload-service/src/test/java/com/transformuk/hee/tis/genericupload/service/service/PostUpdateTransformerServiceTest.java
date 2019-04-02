package com.transformuk.hee.tis.genericupload.service.service;
import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;

import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PostUpdateTransformerServiceTest {
    PostUpdateTransformerService postUpdateTransformerService;
    static Map<String, List<SpecialtyDTO>> specialtyByName, specialtyByNameWithDuplicate;
    static Map<String, List<GradeDTO>> gradeByName;
    static Map<String, List<SiteDTO>> siteByName;
    static Map<String, List<TrustDTO>> trustByTrustKnownAs;
    public static final String ANOTHER = "12345another";
    public static final String SUB_SPECIALTY = "12345sub";
    public static final String TRAINING_DESCRIPTION = "12345training";
    public static final Long TRAINING_BODY_ID = 1234L;
    public static final String TRAINING_BODY = "1234body";
    public static final Long EMPLOYING_BODY_ID = 5678L;
    public static final String EMPLOYING_BODY = "5678body";
    public static final Long APPROVED_GRADE_ID = 1L;
    public static final String APPROVED_GRADE_NAME = "1234grade";
    public static final Long SITE_ID = 123L;
    public static final String SITE_NAME = "1234site";

    private PostUpdateXLS postXLS;
    private PostDTO postDTO;

    @Before
    public void initialise() throws Exception {
        postUpdateTransformerService = new PostUpdateTransformerService();
        initialiseData();
    }

    private static SpecialtyDTO createSpecialtyDTO(Long id, String intrepidId, String name, String college, String specialtyCode, Status status ){
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

    public static List<SpecialtyDTO> getSpecialtiesWithDuplicatesForSpecialtyName(String specialtyName) {
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

    public void initialiseData() throws Exception {
        // mock specialties
        SpecialtyDTO specialtyDTO = createSpecialtyDTO(12345L, "12345", "12345", "A MEDIA COLLEGE", "NHS_CODE", Status.CURRENT);
        SpecialtyDTO specialtyDTOWithSameName = createSpecialtyDTO(123456L, "123456", "12345", "A MEDIA COLLEGE", "NHS_CODE", Status.CURRENT);
        SpecialtyDTO anotherSpecialtyDTO = createSpecialtyDTO(123457L, "123457", ANOTHER, "A MEDIA COLLEGE 3", "NHS_CODE 3", Status.CURRENT);
        SpecialtyDTO subSpecialtyDTO = createSpecialtyDTO(123458L, "123458", SUB_SPECIALTY, "A MEDIA COLLEGE 3", "NHS_CODE 3", Status.CURRENT);
        specialtyByName = new HashMap<>();
        createSingleListWithSpecialty(specialtyByName, specialtyDTO);
        createSingleListWithSpecialty(specialtyByName, anotherSpecialtyDTO);
        createSingleListWithSpecialty(specialtyByName, subSpecialtyDTO);
        specialtyByNameWithDuplicate = new HashMap<>();
        createSingleListWithSpecialty(specialtyByNameWithDuplicate, specialtyDTO);
        specialtyByNameWithDuplicate.get(specialtyDTO.getName()).add(specialtyDTOWithSameName);

        // mock grades
        GradeDTO gradeDTO = createGradeDTO(APPROVED_GRADE_ID,APPROVED_GRADE_NAME);
        gradeByName = new HashMap<>();
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
        postXLS = createPostXLS("1", specialtyDTO.getName(), anotherSpecialtyDTO.getName(), subSpecialtyDTO.getName(), TRAINING_BODY);
        postDTO = new PostDTO();
    }

    public PostUpdateXLS createPostXLS(String postId, String specialtyName, String otherSpecialtyName, String subSpecialtyName, String trainingBody) {
        PostUpdateXLS postXLS = new PostUpdateXLS();
        postXLS.setPostTISId(postId);
        postXLS.setSpecialty(specialtyName);
        postXLS.setOtherSpecialties(otherSpecialtyName);
        postXLS.setSubSpecialties(subSpecialtyName);
        postXLS.setTrainingBody(trainingBody);
        return postXLS;
    }

    public void createSingleListWithSpecialty(Map<String, List<SpecialtyDTO>> specialtyByName, SpecialtyDTO specialtyDTO) throws Exception {
        if(specialtyByName.get(specialtyDTO.getName()) == null) {
            specialtyByName.put(specialtyDTO.getName(), new ArrayList<>());
            specialtyByName.get(specialtyDTO.getName()).add(specialtyDTO);
        } else {
            throw new Exception("Duplicated specialtyDTO : " + specialtyDTO.getName());
        }
    }

    public void createSingleListWithGrade(Map<String, List<GradeDTO>> gradeByName, GradeDTO gradeDTO) throws Exception {
        if(gradeByName.get(gradeDTO.getName()) == null) {
            gradeByName.put(gradeDTO.getName(), new ArrayList<>());
            gradeByName.get(gradeDTO.getName()).add(gradeDTO);
        } else {
            throw new Exception("Duplicated gradeDTO : " + gradeDTO.getName());
        }
    }

    public void createSingleListWithSite(Map<String, List<SiteDTO>> siteByName, SiteDTO siteDTO) throws Exception {
        if(siteByName.get(siteDTO.getSiteName()) == null) {
            siteByName.put(siteDTO.getSiteName(), new ArrayList<>());
            siteByName.get(siteDTO.getSiteName()).add(siteDTO);
        } else {
            throw new Exception("Duplicated siteDTO : " + siteDTO.getSiteName());
        }
    }

    public void createSingleListWithTrust(Map<String, List<TrustDTO>> trustByTrustKnownAs, TrustDTO trustDTO) throws Exception {
        if(trustByTrustKnownAs.get(trustDTO.getTrustKnownAs()) == null) {
            trustByTrustKnownAs.put(trustDTO.getTrustKnownAs(), new ArrayList<>());
            trustByTrustKnownAs.get(trustDTO.getTrustKnownAs()).add(trustDTO);
        } else {
            throw new Exception("Duplicated siteDTO : " + trustDTO.getTrustKnownAs());
        }
    }

    @Test
    public void canHandleAnUnknownSpecialty() {
        postXLS.setSpecialty("Unknown");
        postUpdateTransformerService.setSpecialties(postXLS, postDTO, PostUpdateTransformerServiceTest::getSpecialtiesForString);
        assertThat(postDTO.getSpecialties().size()).isEqualTo(2);
    }

    @Test
    public void canBuildSpecialtiesForPost() {
        postUpdateTransformerService.setSpecialties(postXLS, postDTO, PostUpdateTransformerServiceTest::getSpecialtiesForString);
        assertThat(postDTO.getSpecialties().size()).isEqualTo(3);
        Iterator<PostSpecialtyDTO> postSpecialtyDTO = postDTO.getSpecialties().iterator();
        while (postSpecialtyDTO.hasNext()) {
            assertThat(postSpecialtyDTO.next().getPostSpecialtyType()).isNotNull();
        }
    }

    @Test
    public void doesNotBuildSpecialtiesIfDuplicatesSpecialtiesExist() {
        postUpdateTransformerService.setSpecialties(postXLS, postDTO, PostUpdateTransformerServiceTest::getSpecialtiesWithDuplicatesForSpecialtyName);
        assertThat(postDTO.getSpecialties().size()).isEqualTo(0);
    }

    @Test
    public void doesNotUpdateTrainingDescriptionIfNull() {
        postXLS.setTrainingDescription(TRAINING_DESCRIPTION);
        postUpdateTransformerService.updateTrainingDescription(postXLS, postDTO);
        postXLS.setTrainingDescription(null);
        postUpdateTransformerService.updateTrainingDescription(postXLS, postDTO);
        assertThat(postDTO.getTrainingDescription().equals(TRAINING_DESCRIPTION));
    }

    @Test
    public void canUpdateTrainingDescription() {
        postXLS.setTrainingDescription(TRAINING_DESCRIPTION);
        postUpdateTransformerService.updateTrainingDescription(postXLS, postDTO);
        assertThat(postDTO.getTrainingDescription().equals(TRAINING_DESCRIPTION));
    }

    @Test
    public void canUpdateTrainingBody() {
        postXLS.setTrainingBody(TRAINING_BODY);
        postUpdateTransformerService.updateTrustReferences(postXLS, postDTO, PostUpdateTransformerServiceTest::getTrustsByTrustKnownAs);
        assertThat(postDTO.getTrainingBodyId().equals(TRAINING_BODY_ID));
    }

    @Test
    public void canUpdateMainSite() {
        postXLS.setMainSite(SITE_NAME);
        postUpdateTransformerService.updateSites(postXLS, postDTO, PostUpdateTransformerServiceTest::getSiteDTOsForName);
        assertThat(postDTO.getSites().size()).isEqualTo(1);
        assertThat(postDTO.getSites().iterator().next().getSiteId().equals(SITE_ID));
    }

    @Test
    public void canUpdateApprovedGrade() {
        postXLS.setApprovedGrade(APPROVED_GRADE_NAME);
        postUpdateTransformerService.updateGrades(postXLS, postDTO, PostUpdateTransformerServiceTest::getGradeDTOsForName);
        assertThat(postDTO.getGrades().size()).isEqualTo(1);
        assertThat(postDTO.getGrades().iterator().next().getGradeId().equals(APPROVED_GRADE_ID));
    }

}
