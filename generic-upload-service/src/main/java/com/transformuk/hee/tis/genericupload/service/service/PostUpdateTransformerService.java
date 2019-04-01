package com.transformuk.hee.tis.genericupload.service.service;

import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.LocalOfficeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostGradeDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostGradeType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSiteType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

@Component
public class PostUpdateTransformerService {

  private static final Logger logger = getLogger(PostUpdateTransformerService.class);
  private static final String DID_NOT_FIND_GRADE_FOR_NAME = "Did not find grade for name \"%s\".";
  private static final String FOUND_MULTIPLE_GRADES_FOR_NAME = "Found multiple grades for name \"%s\".";
  private static final String DID_NOT_FIND_PROGRAMMES_FOR_IDS = "Did not find current programmes with IDs \"%s\".";
  private static final String PROGRAMME_ID_NOT_A_NUMBER = "The programme ID \"%s\" is not a number.";
  private static final String DID_NOT_FIND_SITE_FOR_NAME = "Did not find site for name \"%s\".";
  private static final String FOUND_MULTIPLE_SITES_FOR_NAME = "Found multiple sites for name \"%s\".";
  private static final String DID_NOT_FIND_SPECIALTY_FOR_NAME = "Did not find specialty for name \"%s\".";
  private static final String FOUND_MULTIPLE_SPECIALTIES_FOR_NAME = "Found multiple specialties for name \"%s\".";
  private static final String GIVEN_POST_STATUS_IS_NOT_VALID = "Given post status is not valid. ";
  private static final String GIVEN_OLD_POST_IS_NOT_VALID = "Given old post is not valid. ";
  private static final String DID_NOT_FIND_OWNER_FOR_NAME = "Owner name not found in the database ";
  private static final String FOUND_MULTIPLE_OWNERS_FOR_NAME = "Multiple owners are found in the database ";
  private static final String DID_NOT_FIND_TRUST_FOR_NAME = "Did not find trust for name \"%s\".";
  private static final String FOUND_MULTIPLE_TRUSTS_FOR_NAME = "Found multiple trusts for name \"%s\".";
  private static final String DID_NOT_FIND_POST_FOR_ID = "Did not find the post for id \"%s\".";

  @Autowired
  private TcsServiceImpl tcsServiceImpl;
  @Autowired
  private ReferenceServiceImpl referenceServiceImpl;

  void processPostUpdateUpload(List<PostUpdateXLS> postUpdateXLSS, String username) {
    postUpdateXLSS.forEach(PostUpdateXLS::initialiseSuccessfullyImported);
    //This is where we need to extract the data from the Excel file and start building the business logic
    //to form the PostDTO so that it can be used to call the TCS's and Reference's REST end points (e.g. /api/posts)
    //which is from TcsServiceImpl or ReferenceServiceImpl

    for (PostUpdateXLS postUpdateXLS : postUpdateXLSS) {
      useMatchingCriteriaToUpdatePost(postUpdateXLS, username);
    }

  }

  private void useMatchingCriteriaToUpdatePost(PostUpdateXLS postUpdateXLS, String username){
    //TIS_PostID* //Should match to one of TIS_PostID's
    String postTISId = postUpdateXLS.getPostTISId();
    if(!StringUtils.isEmpty(postTISId)){
      //This getPostById() method is written in TCS service
      try {
        PostDTO dbPostDTO = tcsServiceImpl.getPostById(Long.valueOf(postUpdateXLS.getPostTISId()));
        if(dbPostDTO != null) {
          updatePost(postUpdateXLS, dbPostDTO, username);
        } else {
          postUpdateXLS.addErrorMessage(String.format(DID_NOT_FIND_POST_FOR_ID, postTISId));
        }
      } catch (ResourceAccessException e) {
        postUpdateXLS.addErrorMessage(String.format(DID_NOT_FIND_POST_FOR_ID, postTISId));
      }
    }
  }

