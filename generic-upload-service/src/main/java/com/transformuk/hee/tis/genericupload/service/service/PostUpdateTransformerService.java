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
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Component
public class PostUpdateTransformerService {

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
    if(!StringUtils.isEmpty(postUpdateXLS.getPostTISTd())){
      //This getPostById() method is written in TCS service
      PostDTO dbPostDTO = tcsServiceImpl.getPostById(Long.valueOf(postUpdateXLS.getPostTISTd()));
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

    if (!postUpdateXLS.hasErrors()) {
      //logger.info("dbPlacementDetailsDTO => {}", dbPlacementDetailsDTO);
      tcsServiceImpl.updatePost(dbPostDTO);// updatePost() method is written in TCS service
      postUpdateXLS.setSuccessfullyImported(true);
    }

  }

  /*********************Grade starts here*******************************/
  public void updateGrades(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO, Function<String, List<GradeDTO>> getGradeDTOsForName) {
    Set<PostGradeDTO> postGradeDTOS = dbPostDTO.getGrades();
    if (postGradeDTOS == null) {
      postGradeDTOS = initialiseNewPostGradeDTOS(dbPostDTO);
    }
    Optional<PostGradeDTO> postGradeDTOOptional1 = buildPostGradeDTO(postUpdateXLS, dbPostDTO, getGradeDTOsForName, postUpdateXLS.getApprovedGrade(), true);
    if (postGradeDTOOptional1.isPresent()) {
      postGradeDTOS = initialiseNewPostGradeDTOS(dbPostDTO);
      PostGradeDTO postGradeDTO = postGradeDTOOptional1.get();
      addDTOIfNotPresentAsApprovedOrOther1(postGradeDTOS, postGradeDTO);
    }
    String otherGradesCommaSeparated = postUpdateXLS.getOtherGrades();
    String otherGrades[] =  otherGradesCommaSeparated.split(",");
    for(String otherGrade : otherGrades) {
      Optional<PostGradeDTO> postGradeDTOOptional2 = buildPostGradeDTO(postUpdateXLS, dbPostDTO, getGradeDTOsForName, otherGrade, false);
      if (postGradeDTOOptional2.isPresent()) {
        PostGradeDTO postGradeDTO = postGradeDTOOptional2.get();
        addDTOIfNotPresentAsApprovedOrOther1(postGradeDTOS, postGradeDTO);
      }
    }
  }

  public Set<PostGradeDTO> initialiseNewPostGradeDTOS(PostDTO dbPostDTO) {
    Set<PostGradeDTO> postGradeDTOS = new HashSet<>();
    dbPostDTO.setGrades(postGradeDTOS);
    return postGradeDTOS;
  }

  public void addDTOIfNotPresentAsApprovedOrOther1(Set<PostGradeDTO> postGradeDTOS, PostGradeDTO postGradeDTO) {
    if (postGradeDTOS.isEmpty()) {
      postGradeDTOS.add(postGradeDTO);
    } else if (!postGradeDTOS.contains(postGradeDTO)) {
      postGradeDTO.setPostGradeType(PostGradeType.OTHER);
      postGradeDTOS.add(postGradeDTO);
    }
  }

  public Optional<PostGradeDTO> buildPostGradeDTO(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO,
                                                Function<String, List<GradeDTO>> getGradeDTOsForName,
                                                String gradeName, boolean approved) {
    Optional<GradeDTO> aSingleValidGrade = getASingleValidGradeFromTheReferenceService(postUpdateXLS, getGradeDTOsForName, gradeName);
    if (aSingleValidGrade.isPresent()) {
      GradeDTO gradeDTO = aSingleValidGrade.get();
      PostGradeDTO postGradeDTO = new PostGradeDTO();
      postGradeDTO.setPostId(dbPostDTO.getId());
      postGradeDTO.setGradeId(gradeDTO.getId());
      postGradeDTO.setPostGradeType(approved ? PostGradeType.APPROVED : PostGradeType.OTHER);
      return Optional.of(postGradeDTO);
    }
    return Optional.empty();
  }

  private Optional<GradeDTO> getASingleValidGradeFromTheReferenceService(PostUpdateXLS placementXLS, Function<String, List<GradeDTO>> getGradeDTOsForName, String gradeName) {
    if (!StringUtils.isEmpty(gradeName)) {
      List<GradeDTO> gradeByName = getGradeDTOsForName.apply(gradeName);
      if (gradeByName != null) {
        if (gradeByName.size() != 1) {
          if (gradeByName.isEmpty()) {
            //placementXLS.addErrorMessage(DID_NOT_FIND_SPECIALTY_FOR_NAME + specialtyName);
          } else {
            //placementXLS.addErrorMessage(FOUND_MULTIPLE_SPECIALTIES_FOR_NAME + specialtyName);
          }
        } else {
          return Optional.of(gradeByName.get(0));
        }
      }
    }
    return Optional.empty();
  }
  /*********************Grade ends here*******************************/

