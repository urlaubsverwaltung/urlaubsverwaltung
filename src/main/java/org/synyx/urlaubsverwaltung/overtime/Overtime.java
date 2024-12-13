package org.synyx.urlaubsverwaltung.overtime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.DurationConverter;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;
import org.synyx.urlaubsverwaltung.util.DecimalConverter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static jakarta.persistence.GenerationType.SEQUENCE;
import static java.math.RoundingMode.HALF_EVEN;
import static java.time.Duration.ZERO;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.util.DecimalConverter.toFormattedDecimal;

/**
 * Represents the overtime of a person for a certain period of time.
 *
 * @since 2.11.0
 */
@Entity
public class Overtime extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "overtime_generator")
    @SequenceGenerator(name = "overtime_generator", sequenceName = "overtime_id_seq")
    private Long id;

    @ManyToOne
    private Person person;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @Convert(converter = DurationConverter.class)
    private Duration duration;

    @Column(nullable = false)
    private LocalDate lastModificationDate;

    protected Overtime() {
        // OK
    }

    public Overtime(Person person, LocalDate startDate, LocalDate endDate, Duration duration) {
        this(person, startDate, endDate, duration, LocalDate.now(UTC));
    }

    public Overtime(Person person, LocalDate startDate, LocalDate endDate, Duration duration, LocalDate lastModificationDate) {
        this.person = person;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.lastModificationDate = lastModificationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public LocalDate getStartDate() {
        if (startDate == null) {
            throw new IllegalStateException("Missing start date!");
        }

        return startDate;
    }

    public LocalDate getEndDate() {
        if (endDate == null) {
            throw new IllegalStateException("Missing end date!");
        }

        return endDate;
    }

    public Duration getDuration() {
        return duration;
    }

    public Duration getDurationForDateRange(DateRange dateRange) {
        final DateRange overtimeDateRange = new DateRange(startDate, endDate);
        final Duration durationOfOverlap = overtimeDateRange.overlap(dateRange).map(DateRange::duration).orElse(ZERO);

        final Duration overtimeDateRangeDuration = overtimeDateRange.duration();
        final BigDecimal secondsProRata = toFormattedDecimal(duration)
            .divide(toFormattedDecimal(overtimeDateRangeDuration), HALF_EVEN)
            .multiply(toFormattedDecimal(durationOfOverlap))
            .setScale(0, HALF_EVEN);

        return DecimalConverter.toDuration(secondsProRata);
    }

    private List<DateRange> splitByYear() {
        List<DateRange> dateRangesByYear = new ArrayList<>();

        LocalDate currentStartDate = startDate;
        LocalDate currentEndDate = startDate.withDayOfYear(1).plusYears(1).minusDays(1);

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


    public Map<Integer, Duration> getDurationByYear() {
        return this.splitByYear().stream()
            .collect(toMap(dateRangeForYear -> dateRangeForYear.startDate().getYear(), this::getDurationForDateRange));
    }

    public Duration getTotalDurationBefore(int year) {
        return this.getDurationByYear().entrySet().stream()
            .filter(entry -> entry.getKey() < year)
            .map(Map.Entry::getValue)
            .reduce(ZERO, Duration::plus);
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDate getLastModificationDate() {
        if (lastModificationDate == null) {
            throw new IllegalStateException("Missing last modification date!");
        }

        return this.lastModificationDate;
    }

    /**
     * Should be called whenever an overtime entity is updated.
     */
    public void onUpdate() {
        this.lastModificationDate = LocalDate.now(UTC);
    }

    @Override
    public String toString() {
        return "Overtime{" +
            "id=" + getId() +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", duration=" + duration +
            ", person=" + person +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Overtime that = (Overtime) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
