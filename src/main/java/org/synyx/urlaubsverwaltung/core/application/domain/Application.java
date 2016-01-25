package org.synyx.urlaubsverwaltung.core.application.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.period.Period;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.time.LocalTime;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;


/**
 * This class describes an application for leave.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Entity
public class Application extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1234589209309L;

    // One person may own multiple applications for leave
    @ManyToOne
    private Person person;

    // The person that applied the application
    @ManyToOne
    private Person applier;

    // The person that allowed/rejected the application
    @ManyToOne
    private Person boss;

    // The person that cancelled the application
    @ManyToOne
    private Person canceller;

    // Period of holiday
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    private LocalTime startTime;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    private LocalTime endTime;

    // Type of holiday, e.g. holiday, special leave, etc.
    @Enumerated(EnumType.STRING)
    private VacationType vacationType;

    // length of day: contains time of day (morning, noon or full day) and value (1.0 or 0.5 - as BigDecimal)
    @Enumerated(EnumType.STRING)
    private DayLength dayLength;

    // For special and unpaid leave a reason is required
    private String reason;

    // Holiday replacement: stands in while the person is on holiday
    @ManyToOne
    @JoinColumn(name = "rep_id")
    private Person holidayReplacement;

    // Address and phone number during holiday
    private String address;

    // Date of applying for leave
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date applicationDate;

    // Date of cancelling an application for leave
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date cancelDate;

    // Date of editing (allow or reject) an application for leave
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date editedDate;

    // Last date of sending a reminding email to boss
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date remindDate;

    // State of application (e.g. waiting, allowed, ...)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    // Signature of applicant
    @Column(columnDefinition = "longblob")
    private byte[] signaturePerson;

    // Signature of boss
    @Column(columnDefinition = "longblob")
    private byte[] signatureBoss;

    // team informed about holidays?
    private boolean teamInformed;

    // How many hours of overtime are used for this application for leave?
    private BigDecimal hours;

    public String getAddress() {

        return address;
    }


    public void setAddress(String address) {

        this.address = address;
    }


    public DateMidnight getApplicationDate() {

        if (this.applicationDate == null) {
            return null;
        }

        return new DateTime(this.applicationDate).toDateMidnight();
    }


    public void setApplicationDate(DateMidnight applicationDate) {

        if (applicationDate == null) {
            this.applicationDate = null;
        } else {
            this.applicationDate = applicationDate.toDate();
        }
    }


    public DateMidnight getCancelDate() {

        if (this.cancelDate == null) {
            return null;
        }

        return new DateTime(this.cancelDate).toDateMidnight();
    }


    public void setCancelDate(DateMidnight cancelDate) {

        if (cancelDate == null) {
            this.cancelDate = null;
        } else {
            this.cancelDate = cancelDate.toDate();
        }
    }


    public DateMidnight getEditedDate() {

        if (this.editedDate == null) {
            return null;
        }

        return new DateTime(this.editedDate).toDateMidnight();
    }


    public void setEditedDate(DateMidnight editedDate) {

        if (editedDate == null) {
            this.editedDate = null;
        } else {
            this.editedDate = editedDate.toDate();
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


    public DateMidnight getEndDate() {

        if (this.endDate == null) {
            return null;
        }

        return new DateTime(this.endDate).toDateMidnight();
    }


    public void setEndDate(DateMidnight endDate) {

        if (endDate == null) {
            this.endDate = null;
        } else {
            this.endDate = endDate.toDate();
        }
    }


    public LocalTime getStartTime() {

        return startTime;
    }


    public void setStartTime(LocalTime startTime) {

        this.startTime = startTime;
    }


    public LocalTime getEndTime() {

        return endTime;
    }


    public void setEndTime(LocalTime endTime) {

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


    public byte[] getSignatureBoss() {

        if (signatureBoss == null) {
            return null;
        }

        return Arrays.copyOf(signatureBoss, signatureBoss.length);
    }


    public void setSignatureBoss(byte[] signatureBoss) {

        if (signatureBoss != null) {
            this.signatureBoss = Arrays.copyOf(signatureBoss, signatureBoss.length);
        } else {
            this.signatureBoss = null;
        }
    }


    public byte[] getSignaturePerson() {

        if (signaturePerson == null) {
            return null;
        }

        return Arrays.copyOf(signaturePerson, signaturePerson.length);
    }


    public void setSignaturePerson(byte[] signaturePerson) {

        if (signaturePerson != null) {
            this.signaturePerson = Arrays.copyOf(signaturePerson, signaturePerson.length);
        } else {
            this.signaturePerson = null;
        }
    }


    public DateMidnight getStartDate() {

        if (this.startDate == null) {
            return null;
        }

        return new DateTime(this.startDate).toDateMidnight();
    }


    public void setStartDate(DateMidnight startDate) {

        if (startDate == null) {
            this.startDate = null;
        } else {
            this.startDate = startDate.toDate();
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


    public DateMidnight getRemindDate() {

        if (this.remindDate == null) {
            return null;
        }

        return new DateTime(this.remindDate).toDateMidnight();
    }


    public void setRemindDate(DateMidnight remindDate) {

        if (remindDate == null) {
            this.remindDate = null;
        } else {
            this.remindDate = remindDate.toDate();
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
        toStringBuilder.append("startDate", getStartDate());
        toStringBuilder.append("endDate", getEndDate());
        toStringBuilder.append("vacationType", getVacationType());
        toStringBuilder.append("dayLength", getDayLength());

        if (getPerson() != null && getApplier() != null && getPerson().equals(getApplier())) {
            toStringBuilder.append("person", getPerson());
        } else {
            toStringBuilder.append("person", getPerson());
            toStringBuilder.append("applier", getApplier());
        }

        if (getBoss() != null) {
            toStringBuilder.append("boss", getBoss());
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
}
