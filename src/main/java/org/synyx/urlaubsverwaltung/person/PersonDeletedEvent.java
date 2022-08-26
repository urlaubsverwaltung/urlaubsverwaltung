package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.ApplicationEvent;

public class PersonDeletedEvent {

    private final Person person;

    public PersonDeletedEvent(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
