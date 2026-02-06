package com.transformuk.hee.tis.genericupload.service.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.genericupload.api.dto.PersonUpdateXls;
import com.transformuk.hee.tis.genericupload.service.service.mapper.PersonMapper;
import com.transformuk.hee.tis.genericupload.service.service.mapper.TrainerApprovalMapper;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.TrainerApprovalDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ApprovalStatus;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PersonUpdateTransformServiceTest {

  @Mock
  TcsServiceImpl tcsServiceImplMock;
  @Mock
  PersonMapper personMapperMock;
  @Mock
  TrainerApprovalMapper trainerApprovalMapper;
  @InjectMocks
  private PersonUpdateTransformerService personUpdateTransformerService;

  @Test
  public void shouldReturnErrorMessageWhenTrainerApprovalStatusDoesNotExists() {
    PersonUpdateXls xls = new PersonUpdateXls();
    xls.setTisPersonId("1111111");
    xls.setTrainerApprovalStatus("invalid");

    personUpdateTransformerService.processUpload(List.of(xls));
    assertThat("Should validate trainer approval status",
        xls.getErrorMessage(), is(String
            .format(PersonUpdateTransformerService.TRAINER_APPROVAL_STATUS_NOT_EXISTS,
                xls.getTrainerApprovalStatus())));
  }

  @Test
  public void shouldNotReturnErrorMessageWhenTrainerApprovalStatusExists() {
    PersonUpdateXls xls = new PersonUpdateXls();
    xls.setTisPersonId("1111111");
    xls.setRole("role1");
    xls.setTrainerApprovalStatus("CURRENT");

    PersonDTO personDto = new PersonDTO();
    personDto.setId(1111111L);
    personDto.setRole(xls.getRole());

    TrainerApprovalDTO taDto = new TrainerApprovalDTO();
    taDto.setPerson(personDto);
    taDto.setApprovalStatus(ApprovalStatus.CURRENT);

    when(personMapperMock.toDto(xls)).thenReturn(personDto);
    when(trainerApprovalMapper.toDto(xls)).thenReturn(taDto);

    personUpdateTransformerService.processUpload(List.of(xls));
    assertNull(xls.getErrorMessage());
  }

  @Test
  public void shouldReturnErrorMessageWhenCommaExistsInRole() {
    PersonUpdateXls xls = new PersonUpdateXls();
    xls.setTisPersonId("1111111");
    xls.setRole("role1, role2");

    personUpdateTransformerService.processUpload(List.of(xls));
    assertThat("Should validate role",
        xls.getErrorMessage(), is(String
            .format(PersonUpdateTransformerService.ROLE_ERROR_SEPARATOR,
                xls.getRole())));
  }

  @Test
  public void shouldNotReturnErrorMessageWhenCommaDoesNotExistInRole() {
    PersonUpdateXls xls = new PersonUpdateXls();
    xls.setTisPersonId("1111111");
    xls.setRole("role");

    PersonDTO personDto = new PersonDTO();
    personDto.setId(1111111L);
    personDto.setRole(xls.getRole());

    TrainerApprovalDTO taDto = new TrainerApprovalDTO();
    taDto.setPerson(personDto);
    taDto.setApprovalStatus(ApprovalStatus.CURRENT);

    when(personMapperMock.toDto(xls)).thenReturn(personDto);
    when(trainerApprovalMapper.toDto(xls)).thenReturn(taDto);

    personUpdateTransformerService.processUpload(List.of(xls));
    assertNull(xls.getErrorMessage());
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
    when(tcsServiceImplMock.patchPeople(any())).thenReturn(personDTOList);

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
    when(tcsServiceImplMock.patchPeople(any())).thenReturn(personDTOList);

    // When.
    personUpdateTransformerService.processUpload(xlsList);
    // Then.
    verify(xls).addErrorMessages(errorMsgs);
    assertThat("", xlsList.get(0).getErrorMessage(), is(errorMsg));
  }

  @Test
  public void shouldReturnErrorMessageWhenTisPersonIdInvalid() {
    String nbspId = "111&nbsp1111";
    PersonUpdateXls xls1 = new PersonUpdateXls();
    xls1.setTisPersonId(nbspId);

    String spaceId = "    1111111";
    PersonUpdateXls xls2 = new PersonUpdateXls();
    xls2.setTisPersonId(spaceId);

    String nonNumericId = "111a111";
    PersonUpdateXls xls3 = new PersonUpdateXls();
    xls3.setTisPersonId(nonNumericId);

    List<PersonUpdateXls> xlsList = List.of(xls1, xls2, xls3);

    personUpdateTransformerService.processUpload(xlsList);

    assertThat(xlsList.get(0).getErrorMessage(),
        is(String.format(PersonUpdateTransformerService.PERSON_ID_MUST_BE_VALID,
            nbspId)));
    assertThat(xlsList.get(1).getErrorMessage(),
        is(String.format(PersonUpdateTransformerService.PERSON_ID_MUST_BE_VALID,
            spaceId)));
    assertThat(xlsList.get(2).getErrorMessage(),
        is(String.format(PersonUpdateTransformerService.PERSON_ID_MUST_BE_VALID,
            nonNumericId)));

    verify(personMapperMock, never()).toDto(any());
    verify(tcsServiceImplMock, never()).patchPeople(any());
  }
}
