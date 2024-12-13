package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.calendar.DepartmentCalendar;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Period;

/**
 * @param id             internal id of the calendar
 * @param externalId     external id of the person (owner) of the calendar
 * @param departmentId   id of the department
 * @param calendarPeriod period of the calendar
 * @param secret         secret to access the calendar
 */
public record DepartmentCalendarDTO(Long id, String externalId, Long departmentId, Period calendarPeriod,
                                    String secret) {
    public DepartmentCalendar toDepartmentCalendarEntity(Long idOfCreatedDepartment, Person owner) {
        return new DepartmentCalendar(idOfCreatedDepartment, owner, this.calendarPeriod, this.secret);
    }
}
