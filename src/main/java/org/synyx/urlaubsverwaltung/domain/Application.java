package org.synyx.urlaubsverwaltung.domain;

import org.joda.time.DateMidnight;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */

@Entity
public class Application extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = getSerialVersionUID();

    // One person may own multiple applications for leave
    @ManyToOne
    private Person person;

    // The boss who allowed/rejected an application
    @OneToOne
    private Person boss;

    // Reason why boss rejected an application
    @OneToOne
    private Kommentar reasonToReject;

    // Number of days that is substract from HolidayAccount
    private Double days;

    // If belatedly added, number of days is added to HolidayAccount
    private Double sickDays;

    // Period of holiday
    private DateMidnight startDate;

    private DateMidnight endDate;

    // Type of holiday, e.g. holiday, special leave, etc.
    private VacationType vacationType;

    // lassen oder nicht?
    // nur morgens, nur mittags oder ganztägig
    private Length howLong;

    // For special and unpaid leave a reason is required
    // for holiday default = "Erholung"
    private String reason;

    // Representative of employee during his/her holiday
    @OneToOne
    private Person rep;

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

    public static long getSerialVersionUID() {

        return serialVersionUID;
    }


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


    public Double getDays() {

        return days;
    }


    public void setDays(Double days) {

        this.days = days;
    }


    public DateMidnight getEndDate() {

        return endDate;
    }


    public void setEndDate(DateMidnight endDate) {

        this.endDate = endDate;
    }


    public Length getHowLong() {

        return howLong;
    }


    public void setHowLong(Length howLong) {

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


    public Kommentar getReasonToReject() {

        return reasonToReject;
    }


    public void setReasonToReject(Kommentar reasonToReject) {

        this.reasonToReject = reasonToReject;
    }


    public Person getRep() {

        return rep;
    }


    public void setRep(Person rep) {

        this.rep = rep;
    }


    public Double getSickDays() {

        return sickDays;
    }


    public void setSickDays(Double sickDays) {

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
