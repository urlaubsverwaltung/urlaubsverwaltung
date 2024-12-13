package org.synyx.urlaubsverwaltung.extension.backup.model;


import org.synyx.urlaubsverwaltung.calendar.CompanyCalendar;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Period;

/**
 * @param id             internal id of the calendar
 * @param externalId     external id of the person (owner) of the calendar
 * @param calendarPeriod period of the calendar
 * @param secret         secret to access the calendar
 */
public record CompanyCalendarDTO(Long id, String externalId, Period calendarPeriod, String secret) {
    public CompanyCalendar toCompanyCalendarEntity(Person owner) {
        return new CompanyCalendar(owner, calendarPeriod, secret);
    }
}