  private void updatePost(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO, String username){
    updateGrades(postUpdateXLS, dbPostDTO, referenceServiceImpl::findGradesByName);
    setSpecialties(postUpdateXLS, dbPostDTO, tcsServiceImpl::getSpecialtyByName);
    updateSites(postUpdateXLS, dbPostDTO, referenceServiceImpl::findSitesByName);
    updateOwner(postUpdateXLS, dbPostDTO, referenceServiceImpl::findLocalOfficesByName);
    updateTrainingDescription(postUpdateXLS, dbPostDTO);
    updateProgrammes(postUpdateXLS, dbPostDTO, tcsServiceImpl::findProgrammesIn);
    updateTrustReferences(postUpdateXLS, dbPostDTO, referenceServiceImpl::findTrustByTrustKnownAs);

    // check status
    String postStatus = postUpdateXLS.getStatus();
    if (!StringUtils.isEmpty(postStatus)) {
      if(EnumUtils.isValidEnum(Status.class, postStatus.toUpperCase())){
        dbPostDTO.setStatus(Status.valueOf(postStatus.toUpperCase()));
      } else {
        postUpdateXLS.addErrorMessage(GIVEN_POST_STATUS_IS_NOT_VALID);
      }
    }

    // check old post
    String oldPost = postUpdateXLS.getOldPost();
    if (!StringUtils.isEmpty(oldPost)) {
      List<String> npnList = new ArrayList<>();
      npnList.add(oldPost);
      List<PostDTO> postDTOSList = tcsServiceImpl.findPostsByNationalPostNumbersIn(npnList);
      if (postDTOSList.size() == 0) {
        postUpdateXLS.addErrorMessage(GIVEN_OLD_POST_IS_NOT_VALID);
      } else {
        PostDTO oldPostDTO = postDTOSList.get(0);
        dbPostDTO.setOldPost(oldPostDTO);
      }
    }

    if (!postUpdateXLS.hasErrors()) {
      logger.info("dbPostDTO => {}", dbPostDTO);
      tcsServiceImpl.updatePost(dbPostDTO);// updatePost() method is written in TCS service
      postUpdateXLS.setSuccessfullyImported(true);
    }

  }

  /*********************Grade starts here*******************************/
  private void updateGrades(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO,
      Function<String, List<GradeDTO>> getGradeDTOsForName) {
    Set<PostGradeDTO> postGradeDTOS = dbPostDTO.getGrades();
    if (postGradeDTOS == null) {
      postGradeDTOS = initialiseNewPostGradeDTOS(dbPostDTO);
    }
    Optional<PostGradeDTO> postGradeDTOOptional1 = buildPostGradeDTO(postUpdateXLS, dbPostDTO, getGradeDTOsForName, postUpdateXLS.getApprovedGrade(), PostGradeType.APPROVED);
    if (postGradeDTOOptional1.isPresent()) {
      postGradeDTOS = initialiseNewPostGradeDTOS(dbPostDTO);
      PostGradeDTO postGradeDTO = postGradeDTOOptional1.get();
      addDTOIfNotPresentAsApprovedOrOther1(postGradeDTOS, postGradeDTO);
    }
    String otherGradesCommaSeparated = postUpdateXLS.getOtherGrades();
    if (otherGradesCommaSeparated != null) {
      String[] otherGrades = otherGradesCommaSeparated.split(",");
      for (String otherGrade : otherGrades) {
        Optional<PostGradeDTO> postGradeDTOOptional2 = buildPostGradeDTO(postUpdateXLS, dbPostDTO, getGradeDTOsForName, otherGrade, PostGradeType.OTHER);
        if (postGradeDTOOptional2.isPresent()) {
          PostGradeDTO postGradeDTO = postGradeDTOOptional2.get();
          addDTOIfNotPresentAsApprovedOrOther1(postGradeDTOS, postGradeDTO);
        }
      }
    }
  }

  private Set<PostGradeDTO> initialiseNewPostGradeDTOS(PostDTO dbPostDTO) {
    Set<PostGradeDTO> postGradeDTOS = new HashSet<>();
    dbPostDTO.setGrades(postGradeDTOS);
    return postGradeDTOS;
  }

  private void addDTOIfNotPresentAsApprovedOrOther1(Set<PostGradeDTO> postGradeDTOS,
      PostGradeDTO postGradeDTO) {
    if (postGradeDTOS.isEmpty()) {
      postGradeDTOS.add(postGradeDTO);
    } else if (!postGradeDTOS.contains(postGradeDTO)) {
      postGradeDTO.setPostGradeType(PostGradeType.OTHER);
      postGradeDTOS.add(postGradeDTO);
    }
  }

