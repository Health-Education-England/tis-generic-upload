package com.transformuk.hee.tis.genericupload.service.repository;

import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationTypeRepository extends JpaRepository<ApplicationType, Long>, JpaSpecificationExecutor {

    String SEARCH_QUERY = "select at from ApplicationType at where " +
        "at.fileName like %:text% or " +
        "at.fileStatus like %:text% or " +
        "at.username like %:text% or " +
        "at.firstName like %:text% or " +
        "at.lastName like %:text% or " +
        "at.uploadedDate like %:text% or " +
        "at.processedDate like %:text%";

    List<ApplicationType> findByFileStatusOrderByUploadedDate(FileStatus status);

    ApplicationType findByLogId(Long logId);

    @Query(value = SEARCH_QUERY)
    Page<ApplicationType> fullTextSearch(@Param("text") String text, Pageable pageable);
}
