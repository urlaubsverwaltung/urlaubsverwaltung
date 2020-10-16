package org.synyx.urlaubsverwaltung.application.web;

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
        applicationForLeave.setPerson(applicationForLeaveForm.getPerson());

        applicationForLeave.setStartDate(applicationForLeaveForm.getStartDate());
        applicationForLeave.setStartTime(applicationForLeaveForm.getStartTime());

        applicationForLeave.setEndDate(applicationForLeaveForm.getEndDate());
        applicationForLeave.setEndTime(applicationForLeaveForm.getEndTime());

        applicationForLeave.setVacationType(applicationForLeaveForm.getVacationType());
        applicationForLeave.setDayLength(applicationForLeaveForm.getDayLength());
        applicationForLeave.setReason(applicationForLeaveForm.getReason());
        applicationForLeave.setHolidayReplacement(applicationForLeaveForm.getHolidayReplacement());
        applicationForLeave.setAddress(applicationForLeaveForm.getAddress());
        applicationForLeave.setTeamInformed(applicationForLeaveForm.isTeamInformed());

        if (OVERTIME.equals(applicationForLeave.getVacationType().getCategory())) {
            applicationForLeave.setHours(applicationForLeaveForm.getHours());
        }

        return applicationForLeave;
    }
}
