package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.ApplicationEvent;

public class PersonDisabledEvent extends ApplicationEvent {

    private final int personId;

    public PersonDisabledEvent(Object source, int personId) {
        super(source);
        this.personId = personId;
    }

    public int getPersonId() {
        return personId;
    }
}
