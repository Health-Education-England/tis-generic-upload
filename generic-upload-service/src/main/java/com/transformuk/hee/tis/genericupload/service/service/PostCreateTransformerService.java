package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PostCreateXls;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.LocalOfficeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.ReferenceService;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostGradeDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostGradeType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSiteType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

// TODO: Add logging throughout.
// TODO: Graceful handling of empty uploads (generic solution).
// TODO: Handle errors from tcs/reference better to avoid "Internal Server Error" results.
@Component
public class PostCreateTransformerService {

  private TcsServiceImpl tcsService;

  private ReferenceService referenceService;

  private Map<String, GradeDTO> gradeNameToDto = Collections.emptyMap();
  private Map<String, SiteDTO> siteNameToDto = Collections.emptyMap();
  private Map<String, SpecialtyDTO> specialtyNameToDto = Collections.emptyMap();
  private Map<String, Long> trustNameToId = Collections.emptyMap();
  private Map<String, ProgrammeDTO> programmeIdToDto = Collections.emptyMap();
  private Set<String> validLocalOffices = Collections.emptySet();
  private Map<String, PostDTO> postNpnToDto = Collections.emptyMap();

  PostCreateTransformerService(ReferenceService referenceService, TcsServiceImpl tcsService) {
    this.tcsService = tcsService;
    this.referenceService = referenceService;
  }

  void processUpload(List<PostCreateXls> xlsList) {
    xlsList.forEach(TemplateXLS::initialiseSuccessfullyImported);

    // Initialize cache maps.
    gradeNameToDto = new HashMap<>();
    siteNameToDto = new HashMap<>();
    specialtyNameToDto = new HashMap<>();
    trustNameToId = new HashMap<>();
    programmeIdToDto = new HashMap<>();
    validLocalOffices = new HashSet<>();
    postNpnToDto = new HashMap<>();

    List<PostDTO> postDtos = new ArrayList<>();
    Map<String, Long> npnToXls = xlsList.stream().collect(
        Collectors.groupingBy(PostCreateXls::getNationalPostNumber, Collectors.counting()));

    for (PostCreateXls xls : xlsList) {
      try {
        String npn = xls.getNationalPostNumber();

        if (npnToXls.get(npn) > 1) {
          String errorMessage = String.format("Duplicate NPN '%s' in upload.", npn);
          throw new IllegalArgumentException(errorMessage);
        }

        postDtos.add(buildPostDto(xls));
      } catch (IllegalArgumentException e) {
        xls.addErrorMessage(e.getMessage());
      }
    }

    if (!postDtos.isEmpty()) {
      // TODO: create endpoint
      tcsService.bulkCreateDto(postDtos, "/api/bulk-posts", PostDTO.class);
      xlsList.forEach(x -> x.setSuccessfullyImported(true));
    }
  }

  private PostDTO buildPostDto(PostCreateXls xls) throws IllegalArgumentException {
    PostDTO postDto = new PostDTO();

    // Check if NPN already exists.
    String npn = xls.getNationalPostNumber();
    updateExistingPostCache(npn);

    if (!postNpnToDto.containsKey(npn)) {
      postDto.setNationalPostNumber(npn);
    } else {
      String errorMessage = String.format("Post already exists with the NPN '%s'.", npn);
      throw new IllegalArgumentException(errorMessage);
    }

    postDto.setGrades(buildPostGrades(xls));
    postDto.setSpecialties(buildPostSpecialties(xls));
    postDto.setTrainingDescription(xls.getTrainingDescription());
    postDto.setSites(buildPostSites(xls));

    // Update the trust cache with any new names.
    Set<String> namesToCache = new HashSet<>();
    namesToCache.add(xls.getEmployingBody());
    namesToCache.add(xls.getTrainingBody());
    updateTrustCache(namesToCache);

    Long employingBodyId = trustNameToId.get(xls.getEmployingBody());

    if (employingBodyId != null) {
      postDto.setEmployingBodyId(employingBodyId);
    } else {
      String errorMessage = String
          .format("Current employing body not found with the name '%s'.", xls.getEmployingBody());
      throw new IllegalArgumentException(errorMessage);
    }

    Long trainingBodyId = trustNameToId.get(xls.getTrainingBody());

    if (trainingBodyId != null) {
      postDto.setTrainingBodyId(trainingBodyId);
    } else {
      String errorMessage = String
          .format("Current training body not found with the name '%s'.", xls.getTrainingBody());
      throw new IllegalArgumentException(errorMessage);
    }

    postDto.setProgrammes(buildPostProgrammes(xls.getProgrammeTisId()));

    String owner = xls.getOwner();
    updateLocalOfficeCache(owner);

    if (validLocalOffices.contains(owner)) {
      postDto.setOwner(owner);
    } else {
      String errorMessage = String.format("Current owner not found with the name '%s'.", owner);
      throw new IllegalArgumentException(errorMessage);
    }

    String oldPost = xls.getOldPost();
    updateExistingPostCache(oldPost);

    if (postNpnToDto.containsKey(oldPost)) {
      postDto.setOldPost(postNpnToDto.get(oldPost));
    } else {
      String errorMessage = String.format("Old post not found with the NPN '%s'.", oldPost);
      throw new IllegalArgumentException(errorMessage);
    }

    return postDto;
  }

