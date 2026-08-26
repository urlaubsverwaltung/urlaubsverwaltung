package org.synyx.urlaubsverwaltung.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.synyx.urlaubsverwaltung.application.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.application.statistics.ApplicationForLeaveStatisticsService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;

@ExtendWith(MockitoExtension.class)
class HealthVacationDaysStatisticServiceTest {

    private HealthVacationDaysStatisticService sut;

    @Mock
    private ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;

    @BeforeEach
    void setUp() {
        sut = new HealthVacationDaysStatisticService(applicationForLeaveStatisticsService);
    }

    @Nested
    class GetVacationDaysStatistic {

        @Test
        void ensureVacationDaysStatisticIsBuiltFromApplicationForLeaveStatisticsByPerson() {

            final Person viewer = createPerson("viewer");
            final LocalDate from = LocalDate.of(2026, JANUARY, 1);
            final LocalDate to = LocalDate.of(2026, JANUARY, 31);

            final Person personA = createPerson("personA");
            final ApplicationForLeaveStatistics statisticsA = mock(ApplicationForLeaveStatistics.class);
            when(statisticsA.getPerson()).thenReturn(personA);

            final Person personB = createPerson("personB");
            final ApplicationForLeaveStatistics statisticsB = mock(ApplicationForLeaveStatistics.class);
            when(statisticsB.getPerson()).thenReturn(personB);

            final Page<ApplicationForLeaveStatistics> page = new PageImpl<>(List.of(statisticsA, statisticsB));
            when(applicationForLeaveStatisticsService.getStatisticsSortedByStatistics(viewer, new FilterPeriod(from, to))).thenReturn(page);

            final VacationDaysStatistic actual = sut.getVacationDaysStatistic(viewer, from, to);

            assertThat(actual.personCount()).isEqualTo(2);
            assertThat(actual.statisticByPerson()).containsExactlyInAnyOrderEntriesOf(Map.of(
                personA, statisticsA,
                personB, statisticsB
            ));
        }
    }
}