  /*********************specialty starts here*******************************/
  public void setSpecialties(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO, Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName) {
    Set<PostSpecialtyDTO> postSpecialtyDTOS = dbPostDTO.getSpecialties();
    if (postSpecialtyDTOS == null) {
      postSpecialtyDTOS = initialiseNewPostSpecialtyDTOS(dbPostDTO);
    }
    Optional<PostSpecialtyDTO> postSpecialtyDTOOptional1 = buildPostSpecialtyDTO(postUpdateXLS, dbPostDTO, getSpecialtyDTOsForName, postUpdateXLS.getSpecialty(), true);
    if (postSpecialtyDTOOptional1.isPresent()) {
      postSpecialtyDTOS = initialiseNewPostSpecialtyDTOS(dbPostDTO);
      PostSpecialtyDTO postSpecialtyDTO = postSpecialtyDTOOptional1.get();
      addDTOIfNotPresentAsPrimaryOrOther(postSpecialtyDTOS, postSpecialtyDTO);
    }
    String otherSpecialtiesCommaSeperated = postUpdateXLS.getOtherSpecialties();
    String otherSpecialties[] =  otherSpecialtiesCommaSeperated.split(",");
    for(String otherSpecialty : otherSpecialties) {
      Optional<PostSpecialtyDTO> postSpecialtyDTOOptional2 = buildPostSpecialtyDTO(postUpdateXLS, dbPostDTO, getSpecialtyDTOsForName, otherSpecialty, false);
      if (postSpecialtyDTOOptional2.isPresent()) {
        PostSpecialtyDTO postSpecialtyDTO = postSpecialtyDTOOptional2.get();
        addDTOIfNotPresentAsPrimaryOrOther(postSpecialtyDTOS, postSpecialtyDTO);
      }
    }
    String subSpecialtiesCommaSeperated = postUpdateXLS.getSubSpecialties();
    String subSpecialties[] =  subSpecialtiesCommaSeperated.split(",");
    for(String subSpecialty : subSpecialties) {
      Optional<PostSpecialtyDTO> postSpecialtyDTOOptional3 = buildPostSpecialtyDTO(postUpdateXLS, dbPostDTO, getSpecialtyDTOsForName, subSpecialty, false);
      if (postSpecialtyDTOOptional3.isPresent()) {
        PostSpecialtyDTO postSpecialtyDTO = postSpecialtyDTOOptional3.get();
        addDTOIfNotPresentAsPrimaryOrOther(postSpecialtyDTOS, postSpecialtyDTO);
      }
    }
  }

  public Set<PostSpecialtyDTO> initialiseNewPostSpecialtyDTOS(PostDTO dbPostDTO) {
    Set<PostSpecialtyDTO> postSpecialtyDTOS = new HashSet<>();
    dbPostDTO.setSpecialties(postSpecialtyDTOS);
    return postSpecialtyDTOS;
  }

  public void addDTOIfNotPresentAsPrimaryOrOther(Set<PostSpecialtyDTO> postSpecialtyDTOS, PostSpecialtyDTO postSpecialtyDTO) {
    if (postSpecialtyDTOS.isEmpty()) {
      postSpecialtyDTOS.add(postSpecialtyDTO);
    } else if (!postSpecialtyDTOS.contains(postSpecialtyDTO)) {
      postSpecialtyDTO.setPostSpecialtyType(PostSpecialtyType.OTHER);
      postSpecialtyDTOS.add(postSpecialtyDTO);
    }
  }

  public Optional<PostSpecialtyDTO> buildPostSpecialtyDTO(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO,
                                                                    Function<String, List<SpecialtyDTO>> getSpecialtyDTOsForName,
                                                                    String specialtyName, boolean primary) {
    Optional<SpecialtyDTO> aSingleValidSpecialty = getASingleValidSpecialtyFromTheReferenceService(postUpdateXLS, getSpecialtyDTOsForName, specialtyName);
    if (aSingleValidSpecialty.isPresent()) {
      SpecialtyDTO specialtyDTO = aSingleValidSpecialty.get();
      PostSpecialtyDTO postSpecialtyDTO = new PostSpecialtyDTO();
      postSpecialtyDTO.setPostId(dbPostDTO.getId());
      postSpecialtyDTO.setSpecialty(specialtyDTO);
      postSpecialtyDTO.setPostSpecialtyType(PostSpecialtyType.PRIMARY);//We need to check this for updating the SUB_SPECIALTY// Probably an int would be more fit
      //rather than boolean for example 1 for PRIMARY, 2 for OTHER and 3 for SUB_SPECIALTY
      //placementSpecialtyDTO.setPlacementSpecialtyType(primary ? PostSpecialtyType.PRIMARY : PostSpecialtyType.OTHER);
      return Optional.of(postSpecialtyDTO);
    }
    return Optional.empty();
  }

