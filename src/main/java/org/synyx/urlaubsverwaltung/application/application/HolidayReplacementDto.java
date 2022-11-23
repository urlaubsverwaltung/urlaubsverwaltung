package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

public class HolidayReplacementDto {

    private Person person;
    private String note;
    private List<String> departments;

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

    public List<String> getDepartments() {
        return departments;
    }

    public void setDepartments(List<String> departments) {
        this.departments = departments;
    }

    @Override
    public String toString() {
        return "HolidayReplacementDto{" +
            "person=" + person +
            ", departments=" + departments +
            '}';
    }
}
