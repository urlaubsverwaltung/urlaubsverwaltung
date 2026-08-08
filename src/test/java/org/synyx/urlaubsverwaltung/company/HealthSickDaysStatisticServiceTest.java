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

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;

@ExtendWith(MockitoExtension.class)
class HealthSickDaysStatisticServiceTest {

    private HealthSickDaysStatisticService sut;

    @Mock
    private SickNoteStatisticsService sickNoteStatisticsService;

    @BeforeEach
    void setUp() {
        sut = new HealthSickDaysStatisticService(sickNoteStatisticsService);
    }

    @Test
    void ensureHealthRateIsComplementOfSickRate() {

        final Person viewer = createPerson("viewer");
        final LocalDate from = LocalDate.of(2026, JANUARY, 1);
        final LocalDate to = LocalDate.of(2026, JANUARY, 31);

        final BigDecimal shouldWorkDays = BigDecimal.valueOf(20);
        final BigDecimal totalNumberOfAllSickNotes = BigDecimal.valueOf(10);

        final SickNoteStatistics statistics = mock(SickNoteStatistics.class);
        when(statistics.getSickRate()).thenReturn(BigDecimal.valueOf(30));
        when(statistics.getTotalNumberOfSickDaysAllCategories()).thenReturn(totalNumberOfAllSickNotes);
        when(statistics.getShouldWorkDaysForDateRange(from, to)).thenReturn(shouldWorkDays);
        when(statistics.getSickDaysByPersonForDateRange(from, to)).thenReturn(Map.of());
        when(statistics.getNumberOfPersonsToConsider()).thenReturn(0);

        when(sickNoteStatisticsService.createStatisticsForPerson(from, to, viewer)).thenReturn(statistics);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, from, to);

