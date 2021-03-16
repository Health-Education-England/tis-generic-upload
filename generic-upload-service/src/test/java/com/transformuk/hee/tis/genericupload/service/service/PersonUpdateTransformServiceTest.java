package com.transformuk.hee.tis.genericupload.service.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.genericupload.service.service.mapper.PersonMapper;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PersonUpdateTransformServiceTest {

  @Mock
  TcsServiceImpl tcsServiceImplMock;
  @Mock
  PersonMapper personMapperMock;
  @InjectMocks
  private PersonUpdateTransformerService personUpdateTransformerService;

  @Test
  public void ShouldReturnErrorMessageWhenTrainerApprovalStatusDoesNotExists() {
    PersonUpdateXls xls = new PersonUpdateXls();
    xls.setTrainerApprovalStatus("invalid");

    List<String> errorMessages = personUpdateTransformerService.initialValidate(xls);
    assertThat("Should not return error messages", errorMessages.size(), is(1));
    assertThat("Should validate trainer approval status",
        errorMessages.get(0), containsString(String
            .format(PersonUpdateTransformerService.TRAINER_APPROVAL_STATUS_NOT_EXISTS,
                xls.getTrainerApprovalStatus())));
  }

  @Test
  public void ShouldNotReturnErrorMessageWhenTrainerApprovalStatusExists() {
    PersonUpdateXls xls = new PersonUpdateXls();
    xls.setTrainerApprovalStatus("CURRENT");

    List<String> errorMessages = personUpdateTransformerService.initialValidate(xls);
    assertThat("Should not return error messages", errorMessages.size(), is(0));
  }

  @Test
  public void ShouldReturnErrorMessageWhenCommaExistsInRole() {
    PersonUpdateXls xls = new PersonUpdateXls();
    xls.setRole("role1, role2");

    List<String> errorMessages = personUpdateTransformerService.initialValidate(xls);
    assertThat("Should not return error messages", errorMessages.size(), is(1));
    assertThat("Should validate role",
        errorMessages.get(0), containsString(String
            .format(PersonUpdateTransformerService.ROLE_ERROR_SEPARATOR,
                xls.getRole())));
  }

  @Test
  public void ShouldNotReturnErrorMessageWhenCommaDoesNotExistInRole() {
    PersonUpdateXls xls = new PersonUpdateXls();
    xls.setRole("role");

    List<String> errorMessages = personUpdateTransformerService.initialValidate(xls);
    assertThat("Should not return error messages", errorMessages.size(), is(0));
  }

  @Test
  public void testProcessPersonUpdateUpload_duplidateIds() {
    String duplicateId = "1";
    PersonUpdateXls xls1 = new PersonUpdateXls();
    xls1.setTisPersonId(duplicateId);
    PersonUpdateXls xls2 = new PersonUpdateXls();
    xls2.setTisPersonId(duplicateId);

    List<PersonUpdateXls> xlsList = Lists.newArrayList(xls1, xls2);

    personUpdateTransformerService.processUpload(xlsList);
    assertThat("", xlsList.get(0).getErrorMessage(),
        is(String.format(PersonUpdateTransformerService.PERSON_ID_DUPLICATE, duplicateId)));
    assertThat("", xlsList.get(1).getErrorMessage(),
        is(String.format(PersonUpdateTransformerService.PERSON_ID_DUPLICATE, duplicateId)));
  }

  @Test
  public void testProcessPersonUpdateUpload_noError() {
    // Given.
    PersonUpdateXls xls = Mockito.spy(new PersonUpdateXls());
    xls.setRole("role1");
    xls.setTisPersonId("1");
    List<PersonUpdateXls> xlsList = Lists.newArrayList(xls);

    PersonDTO personDto = new PersonDTO();
    personDto.setId(1L);
    personDto.setRole(xls.getRole());
    List<PersonDTO> personDTOList = Lists.newArrayList(personDto);

    when(personMapperMock.toDto(xls)).thenReturn(personDto);
    when(tcsServiceImplMock.patchPeople(Matchers.any())).thenReturn(personDTOList);

    // When.
    personUpdateTransformerService.processUpload(xlsList);
    // Then.
    verify(xls).setSuccessfullyImported(true);
    assertThat("", xlsList.get(0).getErrorMessage(), is(nullValue()));
  }

  @Test
  public void testProcessPersonUpdateUpload_errorMessage() {
    // Given.
    PersonUpdateXls xls = Mockito.spy(new PersonUpdateXls());
    xls.setRole("role1");
    xls.setTisPersonId("1");
    List<PersonUpdateXls> xlsList = Lists.newArrayList(xls);

    PersonDTO personDto = new PersonDTO();
    personDto.setId(1L);
    personDto.setRole(xls.getRole());
    String errorMsg = "error";
    List<String> errorMsgs = Lists.newArrayList(errorMsg);
    personDto.setMessageList(errorMsgs);
    List<PersonDTO> personDTOList = Lists.newArrayList(personDto);

    when(personMapperMock.toDto(xls)).thenReturn(personDto);
    when(tcsServiceImplMock.patchPeople(Matchers.any())).thenReturn(personDTOList);

    // When.
    personUpdateTransformerService.processUpload(xlsList);
    // Then.
    verify(xls).addErrorMessages(errorMsgs);
    assertThat("", xlsList.get(0).getErrorMessage(), is(errorMsg));
  }
}
