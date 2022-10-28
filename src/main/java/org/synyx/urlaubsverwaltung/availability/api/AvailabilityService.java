package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 * Service to retrieve {@link DayAvailability} information.
 */
@Deprecated(forRemoval = true, since = "4.4.0")
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
    AvailabilityListDto getPersonsAvailabilities(LocalDate startDate, LocalDate endDate, Person person) {

        List<DayAvailability> availabilities = new ArrayList<>();

        LocalDate currentDay = startDate;

        while (!currentDay.isAfter(endDate)) {
            TimedAbsenceSpans absences = freeTimeAbsenceProvider.checkForAbsence(person, currentDay);
            BigDecimal presenceRatio = absences.calculatePresenceRatio();

            availabilities.add(new DayAvailability(presenceRatio, currentDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), absences));

            currentDay = currentDay.plusDays(1);
        }

        return new AvailabilityListDto(availabilities, person.getId());
    }
}
