package com.transformuk.hee.tis.genericupload.service.api;

import com.codahale.metrics.annotation.Timed;
import com.microsoft.azure.storage.StorageException;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.api.validation.FileValidator;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import com.transformuk.hee.tis.genericupload.service.service.FileProcessService;
import com.transformuk.hee.tis.genericupload.service.service.UploadFileService;
import com.transformuk.hee.tis.security.model.UserProfile;
import com.transformuk.hee.tis.security.util.TisSecurityHelper;
import io.swagger.annotations.*;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
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
		log.info("Received request to upload files.");

		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		// extract files from MIME body
		List<MultipartFile> fileList = mRequest.getFileMap()
				.entrySet()
				.stream()
				.map(file -> file.getValue())
				.collect(Collectors.toList());
		if (fileList.isEmpty()) {
			log.error("Expected to receive file(s) as part of the upload");
			return new ResponseEntity<>("expecting a file to be uploaded", HttpStatus.BAD_REQUEST);
		}

		// Validate file formats
		for (MultipartFile file : fileList) {
			if(isNotExcelContentType(file) || doesNotHaveAnExcelExtension(file)) {
				log.error("Bad content type or extension");
				return new ResponseEntity<>("Content type %s not supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
			}
		}

		ApplicationType applicationType;
		try {
			// Validate file - for other type of file, please pass path variable and pass to validator
			fileValidator.validate(fileList, FileType.RECRUITMENT, true);

			UserProfile profileFromContext = TisSecurityHelper.getProfileFromContext();
			// if validation is success then store the file into azure and db
			applicationType = uploadFileService.upload(fileList, profileFromContext.getUserName(), profileFromContext.getFirstName(), profileFromContext.getLastName());
		} catch (InvalidKeyException | StorageException | URISyntaxException e) {
			return new ResponseEntity<>("Application error while storing the file : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}	catch (IOException | NoSuchFieldException | InstantiationException | IllegalAccessException | ParseException | InvalidFormatException | MethodArgumentNotValidException e) {
			return new ResponseEntity<>("File uploaded cannot be processed " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(applicationType, HttpStatus.OK);
	}

	public boolean isNotExcelContentType(MultipartFile file) {
		String contentType = file.getContentType();
		return !(XLS_MIME_TYPE.equalsIgnoreCase(contentType) || XLX_MIME_TYPE.equalsIgnoreCase(contentType));
	}

	public boolean doesNotHaveAnExcelExtension(MultipartFile file) {
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		return extension != null && !(extension.equals("xls") || extension.equals("xlsx"));
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
	                                                                 @ApiParam(value = "any wildcard string to be searched") @RequestParam(value = "searchQuery", required = false) String searchQuery) {
		log.info("request for bulk upload status received.");
		Page<ApplicationType> page;
		searchQuery = sanitize(searchQuery);
		if(StringUtils.isBlank(searchQuery)) {
			page = uploadFileService.getUploadStatus(pageable);
		} else {
			page = uploadFileService.searchUploads(searchQuery, pageable);
		}
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/generic-upload/status");

		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	@ApiOperation(value = "View status of bulk uploads", notes = "View status of bulk uploads", responseContainer = "List", response = ApplicationType.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Status list returned")})
	@GetMapping(value = "/uploadedFileErrors/{logId}")
	public ResponseEntity<byte[]> getUploadedFileErrors(@ApiParam(value = "The stored file log id", required = true) @PathVariable(value = "logId") final Long logId) {
		Map.Entry<String, OutputStream> byLogId = uploadFileService.findErrorsByLogId(logId).entrySet().iterator().next();
		ByteArrayOutputStream logIdOutputStream = (ByteArrayOutputStream) byLogId.getValue();

		String filename = byLogId.getKey();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData(filename, filename);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

		return new ResponseEntity<>(logIdOutputStream.toByteArray(), headers, HttpStatus.OK);
	}

	private static String sanitize(String str) {
		if (str == null) {
			return null;
		}
		return str.replaceAll("[^a-zA-Z0-9\\s,/\\-\\(\\)]", "").trim();
	}
}