  /**
   * Build a set of {@link PostGradeDTO}s for the given XLS.
   *
   * @param xls The XSL to get the grade names from.
   * @return A collection of {@code PostGradeDTO}s.
   */
  private Set<PostGradeDTO> buildPostGrades(PostCreateXls xls) {
    List<String> otherGrades = splitMultiValueField(xls.getOtherGrades());

    // Update the cache with any new names.
    Set<String> namesToCache = new HashSet<>();
    namesToCache.add(xls.getApprovedGrade());
    namesToCache.addAll(otherGrades);
    updateGradeCache(namesToCache);

    Set<PostGradeDTO> builtDtos = new HashSet<>();
    addPostGrade(xls.getApprovedGrade(), PostGradeType.APPROVED, builtDtos);

    for (String name : otherGrades) {
      addPostGrade(name, PostGradeType.OTHER, builtDtos);
    }

    return builtDtos;
  }

  /**
   * If there are any grade names which are not in the cache, then query for them and cache the
   * results.
   *
   * @param names The names to add to the cache.
   */
  private void updateGradeCache(Set<String> names) {
    Set<String> namesToFind = names.stream().filter(name -> !gradeNameToDto.containsKey(name))
        .collect(Collectors.toSet());

    if (!namesToFind.isEmpty()) {
      String joinedNames = StringUtils.join(namesToFind, "\",\"");
      List<GradeDTO> dtos = referenceService.findGradesByName(joinedNames);

      for (GradeDTO dto : dtos) {
        gradeNameToDto.put(dto.getName(), dto);
      }
    }
  }

  /**
   * Create a {@link PostGradeDTO} if the grade name exists in the cache.
   *
   * @param name The name of the grade.
   * @param type The type of the grade.
   * @param dtos The collection to add new PostGradeDTOs to.
   * @throws IllegalArgumentException If the given grade does not exist.
   */
  private void addPostGrade(String name, PostGradeType type, Set<PostGradeDTO> dtos)
      throws IllegalArgumentException {
    GradeDTO cachedDto = gradeNameToDto.get(name);

    if (cachedDto != null) {
      dtos.add(new PostGradeDTO(null, cachedDto.getId(), type));
    } else {
      String errorMessage = String.format("Current grade not found with the name '%s'.", name);
      throw new IllegalArgumentException(errorMessage);
    }
  }

  private Set<PostSpecialtyDTO> buildPostSpecialties(PostCreateXls xls) {
    List<String> subSpecialties = splitMultiValueField(xls.getSubSpecialties());
    List<String> otherSpecialties = splitMultiValueField(xls.getOtherSpecialties());

    // Update the cache with any new names.
    Set<String> namesToCache = new HashSet<>();
    namesToCache.add(xls.getSpecialty());
    namesToCache.addAll(subSpecialties);
    namesToCache.addAll(otherSpecialties);
    updateSpecialtyCache(namesToCache);

    Set<PostSpecialtyDTO> builtDtos = new HashSet<>();
    addPostSpecialty(xls.getSpecialty(), PostSpecialtyType.PRIMARY, builtDtos);

    for (String name : subSpecialties) {
      addPostSpecialty(name, PostSpecialtyType.SUB_SPECIALTY, builtDtos);
    }

    for (String name : otherSpecialties) {
      addPostSpecialty(name, PostSpecialtyType.OTHER, builtDtos);
    }

    return builtDtos;
  }

  private void updateSpecialtyCache(Set<String> names) {
    Set<String> namesToFind = names.stream().filter(name -> !specialtyNameToDto.containsKey(name))
        .collect(Collectors.toSet());

    if (!namesToFind.isEmpty()) {
      String joinedNames = StringUtils.join(namesToFind, "\",\"");
      List<SpecialtyDTO> dtos = tcsService.getSpecialtyByName(joinedNames);

      for (SpecialtyDTO dto : dtos) {
        specialtyNameToDto.put(dto.getName(), dto);
      }
    }
  }

