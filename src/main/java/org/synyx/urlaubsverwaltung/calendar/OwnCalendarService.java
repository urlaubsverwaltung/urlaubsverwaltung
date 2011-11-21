/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calendar;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;


/**
 * @author  aljona
 */
public class OwnCalendarService {

    private JollydayCalendar jollydayCalendar = new JollydayCalendar();

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


    /**
     * Berechnet wie viele Netto Urlaubstage im angegebenen Zeitraum draufgehen. getWorkDays errechnet die Werktage,
     * getFeiertage errechnet die Feiertage innerhalb der Werktage. Die Substraktion voneinander ergibt die endgueltigen
     * Urlaubstage
     *
     * @param  startDate
     * @param  endDate
     *
     * @return  Netto Urlaubstage
     */
    public Double getVacationDays(DateMidnight startDate, DateMidnight endDate) {

        Double vacDays = 0.0;

        vacDays = getWorkDays(startDate, endDate).doubleValue();

        vacDays = vacDays - jollydayCalendar.getFeiertage(startDate, endDate);

        return vacDays;
    }
}
