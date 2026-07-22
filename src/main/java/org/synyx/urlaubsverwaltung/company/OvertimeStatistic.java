package org.synyx.urlaubsverwaltung.company;

import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.time.Duration;
import java.util.List;
import java.util.Map;

record OvertimeStatistic(Map<PersonId, List<Overtime>> overtimesByPerson) {

    static OvertimeStatistic empty() {
        return new OvertimeStatistic(Map.of());
    }

    int personCount() {
        return overtimesByPerson.size();
    }

    /**
     *
     * @return average overtime duration
     */
    Duration average() {
        if (overtimesByPerson.isEmpty()) {
            return Duration.ZERO;
        }

        final Duration total = overtimesByPerson.values().stream()
            .flatMap(List::stream)
            .map(Overtime::duration)
            .reduce(Duration.ZERO, Duration::plus);

        return total.dividedBy(personCount());
    }

    /**
     *
     * @param min min overtime value, inclusive
     * @param maxExclusive max overtime value, exclusive
     * @return number of persons having overtime duration in the given range
     */
    int numberOfPersonsWithDurationBetween(Duration min, Duration maxExclusive) {
        if (min.compareTo(maxExclusive) > 0) {
            throw new IllegalStateException("min value must be lower than max value");
        }

        return (int) overtimesByPerson.values().stream()
            .map(OvertimeStatistic::sumDuration)
            .filter(duration -> duration.compareTo(min) >= 0 && duration.compareTo(maxExclusive) < 0)
            .count();
    }

    /**
     *
     * @param value overtime duration value, inclusive
     * @return number of persons having overtime duration greater or equal the given value
     */
    int numberOfPersonsWithDurationGreaterOrEqual(Duration value) {
        return (int) overtimesByPerson.values().stream()
            .map(OvertimeStatistic::sumDuration)
            .filter(duration -> duration.compareTo(value) >= 0)
            .count();
    }

    private static Duration sumDuration(List<Overtime> overtimes) {
        return overtimes.stream()
            .map(Overtime::duration)
            .reduce(Duration.ZERO, Duration::plus);
    }
}
