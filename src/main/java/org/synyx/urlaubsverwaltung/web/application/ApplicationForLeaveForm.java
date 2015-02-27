/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.person.Person;


/**
 * View class representing an application for leave.
 *
 * @author  Aljona Murygina
 */
public class ApplicationForLeaveForm {

    // person of the application for leave
    private Person person;

    private DateMidnight startDate;

    private DateMidnight startDateHalf;

    private DateMidnight endDate;

    // Type of holiday, e.g. holiday, special leave, etc.
    private VacationType vacationType;

    // length of day: contains time of day (morning, noon or full day) and value (1.0 or 0.5 - as BigDecimal)
    private DayLength howLong;

    // For special and unpaid leave a reason is required
    private String reason;

    // Stands in while the person is on holiday
    private Person holidayReplacement;

    // Address and phone number during holiday
    private String address;

    private boolean teamInformed;

    private String comment;

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public String getAddress() {

        return address;
    }


    public void setAddress(String address) {

        this.address = address;
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


    public Application createApplicationObject() {

        Application applicationForLeave = new Application();

        applicationForLeave.setPerson(person);
        applicationForLeave.setAddress(address);
        applicationForLeave.setVacationType(vacationType);
        applicationForLeave.setHowLong(howLong);
        applicationForLeave.setReason(reason);
        applicationForLeave.setHolidayReplacement(holidayReplacement);
        applicationForLeave.setAddress(address);
        applicationForLeave.setTeamInformed(teamInformed);
        applicationForLeave.setComment(comment);

        if (howLong == DayLength.FULL) {
            applicationForLeave.setStartDate(startDate);
            applicationForLeave.setEndDate(endDate);
        } else {
            applicationForLeave.setStartDate(startDateHalf);
            applicationForLeave.setEndDate(startDateHalf);
        }

        return applicationForLeave;
    }
}
