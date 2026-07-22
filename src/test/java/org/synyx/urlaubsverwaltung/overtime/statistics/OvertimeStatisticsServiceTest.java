package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeId;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OvertimeStatisticsServiceTest {

    private OvertimeStatisticsService sut;

    @Mock
    private OvertimeService overtimeService;
    @Mock
    private PersonService personService;

    private static final Clock clock = Clock.fixed(Instant.parse("2024-06-15T00:00:00.00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        sut = new OvertimeStatisticsService(overtimeService, personService, clock);
    }

    @Test
    void ensureCreateStatisticsForCurrentYearUsesTodayAsReferenceDate() {

        final Year year = Year.now(clock);

        final Person person = new Person();
        person.setId(1L);
        when(personService.getAllPersonsHavingAccountInYear(year)).thenReturn(List.of(person));

        final Overtime overtime = new Overtime(new OvertimeId(1L), person.getIdAsPersonId(),
            new DateRange(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 10)),
            Duration.ofHours(8), OvertimeType.UV_INTERNAL, Instant.now(clock));

        when(overtimeService.getOvertimeRecordsForPersonsAndDateRange(List.of(person), DateRange.ofYear(year)))
            .thenReturn(List.of(overtime));

        final OvertimeStatistics statistics = sut.createStatistics(year);

        assertThat(statistics.getYear()).isEqualTo(2024);
        assertThat(statistics.getReferenceDate()).isEqualTo(LocalDate.of(2024, 6, 15));
    }

    @Test
    void ensureCreateStatisticsForPastYearUsesLastDayOfYearAsReferenceDate() {

        final Year year = Year.of(2022);

        final Person person = new Person();
        person.setId(1L);
        when(personService.getAllPersonsHavingAccountInYear(year)).thenReturn(List.of(person));
        when(overtimeService.getOvertimeRecordsForPersonsAndDateRange(List.of(person), DateRange.ofYear(year)))
            .thenReturn(List.of());

        final OvertimeStatistics statistics = sut.createStatistics(year);

        assertThat(statistics.getYear()).isEqualTo(2022);
        assertThat(statistics.getReferenceDate()).isEqualTo(LocalDate.of(2022, 12, 31));
    }
}
