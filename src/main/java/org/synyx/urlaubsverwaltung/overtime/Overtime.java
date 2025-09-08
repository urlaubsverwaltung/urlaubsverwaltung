package org.synyx.urlaubsverwaltung.overtime;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.util.DecimalConverter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.HALF_EVEN;
import static java.time.Duration.ZERO;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.util.DecimalConverter.toFormattedDecimal;

/**
 * Represents an overtime entry of a person.
 * In most cases, this entry describes "I worked overtime" with a positive {@link Overtime#duration()}.
 * (while the value can be negative, too.)
 *
 * <p>
 * This is different to {@link org.synyx.urlaubsverwaltung.application.application.Application} of
 * {@link Application#getVacationType() type} {@link org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory#OVERTIME OVERTIME}.
 * An application is always a request to reduce overtime, which may even need to be approved.
 *
 * @param id               overtime identifier
 * @param personId         {@link org.synyx.urlaubsverwaltung.person.Person} identifier
 * @param dateRange        overtime be spread over several days
 * @param duration         duration of the overtime, can be positive or negative.
 * @param type             {@link OvertimeType} of the overtime
 * @param lastModification timestamp of last modification
 */
public record Overtime(
    OvertimeId id,
    PersonId personId,
    DateRange dateRange,
    Duration duration,
    OvertimeType type,
    Instant lastModification
) {

    public LocalDate startDate() {
        return dateRange.startDate();
    }

    public LocalDate endDate() {
        return dateRange.endDate();
    }

    public Duration durationForDateRange(DateRange dateRange) {
        final Duration durationOfOverlap = this.dateRange.overlap(dateRange).map(DateRange::duration).orElse(ZERO);

        final Duration overtimeDateRangeDuration = this.dateRange.duration();
        final BigDecimal secondsProRata = toFormattedDecimal(duration)
            .divide(toFormattedDecimal(overtimeDateRangeDuration), HALF_EVEN)
            .multiply(toFormattedDecimal(durationOfOverlap))
            .setScale(0, HALF_EVEN);

        return DecimalConverter.toDuration(secondsProRata);
    }

    public Map<Integer, Duration> getDurationByYear() {
        return this.splitByYear().stream()
            .collect(toMap(dateRangeForYear -> dateRangeForYear.startDate().getYear(), this::durationForDateRange));
    }

    public Duration getTotalDurationBefore(int year) {
        return this.getDurationByYear().entrySet().stream()
            .filter(entry -> entry.getKey() < year)
            .map(Map.Entry::getValue)
            .reduce(ZERO, Duration::plus);
    }

    private List<DateRange> splitByYear() {
        List<DateRange> dateRangesByYear = new ArrayList<>();

        final LocalDate endDate = dateRange.endDate();

        LocalDate currentStartDate = dateRange.startDate();
        LocalDate currentEndDate = dateRange.startDate().withDayOfYear(1).plusYears(1).minusDays(1);

        while (currentEndDate.isBefore(endDate) || currentEndDate.isEqual(endDate)) {
            dateRangesByYear.add(new DateRange(currentStartDate, currentEndDate));

            currentStartDate = currentEndDate.plusDays(1);
            currentEndDate = currentStartDate.withDayOfYear(1).plusYears(1).minusDays(1);
        }

        // Add the remaining date range if endDate is not on a year boundary
        if (!currentStartDate.isAfter(endDate)) {
            dateRangesByYear.add(new DateRange(currentStartDate, endDate));
        }

        return dateRangesByYear;
    }
}
