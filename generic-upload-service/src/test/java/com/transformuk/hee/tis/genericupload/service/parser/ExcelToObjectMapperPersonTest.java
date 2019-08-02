package com.transformuk.hee.tis.genericupload.service.parser;

import static com.transformuk.hee.tis.genericupload.service.parser.ExcelToObjectMapper.getDate;
import static org.assertj.core.api.Assertions.assertThat;

import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

public class ExcelToObjectMapperPersonTest {

  private static final String FILE_NAME = "TIS Recruitment Import Template - test - single.xlsx";

  public ExcelToObjectMapper setUpExcelToObjectMapper() throws Exception {
    Path filePath = Paths.get(getClass().getClassLoader().getResource(FILE_NAME).toURI());
    FileInputStream inputStream = new FileInputStream(filePath.toFile());
    ExcelToObjectMapper excelToObjectMapper = new ExcelToObjectMapper(inputStream, true);
    inputStream.close();
    return excelToObjectMapper;
  }

  @Test
  public void shouldReturnParseObject() throws Exception {
    List<PersonXLS> actual = setUpExcelToObjectMapper().map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());
    Assert.assertNotNull(actual);
  }

  @Test
  public void shouldMapRecordStatus() throws Exception {
    List<PersonXLS> actual = setUpExcelToObjectMapper().map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());
    assertThat(actual.get(0).getRecordStatus()).isNotNull();
  }

  @Test
  public void shouldMapProgrammeMembershipType() throws Exception {
    List<PersonXLS> actual = setUpExcelToObjectMapper().map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());
    assertThat(actual.get(0).getProgrammeMembership()).isNotNull();
  }

  @Test
  public void shouldSkipEmptyRows() throws Exception {
    List<PersonXLS> actual = setUpExcelToObjectMapper().map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());
    Assert.assertEquals(1, actual.size());
  }

  @Test
  public void shouldHaveGMCNumberAndNoNI() throws Exception {
    List<PersonXLS> actual = setUpExcelToObjectMapper().map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());
    Assert.assertEquals("1234567", actual.get(0).getGmcNumber());
    Assert.assertEquals(null, actual.get(0).getNiNumber());
  }

  @Test
  public void shouldHaveARowNumber() throws Exception {
    List<PersonXLS> actual = setUpExcelToObjectMapper().map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());
    Assert.assertEquals(1, actual.get(0).getRowNumber());
  }

  @Test
  public void shouldParseRotation() throws Exception {
    List<PersonXLS> actual = setUpExcelToObjectMapper().map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());
    Assert.assertEquals("Trauma & Orthopaedic Surgery NCEL UCLP - RNOH",
        actual.get(0).getRotation1());
  }


  @Test
  public void allFieldsAreSet() throws Exception {
    List<PersonXLS> actual = setUpExcelToObjectMapper().map(PersonXLS.class,
        new PersonHeaderMapper().getFieldMap());

    PersonXLS personXLS = actual.get(0);
    Assert.assertEquals("Random", personXLS.getForenames());
    Assert.assertEquals("Guy", personXLS.getSurname());
    Assert.assertEquals("1234567", personXLS.getGmcNumber());
    Assert.assertEquals("Current", personXLS.getRecordStatus());
    Assert.assertEquals("Cardiology", personXLS.getProgrammeName());
    Assert.assertEquals("NOR051", personXLS.getProgrammeNumber());
    Assert.assertEquals("Substantive", personXLS.getProgrammeMembership());
    Assert.assertEquals(getDate("27/2/1999"), personXLS.getProgrammeEndDate());
    Assert.assertEquals("Cardiology", personXLS.getCurriculum1());
    Assert.assertEquals(getDate("31/7/2023"), personXLS.getCurriculum1EndDate());
    Assert.assertEquals(getDate("1/8/2018"), personXLS.getCurriculum1StartDate());
    Assert.assertEquals("General (Internal) Medicine", personXLS.getCurriculum2());
    Assert.assertEquals(getDate("31/7/2023"), personXLS.getCurriculum2EndDate());
    Assert.assertEquals(getDate("1/8/2018"), personXLS.getCurriculum2StartDate());
    Assert.assertEquals("Dr", personXLS.getTitle());
    Assert.assertEquals(getDate("1/12/1971"), personXLS.getDateOfBirth());
    Assert.assertEquals("random.guy@hotmail.co.uk", personXLS.getEmailAddress());
    Assert.assertEquals("7483256211", personXLS.getMobile());
    Assert.assertEquals("7483256211", personXLS.getTelephone());
    Assert.assertEquals("10 Scarborough Flats", personXLS.getAddress1());
    Assert.assertEquals("Wilson Street", personXLS.getAddress2());
    Assert.assertEquals("London", personXLS.getAddress3());
    Assert.assertEquals("EC2 8WY", personXLS.getPostCode());
    Assert.assertEquals("Male", personXLS.getGender());
    Assert.assertEquals("British", personXLS.getNationality());
    Assert.assertEquals("Single", personXLS.getMaritalStatus());
    Assert.assertEquals("Not Stated", personXLS.getEthnicOrigin());
    Assert.assertEquals("Yes", personXLS.getEeaResident());
    Assert.assertEquals("MBBS", personXLS.getQualification());
    Assert.assertEquals("University of Newcastle", personXLS.getMedicalSchool());
    Assert.assertEquals("United Kingdom", personXLS.getCountryOfQualification());
    Assert.assertEquals(getDate("7/9/2006"), personXLS.getDateAttained());
  }

  @Test
  public void canParseDates() throws ParseException {
    LocalDate localDate = new LocalDate(2001, 7, 6);
    Assert.assertEquals(localDate.toDate(), getDate("6/7/2001"));
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
}
