package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;

import java.sql.Time;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.TimeZone;

final class ApplicationMapper {

    private ApplicationMapper() {
        // prevents init
    }

    static Application mapToApplication(ApplicationForLeaveForm applicationForLeaveForm, TimeZone timeZone) {

        Application applicationForLeave = new Application();

        applicationForLeave.setPerson(applicationForLeaveForm.getPerson());

        applicationForLeave.setStartDate(applicationForLeaveForm.getStartDate());
        applicationForLeave.setStartTime(applicationForLeaveForm.getStartTime());

        applicationForLeave.setStartDateTime(ZonedDateTime.of(applicationForLeaveForm.getStartDate(),
            toLocalTime(applicationForLeaveForm.getStartTime()), timeZone.toZoneId()).toInstant());

        applicationForLeave.setEndDateTime(ZonedDateTime.of(applicationForLeaveForm.getEndDate(),
            toLocalTime(applicationForLeaveForm.getEndTime()), timeZone.toZoneId()).toInstant());

        applicationForLeave.setEndDate(applicationForLeaveForm.getEndDate());
        applicationForLeave.setEndTime(applicationForLeaveForm.getEndTime());

        applicationForLeave.setVacationType(applicationForLeaveForm.getVacationType());
        applicationForLeave.setDayLength(applicationForLeaveForm.getDayLength());
        applicationForLeave.setReason(applicationForLeaveForm.getReason());
        applicationForLeave.setHolidayReplacement(applicationForLeaveForm.getHolidayReplacement());
        applicationForLeave.setAddress(applicationForLeaveForm.getAddress());
        applicationForLeave.setTeamInformed(applicationForLeaveForm.isTeamInformed());

        if (VacationCategory.OVERTIME.equals(applicationForLeave.getVacationType().getCategory())) {
            applicationForLeave.setHours(applicationForLeaveForm.getHours());
        }

        return applicationForLeave;
    }

    private static LocalTime toLocalTime(Time time) {
        return time == null ? LocalTime.MIN : time.toLocalTime();
    }
}