  private Optional<SpecialtyDTO> getASingleValidSpecialtyFromTheReferenceService(PostUpdateXLS postUpdateXLS, Function<String,
      List<SpecialtyDTO>> getSpecialtyDTOsForName, String specialtyName) {
    if (!StringUtils.isEmpty(specialtyName)) {
      List<SpecialtyDTO> specialtyByName = getSpecialtyDTOsForName.apply(specialtyName);
      if (specialtyByName != null) {
        if (specialtyByName.size() != 1) {
          /*if (specialtyByName.isEmpty()) {
            postUpdateXLS.addErrorMessage(DID_NOT_FIND_SPECIALTY_FOR_NAME + specialtyName);
          } else {
            postUpdateXLS.addErrorMessage(FOUND_MULTIPLE_SPECIALTIES_FOR_NAME + specialtyName);
          }*/
        } else {
          return Optional.of(specialtyByName.get(0));
        }
      }
    }
    return Optional.empty();
  }

  /****************************Specialty ends here****************************/


  /**************************Training Description starts here*******************/
  public void updateTrainingDescription(PostUpdateXLS postUpdateXLS, PostDTO dbPostDTO) {
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
  public void updateSites(PostUpdateXLS postUpdateXLS, PostDTO postDTO, Function<String, List<SiteDTO>> getSiteDTOsForName) {
    Set<PostSiteDTO> postSiteDTOS = postDTO.getSites();
    if (postSiteDTOS == null) {
      postSiteDTOS = initialiseNewPostSiteDTOS(postDTO);
    }
    Optional<PostSiteDTO> postSiteDTOOptional1 = buildPostSiteDTO(postUpdateXLS, postDTO, getSiteDTOsForName, postUpdateXLS.getMainSite(), true);
    if (postSiteDTOOptional1.isPresent()) {
      postSiteDTOS = initialiseNewPostSiteDTOS(postDTO);
      PostSiteDTO postSiteDTO = postSiteDTOOptional1.get();
      addDTOIfNotPresentAsPrimaryOrOther1(postSiteDTOS, postSiteDTO);
    }
    String otherSitesCommaSeperated = postUpdateXLS.getOtherSites();
    String otherSites[] =  otherSitesCommaSeperated.split(",");
    for(String otherSite : otherSites) {
      Optional<PostSiteDTO> postSiteDTOOptional2 = buildPostSiteDTO(postUpdateXLS, postDTO, getSiteDTOsForName, otherSite, false);
      if (postSiteDTOOptional2.isPresent()) {
        PostSiteDTO postSiteDTO = postSiteDTOOptional2.get();
        addDTOIfNotPresentAsPrimaryOrOther1(postSiteDTOS, postSiteDTO);
      }
    }
  }

  public Set<PostSiteDTO> initialiseNewPostSiteDTOS(PostDTO postDTO) {
    Set<PostSiteDTO> postSiteDTOS = new HashSet<>();
    postDTO.setSites(postSiteDTOS);
    return postSiteDTOS;
  }

  public void addDTOIfNotPresentAsPrimaryOrOther1(Set<PostSiteDTO> postSiteDTOS, PostSiteDTO postSiteDTO) {
    if (postSiteDTOS.isEmpty()) {
      postSiteDTOS.add(postSiteDTO);
    } else if (!postSiteDTOS.contains(postSiteDTO)) {
      postSiteDTO.setPostSiteType(PostSiteType.OTHER);
      postSiteDTOS.add(postSiteDTO);
    }
  }

  public Optional<PostSiteDTO> buildPostSiteDTO(PostUpdateXLS postUpdateXLS, PostDTO postDTO,
                                                                    Function<String, List<SiteDTO>> getSiteDTOsForName,
                                                                    String siteName, boolean primary) {
    Optional<SiteDTO> aSingleValidSite = getASingleValidSiteFromTheReferenceService(postUpdateXLS, getSiteDTOsForName, siteName);
    if (aSingleValidSite.isPresent()) {
      SiteDTO siteDTO = aSingleValidSite.get();
      PostSiteDTO postSiteDTO = new PostSiteDTO();
      postSiteDTO.setPostId(postDTO.getId());
      postSiteDTO.setSiteId(siteDTO.getId());
      postSiteDTO.setPostSiteType(primary ? PostSiteType.PRIMARY : PostSiteType.OTHER);
      return Optional.of(postSiteDTO);
    }
    return Optional.empty();
  }

  private Optional<SiteDTO> getASingleValidSiteFromTheReferenceService(PostUpdateXLS placementXLS, Function<String,
      List<SiteDTO>> getSiteDTOsForName, String siteName) {
    if (!StringUtils.isEmpty(siteName)) {
      List<SiteDTO> siteByName = getSiteDTOsForName.apply(siteName);
      if (siteByName != null) {
        if (siteByName.size() != 1) {
          if (siteByName.isEmpty()) {
            //placementXLS.addErrorMessage(DID_NOT_FIND_SPECIALTY_FOR_NAME + specialtyName);
          } else {
            //placementXLS.addErrorMessage(FOUND_MULTIPLE_SPECIALTIES_FOR_NAME + specialtyName);
          }
        } else {
          return Optional.of(siteByName.get(0));
        }
      }
    }
    return Optional.empty();
  }
  /***************************Site ends here**********************************/

}
