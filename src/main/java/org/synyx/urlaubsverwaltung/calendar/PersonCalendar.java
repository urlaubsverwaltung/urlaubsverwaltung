package org.synyx.urlaubsverwaltung.calendar;

import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
class PersonCalendar extends Calendar {

    @NotNull
    @OneToOne
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
