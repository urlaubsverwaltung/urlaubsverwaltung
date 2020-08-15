package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import org.synyx.urlaubsverwaltung.person.api.PersonDto;

import java.util.List;

/**
 * @deprecated this class has been used for the client side rendered vacation overview which is obsolete now.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
public class VacationOverviewDto {

    private PersonDto person;
    private Integer personID;
    private List<DayOfMonth> days;

    public PersonDto getPerson() {
        return person;
    }

    public void setPerson(PersonDto person) {
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
}
