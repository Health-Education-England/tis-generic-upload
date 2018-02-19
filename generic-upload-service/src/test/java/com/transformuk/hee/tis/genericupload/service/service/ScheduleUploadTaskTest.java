package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyGroupDTO;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;

public class ScheduleUploadTaskTest {
    @Test
    public void canBuildAPersonDTOFromPersonXLS() throws ParseException {
        ScheduledUploadTask scheduledUploadTask =  new ScheduledUploadTask(null, null);
        PersonXLS personXLS = PersonDTOHelper.getPersonXLS();
        SpecialtyGroupDTO specialtyGroupDTO = PersonDTOHelper.createSpecialtyGroupDTO();
        SpecialtyDTO specialtyDTO = PersonDTOHelper.createSpecialtyDTO(specialtyGroupDTO);
        PersonDTO personDTO = scheduledUploadTask.getPersonDTO(personXLS,
                PersonDTOHelper.getCurriculumDTO(personXLS.getCurriculum1(),specialtyDTO),
                PersonDTOHelper.getCurriculumDTO(personXLS.getCurriculum2(),specialtyDTO),
                PersonDTOHelper.getCurriculumDTO(personXLS.getCurriculum3(),specialtyDTO),
                PersonDTOHelper.createProgrammeDTO(personXLS.getProgrammeName(), personXLS.getProgrammeNumber()));
        Assert.assertEquals(personDTO.getContactDetails().getForenames(), personXLS.getForenames());
        Assert.assertEquals(personDTO.getStatus().toString(), personXLS.getRecordStatus().toLowerCase());

        //TODO more asserts !
    }
}
