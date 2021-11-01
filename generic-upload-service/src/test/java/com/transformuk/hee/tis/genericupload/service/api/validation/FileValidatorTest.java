package com.transformuk.hee.tis.genericupload.service.api.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.slf4j.LoggerFactory.getLogger;

import com.transformuk.hee.tis.genericupload.api.dto.AssessmentDeleteXLS;
import com.transformuk.hee.tis.genericupload.api.dto.AssessmentXLS;
import com.transformuk.hee.tis.genericupload.api.dto.FundingUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementDeleteXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PlacementXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PostCreateXls;
import com.transformuk.hee.tis.genericupload.api.dto.PostFundingUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.dto.PostUpdateXLS;
import com.transformuk.hee.tis.genericupload.api.enumeration.FileType;
import com.transformuk.hee.tis.genericupload.service.Application;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class FileValidatorTest {

  private static final Logger logger = getLogger(FileValidatorTest.class);

  @Autowired
  FileValidator fileValidator;

  @Test(expected = ValidationException.class)
  public void shouldValidateMandatoryFields()
      throws ReflectiveOperationException, InvalidFormatException, ValidationException, IOException {
    String filename = "TIS Placement Import Template - Test 4 (multiple errors).xls";
    String filePath = new ClassPathResource(filename).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    MultipartFile multipartFile = new MockMultipartFile("file",
        filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n",
        IOUtils.toByteArray(inputStream));

    fileValidator.validate(Collections.singletonList(multipartFile), true, false);
  }

  @Test
  public void shouldErrorWithMessage() throws Exception {
    String filename = "TIS Placement Import Template - Test 4 (multiple errors).xls";
    String filePath = new ClassPathResource(filename).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    MultipartFile multipartFile = new MockMultipartFile("file",
        filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n",
        IOUtils.toByteArray(inputStream));
    try {
      fileValidator.validate(Collections.singletonList(multipartFile), true, false);
    } catch (ValidationException ve) {
      BindingResult bindingResult = ve.getBindingResult();
      Assert.assertEquals(2, bindingResult.getErrorCount());
    }
  }

  @Test
  public void shouldErrorForIncorrectDatesOnMandatoryDateFieldsWithMessage() throws Exception {
    String filename = "TIS Placement Import Template - Test 4 (multiple errors).xls";
    String filePath = new ClassPathResource(filename).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    MultipartFile multipartFile = new MockMultipartFile("file",
        filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n",
        IOUtils.toByteArray(inputStream));
    try {
      fileValidator.validate(Collections.singletonList(multipartFile), true, true);
    } catch (ValidationException ve) {
      Assert.assertTrue(oneOfTheFieldErrorsIs(ve.getBindingResult(), "Date missing"));
    }
  }

  @Test
  public void shouldErrorForIncorrectDatesOnAssessmentMandatoryDateFieldsWithMessage()
      throws Exception {
    String filename = "TIS Assessment Import Template - Step 2.xlsx";
    String filePath = new ClassPathResource(filename).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    MultipartFile multipartFile = new MockMultipartFile("file",
        filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n",
        IOUtils.toByteArray(inputStream));
    try {
      fileValidator.validate(Collections.singletonList(multipartFile), true, true);
    } catch (ValidationException ve) {
      Assert.assertTrue(oneOfTheFieldErrorsIs(ve.getBindingResult(), "Date missing"));
    }
  }

  @Test
  public void shouldErrorForIncorrectDatesOnPlacementUpdateMandatoryIdWithMessage()
      throws Exception {
    String filename = "TIS Placement Update Template - IntrepidId's only.xls";
    String filePath = new ClassPathResource(filename).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    MultipartFile multipartFile = new MockMultipartFile("file",
        filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n",
        IOUtils.toByteArray(inputStream));
    try {
      fileValidator.validate(Collections.singletonList(multipartFile), true, true);
    } catch (ValidationException ve) {
      Assert.assertTrue(oneOfTheFieldErrorsIs(ve.getBindingResult(), "PlacementId missing"));
    }
  }

  private boolean oneOfTheFieldErrorsIs(BindingResult bindingResult,
      String subStringErrorToLookFor) {
    logger.info(bindingResult.getFieldErrors().toString());

    for (FieldError fieldError : bindingResult.getFieldErrors()) {
      if (fieldError.toString().contains(subStringErrorToLookFor)) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void shouldErrorForMissingDatesOnMandatoryDateFieldsWithMessage() throws Exception {
    String filename = "TIS Placement Import Template - Test 4 (multiple errors).xls";
    String filePath = new ClassPathResource(filename).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    MultipartFile multipartFile = new MockMultipartFile("file",
        filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n",
        IOUtils.toByteArray(inputStream));
    try {
      fileValidator.validate(Collections.singletonList(multipartFile), true, true);
    } catch (ValidationException ve) {
      Assert.assertTrue(oneOfTheFieldErrorsIs(ve.getBindingResult(), "Date missing"));
    }
  }

  @Test
  public void shouldErrorForMissingSpecialty1FieldWithMessage() throws Exception {
    String filename = "TIS Placement Import Template - Test 4 (multiple errors).xls";
    String filePath = new ClassPathResource(filename).getURI().getPath();
    FileInputStream inputStream = new FileInputStream(filePath);
    MultipartFile multipartFile = new MockMultipartFile("file",
        filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n",
        IOUtils.toByteArray(inputStream));
    try {
      fileValidator.validate(Collections.singletonList(multipartFile), true, true);
    } catch (ValidationException ve) {
      Assert.assertTrue(oneOfTheFieldErrorsIs(ve.getBindingResult(), "Field is required"));
    }
  }

  @Test
  public void getFileTypeShouldIdentifyAssessmentsTemplate() throws Exception {
    // Given.
    FileValidator fileValidator = spy(this.fileValidator);

    ArgumentCaptor<Class> xlsCaptor = ArgumentCaptor.forClass(Class.class);

    doNothing().when(fileValidator)
        .validateMandatoryFieldsOrThrowException(any(), any(), xlsCaptor.capture(), any());

    // When.
    FileType fileType =
        fileValidator.getFileType(null, null, null, Collections.singleton("Review date*"));

    // Then.
    assertThat(fileType, is(FileType.ASSESSMENTS));
    assertThat(xlsCaptor.getValue(), is((Object) AssessmentXLS.class));
  }

  @Test
  public void getFileTypeShouldIdentifyAssessmentsDeleteTemplate() throws Exception {
    // Given.
    FileValidator fileValidator = spy(this.fileValidator);

    ArgumentCaptor<Class> xlsCaptor = ArgumentCaptor.forClass(Class.class);

    doNothing().when(fileValidator)
            .validateMandatoryFieldsOrThrowException(any(), any(), xlsCaptor.capture(), any());

    Set<String> headers = new HashSet<>();
    headers.add("TIS_Assessment_ID*");
    headers.add("Assessment Status*");

    // When.
    FileType fileType =
            fileValidator.getFileType(null, null, null, headers);

    // Then.
    assertThat(fileType, is(FileType.ASSESSMENTS_DELETE));
    assertThat(xlsCaptor.getValue(), is((Object) AssessmentDeleteXLS.class));
  }

  @Test
  public void getFileTypeShouldIdentifyFundingUpdateTemplate() throws Exception {
    // Given.
    FileValidator fileValidator = spy(this.fileValidator);

    ArgumentCaptor<Class> xlsCaptor = ArgumentCaptor.forClass(Class.class);

    doNothing().when(fileValidator)
        .validateMandatoryFieldsOrThrowException(any(), any(), xlsCaptor.capture(), any());

    // When.
    FileType fileType =
        fileValidator.getFileType(null, null, null, Collections.singleton("TIS_PostFunding_ID*"));

    // Then.
    assertThat(fileType, is(FileType.FUNDING_UPDATE));
    assertThat(xlsCaptor.getValue(), is((Object) FundingUpdateXLS.class));
  }

  @Test
  public void getFileTypeShouldIdentifyPeopleTemplate() throws Exception {
    // Given.
    FileValidator fileValidator = spy(this.fileValidator);

    ArgumentCaptor<Class> xlsCaptor = ArgumentCaptor.forClass(Class.class);

    doNothing().when(fileValidator)
        .validateMandatoryFieldsOrThrowException(any(), any(), xlsCaptor.capture(), any());

    // When.
    FileType fileType =
        fileValidator.getFileType(null, null, null, Collections.singleton("Email Address"));

    // Then.
    assertThat(fileType, is(FileType.PEOPLE));
    assertThat(xlsCaptor.getValue(), is((Object) PersonXLS.class));
  }

  @Test
  public void getFileTypeShouldIdentifyPeopleUpdateTemplate() throws Exception {
    // Given.
    FileValidator fileValidator = spy(this.fileValidator);

    ArgumentCaptor<Class> xlsCaptor = ArgumentCaptor.forClass(Class.class);

    doNothing().when(fileValidator)
        .validateMandatoryFieldsOrThrowException(any(), any(), xlsCaptor.capture(), any());

    // When.
    FileType fileType =
        fileValidator.getFileType(null, null, null, Collections.singleton("TIS_Person_ID*"));

    // Then.
    assertThat(fileType, is(FileType.PEOPLE_UPDATE));
    assertThat(xlsCaptor.getValue(), is((Object) PersonUpdateXls.class));
  }

  @Test
  public void getFileTypeShouldIdentifyPlacementsTemplate() throws Exception {
    // Given.
    FileValidator fileValidator = spy(this.fileValidator);

    ArgumentCaptor<Class> xlsCaptor = ArgumentCaptor.forClass(Class.class);

    doNothing().when(fileValidator)
        .validateMandatoryFieldsOrThrowException(any(), any(), xlsCaptor.capture(), any());

    // When.
    FileType fileType =
        fileValidator.getFileType(null, null, null, Collections.singleton("Placement Type*"));

    // Then.
    assertThat(fileType, is(FileType.PLACEMENTS));
    assertThat(xlsCaptor.getValue(), is((Object) PlacementXLS.class));
  }

  @Test
  public void getFileTypeShouldIdentifyPlacementsDeleteTemplate() throws Exception {
    // Given.
    FileValidator fileValidator = spy(this.fileValidator);

    ArgumentCaptor<Class> xlsCaptor = ArgumentCaptor.forClass(Class.class);

    doNothing().when(fileValidator)
        .validateMandatoryFieldsOrThrowException(any(), any(), xlsCaptor.capture(), any());

    Set<String> headers = new HashSet<>();
    headers.add("Placement Id*");
    headers.add("Placement Status*");

    // When.
    FileType fileType =
        fileValidator.getFileType(null, null, null, headers);

    // Then.
    assertThat(fileType, is(FileType.PLACEMENTS_DELETE));
    assertThat(xlsCaptor.getValue(), is((Object) PlacementDeleteXLS.class));
  }

  @Test
  public void getFileTypeShouldIdentifyPlacementsUpdateTemplate() throws Exception {
    // Given.
    FileValidator fileValidator = spy(this.fileValidator);

    ArgumentCaptor<Class> xlsCaptor = ArgumentCaptor.forClass(Class.class);

    doNothing().when(fileValidator)
        .validateMandatoryFieldsOrThrowException(any(), any(), xlsCaptor.capture(), any());

    Set<String> headers = new HashSet<>();
    headers.add("TIS_Placement_ID*");
    headers.add("Intrepid_Placement_ID");

    // When.
    FileType fileType =
        fileValidator.getFileType(null, null, null, headers);

    // Then.
    assertThat(fileType, is(FileType.PLACEMENTS_UPDATE));
    assertThat(xlsCaptor.getValue(), is((Object) PlacementUpdateXLS.class));
  }

  @Test
  public void getFileTypeShouldIdentifyPostsCreateTemplate() throws Exception {
    // Given.
    FileValidator fileValidator = spy(this.fileValidator);

    ArgumentCaptor<Class> xlsCaptor = ArgumentCaptor.forClass(Class.class);

    doNothing().when(fileValidator)
        .validateMandatoryFieldsOrThrowException(any(), any(), xlsCaptor.capture(), any());

    // When.
    FileType fileType =
        fileValidator.getFileType(null, null, null, Collections.singleton("National Post Number*"));

    // Then.
    assertThat(fileType, is(FileType.POSTS_CREATE));
    assertThat(xlsCaptor.getValue(), is((Object) PostCreateXls.class));
  }

  @Test
  public void getFileTypeShouldIdentifyPostsUpdateTemplate() throws Exception {
    // Given.
    FileValidator fileValidator = spy(this.fileValidator);

    ArgumentCaptor<Class> xlsCaptor = ArgumentCaptor.forClass(Class.class);

    doNothing().when(fileValidator)
        .validateMandatoryFieldsOrThrowException(any(), any(), xlsCaptor.capture(), any());

    // When.
    FileType fileType =
        fileValidator.getFileType(null, null, null, Collections.singleton("TIS_Post_ID*"));

    // Then.
    assertThat(fileType, is(FileType.POSTS_UPDATE));
    assertThat(xlsCaptor.getValue(), is((Object) PostUpdateXLS.class));
  }

  @Test
  public void getFileTypeShouldIdentifyPostsFundingUpdateTemplate() throws Exception {
    // Given.
    FileValidator fileValidator = spy(this.fileValidator);

    ArgumentCaptor<Class> xlsCaptor = ArgumentCaptor.forClass(Class.class);

    doNothing().when(fileValidator)
        .validateMandatoryFieldsOrThrowException(any(), any(), xlsCaptor.capture(), any());

    Set<String> headers = new HashSet<>();
    headers.add("TIS_Post_ID*");
    headers.add("Funding type");

    // When.
    FileType fileType =
        fileValidator.getFileType(null, null, null, headers);

    // Then.
    assertThat(fileType, is(FileType.POSTS_FUNDING_UPDATE));
    assertThat(xlsCaptor.getValue(), is((Object) PostFundingUpdateXLS.class));
  }

  @Test(expected = InvalidFormatException.class)
  public void getFileTypeShouldThrowExceptionIfTemplateNotIdentifiable() throws Exception {
    // When.
    fileValidator.getFileType(null, null, null, Collections.emptySet());
  }
}
