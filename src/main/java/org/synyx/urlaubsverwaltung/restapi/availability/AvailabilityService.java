package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Service to retrieve {@link DayAvailability} information.
 */
@Service
public class AvailabilityService {

    private final FreeTimeAbsenceProvider freeTimeAbsenceProvider;

    @Autowired
    public AvailabilityService(FreeTimeAbsenceProvider freeTimeAbsenceProvider) {

        this.freeTimeAbsenceProvider = freeTimeAbsenceProvider;
    }

    /**
     * Fetch an {@link AvailabilityList} for the given person on all days in the given period of time.
     *
     * @param startDate start date of the of the requested availability duration
     * @param endDate end date of the of the requested availability duration
     * @param person to receive the availability information
     *
     * @return a {@link AvailabilityList availability list} of the requested person
     */
    public AvailabilityList getPersonsAvailabilities(DateMidnight startDate, DateMidnight endDate, Person person) {

        List<DayAvailability> availabilities = new ArrayList<>();

        DateMidnight currentDay = startDate;

        while (!currentDay.isAfter(endDate)) {
            TimedAbsenceSpans absences = freeTimeAbsenceProvider.checkForAbsence(person, currentDay);
            BigDecimal presenceRatio = absences.calculatePresenceRatio();

            availabilities.add(new DayAvailability(presenceRatio, currentDay.toString("yyyy-MM-dd"), absences));

            currentDay = currentDay.plusDays(1);
        }

        return new AvailabilityList(availabilities, person);
    }
}
