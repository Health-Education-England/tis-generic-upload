package com.transformuk.hee.tis.genericupload.service.repository;

import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import javax.persistence.EntityManager;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
public class ApplicationTypeRepositoryTest {

  @Mock
  private ApplicationTypeRepository applicationTypeRepository;
  @Mock
  private EntityManager em;

  @BeforeEach
  public void setup() {
    ApplicationType applicationType = new ApplicationType();
    applicationType.setLogId(1L);
    when(applicationTypeRepository.findByLogId(1L)).thenReturn(applicationType);
  }

  @Transactional
  @Test
  public void findApplicationTypeByLogId() {
    ApplicationType applicationType = applicationTypeRepository.findByLogId(1L);
    Assert.assertNotNull(applicationType);
  }
}
