package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.BeanUtils;

import java.time.Duration;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;

final class ApplicationMapper {

    private ApplicationMapper() {
        // prevents init
    }

    static Application mapToApplication(ApplicationForLeaveForm applicationForLeaveForm) {

        final Application target = new Application();
        target.setId(applicationForLeaveForm.getId());

        return merge(target, applicationForLeaveForm);
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

        final List<HolidayReplacementEntity> holidayReplacementEntities = applicationForLeaveForm.getHolidayReplacements().stream()
            .map(HolidayReplacementEntity::from)
            .collect(toList());
        newApplication.setHolidayReplacements(holidayReplacementEntities);

        return newApplication;
    }
}
