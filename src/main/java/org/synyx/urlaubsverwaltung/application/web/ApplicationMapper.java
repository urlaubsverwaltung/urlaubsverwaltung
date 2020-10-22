package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;

final class ApplicationMapper {

    private ApplicationMapper() {
        // prevents init
    }

    static Application mapToApplication(ApplicationForLeaveForm applicationForLeaveForm) {

        Application applicationForLeave = new Application();

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

        if (VacationCategory.OVERTIME.equals(applicationForLeave.getVacationType().getCategory())) {
            applicationForLeave.setHours(applicationForLeaveForm.getHours());
        }

        return applicationForLeave;
    }
}
