package com.transformuk.hee.tis.genericupload.service.config.steps;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.genericupload.service.config.StepConfiguration;
import com.transformuk.hee.tis.genericupload.service.config.StepConfiguration.Action;
import com.transformuk.hee.tis.tcs.api.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PeopleSteps extends Steps {

  public static final String TCS_CLIENT_SERVICE = "tcsServiceImpl";
  public static final String PERSON_TCS_STEP = "PersonStep";
  public static final String CONTACT_DETAIL_TCS_STEP = "ContactDetailStep";
  public static final String PERSONAL_DETAIL_TCS_STEP = "PersonalDetailStep";
  public static final String GMC_DETAIL_TCS_STEP = "GmcDetailStep";
  public static final String GDC_DETAIL_TCS_STEP = "GdcDetailStep";
  public static final String RIGHT_TO_WORK_TCS_STEP = "RightToWorkStep";
  public static final String QUALIFICATION_TCS_STEP = "QualificationStep";

  @Override
  public void add(List<StepConfiguration> steps) {
    steps.add(createStepConfiguration(PERSON_TCS_STEP, true, "personMapper", PersonDTO.class,
            Lists.newArrayList(INTREPID_ID), "/api/people", "/api/people",
            TCS_CLIENT_SERVICE, Action.PATCH));

    steps.add(createStepConfiguration(CONTACT_DETAIL_TCS_STEP, true, "contactDetailMapper", ContactDetailsDTO.class,
            Lists.newArrayList(AUTO_ID), "/api/contact-details", "/api/contact-details",
            TCS_CLIENT_SERVICE, Action.PATCH));

    steps.add(createStepConfiguration(PERSONAL_DETAIL_TCS_STEP, true, "personalDetailMapper", PersonalDetailsDTO.class,
            Lists.newArrayList(AUTO_ID), "/api/personal-details", "/api/personal-details",
            TCS_CLIENT_SERVICE, Action.PATCH));

    steps.add(createStepConfiguration(GMC_DETAIL_TCS_STEP, true, "gmcDetailMapper", GmcDetailsDTO.class,
            Lists.newArrayList(AUTO_ID), "/api/gmc-details", "/api/gmc-details",
            TCS_CLIENT_SERVICE, Action.PATCH));

    steps.add(createStepConfiguration(GDC_DETAIL_TCS_STEP, true, "gdcDetailMapper", GdcDetailsDTO.class,
            Lists.newArrayList(AUTO_ID), "/api/gdc-details", "/api/gdc-details",
            TCS_CLIENT_SERVICE, Action.PATCH));

    steps.add(createStepConfiguration(RIGHT_TO_WORK_TCS_STEP, true, "rightToWorkMapper", RightToWorkDTO.class,
            Lists.newArrayList(AUTO_ID), "/api/right-to-works", "/api/right-to-works",
            TCS_CLIENT_SERVICE, Action.PATCH));

    steps.add(createStepConfiguration(QUALIFICATION_TCS_STEP, true, "qualificationMapper", QualificationDTO.class,
            Lists.newArrayList(INTREPID_ID), "/api/qualifications", "/api/qualifications",
            TCS_CLIENT_SERVICE, Action.PATCH));

  }
}
