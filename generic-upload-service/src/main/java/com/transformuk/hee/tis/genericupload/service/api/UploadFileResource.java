package com.transformuk.hee.tis.genericupload.service.api;

import com.codahale.metrics.annotation.Timed;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.api.validation.FileValidator;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import com.transformuk.hee.tis.genericupload.service.service.FileProcessService;
import com.transformuk.hee.tis.genericupload.service.service.UploadFileService;
import com.transformuk.hee.tis.security.util.TisSecurityHelper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UploadFileResource {
	private final Logger log = LoggerFactory.getLogger(UploadFileResource.class);

	private final UploadFileService uploadFileService;
	private final FileProcessService fileProcessService;

	private final FileValidator fileValidator;

	private final String XLS_MIME_TYPE = "application/vnd.ms-excel";
    private final String XLX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	public UploadFileResource(UploadFileService uploadFileService, FileProcessService fileProcessService,
			FileValidator fileValidator) {
		this.uploadFileService = uploadFileService;
		this.fileProcessService = fileProcessService;
		this.fileValidator = fileValidator;
	}

	@ApiOperation(value = "bulk upload file", notes = "bulk upload file", response = String.class, responseContainer = "Accepted")
	@ApiResponses(value = {
			@ApiResponse(code = 202, message = "Uploaded given files successfully with logId", response = Long.class) })
	@PostMapping("/file")
	@Timed
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ResponseEntity<Long> handleFileUpload(HttpServletRequest request) throws Exception { // URISyntaxException
		log.info("Received request to upload files.");
        String userName = TisSecurityHelper.getProfileFromContext().getUserName();

		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;

    // extract files from MIME body
    List<MultipartFile> fileList = mRequest.getFileMap()
            .entrySet()
            .stream()
            .map(file -> file.getValue())
            .collect(Collectors.toList());

    if(fileList.isEmpty()) {
    	log.error("Expected to receive file(s) as part of the upload");
    	return ResponseEntity.badRequest().body(0L);
    }

    // Validate file formats
    for (MultipartFile file : fileList) {
        // TODO support multiple file types here - xlsx, xls for now; CSV perhaps
        String contentType = file.getContentType();
        if (!(XLS_MIME_TYPE.equalsIgnoreCase(contentType) || XLX_MIME_TYPE.equalsIgnoreCase(contentType))) {
            throw new InvalidFormatException(String.format("Content type %s not supported", file.getContentType()));
        }
    }

    //TODO is this necessary
    // Validate file
    // for other type of file, please pass path variable and pass to validator
    fileValidator.validate(fileList, FileType.RECRUITMENT); //TODO allow validation exceptions to bubble up to REST response

    // if validation is success then store the file into azure and db
    long logId = uploadFileService.upload(fileList, userName);

    return ResponseEntity.accepted()
            .body(logId);
	}

	@ApiOperation(value = "View status of bulk uploads", notes = "View status of bulk uploads", responseContainer = "List", response = ApplicationType.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "File process successfully") })
	@GetMapping("/status")
	@Timed
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ResponseEntity<List<ApplicationType>> getBulkUploadStatus(@ApiParam Pageable pageable) throws Exception { // URISyntaxException
		log.info("request for bulk upload status received.");
		Page<ApplicationType> page = uploadFileService.getUploadStatus(pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/generic-upload/status");

		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}
}
