package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;

import java.time.Duration;
import java.util.List;

import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;

@Component
final class ApplicationMapper {

    private final VacationTypeService vacationTypeService;

    ApplicationMapper(VacationTypeService vacationTypeService) {
        this.vacationTypeService = vacationTypeService;
    }

    Application mapToApplication(ApplicationForLeaveForm applicationForLeaveForm) {

        final Application target = new Application();
        target.setId(applicationForLeaveForm.getId());

        return merge(target, applicationForLeaveForm);
    }

    Application merge(Application applicationForLeave, ApplicationForLeaveForm applicationForLeaveForm) {

        final Long vacationTypeId = applicationForLeaveForm.getVacationType().getId();
        final VacationType<?> vacationType = vacationTypeService.getById(vacationTypeId)
            .orElseThrow(() -> new IllegalStateException("could not find vacationType with id=" + vacationTypeId));

        final Application newApplication = new Application();
        BeanUtils.copyProperties(applicationForLeave, newApplication);

        newApplication.setId(applicationForLeave.getId());
        newApplication.setPerson(applicationForLeaveForm.getPerson());

        newApplication.setStartDate(applicationForLeaveForm.getStartDate());
        newApplication.setStartTime(applicationForLeaveForm.getStartTime());

        newApplication.setEndDate(applicationForLeaveForm.getEndDate());
        newApplication.setEndTime(applicationForLeaveForm.getEndTime());

        newApplication.setVacationType(vacationType);
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
            .toList();
        newApplication.setHolidayReplacements(holidayReplacementEntities);

        return newApplication;
    }
}
