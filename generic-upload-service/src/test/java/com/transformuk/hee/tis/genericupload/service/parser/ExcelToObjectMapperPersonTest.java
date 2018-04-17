package com.transformuk.hee.tis.genericupload.service.parser;

import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;


import static com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper.getDate;
import static org.assertj.core.api.Assertions.assertThat;

public class ExcelToObjectMapperPersonTest {

  private ExcelToObjectMapper excelToObjectMapper;

  private static final String FILE_NAME = "TIS Recruitment Import Template - test - single.xlsx";

  @Before
  public void setUp() throws Exception {
    String filePath = new ClassPathResource(FILE_NAME).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    excelToObjectMapper = new ExcelToObjectMapper(inputStream, true);
  }

  @Test
  public void shouldReturnParseObject() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
            new PersonHeaderMapper().getFieldMap());
    Assert.assertNotNull(actual);
  }

  @Test
  public void shouldMapRecordStatus() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
            new PersonHeaderMapper().getFieldMap());
    assertThat(actual.get(0).getRecordStatus()).isNotNull();
  }

  @Test
  public void shouldMapProgrammeMembershipType() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
            new PersonHeaderMapper().getFieldMap());
    assertThat(actual.get(0).getProgrammeMembership()).isNotNull();
  }

  @Test
  public void shouldSkipEmptyRows() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
            new PersonHeaderMapper().getFieldMap());
    Assert.assertEquals(1, actual.size());
  }

  @Test
  public void shouldHaveGMCNumberAndNoNI() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());
    Assert.assertEquals("1234567", actual.get(0).getGmcNumber());
    Assert.assertEquals(null, actual.get(0).getNiNumber());
  }

  @Test
  public void shouldHaveARowNumber() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());
    Assert.assertEquals(1, actual.get(0).getRowNumber());
  }

  @Test
  public void allFieldsAreSet() throws Exception {
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());

    Assert.assertEquals("Random", actual.get(0).getForenames());
    Assert.assertEquals("Guy", actual.get(0).getSurname());
    Assert.assertEquals("1234567", actual.get(0).getGmcNumber());
    Assert.assertEquals("Current", actual.get(0).getRecordStatus());
    Assert.assertEquals("Cardiology", actual.get(0).getProgrammeName());
    Assert.assertEquals("NOR051", actual.get(0).getProgrammeNumber());
    Assert.assertEquals("Substantive", actual.get(0).getProgrammeMembership());
    Assert.assertEquals(getDate("27/2/19"), actual.get(0).getProgrammeEndDate());
    Assert.assertEquals("Cardiology", actual.get(0).getCurriculum1());
    Assert.assertEquals(getDate("31/7/23"), actual.get(0).getCurriculum1EndDate());
    Assert.assertEquals(getDate("1/8/18"), actual.get(0).getCurriculum1StartDate());
    Assert.assertEquals("General (Internal) Medicine", actual.get(0).getCurriculum2());
    Assert.assertEquals(getDate("31/7/23"), actual.get(0).getCurriculum2EndDate());
    Assert.assertEquals(getDate("1/8/18"), actual.get(0).getCurriculum2StartDate());
    Assert.assertEquals("Dr", actual.get(0).getTitle());
    Assert.assertEquals(getDate("1/12/71"), actual.get(0).getDateOfBirth());
    Assert.assertEquals("random.guy@hotmail.co.uk", actual.get(0).getEmailAddress());
    Assert.assertEquals("7483256211", actual.get(0).getMobile());
    Assert.assertEquals("7483256211", actual.get(0).getTelephone());
    Assert.assertEquals("10 Scarborough Flats", actual.get(0).getAddress1());
    Assert.assertEquals("Wilson Street", actual.get(0).getAddress2());
    Assert.assertEquals("London", actual.get(0).getAddress3());
    Assert.assertEquals("EC2 8WY", actual.get(0).getPostCode());
    Assert.assertEquals("Male", actual.get(0).getGender());
    Assert.assertEquals("British", actual.get(0).getNationality());
    Assert.assertEquals("Single", actual.get(0).getMaritalStatus());
    Assert.assertEquals("Not Stated", actual.get(0).getEthnicOrigin());
    Assert.assertEquals("Yes", actual.get(0).getEeaResident());
    Assert.assertEquals("MBBS", actual.get(0).getQualification());
    Assert.assertEquals("University of Newcastle", actual.get(0).getMedicalSchool());
    Assert.assertEquals("United Kingdom", actual.get(0).getCountryOfQualification());
    Assert.assertEquals(getDate("7/9/06"), actual.get(0).getDateAttained());
  }

  @Test
  public void canParseDates() throws ParseException {
    LocalDate localDate = new LocalDate(2001, 7, 6);
    Assert.assertEquals(localDate.toDate(), getDate("6/7/01"));
  }

  @Test(expected = ParseException.class)
  public void throwsAnExceptionOnBadDates() throws ParseException {
    Assert.assertNull(getDate("111/11/2124"));
  }

  @Test
  public void canParseDatesWith4CharactersInYears() throws ParseException {
    LocalDate localDate = new LocalDate(2001, 7, 6);
    Assert.assertEquals(localDate.toDate(), getDate("6/7/2001"));
  }

  @Test
  public void shouldSkipEmptyRowsAgain() throws Exception {
    String filePath = new ClassPathResource("TIS People Import Template - empty row.xlsx").getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    excelToObjectMapper = new ExcelToObjectMapper(inputStream, false);
    List<PersonXLS> actual = excelToObjectMapper.map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());
    assertThat(actual.get(0).getRecordStatus()).isNotNull();
  }
}