  private void addPostSpecialty(String name, PostSpecialtyType type, Set<PostSpecialtyDTO> dtos)
      throws IllegalArgumentException {
    SpecialtyDTO cachedDto = specialtyNameToDto.get(name);

    if (cachedDto != null) {
      dtos.add(new PostSpecialtyDTO(null, cachedDto, type));
    } else {
      String errorMessage = String.format("Current specialty not found with the name '%s'.", name);
      throw new IllegalArgumentException(errorMessage);
    }
  }

  private Set<PostSiteDTO> buildPostSites(PostCreateXls xls) {
    List<String> otherSites = splitMultiValueField(xls.getOtherSites());

    // Update the cache with any new names.
    Set<String> namesToCache = new HashSet<>();
    namesToCache.add(xls.getMainSite());
    namesToCache.addAll(otherSites);
    updateSiteCache(namesToCache);

    Set<PostSiteDTO> builtDtos = new HashSet<>();
    addPostSite(xls.getMainSite(), PostSiteType.PRIMARY, builtDtos);

    for (String name : otherSites) {
      addPostSite(name, PostSiteType.OTHER, builtDtos);
    }

    return builtDtos;
  }

  private void updateSiteCache(Set<String> names) {
    Set<String> namesToFind =
        names.stream().filter(name -> !siteNameToDto.containsKey(name)).collect(Collectors.toSet());

    if (!namesToFind.isEmpty()) {
      String joinedNames = StringUtils.join(namesToFind, "\",\"");
      List<SiteDTO> dtos = referenceService.findSitesByName(joinedNames);

      for (SiteDTO dto : dtos) {
        siteNameToDto.put(dto.getSiteKnownAs(), dto);
      }
    }
  }

  private void addPostSite(String name, PostSiteType type, Set<PostSiteDTO> dtos)
      throws IllegalArgumentException {
    SiteDTO cachedDto = siteNameToDto.get(name);

    if (cachedDto != null) {
      dtos.add(new PostSiteDTO(null, cachedDto.getId(), type));
    } else {
      String errorMessage = String.format("Current site not found with the name '%s'.", name);
      throw new IllegalArgumentException(errorMessage);
    }
  }

  private void updateTrustCache(Set<String> names) {
    Set<String> namesToFind =
        names.stream().filter(name -> !trustNameToId.containsKey(name)).collect(Collectors.toSet());

    if (!namesToFind.isEmpty()) {
      List<TrustDTO> dtos = referenceService.findCurrentTrustsByTrustKnownAsIn(namesToFind);

      for (TrustDTO dto : dtos) {
        trustNameToId.put(dto.getTrustKnownAs(), dto.getId());
      }
    }
  }

  private Set<ProgrammeDTO> buildPostProgrammes(String programmeTisId)
      throws IllegalArgumentException {
    List<String> programmeIds = splitMultiValueField(programmeTisId);

    // Update the cache with any new ids.
    updateProgrammeCache(new HashSet<>(programmeIds));

    Set<ProgrammeDTO> dtos = new HashSet<>();

    for (String id : programmeIds) {
      ProgrammeDTO cachedDto = programmeIdToDto.get(id);

      if (cachedDto != null) {
        dtos.add(cachedDto);
      } else {
        String errorMessage = String.format("Current programme not found with the ID '%s'.", id);
        throw new IllegalArgumentException(errorMessage);
      }
    }

    return dtos;
  }

  private void updateProgrammeCache(Set<String> ids) {
    List<String> idsToFind =
        ids.stream().filter(id -> !programmeIdToDto.containsKey(id)).collect(Collectors.toList());

    if (!idsToFind.isEmpty()) {
      // TODO: handle non-numeric values.
      List<ProgrammeDTO> dtos = tcsService.findProgrammesIn(idsToFind);

      for (ProgrammeDTO dto : dtos) {
        programmeIdToDto.put(dto.getId().toString(), dto);
      }
    }
  }

  private void updateLocalOfficeCache(String name) {
    if (!validLocalOffices.contains(name)) {
      List<LocalOfficeDTO> dtos = referenceService.findLocalOfficesByName(name);

      for (LocalOfficeDTO dto : dtos) {
        validLocalOffices.add(dto.getName());
      }
    }
  }

  private void updateExistingPostCache(String npn) {
    if (!postNpnToDto.containsKey(npn)) {
      List<PostDTO> dtos =
          tcsService.findPostsByNationalPostNumbersIn(Collections.singletonList(npn));

      for (PostDTO dto : dtos) {
        postNpnToDto.put(dto.getNationalPostNumber(), dto);
      }
    }
  }

  private List<String> splitMultiValueField(String valueToSplit) {
    if (!valueToSplit.isEmpty()) {
      return Arrays.asList(valueToSplit.split(";"));
    }

    return Collections.emptyList();
  }
}
