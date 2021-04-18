package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.person.Person;

public class HolidayReplacementDto {

    private Person person;
    private String note;

    public HolidayReplacementDto() {
    }

    public HolidayReplacementDto(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "HolidayReplacementDto{" +
            ", person=" + person +
            ", note='" + note + '\'' +
            '}';
    }
}
