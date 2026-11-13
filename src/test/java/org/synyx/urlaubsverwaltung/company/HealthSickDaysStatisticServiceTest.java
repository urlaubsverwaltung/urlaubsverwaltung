package org.synyx.urlaubsverwaltung.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.company.SickDaysStatistic.Distribution;
import org.synyx.urlaubsverwaltung.company.SickDaysStatistic.HealthRate;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatisticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;

@ExtendWith(MockitoExtension.class)
class HealthSickDaysStatisticServiceTest {

    private static final LocalDate FROM = LocalDate.of(2026, NOVEMBER, 1);
    private static final LocalDate TO = LocalDate.of(2026, NOVEMBER, 13);

    private HealthSickDaysStatisticService sut;

    @Mock
    private SickNoteStatisticsService sickNoteStatisticsService;

    private final Person viewer = createPerson("viewer");

    @BeforeEach
    void setUp() {
        sut = new HealthSickDaysStatisticService(sickNoteStatisticsService);
    }

    @Test
    void ensureHealthRateAndSickDaysOfTheRequestedRange() {

        // 9 sick days of 90 target work days in the range: 10% sick, 90% healthy.
        // the statistics of the year (sick rate since january, sick days of whole sick notes) are not asked for.
        statistics(BigDecimal.valueOf(90), Map.of(createPerson(1L), BigDecimal.valueOf(4), createPerson(2L), BigDecimal.valueOf(5)), 10);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, FROM, TO);

        assertThat(actual.healthRate()).isEqualTo(new HealthRate(0.9));
        assertThat(actual.totalNumberOfAllSickNotes()).isEqualByComparingTo(BigDecimal.valueOf(9));
        assertThat(actual.shouldWorkDays()).isEqualByComparingTo(BigDecimal.valueOf(90));
    }

    @Test
    void ensureHealthRateIsFullWhenNoSickDays() {

        statistics(BigDecimal.valueOf(90), Map.of(createPerson(1L), BigDecimal.ZERO), 1);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, FROM, TO);

        assertThat(actual.healthRate()).isEqualTo(new HealthRate(1.0));
        assertThat(actual.totalNumberOfAllSickNotes()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void ensureHealthRateIsFullWhenNoWorkDays() {

        statistics(BigDecimal.ZERO, Map.of(), 0);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, FROM, TO);

        assertThat(actual.healthRate()).isEqualTo(new HealthRate(1.0));
    }

    @Test
    void ensureHealthRateIsZeroWhenSickOnEveryWorkDay() {

        statistics(BigDecimal.valueOf(10), Map.of(createPerson(1L), BigDecimal.valueOf(10)), 1);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, FROM, TO);

        assertThat(actual.healthRate()).isEqualTo(new HealthRate(0.0));
    }

    @Test
    void ensureDistributionBucketsPersonsBySickDaysInRange() {

        final Person personWithTwoSickDays = createPerson(1L);
        final Person personWithFiveSickDays = createPerson(2L);
        final Person personWithTenSickDays = createPerson(3L);

        statistics(BigDecimal.valueOf(100), Map.of(
            personWithTwoSickDays, BigDecimal.valueOf(2),
            personWithFiveSickDays, BigDecimal.valueOf(5),
            personWithTenSickDays, BigDecimal.valueOf(10)
        ), 5);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, FROM, TO);

        // persons without sick days are not in the map but counted via numberOfPersonsToConsider - map.size()
        final Distribution expected = new Distribution(5, List.of(
            new SickDaysStatistic.DistributionEntry(null, 0.0, 2),
            new SickDaysStatistic.DistributionEntry(0.5, 2.0, 1),
            new SickDaysStatistic.DistributionEntry(2.0, 5.0, 1),
            new SickDaysStatistic.DistributionEntry(5.0, null, 1)
        ));

        assertThat(actual.distribution()).isEqualTo(expected);
    }

    private void statistics(BigDecimal shouldWorkDays, Map<Person, BigDecimal> sickDaysByPerson, int numberOfPersonsToConsider) {
        final SickNoteStatistics statistics = mock(SickNoteStatistics.class);
        when(statistics.getShouldWorkDaysForDateRange(FROM, TO)).thenReturn(shouldWorkDays);
        when(statistics.getSickDaysByPersonForDateRange(FROM, TO)).thenReturn(sickDaysByPerson);
        when(statistics.getNumberOfPersonsToConsider()).thenReturn(numberOfPersonsToConsider);
        when(sickNoteStatisticsService.createStatisticsForPerson(FROM, TO, viewer)).thenReturn(statistics);
    }
}
