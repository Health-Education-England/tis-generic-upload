package com.transformuk.hee.tis.genericupload.service.repository;

import com.transformuk.hee.tis.genericupload.service.Application;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import javax.persistence.EntityManager;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTypeRepositoryTest {

  @Autowired
  private ApplicationTypeRepository applicationTypeRepository;

  @Autowired
  private EntityManager em;

  @BeforeEach
  public void setup() {
    ApplicationType applicationType = new ApplicationType();
    applicationType.setLogId(1L);
    em.persist(applicationType);
  }

  @Transactional
  @Test
  public void findApplicationTypeByLogId() {
    ApplicationType applicationType = applicationTypeRepository.findByLogId(1L);
    Assert.assertNotNull(applicationType);
  }
}
