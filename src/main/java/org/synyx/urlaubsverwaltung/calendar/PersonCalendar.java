package org.synyx.urlaubsverwaltung.calendar;

import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import static javax.persistence.CascadeType.REMOVE;

@Entity
class PersonCalendar extends Calendar {

    @NotNull
    @OneToOne(cascade = REMOVE)
    private Person person;

    public PersonCalendar() {
        // for hibernate - do not use this
    }

    PersonCalendar(Person person) {
        super();
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
