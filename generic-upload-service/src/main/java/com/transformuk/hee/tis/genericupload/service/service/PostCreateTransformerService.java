package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PostCreateXls;
import com.transformuk.hee.tis.genericupload.api.dto.TemplateXLS;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.LocalOfficeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
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
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PostCreateTransformerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PostCreateTransformerService.class);

  private TcsServiceImpl tcsService;

  private ReferenceService referenceService;

  // TODO: Use a more universal caching solution.
  private Map<String, GradeDTO> gradeNameToDto = Collections.emptyMap();
  private Map<String, SiteDTO> siteNameToDto = Collections.emptyMap();
  private Map<String, SpecialtyDTO> specialtyNameToDto = Collections.emptyMap();
  private Map<String, TrustDTO> trustNameToDto = Collections.emptyMap();
  private Map<String, ProgrammeDTO> programmeIdToDto = Collections.emptyMap();
  private Map<String, LocalOfficeDTO> localOfficeNameToDto = Collections.emptyMap();
  private Map<String, PostDTO> postNpnToDto = Collections.emptyMap();

  PostCreateTransformerService(ReferenceService referenceService, TcsServiceImpl tcsService) {
    this.tcsService = tcsService;
    this.referenceService = referenceService;
  }

  void processUpload(List<PostCreateXls> xlsList) {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(String.format("Processing upload for post create with %d rows.", xlsList.size()));
    }
    xlsList.forEach(TemplateXLS::initialiseSuccessfullyImported);

    // Initialize cache maps.
    gradeNameToDto = new HashMap<>();
    siteNameToDto = new HashMap<>();
    specialtyNameToDto = new HashMap<>();
    trustNameToDto = new HashMap<>();
    programmeIdToDto = new HashMap<>();
    localOfficeNameToDto = new HashMap<>();
    postNpnToDto = new HashMap<>();

    List<PostDTO> postDtos = new ArrayList<>();
    Map<String, Long> npnToXls = xlsList.stream().collect(
        Collectors.groupingBy(PostCreateXls::getNationalPostNumber, Collectors.counting()));

    for (PostCreateXls xls : xlsList) {
      try {
        String npn = xls.getNationalPostNumber();

        if (npnToXls.get(npn) > 1) {
          validationError(String.format("Duplicate NPN '%s' in upload.", npn));
        }

        postDtos.add(buildPostDto(xls));
      } catch (IllegalArgumentException e) {
        xls.addErrorMessage(e.getMessage());
      }
    }

    if (!postDtos.isEmpty()) {
      List<PostDTO> createdPostDtos =
          tcsService.bulkCreateDto(postDtos, "/api/bulk-posts", PostDTO.class);
      xlsList.forEach(x -> x.setSuccessfullyImported(!x.hasErrors()));

      if (LOGGER.isInfoEnabled()) {
        LOGGER.info(String
            .format("Finished processing upload %d new posts created.", createdPostDtos.size()));
      }
    } else {
      LOGGER.info("No valid posts to create.");
    }
  }

  private PostDTO buildPostDto(PostCreateXls xls) {
    PostDTO postDto = new PostDTO();

    // Check if NPN already exists.
    String npn = xls.getNationalPostNumber();
    updateExistingPostCache(npn);

    if (!postNpnToDto.containsKey(npn)) {
      postDto.setNationalPostNumber(npn);
    } else {
      validationError(String.format("Post already exists with the NPN '%s'.", npn));
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

    TrustDTO trustDto = trustNameToDto.get(xls.getEmployingBody());

    if (trustDto != null && trustDto.getStatus().equals(Status.CURRENT)) {
      postDto.setEmployingBodyId(trustDto.getId());
    } else {
      validationError(String
          .format("Current employing body not found with the name '%s'.", xls.getEmployingBody()));
    }

    trustDto = trustNameToDto.get(xls.getTrainingBody());

    if (trustDto != null && trustDto.getStatus().equals(Status.CURRENT)) {
      postDto.setTrainingBodyId(trustDto.getId());
    } else {
      validationError(String
          .format("Current training body not found with the name '%s'.", xls.getTrainingBody()));
    }

    postDto.setProgrammes(buildPostProgrammes(xls.getProgrammeTisId()));

    String owner = xls.getOwner();
    updateLocalOfficeCache(owner);
    LocalOfficeDTO localOfficeDto = localOfficeNameToDto.get(owner);

    if (localOfficeDto != null && localOfficeDto.getStatus().equals(Status.CURRENT)) {
      postDto.setOwner(owner);
    } else {
      validationError(String.format("Current owner not found with the name '%s'.", owner));
    }

    String oldPost = xls.getOldPost();
    if (oldPost != null) {
      updateExistingPostCache(oldPost);

      if (postNpnToDto.containsKey(oldPost)) {
        postDto.setOldPost(postNpnToDto.get(oldPost));
      } else {
        validationError(String.format("Old post not found with the NPN '%s'.", oldPost));
      }
    }

    // Default the post to CURRENT.
    postDto.status(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);

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
  private void addPostGrade(String name, PostGradeType type, Set<PostGradeDTO> dtos) {
    GradeDTO cachedDto = gradeNameToDto.get(name);

    if (cachedDto != null && cachedDto.getStatus().equals(Status.CURRENT)) {
      dtos.add(new PostGradeDTO(null, cachedDto.getId(), type));
    } else {
      validationError(String.format("Current grade not found with the name '%s'.", name));
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

  private void addPostSpecialty(String name, PostSpecialtyType type, Set<PostSpecialtyDTO> dtos) {
    SpecialtyDTO cachedDto = specialtyNameToDto.get(name);

    if (cachedDto != null && cachedDto.getStatus()
        .equals(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT)) {
      dtos.add(new PostSpecialtyDTO(null, cachedDto, type));
    } else {
      validationError(String.format("Current specialty not found with the name '%s'.", name));
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

  private void addPostSite(String name, PostSiteType type, Set<PostSiteDTO> dtos) {
    SiteDTO cachedDto = siteNameToDto.get(name);

    if (cachedDto != null && cachedDto.getStatus().equals(Status.CURRENT)) {
      dtos.add(new PostSiteDTO(null, cachedDto.getId(), type));
    } else {
      validationError(String.format("Current site not found with the name '%s'.", name));
    }
  }

  private void updateTrustCache(Set<String> names) {
    Set<String> namesToFind =
        names.stream().filter(name -> !trustNameToDto.containsKey(name))
            .collect(Collectors.toSet());

    if (!namesToFind.isEmpty()) {
      List<TrustDTO> dtos = referenceService.findCurrentTrustsByTrustKnownAsIn(namesToFind);

      for (TrustDTO dto : dtos) {
        trustNameToDto.put(dto.getTrustKnownAs(), dto);
      }
    }
  }

  private Set<ProgrammeDTO> buildPostProgrammes(String programmeTisId) {
    List<String> programmeIds = splitMultiValueField(programmeTisId);

    // Verify that programme IDs are numeric.
    for (String id : programmeIds) {
      if (!NumberUtils.isParsable(id)) {
        validationError(String.format("Programme ID '%s' is not a number.", id));
      }
    }

    // Update the cache with any new ids.
    updateProgrammeCache(new HashSet<>(programmeIds));

    Set<ProgrammeDTO> dtos = new HashSet<>();

    for (String id : programmeIds) {
      ProgrammeDTO cachedDto = programmeIdToDto.get(id);

      if (cachedDto != null && cachedDto.getStatus()
          .equals(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT)) {
        dtos.add(cachedDto);
      } else {
        validationError(String.format("Current programme not found with the ID '%s'.", id));
      }
    }

    return dtos;
  }

  private void updateProgrammeCache(Set<String> ids) {
    List<String> idsToFind =
        ids.stream().filter(id -> !programmeIdToDto.containsKey(id)).collect(Collectors.toList());

    if (!idsToFind.isEmpty()) {
      List<ProgrammeDTO> dtos = tcsService.findProgrammesIn(idsToFind);

      for (ProgrammeDTO dto : dtos) {
        programmeIdToDto.put(dto.getId().toString(), dto);
      }
    }
  }

  private void updateLocalOfficeCache(String name) {
    if (!localOfficeNameToDto.containsKey(name)) {
      List<LocalOfficeDTO> dtos = referenceService.findLocalOfficesByName(name);

      for (LocalOfficeDTO dto : dtos) {
        localOfficeNameToDto.put(dto.getName(), dto);
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

  private static List<String> splitMultiValueField(String valueToSplit) {
    if (valueToSplit != null && !valueToSplit.isEmpty()) {
      String[] splitValues = valueToSplit.split(";");
      return Arrays.stream(splitValues)
          .map(String::trim)
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

  private static void validationError(String errorMessage) {
    LOGGER.error(errorMessage);
    throw new IllegalArgumentException(errorMessage);
  }
}