  private Optional<PostGradeDTO> buildPostGradeDTO(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO,
      Function<String, List<GradeDTO>> getGradeDTOsForName,
      String gradeName, PostGradeType gradeType) {
    Optional<GradeDTO> aSingleValidGrade = getASingleValidGradeFromTheReferenceService(postUpdateXLS, getGradeDTOsForName, gradeName);
    if (aSingleValidGrade.isPresent()) {
      GradeDTO gradeDTO = aSingleValidGrade.get();
      PostGradeDTO postGradeDTO = new PostGradeDTO(dbPostDTO.getId(), gradeDTO.getId(), gradeType);
      return Optional.of(postGradeDTO);
    }
    return Optional.empty();
  }

  private Optional<GradeDTO> getASingleValidGradeFromTheReferenceService(PostUpdateXLS postUpdateXLS, Function<String, List<GradeDTO>> getGradeDTOsForName, String gradeName) {
    if (!StringUtils.isEmpty(gradeName)) {
      List<GradeDTO> gradeByName = getGradeDTOsForName.apply(gradeName);
      if (gradeByName != null) {

        if (gradeByName.size() == 1) {
          return Optional.of(gradeByName.get(0));
        } else {
          String errorMessage = gradeByName.isEmpty() ? DID_NOT_FIND_GRADE_FOR_NAME : FOUND_MULTIPLE_GRADES_FOR_NAME;
          postUpdateXLS.addErrorMessage(String.format(errorMessage, gradeName));
        }
      }
    }
    return Optional.empty();
  }
  /*********************Grade ends here*******************************/

  /*********************specialty starts here*******************************/
  private void setSpecialties(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO,
      Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName) {
    Set<PostSpecialtyDTO> postSpecialtyDTOS = dbPostDTO.getSpecialties();
    if (postSpecialtyDTOS == null) {
      postSpecialtyDTOS = initialiseNewPostSpecialtyDTOS(dbPostDTO);
    }
    Optional<PostSpecialtyDTO> postSpecialtyDTOOptional1 = buildPostSpecialtyDTO(postUpdateXLS, dbPostDTO, getSpecialtyDTOsForName, postUpdateXLS.getSpecialty(), PostSpecialtyType.PRIMARY);
    if (postSpecialtyDTOOptional1.isPresent()) {
      postSpecialtyDTOS = initialiseNewPostSpecialtyDTOS(dbPostDTO);
      PostSpecialtyDTO postSpecialtyDTO = postSpecialtyDTOOptional1.get();
      postSpecialtyDTOS.add(postSpecialtyDTO);
    }
    String otherSpecialtiesCommaSeperated = postUpdateXLS.getOtherSpecialties();
    if (otherSpecialtiesCommaSeperated != null) {
      String[] otherSpecialties = otherSpecialtiesCommaSeperated.split(",");
      for (String otherSpecialty : otherSpecialties) {
        Optional<PostSpecialtyDTO> postSpecialtyDTOOptional2 = buildPostSpecialtyDTO(postUpdateXLS, dbPostDTO, getSpecialtyDTOsForName, otherSpecialty, PostSpecialtyType.OTHER);
        if (postSpecialtyDTOOptional2.isPresent()) {
          PostSpecialtyDTO postSpecialtyDTO = postSpecialtyDTOOptional2.get();
          postSpecialtyDTOS.add(postSpecialtyDTO);
        }
      }
    }
    String subSpecialtiesCommaSeperated = postUpdateXLS.getSubSpecialties();
    if (subSpecialtiesCommaSeperated != null) {
      String[] subSpecialties = subSpecialtiesCommaSeperated.split(",");
      for (String subSpecialty : subSpecialties) {
        Optional<PostSpecialtyDTO> postSpecialtyDTOOptional3 = buildPostSpecialtyDTO(postUpdateXLS, dbPostDTO, getSpecialtyDTOsForName, subSpecialty, PostSpecialtyType.SUB_SPECIALTY);
        if (postSpecialtyDTOOptional3.isPresent()) {
          PostSpecialtyDTO postSpecialtyDTO = postSpecialtyDTOOptional3.get();
          postSpecialtyDTOS.add(postSpecialtyDTO);
        }
      }
    }
  }

