package org.synyx.urlaubsverwaltung.domain;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;

import java.util.Date;

import javax.persistence.Column;
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

    // if application was cancelled not by applicant himself: person who cancelled application
    @OneToOne
    private Person office;

    // Number of days that is subtract from HolidayAccount
    private BigDecimal days;

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

    // if application has been cancelled during status allowed: formerlyAllowed is true
    // if application has been cancelled during status waiting: formerlyAllowed is false
    private boolean formerlyAllowed;

    // for applications that spans December and January there are two extra applications: one for the period in December
    // and one for the period in January these supplemental applications are only used for calculations (e.g. to add
    // sick days) true if the application is only a supplemental application for calculation false if the application is
    // a regular application
    private boolean supplementaryApplication;

    // if an application is only a supplementary application, this field contains the id of the regular application
    // that amongs to this supplementary application
    private Integer idOfApplication;

    // Signature of applicant
    @Column(columnDefinition = "longblob")
    private byte[] signaturePerson;

    // Signature of boss
    @Column(columnDefinition = "longblob")
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


    public Person getOffice() {

        return office;
    }


    public void setOffice(Person office) {

        this.office = office;
    }


    public BigDecimal getDays() {

        return days;
    }


    public void setDays(BigDecimal days) {

        this.days = days;
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

        return formerlyAllowed;
    }


    public void setFormerlyAllowed(boolean formerlyAllowed) {

        this.formerlyAllowed = formerlyAllowed;
    }


    public boolean isSupplementaryApplication() {

        return supplementaryApplication;
    }


    public void setSupplementaryApplication(boolean supplementaryApplication) {

        this.supplementaryApplication = supplementaryApplication;
    }


    public Integer getIdOfApplication() {

        return idOfApplication;
    }


    public void setIdOfApplication(Integer idOfApplication) {

        this.idOfApplication = idOfApplication;
    }
}
