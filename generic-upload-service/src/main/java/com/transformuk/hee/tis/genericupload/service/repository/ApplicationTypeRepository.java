package com.transformuk.hee.tis.genericupload.service.repository;

import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationTypeRepository extends JpaRepository<ApplicationType, Long> {

    List<ApplicationType> findByFileStatusOrderByStartDate(FileStatus status); //TODO check this is ordering by oldest upload

    //TODO integrate with API
    @Query(value = "select at from ApplicationType at where at.fileName like %:text% or at.fileStatus like %:text% or at.username like %:text% ")
    Page<ApplicationType> fullTextSearch(@Param("text") String text, Pageable pageable);

}
