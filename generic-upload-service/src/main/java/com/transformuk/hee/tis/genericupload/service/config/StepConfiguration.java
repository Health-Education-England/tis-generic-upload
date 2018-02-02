package com.transformuk.hee.tis.genericupload.service.config;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Class that holds the instances of the steps defined in the application.yml file
 * <p>
 * Each StepConfiguration is for each view/entity and contains a query, a class name to convert the resultset to
 * a dto class that data is converted to again, as well as processors and writers
 * <p>
 * You can have multiple steps per entity, this is especially for cases where data can come from multiple sources
 */
public class StepConfiguration {

  public static final String FROM = "from";
  private String stepName;
  private boolean enabled;
  private String query;
  private Class viewClassName;
  private String mapper;
  private Class dtoClassName;
  private String postMapperProcessor;
  private String writerKeyName;
  private List<String> dtoPKFieldNames;
  private String getEndpointUrl;
  private String createUpdateEndpointUrl;
  private String serviceClientBeanName;
  private Action action;
  private String sortKey;

  public String getStepName() {
    return stepName;
  }

  public void setStepName(String stepName) {
    this.stepName = stepName;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public Class getViewClassName() {
    return viewClassName;
  }

  public void setViewClass(Class viewClassName) {
    this.viewClassName = viewClassName;
  }

  public String getMapper() {
    return mapper;
  }

  public void setMapper(String mapper) {
    this.mapper = mapper;
  }

  public Class getDtoClassName() {
    return dtoClassName;
  }

  public void setDtoClass(Class dtoClassName) {
    this.dtoClassName = dtoClassName;
  }

  public String getPostMapperProcessor() {
    return postMapperProcessor;
  }

  public void setPostMapperProcessor(String postMapperProcessor) {
    this.postMapperProcessor = postMapperProcessor;
  }

  public String getWriterKeyName() {
    return writerKeyName;
  }

  public void setWriterKeyName(String writerKeyName) {
    this.writerKeyName = writerKeyName;
  }

  public List<String> getDtoPKFieldNames() {
    return dtoPKFieldNames;
  }

  public void setDtoPKFieldNames(List<String> dtoPKFieldNames) {
    this.dtoPKFieldNames = dtoPKFieldNames;
  }

  public String getGetEndpointUrl() {
    return getEndpointUrl;
  }

  public void setGetEndpointUrl(String getEndpointUrl) {
    this.getEndpointUrl = getEndpointUrl;
  }

  public String getCreateUpdateEndpointUrl() {
    return createUpdateEndpointUrl;
  }

  public void setCreateUpdateEndpointUrl(String createUpdateEndpointUrl) {
    this.createUpdateEndpointUrl = createUpdateEndpointUrl;
  }

  public String getServiceClientBeanName() {
    return serviceClientBeanName;
  }

  public void setServiceClientBeanName(String serviceClientBeanName) {
    this.serviceClientBeanName = serviceClientBeanName;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public String getSelectClause() {
    if (StringUtils.isNotEmpty(query)) {
      return query.substring(0, StringUtils.lastIndexOfIgnoreCase(query, FROM));
    }
    return null;
  }

  public String getSortKey() {
    return sortKey;
  }

  public void setSortKey(String sortKey) {
    this.sortKey = sortKey;
  }

  public enum Action {
    CREATE_UPDATE, BULK_CREATE_UPDATE, PATCH
  }
}
