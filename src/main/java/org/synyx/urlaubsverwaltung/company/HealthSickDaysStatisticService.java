package org.synyx.urlaubsverwaltung.company;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.company.SickDaysStatistic.HealthRate;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatisticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
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

        final HealthRate healthRate = HealthRate.of(ONE_HUNDRED.subtract(statistics.getSickRate()));
        final BigDecimal totalNrOfSickDays = statistics.getTotalNumberOfSickDaysAllCategories();
        final BigDecimal shouldWorkDays = statistics.getShouldWorkDaysForDateRange(from, to);

        final SickDaysStatistic.Distribution distribution = calculateDistribution(statistics, from, to);

        return new SickDaysStatistic(healthRate, totalNrOfSickDays, shouldWorkDays, distribution);
    }

    private SickDaysStatistic.Distribution calculateDistribution(SickNoteStatistics statistics, LocalDate from, LocalDate to) {

        final Map<Person, BigDecimal> sickDaysByPerson = statistics.getSickDaysByPersonForDateRange(from, to);
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
