package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;

import java.util.List;

public class PostUpdateTransformerService {

  void processPostUpdateUpload(List<PostUpdateXLS> postUpdateXLSS, String username) {
    postUpdateXLSS.forEach(PostUpdateXLS::initialiseSuccessfullyImported);
    //This is where we need to extract the data from the Excel file and start building the business logic
    //to form the PostDTO so that it can be used to call the TCS's REST end point (i.e. /api/posts)
    //which is from TcsServiceImpl
  }
}
