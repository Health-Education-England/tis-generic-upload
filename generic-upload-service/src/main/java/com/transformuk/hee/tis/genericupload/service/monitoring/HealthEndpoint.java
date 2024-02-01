package com.transformuk.hee.tis.genericupload.service.monitoring;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "healthz")
public class HealthEndpoint {

  @ReadOperation
  public Status invoke() {
    return Status.UP;
  }
}
