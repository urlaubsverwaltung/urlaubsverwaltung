/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;

import java.util.List;


/**
 * @author  Aljona Murygina
 */
public class VacationDaysService {

    private ApplicationService applicationService;
    private OwnCalendarService calendarService;

    public VacationDaysService(ApplicationService applicationService, OwnCalendarService calendarService) {

        this.applicationService = applicationService;
        this.calendarService = calendarService;
    }

    /**
     * This method calculates with the regular applications and the supplemental applications the number of days that
     * the given person has used for holidays in the given year.
     *
     * @param  person
     * @param  year
     *
     * @return  number of vacation days that the given person has used in the given year
     */
    public BigDecimal getUsedVacationDaysOfPersonForYear(Person person, int year) {

        BigDecimal numberOfVacationDays = BigDecimal.ZERO;

        // get all non cancelled applications of person for the given year
        List<Application> applications = applicationService.getApplicationsByPersonAndYear(person, year);

        // get the supplemental applications of person for the given year
        List<Application> supplementalApplications = applicationService.getSupplementalApplicationsByPersonAndYear(
                person, year);

        // put the supplemental applications that have status waiting or allowed in the list of applications
        for (Application a : supplementalApplications) {
            if (a.getStatus() == ApplicationStatus.WAITING || a.getStatus() == ApplicationStatus.ALLOWED) {
                applications.add(a);
            }
        }

        // calculate number of vacation days
        for (Application a : applications) {
            // use only the waiting or allowed applications for calculation
            if (a.getStatus() == ApplicationStatus.WAITING || a.getStatus() == ApplicationStatus.ALLOWED) {
                // use only the applications that do not span December and January
                if (a.getStartDate().getYear() == a.getEndDate().getYear()) {
                    numberOfVacationDays = numberOfVacationDays.add(a.getDays());
                }
            }
        }

        return numberOfVacationDays;
    }


    /**
     * This method calculates the number of days that the given person has used for holidays in the given year before
     * 1st April.
     *
     * @param  person
     * @param  year
     *
     * @return  number of vacation days that the given person has used in the given year before 1st April
     */
    public BigDecimal getUsedVacationDaysBeforeAprilOfPerson(Person person, int year) {

        BigDecimal numberOfVacationDays = BigDecimal.ZERO;

        // get all applications of person for the given year before April
        List<Application> applications = applicationService.getApplicationsBeforeAprilByPersonAndYear(person, year);

        // calculate number of vacation days
        for (Application a : applications) {
            // use only the waiting or allowed applications for calculation
            if (a.getStatus() == ApplicationStatus.WAITING || a.getStatus() == ApplicationStatus.ALLOWED) {
                // if application doesn't span March and April, just add application's days to number of used vacation
                // days
                if (a.getEndDate().isBefore(new DateMidnight(year, DateTimeConstants.APRIL, 1))) {
                    numberOfVacationDays = numberOfVacationDays.add(a.getDays());
                } else {
                    // if application spans March and April, add number of days from application start date to 31st
                    // March
                    BigDecimal days = calendarService.getVacationDays(a, a.getStartDate(),
                            new DateMidnight(year, DateTimeConstants.MARCH, 31));
                    numberOfVacationDays = numberOfVacationDays.add(days);
                }
            }
        }

        return numberOfVacationDays;
    }
}
