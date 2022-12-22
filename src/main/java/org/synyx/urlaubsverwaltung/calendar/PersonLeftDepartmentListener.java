package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.PersonLeftDepartmentEvent;

@Component
class PersonLeftDepartmentListener {

    private final DepartmentCalendarService departmentCalendarService;

    @Autowired
    PersonLeftDepartmentListener(DepartmentCalendarService departmentCalendarService) {
        this.departmentCalendarService = departmentCalendarService;
    }

    @Async
    @EventListener
    public void handlePersonDisabledEvent(PersonLeftDepartmentEvent event) {
        final long personId = event.getPersonId();
        final long departmentId = event.getDepartmentId();

        departmentCalendarService.deleteCalendarForDepartmentAndPerson(departmentId, personId);
    }
}
