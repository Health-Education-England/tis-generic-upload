package com.transformuk.hee.tis.genericupload.service.config;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MapperConfigurationTest {
	@Test
	public void parseStringDate() {
		String date = "2018-12-22";
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime localDateTime = MapperConfiguration.convertToLocalDateTime(date, dateTimeFormatter);
		Assert.assertNotNull(localDateTime);
	}
}
