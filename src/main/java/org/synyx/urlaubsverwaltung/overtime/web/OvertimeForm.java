package org.synyx.urlaubsverwaltung.overtime.web;

import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class OvertimeForm {

    private Long id;
    private Person person;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate startDate;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate endDate;
    private String comment;
    private boolean reduce;

    @Min(0)
    private BigDecimal hours;

    @Min(0)
    private Integer minutes;

    OvertimeForm() {
        // OK
    }

    OvertimeForm(Person person) {
        this.person = person;
    }

    OvertimeForm(Overtime overtime) {
        final BigDecimal overtimeHours = overtime.getDuration() == null ? BigDecimal.ZERO : BigDecimal.valueOf((double) overtime.getDuration().toMinutes() / 60);

        this.id = overtime.getId();
        this.person = overtime.getPerson();
        this.startDate = overtime.getStartDate();
        this.endDate = overtime.getEndDate();
        this.hours = overtimeHours.setScale(0, RoundingMode.DOWN).abs();
        this.minutes = overtimeHours.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_EVEN).abs().intValueExact();
        this.reduce = overtimeHours.doubleValue() < 0;
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

    Overtime generateOvertime() {
        return new Overtime(getPerson(), getStartDate(), getEndDate(), getDuration());
    }

    void updateOvertime(Overtime overtime) {
        overtime.setPerson(getPerson());
        overtime.setDuration(getDuration());
        overtime.setStartDate(getStartDate());
        overtime.setEndDate(getEndDate());
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
