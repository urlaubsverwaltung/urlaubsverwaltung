package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.person.PersonDisabledEvent;

@Component
class PersonDisabledListener {

    private final PersonCalendarService personCalendarService;
    private final DepartmentCalendarService departmentCalendarService;
    private final CompanyCalendarService companyCalendarService;

    @Autowired
    PersonDisabledListener(PersonCalendarService personCalendarService, DepartmentCalendarService departmentCalendarService, CompanyCalendarService companyCalendarService) {
        this.personCalendarService = personCalendarService;
        this.departmentCalendarService = departmentCalendarService;
        this.companyCalendarService = companyCalendarService;
    }

    @Async
    @EventListener
    public void handlePersonDisabledEvent(PersonDisabledEvent event) {
        final long personId = event.getPersonId();

        personCalendarService.deletePersonalCalendarForPerson(personId);
        departmentCalendarService.deleteDepartmentsCalendarsForPerson(personId);
        companyCalendarService.deleteCalendarForPerson(personId);
    }
}
