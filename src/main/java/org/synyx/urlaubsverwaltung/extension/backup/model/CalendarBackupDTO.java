package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.calendar.CompanyCalendarAccessible;

import java.util.List;

public record CalendarBackupDTO(List<PersonCalendarDTO> personCalendars, List<CompanyCalendarDTO> companyCalendars,
                                List<DepartmentCalendarDTO> departmentCalendars, boolean companyCalendarAccessible) {

    public CompanyCalendarAccessible toCompanyCalendarAccessibleEntity() {
        final CompanyCalendarAccessible entity = new CompanyCalendarAccessible();
        entity.setAccessible(this.companyCalendarAccessible());
        return entity;
    }
}
