package org.synyx.urlaubsverwaltung.company;

import org.synyx.urlaubsverwaltung.application.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Map;

record VacationDaysStatistic(Map<Person, ApplicationForLeaveStatistics> statisticByPerson) {

    int personCount() {
        return statisticByPerson().size();
    }

    /**
     *
     * @param from start of the range
     * @param to end of the range, inclusive
     * @return person count
     */
    int numberOfPersonsWithRemainingVacationDaysBetween(double from, double to) {
        return (int) statisticByPerson.values().stream()
            .map(ApplicationForLeaveStatistics::getLeftVacationDaysForYear)
            .mapToDouble(java.math.BigDecimal::doubleValue)
            .filter(remaining -> remaining >= from && remaining <= to)
            .count();
    }

    /**
     *
     * @param value start of the range, exclusive
     * @return person count
     */
    int numberOfPersonsWithRemainingVacationDaysGreaterThan(double value) {
        return (int) statisticByPerson.values().stream()
            .map(ApplicationForLeaveStatistics::getLeftVacationDaysForYear)
            .mapToDouble(java.math.BigDecimal::doubleValue)
            .filter(remaining -> remaining > value)
            .count();
    }
}
