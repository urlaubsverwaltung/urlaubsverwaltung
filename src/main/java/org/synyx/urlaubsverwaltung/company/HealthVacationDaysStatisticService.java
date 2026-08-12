package org.synyx.urlaubsverwaltung.company;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.application.statistics.ApplicationForLeaveStatisticsService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Service
class HealthVacationDaysStatisticService {

    private final ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;

    HealthVacationDaysStatisticService(ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService) {
        this.applicationForLeaveStatisticsService = applicationForLeaveStatisticsService;
    }

    VacationDaysStatistic getVacationDaysStatistic(Person viewer, LocalDate from, LocalDate to) {

        final FilterPeriod period = new FilterPeriod(from, to);
        final Page<ApplicationForLeaveStatistics> stats = applicationForLeaveStatisticsService.getStatisticsSortedByStatistics(viewer, period);

        final Map<Person, ApplicationForLeaveStatistics> byPerson = stats.stream()
            .collect(toMap(ApplicationForLeaveStatistics::getPerson, identity()));

        return new VacationDaysStatistic(byPerson);
    }
}
