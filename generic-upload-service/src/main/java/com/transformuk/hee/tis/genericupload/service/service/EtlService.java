package com.transformuk.hee.tis.genericupload.service.service;

import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transformuk.hee.tis.client.ClientService;
import com.transformuk.hee.tis.client.impl.ServiceKey;
import com.transformuk.hee.tis.genericupload.service.config.StepConfiguration;
import com.transformuk.hee.tis.genericupload.service.config.steps.PeopleSteps;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EtlService {

  private static final Logger LOG = LoggerFactory.getLogger(EtlService.class);
  private static final String PAGEABLE_URL_PREFIX = "?size=";
  private static final String AUTO_INCREMENT_FIELD_NAME = "id";
  private static final String AMENDED_FIELD_NAME = "amendedDate";
  private static final Integer PAGE_SIZE = 10_000;
  private static final String ERROR_STORING_DATA_IN_CACHE = "Error storing data in cache";
  private Map<Class, String> classToIdFieldName = new HashMap<>();

  @Autowired
  private Cache<Class, Map<String, ServiceKey>> bulkServiceData;

  private Map<String, ClientService> clientServiceMap;

  @Autowired
  private ClientService tcsServiceImpl;

  @PostConstruct
  public void init() {
    clientServiceMap = Maps.newHashMap();
    clientServiceMap.put(PeopleSteps.TCS_CLIENT_SERVICE,tcsServiceImpl);
  }

  /**
   * Using the client, make a call to the service to get ALL of the entity data, with this data, place it into a
   * Map of primary key to entity and with that map, place it into a cached collection that will evict based
   * on last access time
   *
   * @param stepConfiguration The configuration for this particular step
   * @param dtoClass          The class type of the items
   * @return The results of the get request
   * @throws IOException
   */
  public Map<String, ServiceKey> getServiceData(StepConfiguration stepConfiguration, Class dtoClass) throws IOException {
    LOG.info("Getting all data for [{}] type from endpoint {}", dtoClass.getSimpleName(), stepConfiguration.getGetEndpointUrl());
    String pageableUrl = PAGEABLE_URL_PREFIX + PAGE_SIZE;
    final ClientService clientService = clientServiceMap.get(stepConfiguration.getServiceClientBeanName());
    final String idFieldName = (classToIdFieldName.containsKey(dtoClass)) ? classToIdFieldName.get(dtoClass) : AUTO_INCREMENT_FIELD_NAME;
    return clientService.getAllDto(stepConfiguration.getDtoPKFieldNames(), idFieldName, stepConfiguration.getGetEndpointUrl(), pageableUrl, dtoClass);
  }

  public Map<String, ServiceKey> getBulkServiceData(StepConfiguration stepConfiguration, Class dtoClass) {
    Map<String, ServiceKey> cacheServiceData = new HashMap<>();
    if (bulkServiceData.getIfPresent(dtoClass) != null) {
      cacheServiceData = bulkServiceData.getIfPresent(dtoClass);
    } else {
      try {
        cacheServiceData = getServiceData(stepConfiguration, dtoClass);
        bulkServiceData.put(dtoClass, cacheServiceData);
      } catch (IOException e) {
        LOG.error("Error while retrieving data {}" + e, stepConfiguration.getDtoClassName());
      }
    }
    return cacheServiceData;
  }


  /**
   * Using the configured client, with bulk send the data or send it individually
   *
   * @param stepConfiguration The object that stores all the configuration for this step
   * @param dtoClass          The class of the item type
   * @param newItems          List of new items to be HTTP POST'ed
   * @param updateItems       List of items to be HTTP PUT
   */
  public void sendData(StepConfiguration stepConfiguration, Class dtoClass, List<Object> newItems, List<Object> updateItems) {
    final ClientService clientService = clientServiceMap.get(stepConfiguration.getServiceClientBeanName());

    Map<String, ServiceKey> cachedBulkServiceData = getBulkServiceData(stepConfiguration, dtoClass);
    if (CollectionUtils.isEmpty(cachedBulkServiceData)) {
      cachedBulkServiceData = new HashMap<>();
    }

    switch (stepConfiguration.getAction()) {
      case BULK_CREATE_UPDATE:
        bulkCreateOrUpdateData(stepConfiguration, dtoClass, newItems, updateItems, clientService, cachedBulkServiceData);
        break;
      case PATCH:
        bulkPatchData(stepConfiguration, dtoClass, newItems, updateItems, clientService, cachedBulkServiceData);
        break;
      case CREATE_UPDATE:
        createOrUpdateData(stepConfiguration, dtoClass, newItems, updateItems, clientService, cachedBulkServiceData);
        break;
    }
  }

  private void createOrUpdateData(StepConfiguration stepConfiguration, Class dtoClass, List<Object> newItems,
                                  List<Object> updateItems, ClientService clientService, Map<String, ServiceKey> cachedBulkServiceData) {
    List<Object> newDataList = new ArrayList<>();
    List<Object> updateDataList = new ArrayList<>();
    for (Object newItem : newItems) {
      try {
        Object newData = clientService.createDto(newItem, stepConfiguration.getCreateUpdateEndpointUrl(), dtoClass);
        newDataList.add(newData);
      } catch (Exception e) {
        //deadLetterService.placeOnDeadLetterQueue(newItem, "CREATE", e);
      }
    }
    try {
      storePrimaryKeyToServiceIdMap(stepConfiguration, dtoClass, cachedBulkServiceData, newDataList);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      LOG.error(ERROR_STORING_DATA_IN_CACHE + e);
    }

    for (Object updateItem : updateItems) {
      try {
        Object updateData = clientService.updateDto(updateItem, stepConfiguration.getCreateUpdateEndpointUrl(), dtoClass);
        updateDataList.add(updateData);
      } catch (Exception e) {
        //deadLetterService.placeOnDeadLetterQueue(updateItem, "UPDATE", e);
      }
    }
    try {
      storePrimaryKeyToServiceIdMap(stepConfiguration, dtoClass, cachedBulkServiceData, updateDataList);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      LOG.error(ERROR_STORING_DATA_IN_CACHE + e);
    }
  }


  private void bulkCreateOrUpdateData(StepConfiguration stepConfiguration, Class dtoClass, List<Object> newItems,
                                      List<Object> updateItems, ClientService clientService, Map<String, ServiceKey> cachedBulkServiceData) {
    List<Object> newItemsDataList = Lists.newArrayList();
    try {
      if (!CollectionUtils.isEmpty(newItems)) {
        LOG.info("Sending data to BULK CREATE [{}] items to endpoint [{}]", newItems.size(), stepConfiguration.getCreateUpdateEndpointUrl());
        newItemsDataList = clientService.bulkCreateDto(newItems, stepConfiguration.getCreateUpdateEndpointUrl(), dtoClass);
      }
    } catch (Exception e) {
      //deadLetterService.placeOnDeadLetterQueue(newItems, "BULK-CREATE", e);
    }

    try {
      storePrimaryKeyToServiceIdMap(stepConfiguration, dtoClass, cachedBulkServiceData, newItemsDataList);
    } catch (Exception e) {
      LOG.error(ERROR_STORING_DATA_IN_CACHE + e);
    }

    List<Object> updateItemsDataList = Lists.newArrayList();
    try {
      if (!CollectionUtils.isEmpty(updateItems)) {
        LOG.info("Sending data to BULK UPDATE [{}] items to endpoint [{}]", updateItems.size(), stepConfiguration.getCreateUpdateEndpointUrl());
        updateItemsDataList = clientService.bulkUpdateDto(updateItems, stepConfiguration.getCreateUpdateEndpointUrl(), dtoClass);
      }
    } catch (Exception e) {
      //deadLetterService.placeOnDeadLetterQueue(updateItems, "BULK-UPDATE", e);
      LOG.error("Exception occored " + e);
    }

    try {
      storePrimaryKeyToServiceIdMap(stepConfiguration, dtoClass, cachedBulkServiceData, updateItemsDataList);
    } catch (Exception e) {
      LOG.error(ERROR_STORING_DATA_IN_CACHE + e);
    }
  }

  private void bulkPatchData(StepConfiguration stepConfiguration, Class dtoClass, List<Object> newItems,
                             List<Object> updateItems, ClientService clientService, Map<String, ServiceKey> cachedBulkServiceData) {
    List<Object> dataList = Lists.newArrayList();
    try {
      List<Object> allItems = Lists.newArrayList();
      allItems.addAll(newItems);
      allItems.addAll(updateItems);
      if (!CollectionUtils.isEmpty(allItems)) {
        LOG.info("Sending data to BULK PATCH [{}] items to endpoint [{}]", allItems.size(), stepConfiguration.getCreateUpdateEndpointUrl());
        dataList = clientService.bulkPatchDto(allItems, stepConfiguration.getCreateUpdateEndpointUrl(), dtoClass);
      }
    } catch (Exception e) {
      //deadLetterService.placeOnDeadLetterQueue(updateItems, "BULK-PATCH", e);
      LOG.error("Exception occurred " + e);
    }

    try {
      storePrimaryKeyToServiceIdMap(stepConfiguration, dtoClass, cachedBulkServiceData, dataList);
    } catch (Exception e) {
      LOG.error(ERROR_STORING_DATA_IN_CACHE + e);
    }
  }

  /**
   * Method to cache the map of intrepidId/name reference key with auto increment Id after bulk insert/update
   *
   * @param dtoClass
   * @param cachedBulkServiceData
   * @param dataList
   * @throws NoSuchFieldException`
   * @throws IllegalAccessException
   */
  private void storePrimaryKeyToServiceIdMap(StepConfiguration stepConfiguration, Class dtoClass,
                                             Map<String, ServiceKey> cachedBulkServiceData, List<Object> dataList)
          throws NoSuchFieldException, IllegalAccessException {

    List<Field> dtoPKFields = getFieldsForEntity(stepConfiguration, dtoClass);
    final Field idField;
    if (classToIdFieldName.containsKey(dtoClass)) {
      idField = dtoClass.getDeclaredField(classToIdFieldName.get(dtoClass));
    } else {
      idField = dtoClass.getDeclaredField(AUTO_INCREMENT_FIELD_NAME);
    }
    final Field amendedField = getAmendedDateFieldIfExists(dtoClass);
    if (idField != null) {
      idField.setAccessible(true);
    }
    if (amendedField != null) {
      amendedField.setAccessible(true);
    }
    for (Object data : dataList) {
      String primaryKey = generatePrimaryKeyFromFields(stepConfiguration, dtoPKFields, data);
      if (primaryKey != null && idField != null) {
        ServiceKey serviceKey = new ServiceKey();
        if (idField.get(data) instanceof Number) {
          serviceKey.setId(idField.get(data).toString());
        } else {
          serviceKey.setId((String) idField.get(data));
        }
        if (amendedField != null) {
          serviceKey.setAmendedDate((LocalDateTime) amendedField.get(data));
        }
        cachedBulkServiceData.put(primaryKey, serviceKey);
      }
    }
    bulkServiceData.put(dtoClass, cachedBulkServiceData);
  }

  /**
   * If field exists in DTO then return the field otherwise null
   *
   * @param dtoClass
   * @return
   */
  private Field getAmendedDateFieldIfExists(Class dtoClass) {
    Field amendedDate = null;
    try {
      amendedDate = dtoClass.getDeclaredField(AMENDED_FIELD_NAME);
    } catch (NoSuchFieldException e) {
      //LOG.info("AmendedDate Field is not present in DTO {}", dtoClass.getSimpleName());
    }
    return amendedDate;
  }


  /**
   * Place all predicates the test whether an object is new here
   * <p>
   * This method checks equality by using a list of fields as a key. This can be a single field (PK) or
   * a list of fields (Compound key). These fields will be converted to a string to be used as the key in the map of
   * data from the service
   *
   * @param objectDto         The object that will be sent to the service as a create or update
   * @param stepConfiguration The configuration for this particular entity
   * @throws IllegalAccessException Thrown when no id field
   * @throws NoSuchFieldException   Thrown when no id field
   */
  public boolean isNewEntity(Object objectDto, StepConfiguration stepConfiguration, Map<String, ServiceKey> bulkServiceDataMap)
          throws IllegalAccessException, NoSuchFieldException {

    final Class dtoClass = stepConfiguration.getDtoClassName();

    List<Field> fieldsForEntity = getFieldsForEntity(stepConfiguration, dtoClass);
    String primaryKey = generatePrimaryKeyFromFields(stepConfiguration, fieldsForEntity, objectDto);

    boolean serviceHasSameData = bulkServiceDataMap.containsKey(primaryKey);
    if (serviceHasSameData) {
      Object serviceData = bulkServiceDataMap.get(primaryKey);

      Field autoIdField = null;
      if (classToIdFieldName.containsKey(stepConfiguration.getDtoClassName())) {
        String dtoClassIdField = classToIdFieldName.get(stepConfiguration.getDtoClassName());
        autoIdField = dtoClass.getDeclaredField(dtoClassIdField);
        autoIdField.setAccessible(true);
        autoIdField.set(objectDto, ((ServiceKey) serviceData).getId());
      } else {
        autoIdField = dtoClass.getDeclaredField(AUTO_INCREMENT_FIELD_NAME);
        autoIdField.setAccessible(true);
        String id = ((ServiceKey) serviceData).getId();
        autoIdField.set(objectDto, Long.parseLong(id));
      }
      final Field amendedDateField = getAmendedDateFieldIfExists(dtoClass);
      if (amendedDateField != null) {
        amendedDateField.setAccessible(true);
        amendedDateField.set(objectDto, ((ServiceKey) serviceData).getAmendedDate());
      }

    }
    return !serviceHasSameData;
  }


  /**
   * For a step configuration, return a List of @see Field that the step was configured for
   *
   * @param stepConfiguration
   * @param dtoClass
   * @return
   * @throws NoSuchFieldException
   */
  public List<Field> getFieldsForEntity(StepConfiguration stepConfiguration, Class dtoClass) throws NoSuchFieldException {
    List<String> dtoPKFieldNames = stepConfiguration.getDtoPKFieldNames();
    List<Field> dtoPKFields = Lists.newArrayList();
    for (String field : dtoPKFieldNames) {
      final Field idField = dtoClass.getDeclaredField(field);
      if (idField != null) {
        idField.setAccessible(true);
        dtoPKFields.add(idField);
      }
    }
    return dtoPKFields;
  }


  /**
   * Generate a key using a list of field
   * Note: primary key fields values must be set before calling the method
   *
   * @param stepConfiguration
   * @param dtoPKFields
   * @param data
   * @return
   */
  public String generatePrimaryKeyFromFields(StepConfiguration stepConfiguration, List<Field> dtoPKFields, Object data) {
    StringBuilder concatedFieldValues = new StringBuilder();

    for (Field field : dtoPKFields) {
      try {
        Object fieldValue = field.get(data);
        if (fieldValue != null) {
          concatedFieldValues.append(generatePrimaryKeyFromFieldValue(fieldValue));
        }
      } catch (IllegalAccessException iae) {
        LOG.warn("cannot access field [{}] from object type [{}]" + iae, field.getName(), stepConfiguration.getStepName());
      }

    }
    return concatedFieldValues.toString();
  }

  public String generatePrimaryKeyFromFieldValue(Object fieldValue) {
    String result = StringUtils.EMPTY;
    if (fieldValue != null) {
      if (fieldValue instanceof Number) {
        result = String.valueOf(fieldValue);
      } else if (fieldValue instanceof String) {
        result = ((String) fieldValue).toLowerCase(Locale.ENGLISH);
      } else if (fieldValue instanceof LocalDate) {
        long timeInLong = ((LocalDate) fieldValue).toEpochDay();
        result = String.valueOf(timeInLong);
      } else {
        result = fieldValue.toString();
      }
    }
    return result;
  }
}