        final HealthRate healthRate = new HealthRate(0.7);
        final Distribution distribution = new Distribution(0, List.of(
            new SickDaysStatistic.DistributionEntry(null, 0.0, 0),
            new SickDaysStatistic.DistributionEntry(0.5, 2.0, 0),
            new SickDaysStatistic.DistributionEntry(2.0, 5.0, 0),
            new SickDaysStatistic.DistributionEntry(5.0, null, 0)
        ));
        assertThat(actual).isEqualTo(new SickDaysStatistic(healthRate, totalNumberOfAllSickNotes, shouldWorkDays, distribution));
    }

    @Test
    void ensureHealthRateIsFullWhenNoSickDays() {

        final Person viewer = createPerson("viewer");
        final LocalDate from = LocalDate.of(2026, JANUARY, 1);
        final LocalDate to = LocalDate.of(2026, JANUARY, 31);

        final BigDecimal shouldWorkDays = BigDecimal.valueOf(20);
        final BigDecimal totalNumberOfAllSickNotes = BigDecimal.ZERO;

        final SickNoteStatistics statistics = mock(SickNoteStatistics.class);
        when(statistics.getSickRate()).thenReturn(BigDecimal.ZERO);
        when(statistics.getTotalNumberOfSickDaysAllCategories()).thenReturn(totalNumberOfAllSickNotes);
        when(statistics.getShouldWorkDaysForDateRange(from, to)).thenReturn(shouldWorkDays);
        when(statistics.getSickDaysByPersonForDateRange(from, to)).thenReturn(Map.of());
        when(statistics.getNumberOfPersonsToConsider()).thenReturn(0);

        when(sickNoteStatisticsService.createStatisticsForPerson(from, to, viewer)).thenReturn(statistics);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, from, to);

        final HealthRate healthRate = new HealthRate(1.0);
        final Distribution distribution = new Distribution(0, List.of(
            new SickDaysStatistic.DistributionEntry(null, 0.0, 0),
            new SickDaysStatistic.DistributionEntry(0.5, 2.0, 0),
            new SickDaysStatistic.DistributionEntry(2.0, 5.0, 0),
            new SickDaysStatistic.DistributionEntry(5.0, null, 0)
        ));
        assertThat(actual).isEqualTo(new SickDaysStatistic(healthRate, totalNumberOfAllSickNotes, shouldWorkDays, distribution));
    }

    @Test
    void ensureHealthRateIsZeroWhenFullSickRate() {

        final Person viewer = createPerson("viewer");
        final LocalDate from = LocalDate.of(2026, JANUARY, 1);
        final LocalDate to = LocalDate.of(2026, JANUARY, 31);

        final BigDecimal shouldWorkDays = BigDecimal.valueOf(20);
        final BigDecimal totalNumberOfAllSickNotes = BigDecimal.valueOf(20);

        final SickNoteStatistics statistics = mock(SickNoteStatistics.class);
        when(statistics.getSickRate()).thenReturn(BigDecimal.valueOf(100));
        when(statistics.getTotalNumberOfSickDaysAllCategories()).thenReturn(totalNumberOfAllSickNotes);
        when(statistics.getShouldWorkDaysForDateRange(from, to)).thenReturn(shouldWorkDays);
        when(statistics.getSickDaysByPersonForDateRange(from, to)).thenReturn(Map.of());
        when(statistics.getNumberOfPersonsToConsider()).thenReturn(0);

        when(sickNoteStatisticsService.createStatisticsForPerson(from, to, viewer)).thenReturn(statistics);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, from, to);

        final HealthRate healthRate = new HealthRate(0.0);
        final Distribution distribution = new Distribution(0, List.of(
            new SickDaysStatistic.DistributionEntry(null, 0.0, 0),
            new SickDaysStatistic.DistributionEntry(0.5, 2.0, 0),
            new SickDaysStatistic.DistributionEntry(2.0, 5.0, 0),
            new SickDaysStatistic.DistributionEntry(5.0, null, 0)
        ));
        assertThat(actual).isEqualTo(new SickDaysStatistic(healthRate, totalNumberOfAllSickNotes, shouldWorkDays, distribution));
    }

    @Test
    void ensureTotalNumberOfSickDaysIsTakenFromStatistics() {

        final Person viewer = createPerson("viewer");
        final LocalDate from = LocalDate.of(2026, JANUARY, 1);
        final LocalDate to = LocalDate.of(2026, JANUARY, 31);

        final BigDecimal shouldWorkDays = BigDecimal.valueOf(21);
        final BigDecimal totalNumberOfAllSickNotes = BigDecimal.valueOf(12.5);

        final SickNoteStatistics statistics = mock(SickNoteStatistics.class);
        when(statistics.getSickRate()).thenReturn(BigDecimal.valueOf(50));
        when(statistics.getTotalNumberOfSickDaysAllCategories()).thenReturn(totalNumberOfAllSickNotes);
        when(statistics.getShouldWorkDaysForDateRange(from, to)).thenReturn(shouldWorkDays);
        when(statistics.getSickDaysByPersonForDateRange(from, to)).thenReturn(Map.of());
        when(statistics.getNumberOfPersonsToConsider()).thenReturn(0);

        when(sickNoteStatisticsService.createStatisticsForPerson(from, to, viewer)).thenReturn(statistics);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, from, to);

        assertThat(actual.totalNumberOfAllSickNotes()).isEqualTo(totalNumberOfAllSickNotes);
        assertThat(actual.shouldWorkDays()).isEqualTo(shouldWorkDays);
    }

    @Test
    void ensureDistributionBucketsPersonsBySickDaysInRange() {

        final Person viewer = createPerson("viewer");
        final LocalDate from = LocalDate.of(2026, JANUARY, 1);
        final LocalDate to = LocalDate.of(2026, JANUARY, 31);

        final Person personWithTwoSickDays = createPerson("two-sick-days");
        final Person personWithFiveSickDays = createPerson("five-sick-days");
        final Person personWithTenSickDays = createPerson("ten-sick-days");

        final SickNoteStatistics statistics = mock(SickNoteStatistics.class);
        when(statistics.getSickRate()).thenReturn(BigDecimal.ZERO);
        when(statistics.getTotalNumberOfSickDaysAllCategories()).thenReturn(BigDecimal.ZERO);
        when(statistics.getShouldWorkDaysForDateRange(from, to)).thenReturn(BigDecimal.ZERO);
        when(statistics.getNumberOfPersonsToConsider()).thenReturn(5);
        when(statistics.getSickDaysByPersonForDateRange(from, to)).thenReturn(Map.of(
            personWithTwoSickDays, BigDecimal.valueOf(2),
            personWithFiveSickDays, BigDecimal.valueOf(5),
            personWithTenSickDays, BigDecimal.valueOf(10)
        ));

        when(sickNoteStatisticsService.createStatisticsForPerson(from, to, viewer)).thenReturn(statistics);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, from, to);

        // personWithNoSickDays is not in the map but counted via numberOfPersonsToConsider - map.size()
        final Distribution expected = new Distribution(5, List.of(
            new SickDaysStatistic.DistributionEntry(null, 0.0, 2),
            new SickDaysStatistic.DistributionEntry(0.5, 2.0, 1),
            new SickDaysStatistic.DistributionEntry(2.0, 5.0, 1),
            new SickDaysStatistic.DistributionEntry(5.0, null, 1)
        ));

        assertThat(actual.distribution()).isEqualTo(expected);
    }
}
