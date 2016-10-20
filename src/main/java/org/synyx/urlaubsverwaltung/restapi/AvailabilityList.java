package org.synyx.urlaubsverwaltung.restapi;

import java.util.List;


/**
 * @author Marc Kannegiesser - kannegiesser@synyx.de
 */
class AvailabilityList {

    private final List<DayAvailability> availabilities;
    private final Integer personId;

    public AvailabilityList(List<DayAvailability> availabilities, Integer personId) {
        this.availabilities = availabilities;
        this.personId = personId;
    }

    public List<DayAvailability> getAvailabilities() {
        return availabilities;
    }

    public Integer getPerson() {
        return personId;
    }
}
