package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.person.PersonDisabledEvent;

@Component
class PersonDisabledListener {

    private final PersonCalendarService personCalendarService;

    @Autowired
    PersonDisabledListener(PersonCalendarService personCalendarService) {
        this.personCalendarService = personCalendarService;
    }

    @Async
    @EventListener
    public void handlePersonDisabledEvent(PersonDisabledEvent event) {
        final int personId = event.getPersonId();

        personCalendarService.deletePersonalCalendarForPerson(personId);
    }
}
