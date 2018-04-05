package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PersonXLS;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.QualificationDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyGroupDTO;
import org.junit.Assert;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;

public class ScheduleUploadTaskTest {
    @Test
    public void canBuildAPersonDTOFromPersonXLS() throws ParseException {
        ScheduledUploadTask scheduledUploadTask = new ScheduledUploadTask(null, null, null);
        PersonXLS personXLS = PersonDTOHelper.getPersonXLS();
        SpecialtyGroupDTO specialtyGroupDTO = PersonDTOHelper.createSpecialtyGroupDTO();
        SpecialtyDTO specialtyDTO = PersonDTOHelper.createSpecialtyDTO(specialtyGroupDTO);
        PersonDTO personDTO = scheduledUploadTask.getPersonDTO(personXLS,
            PersonDTOHelper.getCurriculumDTO(personXLS.getCurriculum1(), specialtyDTO),
            PersonDTOHelper.getCurriculumDTO(personXLS.getCurriculum2(), specialtyDTO),
            PersonDTOHelper.getCurriculumDTO(personXLS.getCurriculum3(), specialtyDTO),
            PersonDTOHelper.createProgrammeDTO(personXLS.getProgrammeName(), personXLS.getProgrammeNumber()));
        Assert.assertEquals(personDTO.getContactDetails().getForenames(), personXLS.getForenames());
        Assert.assertEquals(personDTO.getStatus().toString(), personXLS.getRecordStatus().toLowerCase());
    }

    @Test
    public void buildsANullQualificationIfFieldsAreNull() throws ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ScheduledUploadTask scheduledUploadTask = new ScheduledUploadTask(null, null, null);

        PersonXLS personXLS = PersonDTOHelper.getPersonXLS();
        personXLS.setCountryOfQualification(null);
        personXLS.setQualification(null);
        personXLS.setDateAttained(null);
        personXLS.setMedicalSchool(null);

        QualificationDTO result = getQualificationDTO(scheduledUploadTask, personXLS);
        assertThat(result).isNull();
    }

    @Test
    public void buildsAQualificationIfSomeFieldsAreNotNull() throws ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ScheduledUploadTask scheduledUploadTask = new ScheduledUploadTask(null, null, null);

        PersonXLS personXLS = PersonDTOHelper.getPersonXLS();
        personXLS.setCountryOfQualification(null);
        personXLS.setQualification("Medical");
        personXLS.setDateAttained(null);
        personXLS.setMedicalSchool(null);

        QualificationDTO result = getQualificationDTO(scheduledUploadTask, personXLS);
        assertThat(result).isNotNull();
    }

    //have to use reflection to invoke
    public QualificationDTO getQualificationDTO(ScheduledUploadTask scheduledUploadTask, PersonXLS personXLS) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class[] parameterTypes = new Class[1];
        parameterTypes[0] = PersonXLS.class;
        Method m = ScheduledUploadTask.class.getDeclaredMethod("getQualificationDTO", parameterTypes);
        m.setAccessible(true);
        return (QualificationDTO) m.invoke(scheduledUploadTask, personXLS);
    }
}