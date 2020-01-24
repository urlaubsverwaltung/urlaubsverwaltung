package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.ApplicationEvent;

class PersonUpdatedEvent extends ApplicationEvent {

    private final transient Person personBeforeUpdate;
    private final transient Person personAfterUpdate;

    PersonUpdatedEvent(Object source, Person personBeforeUpdate, Person personAfterUpdate) {
        super(source);
        this.personBeforeUpdate = personBeforeUpdate;
        this.personAfterUpdate = personAfterUpdate;
    }

    Person getPersonBeforeUpdate() {
        return personBeforeUpdate;
    }

    Person getPersonAfterUpdate() {
        return personAfterUpdate;
    }
}
