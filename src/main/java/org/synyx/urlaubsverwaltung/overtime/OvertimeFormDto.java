package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.format.annotation.DateTimeFormat;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.requireNonNullElse;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

/**
 * View class to record overtime for a certain period of time.
 */
public class OvertimeFormDto {

    private Long id;
    private Person person;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate startDate;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate endDate;
    private String comment;
    private boolean reduce;

    private BigDecimal hours;
    private Integer minutes;

    OvertimeFormDto() {
        // OK
    }

    OvertimeFormDto(Person person) {
        this.person = person;
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

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getStartDateIsoValue() {
        if (startDate == null) {
            return "";
        }

        return startDate.format(DateTimeFormatter.ISO_DATE);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getEndDateIsoValue() {
        if (endDate == null) {
            return "";
        }

        return endDate.format(DateTimeFormatter.ISO_DATE);
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isReduce() {
        return reduce;
    }

    public void setReduce(boolean reduce) {
        this.reduce = reduce;
    }

    /**
     * @return the hours and minutes fields mapped to a {@link Duration}
     */
    public Duration getDuration() {

        if (hours == null && minutes == null) {
            return null;
        }

        final BigDecimal originalHours = requireNonNullElse(hours, BigDecimal.ZERO);
        final int originalMinutes = requireNonNullElse(minutes, 0);

        final Duration duration = Duration.ofMinutes(originalHours.multiply(BigDecimal.valueOf(60)).longValue() + originalMinutes);

        return reduce ? duration.negated() : duration;
    }
}
