/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.web.application;

import lombok.Data;
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
@Data
public class ApplicationForLeaveForm {

    // person of the application for leave
    private Person person;

    private DateMidnight startDate;

    private DateMidnight startDateHalf;

    private DateMidnight endDate;

    // Type of holiday, e.g. holiday, special leave, etc.
    private VacationType vacationType;

    // length of day: contains time of day (morning, noon or full day) and value (1.0 or 0.5 - as BigDecimal)
    private DayLength dayLength;

    // For special and unpaid leave a reason is required
    private String reason;

    // Stands in while the person is on holiday
    private Person holidayReplacement;

    // Address and phone number during holiday
    private String address;

    private boolean teamInformed;

    private String comment;


    public Application generateApplicationForLeave() {

        Application applicationForLeave = new Application();

        applicationForLeave.setPerson(person);
        applicationForLeave.setAddress(address);
        applicationForLeave.setVacationType(vacationType);
        applicationForLeave.setDayLength(dayLength);
        applicationForLeave.setReason(reason);
        applicationForLeave.setHolidayReplacement(holidayReplacement);
        applicationForLeave.setAddress(address);
        applicationForLeave.setTeamInformed(teamInformed);

        if (dayLength == DayLength.FULL) {
            applicationForLeave.setStartDate(startDate);
            applicationForLeave.setEndDate(endDate);
        } else {
            applicationForLeave.setStartDate(startDateHalf);
            applicationForLeave.setEndDate(startDateHalf);
        }

        return applicationForLeave;
    }
}
