/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calendar;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import java.io.IOException;


/**
 * @author  aljona
 */
public class OwnCalendarService {

    private final String username = "google.username";
    private final String password = "google.pw";

    private GoogleCalendarServiceImpl googleCalendarServiceImpl = new GoogleCalendarServiceImpl(username, password);

    /**
     * es wird vorher validiert, dass Startdatum vor dem Enddatum liegt oder gleich dem Enddatum ist
     *
     * <p>Methode berechnet wie viele Werktage (Feiertage nicht beruecksichtigt) im angegebenen Zeitraum liegen</p>
     *
     * @param  startDate
     * @param  endDate
     *
     * @return  Anzahl der Werktage
     */
    public Integer getWorkDays(DateMidnight startDate, DateMidnight endDate) {

        Integer workDays = 1;

        if (!startDate.equals(endDate)) {
            DateMidnight day = startDate;

            while (!day.equals(endDate)) {
                if (!(day.getDayOfWeek() == DateTimeConstants.SATURDAY
                            || day.getDayOfWeek() == DateTimeConstants.SUNDAY)) {
                    workDays++;
                }

                day = day.plusDays(1);
            }
        }

        return workDays;
    }


    public Integer getVacationDays(DateMidnight startDate, DateMidnight endDate) throws AuthenticationException,
        IOException, ServiceException {

        Integer vacDays = 0;

        vacDays = getWorkDays(startDate, endDate);

        vacDays = vacDays - googleCalendarServiceImpl.getFeiertage(startDate.toLocalDate(), endDate.toLocalDate());

        return vacDays;
    }
}
