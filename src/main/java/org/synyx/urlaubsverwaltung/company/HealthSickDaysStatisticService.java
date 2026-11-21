package org.synyx.urlaubsverwaltung.company;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.company.SickDaysStatistic.HealthRate;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatisticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

@Service
class HealthSickDaysStatisticService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    private final SickNoteStatisticsService sickNoteStatisticsService;

    HealthSickDaysStatisticService(SickNoteStatisticsService sickNoteStatisticsService) {
        this.sickNoteStatisticsService = sickNoteStatisticsService;
    }

    /**
     *
     * @param viewer id of the person requesting statistics
     * @param from from date, inclusive
     * @param to to date, inclusive
     */
    SickDaysStatistic getSickDaysStatistics(Person viewer, LocalDate from, LocalDate to) {

        final SickNoteStatistics statistics = sickNoteStatisticsService.createStatisticsForPerson(from, to, viewer);

        // the requested range only: the sick rate and the sick days of the statistics cover the whole year
        final Map<Person, BigDecimal> sickDaysByPerson = statistics.getSickDaysByPersonForDateRange(from, to);
        final BigDecimal sickDays = sickDaysByPerson.values().stream().reduce(ZERO, BigDecimal::add);
        final BigDecimal shouldWorkDays = statistics.getShouldWorkDaysForDateRange(from, to);

        final BigDecimal sickRate = shouldWorkDays.signum() == 0 ? ZERO : sickDays.divide(shouldWorkDays, 3, HALF_UP).multiply(ONE_HUNDRED);
        final HealthRate healthRate = HealthRate.of(ONE_HUNDRED.subtract(sickRate));

        final SickDaysStatistic.Distribution distribution = calculateDistribution(statistics, sickDaysByPerson);

        return new SickDaysStatistic(healthRate, sickDays, shouldWorkDays, distribution);
    }

    private SickDaysStatistic.Distribution calculateDistribution(SickNoteStatistics statistics, Map<Person, BigDecimal> sickDaysByPerson) {

        final int numberOfPersons = statistics.getNumberOfPersonsToConsider();

        int zero = numberOfPersons - sickDaysByPerson.size();
        int upToTwo = 0;
        int upToFive = 0;
        int moreThanFive = 0;

        for (BigDecimal sickDays : sickDaysByPerson.values()) {
            if (sickDays.compareTo(BigDecimal.ZERO) <= 0) {
                zero++;
            } else if (sickDays.compareTo(BigDecimal.valueOf(2.0)) <= 0) {
                upToTwo++;
            } else if (sickDays.compareTo(BigDecimal.valueOf(5.0)) <= 0) {
                upToFive++;
            } else {
                moreThanFive++;
            }
        }

        return new SickDaysStatistic.Distribution(numberOfPersons, List.of(
            new SickDaysStatistic.DistributionEntry(null, 0.0, zero),
            new SickDaysStatistic.DistributionEntry(0.5, 2.0, upToTwo),
            new SickDaysStatistic.DistributionEntry(2.0, 5.0, upToFive),
            new SickDaysStatistic.DistributionEntry(5.0, null, moreThanFive)
        ));
    }
}
