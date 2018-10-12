package com.transformuk.hee.tis.genericupload.service.config;

import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class MapperConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(MapperConfiguration.class);

    static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy");

    protected List<Object> existingPersons;

    protected static String convertLongToString(Long longParam) {
        String result = null;
        if (longParam != null) {
            result = longParam.toString();
        }
        return result;
    }

    public static LocalDate convertDate(Date date) throws IllegalArgumentException {
        LocalDate localDate = null;
        if (date != null) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            // As agreed with TIS head business analysis, we should only allow years after 1753
            // because the NDW uses SQL Server for which the lowest allowed date field year is 1753
            if (cal.get(Calendar.YEAR) < 1753) {
                throw new IllegalArgumentException("The year cannot be less than 1753");
            }

            //plus 1 on month as month starts from zero in a Calendar object
            localDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        }
        return localDate;
    }

    public static LocalDateTime convertToLocalDateTime(String date, DateTimeFormatter dateTimeFormatter) {
        LocalDateTime localDate = null;
        if (date != null) {
            return LocalDateTime.from(LocalDate.parse(date, dateTimeFormatter).atStartOfDay());
        }
        return localDate;
    }

    public static LocalDate convertDate(String date) {
        LocalDate localDate = null;
        if (date != null) {
            return LocalDate.parse(date, dateTimeFormatter);
        }
        return localDate;
    }

    public static LocalDateTime convertDateTime(Date date) {
        LocalDateTime localDateTime = null;
        if (date != null) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            //plus 1 on month as month starts from zero in a Calendar object
            localDateTime = LocalDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }
        return localDateTime;
    }

    protected static Status convertStatus(String statusString) {
        Status status = null;
        if ("Current".equalsIgnoreCase(statusString)) {
            status = Status.CURRENT;
        } else if ("Inactive".equalsIgnoreCase(statusString)) {
            status = Status.INACTIVE;
        } else if ("Delete".equalsIgnoreCase(statusString)) {
            status = Status.DELETE;
        } else {
            LOG.warn("Status {} was found but not accounted for in if statements", statusString);
        }
        return status;
    }

    public List<Object> getExistingPersons() {
        return existingPersons;
    }

    public void setExistingPersons(List<Object> existingPersons) {
        this.existingPersons = existingPersons;
    }
}
