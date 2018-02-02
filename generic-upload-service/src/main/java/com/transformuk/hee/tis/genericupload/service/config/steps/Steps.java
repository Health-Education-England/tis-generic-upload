package com.transformuk.hee.tis.genericupload.service.config.steps;

import com.transformuk.hee.tis.genericupload.service.config.StepConfiguration;

import java.util.List;

public abstract class Steps {

  protected static final String INTREPID_ID = "intrepidId";
  protected static final String AUTO_ID = "id";

  public static StepConfiguration createStepConfiguration(String stepName, boolean enabled, String mapper, Class dtoClass,
                                                          List<String> dtoPKFieldNames, String getEndpointUrl,
                                                          String createUpdateEndpointUrl,
                                                          String serviceClientBeanName,
                                                          StepConfiguration.Action action) {

    StepConfiguration stepConfiguration = new StepConfiguration();
    stepConfiguration.setStepName(stepName);
    stepConfiguration.setEnabled(enabled);
    stepConfiguration.setMapper(mapper);
    stepConfiguration.setDtoClass(dtoClass);
    stepConfiguration.setDtoPKFieldNames(dtoPKFieldNames);
    stepConfiguration.setGetEndpointUrl(getEndpointUrl);
    stepConfiguration.setCreateUpdateEndpointUrl(createUpdateEndpointUrl);
    stepConfiguration.setServiceClientBeanName(serviceClientBeanName);
    stepConfiguration.setAction(action);
    return stepConfiguration;
  }

  public abstract void add(List<StepConfiguration> steps);


}
