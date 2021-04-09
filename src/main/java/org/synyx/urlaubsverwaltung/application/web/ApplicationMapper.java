package org.synyx.urlaubsverwaltung.application.web;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.application.domain.Application;

import java.time.Duration;

import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.SPECIALLEAVE;

final class ApplicationMapper {

    private ApplicationMapper() {
        // prevents init
    }

    static ApplicationForLeaveForm mapToApplicationForm(Application application) {
        return new ApplicationForLeaveForm.Builder()
            .id(application.getId())
            .address(application.getAddress())
            .startDate(application.getStartDate())
            .startTime(application.getStartTime())
            .endDate(application.getEndDate())
            .endTime(application.getEndTime())
            .teamInformed(application.isTeamInformed())
            .dayLength(application.getDayLength())
            .hoursAndMinutes(application.getHours())
            .person(application.getPerson())
            .holidayReplacement(application.getHolidayReplacement())
            .holidayReplacementNote(application.getHolidayReplacementNote())
            .vacationType(application.getVacationType())
            .reason(application.getReason())
            .build();
    }

    static Application mapToApplication(ApplicationForLeaveForm applicationForLeaveForm) {
        return merge(new Application(), applicationForLeaveForm);
    }

    static Application merge(Application applicationForLeave, ApplicationForLeaveForm applicationForLeaveForm) {

        final Application newApplication = new Application();
        BeanUtils.copyProperties(applicationForLeave, newApplication);

        newApplication.setId(applicationForLeave.getId());
        newApplication.setPerson(applicationForLeaveForm.getPerson());

        newApplication.setStartDate(applicationForLeaveForm.getStartDate());
        newApplication.setStartTime(applicationForLeaveForm.getStartTime());

        newApplication.setEndDate(applicationForLeaveForm.getEndDate());
        newApplication.setEndTime(applicationForLeaveForm.getEndTime());

        newApplication.setVacationType(applicationForLeaveForm.getVacationType());
        newApplication.setDayLength(applicationForLeaveForm.getDayLength());
        newApplication.setHolidayReplacement(applicationForLeaveForm.getHolidayReplacement());
        newApplication.setHolidayReplacementNote(applicationForLeaveForm.getHolidayReplacementNote());
        newApplication.setAddress(applicationForLeaveForm.getAddress());
        newApplication.setTeamInformed(applicationForLeaveForm.isTeamInformed());

        if (OVERTIME.equals(newApplication.getVacationType().getCategory())) {
            final Duration overtimeReduction = applicationForLeaveForm.getOvertimeReduction();
            newApplication.setHours(overtimeReduction);
        } else {
            newApplication.setHours(null);
        }

        if (SPECIALLEAVE.equals(newApplication.getVacationType().getCategory())) {
            newApplication.setReason(applicationForLeaveForm.getReason());
        } else {
            newApplication.setReason(null);
        }

        return newApplication;
    }
}
