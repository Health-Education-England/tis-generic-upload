package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PersonUpdateTransformerService {

  public void processUpload(List<PersonUpdateXls> xlsList) {
    xlsList.forEach(xls -> {
      xls.initialiseSuccessfullyImported();
      xls.addErrorMessage("Not yet implemented.");
    });
  }
}
