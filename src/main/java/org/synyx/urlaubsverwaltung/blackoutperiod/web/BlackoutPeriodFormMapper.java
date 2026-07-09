package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriod;
import org.synyx.urlaubsverwaltung.department.Department;

import java.util.List;
import java.util.Set;

final class BlackoutPeriodFormMapper {

    private BlackoutPeriodFormMapper() {
        // ok
    }

    static BlackoutPeriodForm mapToForm(BlackoutPeriod blackoutPeriod) {

        final BlackoutPeriodForm form = new BlackoutPeriodForm();
        form.setId(blackoutPeriod.getId());
        form.setTitle(blackoutPeriod.getTitle());
        form.setStartDate(blackoutPeriod.getStartDate());
        form.setEndDate(blackoutPeriod.getEndDate());
        form.setDepartmentIds(blackoutPeriod.getDepartments().stream().map(Department::getId).toList());
        form.setVacationTypeIds(blackoutPeriod.getVacationTypes().stream().map(VacationType::getId).toList());

        return form;
    }

    static BlackoutPeriod mapToBlackoutPeriod(BlackoutPeriodForm form, List<Department> allDepartments, List<VacationType<?>> allVacationTypes) {

        final Set<Long> selectedDepartmentIds = Set.copyOf(form.getDepartmentIds());
        final Set<Long> selectedVacationTypeIds = Set.copyOf(form.getVacationTypeIds());

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(form.getId());
        blackoutPeriod.setTitle(form.getTitle());
        blackoutPeriod.setStartDate(form.getStartDate());
        blackoutPeriod.setEndDate(form.getEndDate());
        blackoutPeriod.setDepartments(allDepartments.stream().filter(d -> selectedDepartmentIds.contains(d.getId())).toList());
        blackoutPeriod.setVacationTypes(allVacationTypes.stream().filter(vt -> selectedVacationTypeIds.contains(vt.getId())).toList());

        return blackoutPeriod;
    }
}
