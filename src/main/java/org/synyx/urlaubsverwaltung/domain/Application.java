package org.synyx.urlaubsverwaltung.domain;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */

@Entity
public class Application extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1234589209309L;

    // One person may own multiple applications for leave
    @ManyToOne
    private Person person;

    // The boss who allowed/rejected an application
    @OneToOne
    private Person boss;

    // Number of days that is subtract from HolidayAccount
    private BigDecimal days;

    // If belatedly added, number of days is added to HolidayAccount
    private BigDecimal sickDays;

    // Date of adding sick days to application
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateOfAddingSickDays;

    // Period of holiday
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    // Type of holiday, e.g. holiday, special leave, etc.
    private VacationType vacationType;

    // length of day: contains time of day (morning, noon or full day) and value (1.0 or 0.5 - as BigDecimal)
    private DayLength howLong;

    // For special and unpaid leave a reason is required
    // for holiday default = "Erholung"
    private String reason;

    // Name of representative of employee during his/her holiday
    private String rep;

    // Address and phone number during holiday
    private String address;

    private String phone;

    // Date of application
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date applicationDate;

    // State of application (e.g. waiting, allowed, ...)
    private ApplicationStatus status;

    // Signature of applicant
    private byte[] signaturePerson;

    // Signature of boss
    private byte[] signatureBoss;

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


    public Person getBoss() {

        return boss;
    }


    public void setBoss(Person boss) {

        this.boss = boss;
    }


    public BigDecimal getDays() {

        return days;
    }


    public void setDays(BigDecimal days) {

        this.days = days;
    }


    public DateMidnight getDateOfAddingSickDays() {

        if (this.dateOfAddingSickDays == null) {
            return null;
        }

        return new DateTime(this.dateOfAddingSickDays).toDateMidnight();
    }


    public void setDateOfAddingSickDays(DateMidnight dateOfAddingSickDays) {

        if (dateOfAddingSickDays == null) {
            this.dateOfAddingSickDays = null;
        } else {
            this.dateOfAddingSickDays = dateOfAddingSickDays.toDate();
        }
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


    public DayLength getHowLong() {

        return howLong;
    }


    public void setHowLong(DayLength howLong) {

        this.howLong = howLong;
    }


    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public String getPhone() {

        return phone;
    }


    public void setPhone(String phone) {

        this.phone = phone;
    }


    public String getReason() {

        return reason;
    }


    public void setReason(String reason) {

        this.reason = reason;
    }


    public String getRep() {

        return rep;
    }


    public void setRep(String rep) {

        this.rep = rep;
    }


    public BigDecimal getSickDays() {

        return sickDays;
    }


    public void setSickDays(BigDecimal sickDays) {

        this.sickDays = sickDays;
    }


    public byte[] getSignatureBoss() {

        return signatureBoss.clone();
    }


    public void setSignatureBoss(byte[] signatureBoss) {

        this.signatureBoss = signatureBoss.clone();
    }


    public byte[] getSignaturePerson() {

        return signaturePerson.clone();
    }


    public void setSignaturePerson(byte[] signaturePerson) {

        this.signaturePerson = signaturePerson.clone();
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
}
