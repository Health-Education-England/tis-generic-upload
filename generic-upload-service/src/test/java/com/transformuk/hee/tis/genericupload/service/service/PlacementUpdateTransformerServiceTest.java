package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementUpdateXLS;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PlacementUpdateTransformerServiceTest {
	public static final String ANOTHER = "12345another";
	PlacementUpdateTransformerService placementUpdateTransformerService;

	@Before
	public void initialise() throws Exception {
		placementUpdateTransformerService = new PlacementUpdateTransformerService();
		initialiseData();
	}

	static Map<String, List<SpecialtyDTO>> specialtyByName, specialtyByNameWithDuplicate;

	private PlacementUpdateXLS placementXLS;
	PlacementDetailsDTO placementDTO;

	private static SpecialtyDTO createSpeciltyDTO(Long id, String intrepidId, String name, String college, String specialtyCode, Status status ){
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
		SpecialtyDTO specialtyDTO = createSpeciltyDTO(12345L, "12345", "12345", "A MEDIA COLLEGE", "NHS_CODE", Status.CURRENT);
		SpecialtyDTO specialtyDTOWithSameName = createSpeciltyDTO(123456L, "123456", "12345", "A MEDIA COLLEGE", "NHS_CODE", Status.CURRENT);
		SpecialtyDTO anotherSpecialtyDTO = createSpeciltyDTO(123453L, "123453", ANOTHER, "A MEDIA COLLEGE 3", "NHS_CODE 3", Status.CURRENT);

		specialtyByName = new HashMap<>();
		createSingleListWithSpecialty(specialtyByName, specialtyDTO);
		createSingleListWithSpecialty(specialtyByName, anotherSpecialtyDTO);

		specialtyByNameWithDuplicate = new HashMap<>();
		createSingleListWithSpecialty(specialtyByNameWithDuplicate, specialtyDTO);
		specialtyByNameWithDuplicate.get(specialtyDTO.getName()).add(specialtyDTOWithSameName);

		placementXLS = createPlacementXLS("1", "100",  "WMD/5AT01/085/ST1/001", specialtyDTO.getName());

		placementDTO = new PlacementDetailsDTO();
	}

	public PlacementUpdateXLS createPlacementXLS(String placementId, String intrepidId, String npn, String specialtyName) {
		PlacementUpdateXLS placementXLS = new PlacementUpdateXLS();
		placementXLS.setPlacementId(placementId);
		placementXLS.setIntrepidId(intrepidId);
		placementXLS.setNationalPostNumber(npn);
		placementXLS.setSpecialty1(specialtyName);
		return placementXLS;
	}

	public void createSingleListWithSpecialty(Map<String, List<SpecialtyDTO>> specialtyByName, SpecialtyDTO specialtyDTO) throws Exception {
		if(specialtyByName.get(specialtyDTO.getName()) == null) {
			specialtyByName.put(specialtyDTO.getName(), new ArrayList<>());
			specialtyByName.get(specialtyDTO.getName()).add(specialtyDTO);
		} else {
			throw new Exception("Duplicated specialtyDTO : " + specialtyDTO.getName());
		}
	}

	@Test
	public void canHandleAnUnknownSpecialty() {
		placementXLS.setSpecialty1("Unknown");
		placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO, PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
		assertThat(placementDTO.getSpecialties().size()).isEqualTo(0);
	}

	@Test
	public void canBuildSpecialtiesForPlacement() {
		placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO, PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
		assertThat(placementDTO.getSpecialties().size()).isEqualTo(1);
		PlacementSpecialtyDTO placementSpecialtyDTO = placementDTO.getSpecialties().iterator().next();
		assertThat(placementSpecialtyDTO.getPlacementSpecialtyType()).isNotNull();
		assertThat(placementSpecialtyDTO.getPlacementSpecialtyType()).isEqualTo(PostSpecialtyType.PRIMARY);
	}

	@Test
	public void canUpdateIntrepidIdForPlacementWhenDbIntrepidIdIsNull(){
		placementUpdateTransformerService.updateIntrepidId(placementXLS,placementDTO);
		assertThat(placementDTO.getIntrepidId()).isEqualToIgnoringCase(placementXLS.getIntrepidId());
	}

	@Test
	public void canUpdateIntrepidIdForPlacementWhenDbIntrepidIdIsNotNull(){
		String expectedIntrepidId = "222";
		placementDTO.setIntrepidId(expectedIntrepidId);
		placementUpdateTransformerService.updateIntrepidId(placementXLS,placementDTO);
		assertThat(placementDTO.getIntrepidId()).isEqualToIgnoringCase(expectedIntrepidId);
	}

	@Test
	public void doesNotBuildSpecialtiesIfDuplicatesSpecialtiesExist() {
		placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO, PlacementUpdateTransformerServiceTest::getSpecialtiesWithDuplicatesForSpecialtyName);
		assertThat(placementDTO.getSpecialties().size()).isEqualTo(0);
	}

	@Test
	public void canBuildMultipleSpecialtiesForPlacement() {
		placementXLS.setSpecialty2(ANOTHER);
		placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO, PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
		assertThat(placementDTO.getSpecialties().size()).isEqualTo(2);
	}

	@Test
	public void handlesDuplicationOnOther() {
		placementXLS.setSpecialty2(ANOTHER);
		placementXLS.setSpecialty3(ANOTHER);

		placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO, PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
		assertThat(placementDTO.getSpecialties().size()).isEqualTo(2);
	}

	@Test
	public void handlesDuplicationOnPrimary() {
		placementXLS.setSpecialty2(ANOTHER);
		placementXLS.setSpecialty3("12345");

		placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO, PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
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

		placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO, PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
		assertThat(placementDTO.getSpecialties().size()).isEqualTo(1);
		for(PlacementSpecialtyDTO placementSpecialtyDTOFromPlacement : placementDTO.getSpecialties()) {
			if(placementSpecialtyDTOFromPlacement.getSpecialtyId() == 12345L) {
				assertThat(placementSpecialtyDTOFromPlacement.getPlacementSpecialtyType()).isEqualTo(PostSpecialtyType.PRIMARY);
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

		placementUpdateTransformerService.setSpecialties(placementXLS, placementDTO, PlacementUpdateTransformerServiceTest::getSpecialtiesForString);
		assertThat(placementDTO.getSpecialties().size()).isEqualTo(1);
		for(PlacementSpecialtyDTO placementSpecialtyDTOFromPlacement : placementDTO.getSpecialties()) {
			if(placementSpecialtyDTOFromPlacement.getSpecialtyId() == 10L) {
				assertThat(placementSpecialtyDTOFromPlacement.getPlacementSpecialtyType()).isEqualTo(PostSpecialtyType.PRIMARY);
			}
		}
	}
}
