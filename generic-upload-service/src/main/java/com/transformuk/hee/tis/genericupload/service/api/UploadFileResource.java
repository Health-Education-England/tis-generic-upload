package com.transformuk.hee.tis.genericupload.service.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.codahale.metrics.annotation.Timed;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileStatus;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.api.validation.FileValidator;
import com.transformuk.hee.tis.genericupload.service.service.FileProcessService;
import com.transformuk.hee.tis.genericupload.service.service.UploadFileService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
			@ApiResponse(code = 201, message = "Uploaded given file successfully", response = String.class) })
	@PostMapping("/generic-upload/file")
	@Timed
	@PreAuthorize("hasPermission('tis:people::person:', 'Create')")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void handleFileUpload(HttpServletRequest request) throws Exception { // URISyntaxException
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;

        // extract files from MIME body
        List<MultipartFile> fileList = mRequest.getFileMap()
                .entrySet()
                .stream()
                .map(file -> file.getValue())
                .collect(Collectors.toList());

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
        fileValidator.validate(fileList, FileType.RECRUITMENT);
        
        // if validation is success then store the file into azure and db
        uploadFileService.upload(fileList);
	}

	@ApiOperation(value = "bulk upload file response", notes = "bulk upload file response", responseContainer = "Completed")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "File process successfully", response = String.class) })
	@PostMapping("/generic-upload/process")
	@Timed
	@PreAuthorize("hasPermission('tis:people::person:', 'Update')")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void processFile() throws Exception { // URISyntaxException
		fileProcessService.loadFilesByStatus(FileStatus.PENDING);
	}
}