  private Set<PostSpecialtyDTO> initialiseNewPostSpecialtyDTOS(PostDTO dbPostDTO) {
    Set<PostSpecialtyDTO> postSpecialtyDTOS = new HashSet<>();
    dbPostDTO.setSpecialties(postSpecialtyDTOS);
    return postSpecialtyDTOS;
  }

  private Optional<PostSpecialtyDTO> buildPostSpecialtyDTO(PostUpdateXLS postUpdateXLS,
      PostDTO dbPostDTO,
      Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName,
      String specialtyName, PostSpecialtyType specialityType) {
    Optional<SpecialtyDTO> aSingleValidSpecialty = getASingleValidSpecialtyFromTheReferenceService(postUpdateXLS, getSpecialtyDTOsForName, specialtyName);
    if (aSingleValidSpecialty.isPresent()) {
      SpecialtyDTO specialtyDTO = aSingleValidSpecialty.get();
      PostSpecialtyDTO postSpecialtyDTO = new PostSpecialtyDTO(dbPostDTO.getId(), specialtyDTO, specialityType);
      return Optional.of(postSpecialtyDTO);
    }
    return Optional.empty();
  }

  private Optional<SpecialtyDTO> getASingleValidSpecialtyFromTheReferenceService(PostUpdateXLS postUpdateXLS, Function<String,
      List<SpecialtyDTO>> getSpecialtyDTOsForName, String specialtyName) {
    if (!StringUtils.isEmpty(specialtyName)) {
      List<SpecialtyDTO> specialtyByName = getSpecialtyDTOsForName.apply(specialtyName);
      if (specialtyByName != null) {

        if (specialtyByName.size() == 1) {
          return Optional.of(specialtyByName.get(0));
        } else {
          String errorMessage = specialtyByName.isEmpty() ? DID_NOT_FIND_SPECIALTY_FOR_NAME : FOUND_MULTIPLE_SPECIALTIES_FOR_NAME;
          postUpdateXLS.addErrorMessage(String.format(errorMessage, specialtyName));
        }
      }
    }
    return Optional.empty();
  }

  /****************************Specialty ends here****************************/


  /**************************Training Description starts here*******************/
  private void updateTrainingDescription(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO) {
    if (!StringUtils.isEmpty(postUpdateXLS.getTrainingDescription())) {
      dbPostDTO.setTrainingDescription(postUpdateXLS.getTrainingDescription());
    }
  }
  /******************Training Description ends here*****************************/

  private void updateTrustReferences(PostUpdateXLS postUpdateXls, PostDTO postDto, Function<String, List<TrustDTO>> findTrustsByTrustKnownAs) {
    // Update training body.
      String trainingBody = postUpdateXls.getTrainingBody();
      Long trainingBodyId = getTrustIdFromTrustKnownAs(postUpdateXls, trainingBody, findTrustsByTrustKnownAs, postDto.getTrainingBodyId());
      postDto.setTrainingBodyId(trainingBodyId);

      // Update employing body.
      String employingBody = postUpdateXls.getEmployingBody();
      Long employingBodyId = getTrustIdFromTrustKnownAs(postUpdateXls, employingBody, findTrustsByTrustKnownAs, postDto.getEmployingBodyId());
      postDto.setEmployingBodyId(employingBodyId);
  }

  private Long getTrustIdFromTrustKnownAs(PostUpdateXLS postUpdateXls, String trustKnownAs, Function<String, List<TrustDTO>> findTrustsByTrustKnownAs, Long defaultValue) {
    if (trustKnownAs == null) {
      return defaultValue;
    }

    List<TrustDTO> trusts = findTrustsByTrustKnownAs.apply(trustKnownAs);

    if (trusts.size() == 1) {
      return trusts.get(0).getId();
    } else {
      String errorMessage = trusts.isEmpty() ? DID_NOT_FIND_TRUST_FOR_NAME : FOUND_MULTIPLE_TRUSTS_FOR_NAME;
      postUpdateXls.addErrorMessage(String.format(errorMessage, trustKnownAs));
      return defaultValue;
    }
  }

