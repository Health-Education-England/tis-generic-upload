package com.transformuk.hee.tis.genericupload.service.repository;

import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationTypeRepository extends JpaRepository<ApplicationType, Long> {

    List<ApplicationType> findByFileStatusOrderByStartDate(FileStatus status);

    List<ApplicationType> findByFileName(String fileName);

}
