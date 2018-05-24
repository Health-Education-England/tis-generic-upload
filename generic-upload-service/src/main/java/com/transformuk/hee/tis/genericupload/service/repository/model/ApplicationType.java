package com.transformuk.hee.tis.genericupload.service.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class ApplicationType implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Column(name = "fileType")
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private LocalDateTime uploadedDate;

    private LocalDateTime processedDate;

    private LocalDateTime jobStartTime;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private FileStatus fileStatus;

    private Long logId;
    private String username;
    private String firstName;
    private String lastName;

    @JsonIgnore
    @ApiModelProperty(hidden=true)
    private String errorJson;

    private Integer numberOfErrors;
    private Integer numberImported;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(LocalDateTime uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public LocalDateTime getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(LocalDateTime processedDate) {
        this.processedDate = processedDate;
    }

    public LocalDateTime getJobStartTime() { return jobStartTime; }

    public void setJobStartTime(LocalDateTime jobStartTime) { this.jobStartTime = jobStartTime; }

    public FileStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(FileStatus fileStatus) {
        this.fileStatus = fileStatus;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getErrorJson() {
        return errorJson;
    }

    public void setErrorJson(String errorJson) {
        this.errorJson = errorJson;
    }

    public Integer getNumberOfErrors() { return numberOfErrors; }

    public void setNumberOfErrors(Integer numberOfErrors) { this.numberOfErrors = numberOfErrors; }

    public Integer getNumberImported() { return numberImported; }

    public void setNumberImported(Integer numberImported) { this.numberImported = numberImported; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationType that = (ApplicationType) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApplicationType{");
        sb.append("id=").append(id);
        sb.append(", fileName='").append(fileName).append('\'');
        sb.append(", fileType=").append(fileType);
        sb.append(", uploadedDate=").append(uploadedDate);
        sb.append(", processedDate=").append(processedDate);
        sb.append(", jobStartTime=").append(jobStartTime);
        sb.append(", fileStatus=").append(fileStatus);
        sb.append(", logId=").append(logId);
        sb.append(", username='").append(username).append('\'');
        sb.append(", firstName='").append(firstName).append('\'');
        sb.append(", lastName='").append(lastName).append('\'');
        sb.append(", errorJson='").append(errorJson).append('\'');
        sb.append(", numberOfErrors=").append(numberOfErrors);
        sb.append(", numberImported=").append(numberImported);
        sb.append('}');
        return sb.toString();
    }
}
