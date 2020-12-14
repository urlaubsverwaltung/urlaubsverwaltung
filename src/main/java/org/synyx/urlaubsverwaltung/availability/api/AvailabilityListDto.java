package org.synyx.urlaubsverwaltung.availability.api;

import java.util.List;

@Deprecated(forRemoval = true, since = "4.4.0")
class AvailabilityListDto {

    private final Integer personId;
    private final List<DayAvailability> availabilities;

    AvailabilityListDto(List<DayAvailability> availabilities, Integer personId) {

        this.availabilities = availabilities;
        this.personId = personId;
    }

    public List<DayAvailability> getAvailabilities() {

        return availabilities;
    }


    public Integer getPersonId() {

        return personId;
    }
}
