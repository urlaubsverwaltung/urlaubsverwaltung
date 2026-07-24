package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
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

    // PersonServiceImpl.update() publishes this event from within a transaction; since this listener
    // reads the person back from the DB on a separate (@Async) thread, it must wait until that
    // transaction has committed, otherwise the person may not be visible yet.
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void handlePersonDisabledEvent(PersonDisabledEvent event) {
        final long personId = event.getPersonId();

        personCalendarService.deletePersonalCalendarForPerson(personId);
        departmentCalendarService.deleteDepartmentsCalendarsForPerson(personId);
        companyCalendarService.deleteCalendarForPerson(personId);
    }
}
