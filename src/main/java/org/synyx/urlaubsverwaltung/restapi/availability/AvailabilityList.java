package org.synyx.urlaubsverwaltung.restapi.availability;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.restapi.PersonResponse;

import java.util.List;


/**
 * @author  Marc Kannegiesser - kannegiesser@synyx.de
 */
class AvailabilityList {

    private final List<DayAvailability> availabilities;
    private final PersonResponse personResponse;

    public AvailabilityList(List<DayAvailability> availabilities, Person person) {

        this.availabilities = availabilities;
        this.personResponse = new PersonResponse(person);
    }

    public List<DayAvailability> getAvailabilities() {

        return availabilities;
    }


    public PersonResponse getPersonResponse() {

        return personResponse;
    }
}
