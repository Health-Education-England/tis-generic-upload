package com.transformuk.hee.tis.genericupload.service.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.annotation.Timed;
import com.transformuk.hee.tis.genericupload.service.api.vm.LoggerVM;
import com.transformuk.hee.tis.genericupload.service.repository.model.ApplicationType;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping("/management")
public class LogsResource {

	@GetMapping("/logs")
	@Timed
	public List<LoggerVM> getList() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		return context.getLoggerList()
				.stream()
				.map(LoggerVM::new)
				.collect(Collectors.toList());
	}


	@ApiOperation(value = "Change the log level", notes = "Change the log level")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Log level changed successfully") })
	@PutMapping("/logs")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Timed
	public void changeLevel(@ApiParam("jsonLogger") @RequestBody LoggerVM jsonLogger) {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLogger(jsonLogger.getName()).setLevel(Level.valueOf(jsonLogger.getLevel()));
	}
}
