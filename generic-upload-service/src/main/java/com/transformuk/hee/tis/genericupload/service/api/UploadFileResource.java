package com.transformuk.hee.tis.genericupload.service.api;

import static com.transformuk.hee.tis.genericupload.service.config.MapperConfiguration.convertToLocalDateTime;
import static uk.nhs.tis.StringConverter.getConverter;

import com.codahale.metrics.annotation.Timed;
import com.microsoft.azure.storage.StorageException;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.api.validation.FileValidator;
import com.transformuk.hee.tis.genericupload.service.api.validation.ValidationException;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import com.transformuk.hee.tis.genericupload.service.service.UploadFileService;
import com.transformuk.hee.tis.security.model.UserProfile;
import com.transformuk.hee.tis.security.util.TisSecurityHelper;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping("/api")
public class UploadFileResource {

  static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final Logger log = LoggerFactory.getLogger(UploadFileResource.class);
  private final UploadFileService uploadFileService;
  private final FileValidator fileValidator;

  public UploadFileResource(UploadFileService uploadFileService, FileValidator fileValidator) {
    this.uploadFileService = uploadFileService;
    this.fileValidator = fileValidator;
  }

  @ApiOperation(value = "Upload a file", notes = "Although this interface supports multiple file uploads, at the time of development the Angular client only supports a single upload")
  @ApiResponses(value = {
      @ApiResponse(code = 202, message = "Uploaded given files successfully with logId", response = ApplicationType.class),
      @ApiResponse(code = 400, message = "The error message will be in the body of the response", response = String.class),
      @ApiResponse(code = 415, message = "Invalid file format", response = String.class),
      @ApiResponse(code = 500, message = "Problems encountered saving the file", response = String.class)})
  @PostMapping("/file")
  @Timed
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ResponseEntity handleFileUpload(HttpServletRequest request) {
    log.debug("Received request to upload files.");

    MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
    // extract files from MIME body
    List<MultipartFile> fileList = mRequest.getFileMap()
        .entrySet()
        .stream()
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());
    if (fileList.isEmpty()) {
      log.error("Expected to receive file(s) as part of the upload");
      return new ResponseEntity<>("expecting a file to be uploaded", HttpStatus.BAD_REQUEST);
    }

    // Validate file formats
    for (MultipartFile file : fileList) {
      if (!(hasAnExcelExtension(file))) {
        log.error("Did not receive a file with an Excel extension");
        return new ResponseEntity<>("Content type %s not supported",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE);
      }
    }

    ApplicationType applicationType;
    try {
      // Validate file - for other type of file, please pass path variable and pass to validator
      FileType fileType = fileValidator.validate(fileList, true, false);

      UserProfile profileFromContext = TisSecurityHelper.getProfileFromContext();
      // if validation is success then store the file into azure and db
      applicationType = uploadFileService
          .upload(fileList, fileType, profileFromContext.getUserName(),
              profileFromContext.getFirstName(), profileFromContext.getLastName());
    } catch (InvalidKeyException | StorageException | URISyntaxException e) {
      return logAndReturnResponseEntity("Application error while storing the file : ",
          e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (IOException | ReflectiveOperationException | InvalidFormatException e) {
      return logAndReturnResponseEntity("File uploaded cannot be processed : ", e.getMessage(),
          HttpStatus.BAD_REQUEST);
    } catch (ValidationException ve) {
      StringBuilder sb = new StringBuilder();
      for (FieldError fieldError : ve.getBindingResult().getFieldErrors()) {
        sb.append(System.lineSeparator()).append(fieldError.getDefaultMessage());
      }
      return logAndReturnResponseEntity("File uploaded failed validation : ", sb.toString(),
          HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return logAndReturnResponseEntity("Unexpected Exception : ", e.getMessage(),
          HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(applicationType, HttpStatus.OK);
  }

  private ResponseEntity<String> logAndReturnResponseEntity(String messagePrefix,
      String exceptionMessage, HttpStatus httpStatus) {
    log.error(messagePrefix, exceptionMessage);
    return new ResponseEntity<>(messagePrefix + exceptionMessage, httpStatus);
  }


  public boolean hasAnExcelExtension(MultipartFile file) {
    String extension = FilenameUtils.getExtension(file.getOriginalFilename());
    return "xls".equals(extension) || "xlsx".equals(extension);
  }

  @ApiOperation(value = "View status of bulk uploads", notes = "View status of bulk uploads", responseContainer = "List", response = ApplicationType.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Status list returned")})
  //https://stackoverflow.com/a/35427093 document Spring Pageable
  @ApiImplicitParams({
      @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
          value = "Results page you want to retrieve (0..N)"),
      @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
          value = "Number of records per page."),
      @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
          value = "Sorting criteria in the format: property(,asc|desc). " +
              "Default sort order is ascending. " +
              "Multiple sort criteria are supported.")
  })
  @GetMapping("/status")
  @Timed
  public ResponseEntity<List<ApplicationType>> getBulkUploadStatus(@ApiParam Pageable pageable,
      @ApiParam(value = "any wildcard string to be searched") @RequestParam(value = "searchQuery", required = false) String searchQuery,
      @ApiParam(value = "date string any substring of the format YYYY-MM-DD") @RequestParam(value = "uploadedDate", required = false) String uploadedDate,
      @ApiParam(value = "file") @RequestParam(value = "file", required = false) String file,
      @ApiParam(value = "user") @RequestParam(value = "user", required = false) String user) {
    log.debug("request for bulk upload status received.");
    Page<ApplicationType> page;
    // sanitize the specifications of query
    searchQuery = getConverter(searchQuery).escapeForSql().toString();
    file = getConverter(file).escapeForSql().toString();
    user = getConverter(user).escapeForSql().toString();
    uploadedDate = getConverter(uploadedDate).toString();

    if (!StringUtils.isBlank(uploadedDate) || !StringUtils.isBlank(file) || !StringUtils
        .isBlank(user)) {
      page = uploadFileService.searchUploads(StringUtils.isBlank(uploadedDate) ? null
          : convertToLocalDateTime(uploadedDate, dateTimeFormatter), file, user, pageable);
    } else if (!StringUtils.isBlank(searchQuery)) {
      page = uploadFileService.searchUploads(searchQuery, pageable);
    } else {
      page = uploadFileService.getUploadStatus(pageable);
    }

    HttpHeaders headers = PaginationUtil
        .generatePaginationHttpHeaders(page, "/api/generic-upload/status");

    return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "View status of bulk uploads", notes = "View status of bulk uploads", responseContainer = "List", response = ApplicationType.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Status list returned")})
  @GetMapping(value = "/uploadedFileErrors/{logId}")
  public ResponseEntity<byte[]> getUploadedFileErrors(
      @ApiParam(value = "The stored file log id", required = true) @PathVariable(value = "logId") final Long logId) {
    try (ByteArrayOutputStream fileWithErrorsOnly = new ByteArrayOutputStream()) {
      String filename = uploadFileService.findErrorsByLogId(logId, fileWithErrorsOnly);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentDispositionFormData(filename, filename);
      headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
      return new ResponseEntity<>(fileWithErrorsOnly.toByteArray(), headers, HttpStatus.OK);
    } catch (IOException e) {
      log.error("Error reading file : {}", logId);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      log.error("Unexpected error building template with errors : {} ", logId);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
