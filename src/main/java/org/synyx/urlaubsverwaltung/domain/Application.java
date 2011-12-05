package org.synyx.urlaubsverwaltung.domain;

import org.joda.time.DateMidnight;

import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;


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

    // Reason why boss rejected an application
    @OneToOne
    private Comment reasonToReject;

    // Number of days that is subtract from HolidayAccount
    private BigDecimal days;

    // If belatedly added, number of days is added to HolidayAccount
    private BigDecimal sickDays;

    // Date of adding sick days to application
    private DateMidnight dateOfAddingSickDays;

    // Period of holiday
    private DateMidnight startDate;

    private DateMidnight endDate;

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
    private DateMidnight applicationDate;

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

        return applicationDate;
    }


    public void setApplicationDate(DateMidnight applicationDate) {

        this.applicationDate = applicationDate;
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

        return dateOfAddingSickDays;
    }


    public void setDateOfAddingSickDays(DateMidnight dateOfAddingSickDays) {

        this.dateOfAddingSickDays = dateOfAddingSickDays;
    }


    public DateMidnight getEndDate() {

        return endDate;
    }


    public void setEndDate(DateMidnight endDate) {

        this.endDate = endDate;
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


    public Comment getReasonToReject() {

        return reasonToReject;
    }


    public void setReasonToReject(Comment reasonToReject) {

        this.reasonToReject = reasonToReject;
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

        return signatureBoss;
    }


    public void setSignatureBoss(byte[] signatureBoss) {

        this.signatureBoss = signatureBoss;
    }


    public byte[] getSignaturePerson() {

        return signaturePerson;
    }


    public void setSignaturePerson(byte[] signaturePerson) {

        this.signaturePerson = signaturePerson;
    }


    public DateMidnight getStartDate() {

        return startDate;
    }


    public void setStartDate(DateMidnight startDate) {

        this.startDate = startDate;
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
