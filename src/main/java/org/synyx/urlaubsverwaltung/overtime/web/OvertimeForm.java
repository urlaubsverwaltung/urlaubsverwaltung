package org.synyx.urlaubsverwaltung.overtime.web;

import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.requireNonNullElse;


/**
 * View class to record overtime for a certain period of time.
 */
public class OvertimeForm {

    private Integer id;
    private Person person;
    private LocalDate startDate;
    private LocalDate endDate;
    private String comment;
    private boolean reduce;

    @Min(0)
    private Integer hours;

    @Min(0)
    private Integer minutes;

    OvertimeForm() {

        // OK
    }

    public OvertimeForm(Person person) {

        Assert.notNull(person, "Person must be given.");
        this.person = person;
    }

    public OvertimeForm(Overtime overtime) {

        Assert.notNull(overtime, "Overtime must be given.");

        final BigDecimal overtimeHours = overtime.getHours() == null ? BigDecimal.ZERO : overtime.getHours();

        this.id = overtime.getId();
        this.person = overtime.getPerson();
        this.startDate = overtime.getStartDate();
        this.endDate = overtime.getEndDate();
        this.hours = overtimeHours.setScale(0, RoundingMode.DOWN).abs().intValueExact();
        this.minutes = overtimeHours.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_EVEN).abs().intValueExact();
        this.reduce = overtimeHours.doubleValue() < 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
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

    public Overtime generateOvertime() {
        return new Overtime(getPerson(), getStartDate(), getEndDate(), getDuration());
    }

    public void updateOvertime(Overtime overtime) {
        overtime.setPerson(getPerson());
        overtime.setHours(getDuration());
        overtime.setStartDate(getStartDate());
        overtime.setEndDate(getEndDate());
    }

    /**
     *
     * @return the hours and minutes fields mapped to a {@link BigDecimal}
     */
    public BigDecimal getDuration() {

        if (getMinutes() == null && getHours() == null) {
            return null;
        }

        final double originalMinutes = getMinutes() == null ? 0.0 : getMinutes();

        final double durationMinutes = originalMinutes % 60;
        final double hoursToAdd = (originalMinutes - durationMinutes) / 60;
        final double durationHours = requireNonNullElse(getHours(), 0) + hoursToAdd;

        final BigDecimal duration = BigDecimal.valueOf(durationHours + (durationMinutes / 60));
        return reduce ? duration.negate() : duration;
    }
}
