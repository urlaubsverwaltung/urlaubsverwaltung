package org.synyx.urlaubsverwaltung.company;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;

class VacationDaysStatisticTest {

    @Nested
    class PersonCount {

        @Test
        void ensurePersonCountReturnsSizeOfStatisticByPerson() {

            final Person personA = createPerson(1L);
            final ApplicationForLeaveStatistics statisticsA = mock(ApplicationForLeaveStatistics.class);

            final Person personB = createPerson(2L);
            final ApplicationForLeaveStatistics statisticsB = mock(ApplicationForLeaveStatistics.class);

            final VacationDaysStatistic sut = new VacationDaysStatistic(Map.of(personA, statisticsA, personB, statisticsB));

            assertThat(sut.personCount()).isEqualTo(2);
        }

        @Test
        void ensurePersonCountReturnsZeroForEmptyMap() {

            final VacationDaysStatistic sut = new VacationDaysStatistic(Map.of());

            assertThat(sut.personCount()).isZero();
        }
    }

    @Nested
    class NumberOfPersonsWithRemainingVacationDaysBetween {

        @Test
        void ensureCountsPersonsWithinRangeInclusive() {

            final Person personA = createPerson(1L);
            final ApplicationForLeaveStatistics statisticsA = mock(ApplicationForLeaveStatistics.class);
            when(statisticsA.getLeftVacationDaysForYear()).thenReturn(BigDecimal.valueOf(5));

            final Person personB = createPerson(2L);
            final ApplicationForLeaveStatistics statisticsB = mock(ApplicationForLeaveStatistics.class);
            when(statisticsB.getLeftVacationDaysForYear()).thenReturn(BigDecimal.valueOf(10));

            final Person personC = createPerson(3L);
            final ApplicationForLeaveStatistics statisticsC = mock(ApplicationForLeaveStatistics.class);
            when(statisticsC.getLeftVacationDaysForYear()).thenReturn(BigDecimal.valueOf(15));

            final VacationDaysStatistic sut = new VacationDaysStatistic(Map.of(
                personA, statisticsA,
                personB, statisticsB,
                personC, statisticsC
            ));

            assertThat(sut.numberOfPersonsWithRemainingVacationDaysBetween(5, 10)).isEqualTo(2);
        }

        @Test
        void ensureExcludesPersonsOutsideRange() {

            final Person personA = createPerson(1L);
            final ApplicationForLeaveStatistics statisticsA = mock(ApplicationForLeaveStatistics.class);
            when(statisticsA.getLeftVacationDaysForYear()).thenReturn(BigDecimal.valueOf(4.99));

            final Person personB = createPerson(2L);
            final ApplicationForLeaveStatistics statisticsB = mock(ApplicationForLeaveStatistics.class);
            when(statisticsB.getLeftVacationDaysForYear()).thenReturn(BigDecimal.valueOf(10.01));

            final VacationDaysStatistic sut = new VacationDaysStatistic(Map.of(
                personA, statisticsA,
                personB, statisticsB
            ));

            assertThat(sut.numberOfPersonsWithRemainingVacationDaysBetween(5, 10)).isZero();
        }
    }

    @Nested
    class NumberOfPersonsWithRemainingVacationDaysGreaterThan {

        @Test
        void ensureCountsPersonsStrictlyGreaterThanValue() {

            final Person personA = createPerson(1L);
            final ApplicationForLeaveStatistics statisticsA = mock(ApplicationForLeaveStatistics.class);
            when(statisticsA.getLeftVacationDaysForYear()).thenReturn(BigDecimal.valueOf(10.01));

            final Person personB = createPerson(2L);
            final ApplicationForLeaveStatistics statisticsB = mock(ApplicationForLeaveStatistics.class);
            when(statisticsB.getLeftVacationDaysForYear()).thenReturn(BigDecimal.valueOf(10));

            final Person personC = createPerson(3L);
            final ApplicationForLeaveStatistics statisticsC = mock(ApplicationForLeaveStatistics.class);
            when(statisticsC.getLeftVacationDaysForYear()).thenReturn(BigDecimal.valueOf(9.99));

            final VacationDaysStatistic sut = new VacationDaysStatistic(Map.of(
                personA, statisticsA,
                personB, statisticsB,
                personC, statisticsC
            ));

            assertThat(sut.numberOfPersonsWithRemainingVacationDaysGreaterThan(10)).isEqualTo(1);
        }
    }
}
