package org.synyx.urlaubsverwaltung.application.web;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.application.domain.Application;

import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;

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
            .hours(application.getHours())
            .person(application.getPerson())
            .holidayReplacement(application.getHolidayReplacement())
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

        newApplication.setPerson(applicationForLeaveForm.getPerson());

        newApplication.setStartDate(applicationForLeaveForm.getStartDate());
        newApplication.setStartTime(applicationForLeaveForm.getStartTime());

        newApplication.setEndDate(applicationForLeaveForm.getEndDate());
        newApplication.setEndTime(applicationForLeaveForm.getEndTime());

        newApplication.setVacationType(applicationForLeaveForm.getVacationType());
        newApplication.setDayLength(applicationForLeaveForm.getDayLength());
        newApplication.setReason(applicationForLeaveForm.getReason());
        newApplication.setHolidayReplacement(applicationForLeaveForm.getHolidayReplacement());
        newApplication.setAddress(applicationForLeaveForm.getAddress());
        newApplication.setTeamInformed(applicationForLeaveForm.isTeamInformed());

        if (OVERTIME.equals(newApplication.getVacationType().getCategory())) {
            newApplication.setHours(applicationForLeaveForm.getHours());
        }

        return newApplication;
    }
}
