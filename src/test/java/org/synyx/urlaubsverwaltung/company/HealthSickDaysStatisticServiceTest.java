package org.synyx.urlaubsverwaltung.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.company.SickDaysStatistic.HealthRate;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatisticsService;

import java.math.BigDecimal;
import java.time.LocalDate;

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

        final SickNoteStatistics statistics = mock(SickNoteStatistics.class);
        when(statistics.getSickRate()).thenReturn(BigDecimal.valueOf(30));
        when(statistics.getTotalNumberOfSickDaysAllCategories()).thenReturn(BigDecimal.valueOf(10));
        when(statistics.getShouldWorkDaysForDateRange(from, to)).thenReturn(BigDecimal.valueOf(20));

        when(sickNoteStatisticsService.createStatisticsForPerson(from, to, viewer)).thenReturn(statistics);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, from, to);

        assertThat(actual).isEqualTo(new SickDaysStatistic(new HealthRate(0.7), BigDecimal.valueOf(10), BigDecimal.valueOf(20)));
    }

    @Test
    void ensureHealthRateIsFullWhenNoSickDays() {

        final Person viewer = createPerson("viewer");
        final LocalDate from = LocalDate.of(2026, JANUARY, 1);
        final LocalDate to = LocalDate.of(2026, JANUARY, 31);

        final SickNoteStatistics statistics = mock(SickNoteStatistics.class);
        when(statistics.getSickRate()).thenReturn(BigDecimal.ZERO);
        when(statistics.getTotalNumberOfSickDaysAllCategories()).thenReturn(BigDecimal.ZERO);
        when(statistics.getShouldWorkDaysForDateRange(from, to)).thenReturn(BigDecimal.valueOf(20));

        when(sickNoteStatisticsService.createStatisticsForPerson(from, to, viewer)).thenReturn(statistics);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, from, to);

        assertThat(actual).isEqualTo(new SickDaysStatistic(new HealthRate(1.0), BigDecimal.ZERO, BigDecimal.valueOf(20)));
    }

    @Test
    void ensureHealthRateIsZeroWhenFullSickRate() {

        final Person viewer = createPerson("viewer");
        final LocalDate from = LocalDate.of(2026, JANUARY, 1);
        final LocalDate to = LocalDate.of(2026, JANUARY, 31);

        final SickNoteStatistics statistics = mock(SickNoteStatistics.class);
        when(statistics.getSickRate()).thenReturn(BigDecimal.valueOf(100));
        when(statistics.getTotalNumberOfSickDaysAllCategories()).thenReturn(BigDecimal.valueOf(20));
        when(statistics.getShouldWorkDaysForDateRange(from, to)).thenReturn(BigDecimal.valueOf(20));

        when(sickNoteStatisticsService.createStatisticsForPerson(from, to, viewer)).thenReturn(statistics);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, from, to);

        assertThat(actual).isEqualTo(new SickDaysStatistic(new HealthRate(0.0), BigDecimal.valueOf(20), BigDecimal.valueOf(20)));
    }

    @Test
    void ensureTotalNumberOfSickDaysIsTakenFromStatistics() {

        final Person viewer = createPerson("viewer");
        final LocalDate from = LocalDate.of(2026, JANUARY, 1);
        final LocalDate to = LocalDate.of(2026, JANUARY, 31);

        final SickNoteStatistics statistics = mock(SickNoteStatistics.class);
        when(statistics.getSickRate()).thenReturn(BigDecimal.valueOf(50));
        when(statistics.getTotalNumberOfSickDaysAllCategories()).thenReturn(BigDecimal.valueOf(12.5));
        when(statistics.getShouldWorkDaysForDateRange(from, to)).thenReturn(BigDecimal.valueOf(21));

        when(sickNoteStatisticsService.createStatisticsForPerson(from, to, viewer)).thenReturn(statistics);

        final SickDaysStatistic actual = sut.getSickDaysStatistics(viewer, from, to);

        assertThat(actual.totalNumberOfAllSickNotes()).isEqualTo(BigDecimal.valueOf(12.5));
        assertThat(actual.shouldWorkDays()).isEqualTo(BigDecimal.valueOf(21));
    }
}
