package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    private static final int DAYS_IN_TEST_DATE_RANGE = 8;

    private AvailabilityService sut;

    @Mock
    private FreeTimeAbsenceProvider freeTimeAbsenceProvider;
    @Mock
    private TimedAbsenceSpans timedAbsenceSpans;

    @BeforeEach
    void setUp() {
        sut = new AvailabilityService(freeTimeAbsenceProvider);
    }

    @Test
    void ensureFetchesAvailabilityListForEachDayInDateRange() {

        when(freeTimeAbsenceProvider.checkForAbsence(any(Person.class), any(LocalDate.class))).thenReturn(timedAbsenceSpans);

        final LocalDate startDate = LocalDate.of(2016, 1, 1);
        final LocalDate endDate = LocalDate.of(2016, 1, DAYS_IN_TEST_DATE_RANGE);
        final Person person = createPerson();
        sut.getPersonsAvailabilities(startDate, endDate, person);

        verify(freeTimeAbsenceProvider, times(DAYS_IN_TEST_DATE_RANGE)).checkForAbsence(eq(person), any(LocalDate.class));
    }

    @Test
    void ensureReturnsDayAvailabilityWithCalculatedPresenceRatio() {

        when(freeTimeAbsenceProvider.checkForAbsence(any(Person.class), any(LocalDate.class))).thenReturn(timedAbsenceSpans);
        when(timedAbsenceSpans.calculatePresenceRatio()).thenReturn(ONE);

        final LocalDate dayToTest = LocalDate.of(2016, 1, 1);
        final AvailabilityListDto personsAvailabilities = sut.getPersonsAvailabilities(dayToTest, dayToTest, createPerson());

        verify(timedAbsenceSpans, times(1)).calculatePresenceRatio();
        final List<DayAvailability> availabilityList = personsAvailabilities.getAvailabilities();
        assertThat(availabilityList).hasSize(1);
        assertThat(availabilityList.get(0).getTimedAbsenceSpans()).isEqualTo(timedAbsenceSpans);
        assertThat(availabilityList.get(0).getAvailabilityRatio()).isEqualTo(ONE);
    }
}
