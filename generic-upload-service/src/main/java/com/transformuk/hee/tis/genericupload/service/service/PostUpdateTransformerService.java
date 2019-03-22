package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;
import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostGradeDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostGradeType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSiteType;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

@Component
public class PostUpdateTransformerService {

  private static final String DID_NOT_FIND_GRADE_FOR_NAME = "Did not find grade for name \"%s\".";
  private static final String FOUND_MULTIPLE_GRADES_FOR_NAME = "Found multiple grades for name \"%s\".";
  private static final String DID_NOT_FIND_SITE_FOR_NAME = "Did not find site for name \"%s\".";
  private static final String FOUND_MULTIPLE_SITES_FOR_NAME = "Found multiple sites for name \"%s\".";
  private static final String DID_NOT_FIND_SPECIALTY_FOR_NAME = "Did not find specialty for name \"%s\".";
  private static final String FOUND_MULTIPLE_SPECIALTIES_FOR_NAME = "Found multiple specialties for name \"%s\".";
  private static final String GIVEN_POST_STATUS_IS_NOT_VALID = "Given post status is not valid.";
  private static final String GIVEN_OLD_POST_IS_NOT_VALID = "Given old post is not valid.";

  @Autowired
  private TcsServiceImpl tcsServiceImpl;
  @Autowired
  private ReferenceServiceImpl referenceServiceImpl;

  void processPostUpdateUpload(List<PostUpdateXLS> postUpdateXLSS, String username) {
    postUpdateXLSS.forEach(PostUpdateXLS::initialiseSuccessfullyImported);
    //This is where we need to extract the data from the Excel file and start building the business logic
    //to form the PostDTO so that it can be used to call the TCS's REST end point (i.e. /api/posts)
    //which is from TcsServiceImpl

    for (PostUpdateXLS postUpdateXLS : postUpdateXLSS) {
      useMatchingCriteriaToUpdatePost(postUpdateXLS, username);
    }

  }

  private void useMatchingCriteriaToUpdatePost(PostUpdateXLS postUpdateXLS, String username){
    //TIS_PostID* //Should match to one of TIS_PostID's
    if(!StringUtils.isEmpty(postUpdateXLS.getPostTISId())){
      //This getPostById() method is written in TCS service
      PostDTO dbPostDTO = tcsServiceImpl.getPostById(Long.valueOf(postUpdateXLS.getPostTISId()));
      if(dbPostDTO !=null){
        updatePost(postUpdateXLS, dbPostDTO, username);
      }

    }
  }

  private void updatePost(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO, String username){
    updateGrades(postUpdateXLS, dbPostDTO, referenceServiceImpl::findGradesByName);
    setSpecialties(postUpdateXLS, dbPostDTO, tcsServiceImpl::getSpecialtyByName);
    updateSites(postUpdateXLS, dbPostDTO, referenceServiceImpl::findSitesByName);
    updateTrainingDescription(postUpdateXLS, dbPostDTO);

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
      if (!org.apache.commons.lang3.StringUtils.isNumeric(oldPost)) {
        postUpdateXLS.addErrorMessage(GIVEN_OLD_POST_IS_NOT_VALID);
      } else {
        Long oldPostL = Long.valueOf(oldPost);
        if (oldPostL <= 0L) {
          postUpdateXLS.addErrorMessage(GIVEN_OLD_POST_IS_NOT_VALID);
        } else {
          PostDTO oldPostDTO = null;
          boolean ifException = false;
          try {
            oldPostDTO = tcsServiceImpl.getPostById(oldPostL);
            dbPostDTO.setOldPost(oldPostDTO);
          } catch (ResourceAccessException e) {
            postUpdateXLS.addErrorMessage(GIVEN_OLD_POST_IS_NOT_VALID);
            ifException = true;
          } finally {
            if (oldPostDTO == null && ifException == false) {
              postUpdateXLS.addErrorMessage(GIVEN_OLD_POST_IS_NOT_VALID);
            }
          }
        }
      }
    }

