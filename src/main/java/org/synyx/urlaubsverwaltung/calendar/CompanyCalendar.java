package org.synyx.urlaubsverwaltung.calendar;

import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
class CompanyCalendar extends Calendar {

    @NotNull
    @OneToOne
    private Person person;

    protected CompanyCalendar() {
        // for hibernate - do not use this
    }

    CompanyCalendar(Person person) {
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
