package org.synyx.urlaubsverwaltung.person;

public class PersonDeletedEvent {

    private final Person person;

    public PersonDeletedEvent(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
