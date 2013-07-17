/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.application.web;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;


/**
 * View class representing an application for leave.
 *
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

    private boolean teamInformed;

    private String comment;

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


    public String getComment() {

        return comment;
    }


    public void setComment(String comment) {

        this.comment = comment;
    }


    public boolean isTeamInformed() {

        return teamInformed;
    }


    public void setTeamInformed(boolean teamInformed) {

        this.teamInformed = teamInformed;
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
        app.setTeamInformed(this.teamInformed);
        app.setComment(this.comment);

        if (this.howLong == DayLength.FULL) {
            app.setStartDate(this.startDate);
            app.setEndDate(this.endDate);
        } else {
            app.setStartDate(this.startDateHalf);
            app.setEndDate(this.startDateHalf);
        }

        return app;
    }
}
