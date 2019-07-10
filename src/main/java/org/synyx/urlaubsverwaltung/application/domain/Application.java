package org.synyx.urlaubsverwaltung.application.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateFormat;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.ZoneOffset.UTC;


/**
 * This class describes an application for leave.
 */
@Entity
public class Application extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1234589209309L;

    /**
     * Person that will be on vacation if this application for leave is allowed.
     */
    @ManyToOne
    private Person person;

    /**
     * Person that made the application - can be different to the person that will be on vacation.
     */
    @ManyToOne
    private Person applier;

    /**
     * Person that allowed or rejected the application for leave.
     */
    @ManyToOne
    private Person boss;

    /**
     * Person that cancelled the application.
     */
    @ManyToOne
    private Person canceller;

    /**
     * Flag for two stage approval process.
     *
     * @since  2.15.0
     */
    private boolean twoStageApproval;

    /**
     * Start date of the application for leave.
     */
    private LocalDate startDate;

    /**
     * Start time of the application for leave.
     *
     * @since  2.15.0
     */
    private Time startTime;

    /**
     * End date of the application for leave.
     */
    private LocalDate endDate;

    /**
     * End time of the application for leave.
     *
     * @since  2.15.0
     */
    private Time endTime;

    /**
     * Type of vacation, e.g. holiday, special leave etc.
     */
    @ManyToOne
    private VacationType vacationType;

    /**
     * Day length of the vacation period, e.g. full day, morning, noon.
     */
    @Enumerated(EnumType.STRING)
    private DayLength dayLength;

    /**
     * Reason for the vacation, is required for some types of vacation, e.g. for special leave.
     */
    private String reason;

    /**
     * Person that is the holiday replacement during the vacation.
     */
    @ManyToOne
    @JoinColumn(name = "rep_id")
    private Person holidayReplacement;

    /**
     * Further information: address, phone number etc.
     */
    private String address;

    /**
     * Date of application for leave creation.
     */
    private LocalDate applicationDate;

    /**
     * Date of application for leave cancellation.
     */
    private LocalDate cancelDate;

    /**
     * Date of application for leave processing (allow or reject).
     */
    private LocalDate editedDate;

    /**
     * Last date of sending a remind notification that application for leave has to be processed.
     */
    private LocalDate remindDate;

    /**
     * Describes the current status of the application for leave (e.g. allowed, rejected etc.)
     */
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    /**
     * Flag if team is informed about vacation or not.
     */
    private boolean teamInformed;

    /**
     * The number of overtime hours that are used for this application for leave.
     *
     * @since  2.11.0
     */
    private BigDecimal hours;

    @Override
    public void setId(Integer id) { // NOSONAR - make it public instead of protected

        super.setId(id);
    }

    public String getAddress() {

        return address;
    }


    public void setAddress(String address) {

        this.address = address;
    }


    public LocalDate getApplicationDate() {

        if (this.applicationDate == null) {
            return null;
        }

        return this.applicationDate;
    }


    public void setApplicationDate(LocalDate applicationDate) {

        if (applicationDate == null) {
            this.applicationDate = null;
        } else {
            this.applicationDate = applicationDate;
        }
    }


    public LocalDate getCancelDate() {

        if (this.cancelDate == null) {
            return null;
        }

        return this.cancelDate;
    }


    public void setCancelDate(LocalDate cancelDate) {

        if (cancelDate == null) {
            this.cancelDate = null;
        } else {
            this.cancelDate = cancelDate;
        }
    }


    public LocalDate getEditedDate() {

        if (this.editedDate == null) {
            return null;
        }

        return this.editedDate;
    }


    public void setEditedDate(LocalDate editedDate) {

        if (editedDate == null) {
            this.editedDate = null;
        } else {
            this.editedDate = editedDate;
        }
    }


    public Person getApplier() {

        return applier;
    }


    public void setApplier(Person applier) {

        this.applier = applier;
    }


    public Person getBoss() {

        return boss;
    }


    public void setBoss(Person boss) {

        this.boss = boss;
    }


    public Person getCanceller() {

        return canceller;
    }


    public void setCanceller(Person canceller) {

        this.canceller = canceller;
    }


    public boolean isTwoStageApproval() {

        return twoStageApproval;
    }


    public void setTwoStageApproval(boolean twoStageApproval) {

        this.twoStageApproval = twoStageApproval;
    }


    public LocalDate getEndDate() {

        if (this.endDate == null) {
            return null;
        }

        return this.endDate;
    }


    public void setEndDate(LocalDate endDate) {

        if (endDate == null) {
            this.endDate = null;
        } else {
            this.endDate = endDate;
        }
    }


    public Time getStartTime() {

        return startTime;
    }


    public void setStartTime(Time startTime) {

        this.startTime = startTime;
    }


    public Time getEndTime() {

        return endTime;
    }


    public void setEndTime(Time endTime) {

        this.endTime = endTime;
    }


    public DayLength getDayLength() {

        return dayLength;
    }


    public void setDayLength(DayLength dayLength) {

        this.dayLength = dayLength;
    }


    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public String getReason() {

        return reason;
    }


    public void setReason(String reason) {

        this.reason = reason;
    }


    public Person getHolidayReplacement() {

        return holidayReplacement;
    }


    public void setHolidayReplacement(Person holidayReplacement) {

        this.holidayReplacement = holidayReplacement;
    }


    public LocalDate getStartDate() {

        if (this.startDate == null) {
            return null;
        }

        return this.startDate;
    }


    public void setStartDate(LocalDate startDate) {

        if (startDate == null) {
            this.startDate = null;
        } else {
            this.startDate = startDate;
        }
    }


    public ApplicationStatus getStatus() {

        return status;
    }


    public void setStatus(ApplicationStatus status) {

        this.status = status;
    }


    public VacationType getVacationType() {

        return vacationType;
    }


    public void setVacationType(VacationType vacationType) {

        this.vacationType = vacationType;
    }


    public boolean isFormerlyAllowed() {

        return hasStatus(ApplicationStatus.CANCELLED);
    }


    public LocalDate getRemindDate() {

        if (this.remindDate == null) {
            return null;
        }

        return this.remindDate;
    }


    public void setRemindDate(LocalDate remindDate) {

        if (remindDate == null) {
            this.remindDate = null;
        } else {
            this.remindDate = remindDate;
        }
    }


    public boolean isTeamInformed() {

        return teamInformed;
    }


    public void setTeamInformed(boolean teamInformed) {

        this.teamInformed = teamInformed;
    }


    public BigDecimal getHours() {

        return hours;
    }


    public void setHours(BigDecimal hours) {

        this.hours = hours;
    }


    @Override
    public String toString() {

        ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.append("id", getId());
        toStringBuilder.append("startDate", getStartDate().format(DateTimeFormatter.ofPattern(DateFormat.PATTERN)));
        toStringBuilder.append("endDate", getEndDate().format(DateTimeFormatter.ofPattern(DateFormat.PATTERN)));
        toStringBuilder.append("vacationType", getVacationType());
        toStringBuilder.append("twoStageApproval", isTwoStageApproval());
        toStringBuilder.append("status", getStatus().toString());
        toStringBuilder.append("dayLength", getDayLength());

        if (getPerson() != null && getApplier() != null && getPerson().equals(getApplier())) {
            toStringBuilder.append("person", getPerson().getId());
        } else {
            toStringBuilder.append("person", getPerson().getId());
            toStringBuilder.append("applier", getApplier());
        }

        if (getBoss() != null) {
            toStringBuilder.append("boss", getBoss().getId());
        }

        if (getCanceller() != null) {
            toStringBuilder.append("canceller", getCanceller());
        }

        return toStringBuilder.toString();
    }


    /**
     * Checks if the application for leave has the given status.
     *
     * @param  status  to be checked
     *
     * @return  {@code true} if the application for leave has the given status, else {@code false}
     */
    public boolean hasStatus(ApplicationStatus status) {

        return getStatus() == status;
    }


    /**
     * Return period of time of the application for leave.
     *
     * @return  period of time, never {@code null}
     */
    public Period getPeriod() {

        return new Period(getStartDate(), getEndDate(), getDayLength());
    }


    /**
     * Get start of application for leave as date with time.
     *
     * @return  start date with time or {@code null} if start date or start time is missing
     */
    public ZonedDateTime getStartDateWithTime() {

        LocalDate date = getStartDate();
        Time time = getStartTime();

        if (date != null && time != null) {
            return ZonedDateTime.of(date, time.toLocalTime(), UTC);
        }

        return null;
    }


    /**
     * Get end of application for leave as date with time.
     *
     * @return  end date with time or {@code null} if end date or end time is missing
     */
    public ZonedDateTime getEndDateWithTime() {

        LocalDate date = getEndDate();
        Time time = getEndTime();

        if (date != null && time != null) {
            return ZonedDateTime.of(date, time.toLocalTime(), UTC);
        }

        return null;
    }
}
