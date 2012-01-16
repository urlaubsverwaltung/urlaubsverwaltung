/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.view;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.VacationType;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 */
public class AppForm {

    private DateMidnight startDate;

    private DateMidnight startDateHalf;

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

    private DateMidnight applicationDate;

    private BigDecimal sickDays;

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


    public DateMidnight getStartDate() {

        return startDate;
    }


    public void setStartDate(DateMidnight startDate) {

        this.startDate = startDate;
    }


    public DateMidnight getStartDateHalf() {

        return startDateHalf;
    }


    public void setStartDateHalf(DateMidnight startDateHalf) {

        this.startDateHalf = startDateHalf;
    }


    public VacationType getVacationType() {

        return vacationType;
    }


    public void setVacationType(VacationType vacationType) {

        this.vacationType = vacationType;
    }


    public Application fillApplicationObject(Application app) {

        app.setAddress(this.address);
        app.setVacationType(this.vacationType);
        app.setHowLong(this.howLong);
        app.setReason(this.reason);
        app.setRep(this.rep);
        app.setAddress(this.address);
        app.setPhone(this.phone);
        app.setApplicationDate(this.applicationDate);

        if (this.startDateHalf != null) {
            app.setStartDate(this.startDateHalf);
            app.setEndDate(this.startDateHalf);
        } else {
            app.setStartDate(this.startDate);
            app.setEndDate(this.endDate);
        }

        return app;
    }
}