  /*********************Sites start here****************************************/
  private void updateSites(PostUpdateXLS postUpdateXLS, PostDTO postDTO,
      Function<String, List<SiteDTO>> getSiteDTOsForName) {
    Set<PostSiteDTO> postSiteDTOS = postDTO.getSites();
    if (postSiteDTOS == null) {
      postSiteDTOS = initialiseNewPostSiteDTOS(postDTO);
    }
    Optional<PostSiteDTO> postSiteDTOOptional1 = buildPostSiteDTO(postUpdateXLS, postDTO, getSiteDTOsForName, postUpdateXLS.getMainSite(), PostSiteType.PRIMARY);
    if (postSiteDTOOptional1.isPresent()) {
      postSiteDTOS = initialiseNewPostSiteDTOS(postDTO);
      PostSiteDTO postSiteDTO = postSiteDTOOptional1.get();
      addDTOIfNotPresentAsPrimaryOrOther1(postSiteDTOS, postSiteDTO);
    }
    String otherSitesCommaSeperated = postUpdateXLS.getOtherSites();
    if (otherSitesCommaSeperated != null) {
      String[] otherSites = otherSitesCommaSeperated.split(",");
      for (String otherSite : otherSites) {
        Optional<PostSiteDTO> postSiteDTOOptional2 = buildPostSiteDTO(postUpdateXLS, postDTO, getSiteDTOsForName, otherSite, PostSiteType.OTHER);
        if (postSiteDTOOptional2.isPresent()) {
          PostSiteDTO postSiteDTO = postSiteDTOOptional2.get();
          addDTOIfNotPresentAsPrimaryOrOther1(postSiteDTOS, postSiteDTO);
        }
      }
    }
  }

  private Set<PostSiteDTO> initialiseNewPostSiteDTOS(PostDTO postDTO) {
    Set<PostSiteDTO> postSiteDTOS = new HashSet<>();
    postDTO.setSites(postSiteDTOS);
    return postSiteDTOS;
  }

  private void addDTOIfNotPresentAsPrimaryOrOther1(Set<PostSiteDTO> postSiteDTOS,
      PostSiteDTO postSiteDTO) {
    if (postSiteDTOS.isEmpty()) {
      postSiteDTOS.add(postSiteDTO);
    } else if (!postSiteDTOS.contains(postSiteDTO)) {
      postSiteDTO.setPostSiteType(PostSiteType.OTHER);
      postSiteDTOS.add(postSiteDTO);
    }
  }

  private Optional<PostSiteDTO> buildPostSiteDTO(PostUpdateXLS postUpdateXLS, PostDTO postDTO,
      Function<String, List<SiteDTO>> getSiteDTOsForName,
      String siteName, PostSiteType siteType) {
    Optional<SiteDTO> aSingleValidSite = getASingleValidSiteFromTheReferenceService(postUpdateXLS, getSiteDTOsForName, siteName);
    if (aSingleValidSite.isPresent()) {
      SiteDTO siteDTO = aSingleValidSite.get();
      PostSiteDTO postSiteDTO = new PostSiteDTO(postDTO.getId(), siteDTO.getId(), siteType);
      return Optional.of(postSiteDTO);
    }
    return Optional.empty();
  }

  private Optional<SiteDTO> getASingleValidSiteFromTheReferenceService(PostUpdateXLS postUpdateXLS, Function<String,
      List<SiteDTO>> getSiteDTOsForName, String siteName) {
    if (!StringUtils.isEmpty(siteName)) {
      List<SiteDTO> siteByName = getSiteDTOsForName.apply(siteName);
      if (siteByName != null) {

        if (siteByName.size() == 1) {
          return Optional.of(siteByName.get(0));
        } else {
          String errorMessage = siteByName.isEmpty() ? DID_NOT_FIND_SITE_FOR_NAME : FOUND_MULTIPLE_SITES_FOR_NAME;
          postUpdateXLS.addErrorMessage(String.format(errorMessage, siteName));
        }
      }
    }
    return Optional.empty();
  }
  /***************************Site ends here**********************************/

