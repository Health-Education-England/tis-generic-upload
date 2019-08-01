package com.transformuk.hee.tis.genericupload.api.dto;

public interface PlacementSupervisor {

  String getClinicalSupervisor();

  String getEducationalSupervisor();

  void addErrorMessage(String errorMessage);
}
