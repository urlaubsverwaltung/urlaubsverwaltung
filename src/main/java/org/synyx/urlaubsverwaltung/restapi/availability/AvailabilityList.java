package org.synyx.urlaubsverwaltung.restapi.availability;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;


/**
 * @author  Marc Kannegiesser - kannegiesser@synyx.de
 */
class AvailabilityList {

    private final String personId;
    private final List<DayAvailability> availabilities;

    public AvailabilityList(List<DayAvailability> availabilities, Person person) {

        this.availabilities = availabilities;
        this.personId = person.getLoginName();
    }

    public List<DayAvailability> getAvailabilities() {

        return availabilities;
    }


    public String getPersonId() {

        return personId;
    }
}
