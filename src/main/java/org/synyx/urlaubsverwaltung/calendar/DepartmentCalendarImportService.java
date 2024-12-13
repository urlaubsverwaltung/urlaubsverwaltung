package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.stereotype.Service;

@Service
public class DepartmentCalendarImportService {

    private final DepartmentCalendarRepository departmentCalendarRepository;

    DepartmentCalendarImportService(DepartmentCalendarRepository departmentCalendarRepository) {
        this.departmentCalendarRepository = departmentCalendarRepository;
    }

    public void deleteAll() {
        departmentCalendarRepository.deleteAll();
    }

    public void importDepartmentCalendar(DepartmentCalendar departmentCalendar) {
        departmentCalendarRepository.save(departmentCalendar);
    }
}