  /******************Owner starts here*****************************/
  public void updateOwner(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO, Function<String, List<LocalOfficeDTO>> getLocalOfficeDTOsForName) {
    String localOfficeDTOS = dbPostDTO.getOwner();
    if (localOfficeDTOS == null) {
      localOfficeDTOS = initialiseNewLocalOfficeDTOs(dbPostDTO);
    }
    buildLocalOfficeDTO(postUpdateXLS, dbPostDTO, getLocalOfficeDTOsForName);
  }
  public String initialiseNewLocalOfficeDTOs(PostDTO postDTO) {
    String localOfficeDTOs = new String();
    postDTO.setOwner(localOfficeDTOs);
    return localOfficeDTOs;
  }

  public void buildLocalOfficeDTO(PostUpdateXLS postUpdateXLS, PostDTO postDTO,
                                                   Function<String, List<LocalOfficeDTO>> getLocalOfficeDTOsForName) {
    Optional<LocalOfficeDTO> aSingleValidLocalOffice = getASingleValidLocalOfficeFromTheReferenceService(postUpdateXLS, getLocalOfficeDTOsForName, postUpdateXLS.getOwner());
    if (aSingleValidLocalOffice.isPresent()) {
      LocalOfficeDTO localOfficeDTO = aSingleValidLocalOffice.get();
      postDTO.setOwner(localOfficeDTO.getName());
    }
  }
  private Optional<LocalOfficeDTO> getASingleValidLocalOfficeFromTheReferenceService(PostUpdateXLS postUpdateXLS, Function<String,
      List<LocalOfficeDTO>> getLocalOfficeDTOsForName, String owner) {
    if (!StringUtils.isEmpty(owner)) {
      List<LocalOfficeDTO> localOfficeByName = getLocalOfficeDTOsForName.apply(owner);
      if (localOfficeByName != null) {
        if (localOfficeByName.size() != 1) {
          if (localOfficeByName.isEmpty()) {
            postUpdateXLS.addErrorMessage(DID_NOT_FIND_OWNER_FOR_NAME + owner);
          } else {
            postUpdateXLS.addErrorMessage(FOUND_MULTIPLE_OWNERS_FOR_NAME + owner);
          }
        } else {
          return Optional.of(localOfficeByName.get(0));
        }
      }
    }
    return Optional.empty();
  }
  /******************Owner ends here*****************************/

  private void updateProgrammes(PostUpdateXLS postUpdateXls, PostDTO postDto, Function<List<String>,
      List<ProgrammeDTO>> getProgrammeById) {
    String programmeIdsSeparated = postUpdateXls.getProgrammeTisId();

    // If the field is null then there is no need to update programmes.
    if (programmeIdsSeparated == null) {
      return;
    }

    // Split the comma separated field and get the programmes from the IDs.
    List<String> programmeIds = Arrays.asList(programmeIdsSeparated.split(","));

    // If any programme IDs are not numeric then report an error.
    for (String programmeId : programmeIds) {

      if (!programmeId.matches("\\d+")) {
        postUpdateXls.addErrorMessage(String.format(PROGRAMME_ID_NOT_A_NUMBER, programmeId));
        return;
      }
    }

    List<ProgrammeDTO> programmes = getProgrammeById.apply(programmeIds);

    // Filter to only current programmes.
    programmes = programmes.stream().filter(dto -> dto.getStatus().equals(Status.CURRENT))
        .collect(Collectors.toList());

    // If one or more of the programmes was not found or not current then report an error.
    if (programmes.size() != programmeIds.size()) {
      Set<String> currentFoundIds = programmes.stream().map(programme -> String.valueOf(programme.getId()))
          .collect(Collectors.toSet());
      List<String> missingIds = new ArrayList<>(programmeIds);
      missingIds.removeAll(currentFoundIds);
      StringJoiner joiner = new StringJoiner(", ");
      missingIds.forEach(programmeId -> joiner.add(programmeId));
      postUpdateXls.addErrorMessage(String.format(DID_NOT_FIND_PROGRAMMES_FOR_IDS, joiner.toString()));
      return;
    }

    postDto.setProgrammes(new HashSet<>(programmes));
  }
}
