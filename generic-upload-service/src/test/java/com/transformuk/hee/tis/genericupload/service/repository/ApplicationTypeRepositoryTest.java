package com.transformuk.hee.tis.genericupload.service.repository;

import com.transformuk.hee.tis.genericupload.service.Application;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import javax.persistence.EntityManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTypeRepositoryTest {

  @Autowired
  private ApplicationTypeRepository applicationTypeRepository;
  @Autowired
  private EntityManager em;

  @Before
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
