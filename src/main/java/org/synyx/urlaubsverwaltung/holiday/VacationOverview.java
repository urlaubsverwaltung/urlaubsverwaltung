package org.synyx.urlaubsverwaltung.holiday;

import org.synyx.urlaubsverwaltung.absence.api.DayAbsence;
import org.synyx.urlaubsverwaltung.person.api.PersonResponse;

import java.util.List;

public class VacationOverview {

    private PersonResponse person;
    private Integer personID;
    private List<DayOfMonth> days;
    private List<DayAbsence> absences;

    public PersonResponse getPerson() {
        return person;
    }

    public void setPerson(PersonResponse person) {
        this.person = person;
    }

    public List<DayOfMonth> getDays() {
        return days;
    }

    public void setDays(List<DayOfMonth> days) {
        this.days = days;
    }

    public Integer getPersonID() {
        return personID;
    }

    public void setPersonID(Integer personID) {
        this.personID = personID;
    }

    public List<DayAbsence> getAbsences() {
        return absences;
    }

    public void setAbsences(List<DayAbsence> absences) {
        this.absences = absences;
    }
}
