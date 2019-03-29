package org.synyx.urlaubsverwaltung.restapi.availability;

import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;


class AvailabilityList {

    private final String personLoginName;
    private final List<DayAvailability> availabilities;

    public AvailabilityList(List<DayAvailability> availabilities, Person person) {

        this.availabilities = availabilities;
        this.personLoginName = person.getLoginName();
    }

    public List<DayAvailability> getAvailabilities() {

        return availabilities;
    }


    public String getPersonLoginName() {

        return personLoginName;
    }
}
