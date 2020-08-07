package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
     * Fetch an {@link AvailabilityListDto} for the given person on all days in the given period of time.
     *
     * @param startDate start date of the of the requested availability duration
     * @param endDate   end date of the of the requested availability duration
     * @param person    to receive the availability information
     * @return a {@link AvailabilityListDto availability list} of the requested person
     */
    AvailabilityListDto getPersonsAvailabilities(Instant startDate, Instant endDate, Person person) {

        List<DayAvailability> availabilities = new ArrayList<>();

        Instant currentDay = startDate;

        while (!currentDay.isAfter(endDate)) {
            TimedAbsenceSpans absences = freeTimeAbsenceProvider.checkForAbsence(person, currentDay);
            BigDecimal presenceRatio = absences.calculatePresenceRatio();

            availabilities.add(new DayAvailability(presenceRatio, DateTimeFormatter.ofPattern("yyyy-MM-dd").format(currentDay), absences));

            currentDay = currentDay.plus(1, ChronoUnit.DAYS);
        }

        return new AvailabilityListDto(availabilities, person.getId());
    }
}
