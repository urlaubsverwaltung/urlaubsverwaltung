package org.synyx.urlaubsverwaltung.company;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.company.SickDaysStatistic.HealthRate;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatisticsService;

import java.math.BigDecimal;
import java.time.LocalDate;

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

        return new SickDaysStatistic(healthRate);
    }
}