    if (!postUpdateXLS.hasErrors()) {
      //logger.info("dbPlacementDetailsDTO => {}", dbPlacementDetailsDTO);
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
    String[] otherGrades =  otherGradesCommaSeparated.split(",");
    for(String otherGrade : otherGrades) {
      Optional<PostGradeDTO> postGradeDTOOptional2 = buildPostGradeDTO(postUpdateXLS, dbPostDTO, getGradeDTOsForName, otherGrade, PostGradeType.OTHER);
      if (postGradeDTOOptional2.isPresent()) {
        PostGradeDTO postGradeDTO = postGradeDTOOptional2.get();
        addDTOIfNotPresentAsApprovedOrOther1(postGradeDTOS, postGradeDTO);
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
          postUpdateXLS.addErrorMessage(String.format(errorMessage, gradeByName));
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
      addDTOIfNotPresentAsPrimaryOrOther(postSpecialtyDTOS, postSpecialtyDTO);
    }
    String otherSpecialtiesCommaSeperated = postUpdateXLS.getOtherSpecialties();
    String[] otherSpecialties = otherSpecialtiesCommaSeperated.split(",");
    for(String otherSpecialty : otherSpecialties) {
      Optional<PostSpecialtyDTO> postSpecialtyDTOOptional2 = buildPostSpecialtyDTO(postUpdateXLS, dbPostDTO, getSpecialtyDTOsForName, otherSpecialty, PostSpecialtyType.OTHER);
      if (postSpecialtyDTOOptional2.isPresent()) {
        PostSpecialtyDTO postSpecialtyDTO = postSpecialtyDTOOptional2.get();
        addDTOIfNotPresentAsPrimaryOrOther(postSpecialtyDTOS, postSpecialtyDTO);
      }
    }
    String subSpecialtiesCommaSeperated = postUpdateXLS.getSubSpecialties();
    String[] subSpecialties =  subSpecialtiesCommaSeperated.split(",");
    for(String subSpecialty : subSpecialties) {
      Optional<PostSpecialtyDTO> postSpecialtyDTOOptional3 = buildPostSpecialtyDTO(postUpdateXLS, dbPostDTO, getSpecialtyDTOsForName, subSpecialty, PostSpecialtyType.SUB_SPECIALTY);
      if (postSpecialtyDTOOptional3.isPresent()) {
        PostSpecialtyDTO postSpecialtyDTO = postSpecialtyDTOOptional3.get();
        addDTOIfNotPresentAsPrimaryOrOther(postSpecialtyDTOS, postSpecialtyDTO);
      }
    }
  }

  private Set<PostSpecialtyDTO> initialiseNewPostSpecialtyDTOS(PostDTO dbPostDTO) {
    Set<PostSpecialtyDTO> postSpecialtyDTOS = new HashSet<>();
    dbPostDTO.setSpecialties(postSpecialtyDTOS);
    return postSpecialtyDTOS;
  }

  private void addDTOIfNotPresentAsPrimaryOrOther(Set<PostSpecialtyDTO> postSpecialtyDTOS,
      PostSpecialtyDTO postSpecialtyDTO) {
    if (postSpecialtyDTOS.isEmpty()) {
      postSpecialtyDTOS.add(postSpecialtyDTO);
    } else if (!postSpecialtyDTOS.contains(postSpecialtyDTO)) {
      postSpecialtyDTO.setPostSpecialtyType(PostSpecialtyType.OTHER);
      postSpecialtyDTOS.add(postSpecialtyDTO);
    }
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
          postUpdateXLS.addErrorMessage(String.format(errorMessage, specialtyByName));
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
      /*if (!StringUtils.isEmpty(dbPlacementDetailsDTO.getIntrepidId())) {
        placementXLS.addErrorMessage(INTREPID_ID_IS_ALREADY_EXISTS_FOR_THIS_RECORD_AND_IT_CAN_NOT_BE_UPDATED);
      } else {
        dbPlacementDetailsDTO.setIntrepidId(placementXLS.getIntrepidId());
      }*/
    }
  }
  /******************Training Description ends here*****************************/

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
    String[] otherSites =  otherSitesCommaSeperated.split(",");
    for(String otherSite : otherSites) {
      Optional<PostSiteDTO> postSiteDTOOptional2 = buildPostSiteDTO(postUpdateXLS, postDTO, getSiteDTOsForName, otherSite, PostSiteType.OTHER);
      if (postSiteDTOOptional2.isPresent()) {
        PostSiteDTO postSiteDTO = postSiteDTOOptional2.get();
        addDTOIfNotPresentAsPrimaryOrOther1(postSiteDTOS, postSiteDTO);
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
          postUpdateXLS.addErrorMessage(String.format(errorMessage, siteByName));
        }
      }
    }
    return Optional.empty();
  }
  /***************************Site ends here**********************************/

}
