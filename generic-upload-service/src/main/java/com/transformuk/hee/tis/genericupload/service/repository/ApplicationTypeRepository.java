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

    String SEARCH_QUERY_RESTRICT_ALL = "select at from ApplicationType at where " +
        "at.fileName like %:file% and " +
        "(at.username like %:user% or " +
        "at.firstName like %:user% or " +
        "at.lastName like %:user%) and " +
        "at.uploadedDate like %:uploadedDate%";

    String SEARCH_QUERY_RESTRICT_FILE = "select at from ApplicationType at where " +
        "at.fileName like %:file%";
    String SEARCH_QUERY_RESTRICT_DATE = "select at from ApplicationType at where " +
        "at.uploadedDate like %:uploadedDate%";
    String SEARCH_QUERY_RESTRICT_USER = "select at from ApplicationType at where " +
        "at.username like %:user% or " +
        "at.firstName like %:user% or " +
        "at.lastName like %:user%";

    String SEARCH_QUERY_RESTRICT_FILEDATE = "select at from ApplicationType at where " +
        "at.fileName like %:file% and " +
        "at.uploadedDate like %:uploadedDate%";

    String SEARCH_QUERY_RESTRICT_FILEUSER = "select at from ApplicationType at where " +
        "at.fileName like %:file% and " +
        "(at.username like %:user% or " +
        "at.firstName like %:user% or " +
        "at.lastName like %:user%)";

    String SEARCH_QUERY_RESTRICT_USERDATE = "select at from ApplicationType at where " +
        "at.uploadedDate like %:uploadedDate% and " +
        "(at.username like %:user% or " +
        "at.firstName like %:user% or " +
        "at.lastName like %:user%)";

    List<ApplicationType> findByFileStatusOrderByUploadedDate(FileStatus status);

    ApplicationType findByLogId(Long logId);

    @Query(value = SEARCH_QUERY)
    Page<ApplicationType> fullTextSearch(@Param("text") String text, Pageable pageable);

    @Query(value = SEARCH_QUERY_RESTRICT_ALL)
    Page<ApplicationType> restrictedTextSearchAll(@Param("uploadedDate") String uploadedDate,
                                                  @Param("file") String file,
                                                  @Param("user") String user,
                                                  Pageable pageable);

    @Query(value = SEARCH_QUERY_RESTRICT_FILE)
    Page<ApplicationType> restrictedTextSearchFile(@Param("file") String file,
                                                       Pageable pageable);

    @Query(value = SEARCH_QUERY_RESTRICT_USER)
    Page<ApplicationType> restrictedTextSearchUser(@Param("user") String user,
                                                    Pageable pageable);

    @Query(value = SEARCH_QUERY_RESTRICT_DATE)
    Page<ApplicationType> restrictedTextSearchDate(@Param("uploadedDate") String uploadedDate,
                                                   Pageable pageable);

    @Query(value = SEARCH_QUERY_RESTRICT_FILEDATE)
    Page<ApplicationType> restrictedTextSearchFileDate(@Param("uploadedDate") String uploadedDate,
                                                       @Param("file") String file,
                                                       Pageable pageable);

    @Query(value = SEARCH_QUERY_RESTRICT_FILEUSER)
    Page<ApplicationType> restrictedTextSearchFileUser(@Param("file") String file,
                                                       @Param("user") String user,
                                                       Pageable pageable);

    @Query(value = SEARCH_QUERY_RESTRICT_USERDATE)
    Page<ApplicationType> restrictedTextSearchUserDate(@Param("user") String user,
                                                       @Param("uploadedDate") String uploadedDate,
                                                       Pageable pageable);
}
