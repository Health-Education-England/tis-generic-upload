package com.transformuk.hee.tis.genericupload.service.service;
import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.ReferenceService;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PostUpdateTransformerServiceTest {
    PostUpdateTransformerService postUpdateTransformerService;
    static Map<String, List<SpecialtyDTO>> specialtyByName, specialtyByNameWithDuplicate;
    public static final String ANOTHER = "12345another";
    public static final String SUB_SPECIALTY = "12345sub";
    public static final String TRAINING_DESCRIPTION = "12345training";
    public static final Long BODY_DEFAULT_ID = 2346L;
    public static final Long TRAINING_BODY_ID = 1234L;
    public static final String TRAINING_BODY = "1234body";
    public static final Long EMPLOYING_BODY_ID = 5678L;
    public static final String EMPLOYING_BODY = "4567body";
    public static final Long TRUST_ID = 4567L;


    private PostUpdateXLS postXLS;
    private PostDTO postDTO;
    private List<TrustDTO> trustDTOs;
    private TrustDTO trustDTO;

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

    public static List<SpecialtyDTO> getSpecialtiesForString(String specialtyName) {
        return specialtyByName.get(specialtyName);
    }

    public static List<SpecialtyDTO> getSpecialtiesWithDuplicatesForSpecialtyName(String specialtyName) {
        return specialtyByNameWithDuplicate.get(specialtyName);
    }

    public void initialiseData() throws Exception {
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

        postXLS = createPostXLS("1", specialtyDTO.getName(), anotherSpecialtyDTO.getName(), subSpecialtyDTO.getName(), TRAINING_BODY, EMPLOYING_BODY);

        postDTO = new PostDTO();
        postDTO.setTrainingBodyId(BODY_DEFAULT_ID);
        postDTO.setEmployingBodyId(BODY_DEFAULT_ID);

        trustDTOs = new ArrayList<>();
        trustDTO = new TrustDTO();
        trustDTO.setId(TRAINING_BODY_ID);
        trustDTO.setTrustKnownAs(TRAINING_BODY);
        trustDTOs.add(trustDTO);

    }

    public PostUpdateXLS createPostXLS(String postId, String specialtyName, String otherSpecialtyName, String subSpecialtyName, String trainingBody, String employingBody) {
        PostUpdateXLS postXLS = new PostUpdateXLS();
        postXLS.setPostTISId(postId);
        postXLS.setSpecialty(specialtyName);
        postXLS.setOtherSpecialties(otherSpecialtyName);
        postXLS.setSubSpecialties(subSpecialtyName);
        postXLS.setTrainingBody(trainingBody);
        postXLS.setEmployingBody(employingBody);
        // placementXLS.setPlacementId(placementId);
        // placementXLS.setIntrepidId(intrepidId);
        // placementXLS.setNationalPostNumber(npn);
        // placementXLS.setSpecialty1(specialtyName);
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

    public List<TrustDTO> findTrustByTrustKnownAs(String trustKnownAs) {
        return trustDTOs;
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
    public void canUpdateTrustReference() {
        postXLS.setTrainingBody(TRAINING_BODY);
        postXLS.setEmployingBody(EMPLOYING_BODY);
        postUpdateTransformerService.updateTrustReferences(postXLS, postDTO, this::findTrustByTrustKnownAs);
        assertThat(postDTO.getEmployingBodyId().equals(EMPLOYING_BODY_ID));
        assertThat(postDTO.getTrainingBodyId().equals(TRAINING_BODY_ID));
    }

    @Test
    public void canUpdateSites() {

    }




}
