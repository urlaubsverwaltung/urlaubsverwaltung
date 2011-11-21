/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calendar;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.chrono.GregorianChronology;

import java.util.HashSet;
import java.util.Set;


/**
 * @author  aljona
 */
public class JollydayCalendar {

    HolidayManager manager = HolidayManager.getInstance("synyx");

    /**
     * Berechnet Anzahl der Feiertage zwischen zwei Datumsangaben. Wenn ein Feiertag auf Samstag oder Sonntag faellt,
     * zaehlt dieser nicht als Feiertag. Wochenendtage werden im OwnCalendarService beachtet hier geht es tatsaechlich
     * rein um Feiertage, die an einem Tag zwischen Montag und Freitag liegen.
     *
     * @param  startDate
     * @param  endDate
     *
     * @return  Anzahl der Feiertage zwischen startDate und endDate
     */
    public Double getFeiertage(DateMidnight startDate, DateMidnight endDate) {

        startDate = startDate.withChronology(GregorianChronology.getInstance());
        endDate = endDate.withChronology(GregorianChronology.getInstance());

        // hole alle Feiertage dieses Jahrs
        Set<Holiday> holidays = manager.getHolidays(startDate.getYear());

        // pruefe, ob es Feiertage gibt, die auf einen Samstag oder Sonntag fallen wenn ein Feiertag auf Samstag oder
        // Sonntag faellt, ist er fuer Urlaubsberechnung wertlos

        // da man waehrend des Iterierens das Element nicht aus dem Set entfernen kann, wird stattdessen ein "echtes
        // Set" erzeugt, in dem alle Feiertage, die NICHT auf ein Wochenende fallen gespeichert sind

        Set<Holiday> feiertageAnWerktagen = new HashSet<Holiday>();

        for (Holiday holiday : holidays) {
            int day = holiday.getDate().getDayOfWeek();

            if (!((day == DateTimeConstants.SATURDAY) || (day == DateTimeConstants.SUNDAY))) {
                feiertageAnWerktagen.add(holiday);
            }
        }

        Double feiertage = 0.0;

        // schaue, ob start- und enddatum gleich sind
        if (startDate.equals(endDate)) {
            for (Holiday holiday : feiertageAnWerktagen) {
                // pruefe, ob datum auf einen feiertag faellt
                if ((startDate.toLocalDate()).equals(holiday.getDate())) {
                    // pruefe, ob dieser feiertag silvester oder heiligabend ist
                    // denn diese zaehlen nur zu 0,5 als feiertag
                    if (((holiday.getDate().getDayOfMonth() == 24
                                    && holiday.getDate().getMonthOfYear() == DateTimeConstants.DECEMBER)
                                || (holiday.getDate().getDayOfMonth() == 31
                                    && holiday.getDate().getMonthOfYear() == DateTimeConstants.DECEMBER))) {
                        feiertage = feiertage + 0.5;
                    } else {
                        // ansonsten wird ganzer feiertag aufaddiert
                        feiertage = feiertage + 1.0;
                    }
                }
            }
        } else {
            DateMidnight date = startDate;
            DateMidnight day;

            // iteriere ueber die tage drueber: solange startdatum != enddatum
            while (!date.equals(endDate)) {
                // pruefe, ob vorkommender tag ein holiday ist
                for (Holiday holiday : feiertageAnWerktagen) {
                    // pruefe, ob datum auf einen feiertag faellt
                    if ((date.toLocalDate()).equals(holiday.getDate())) {
                        // pruefe, ob dieser feiertag silvester oder heiligabend ist
                        // denn diese zaehlen nur zu 0,5 als feiertag
                        if ((holiday.getDate().getDayOfMonth() == 24
                                    && holiday.getDate().getMonthOfYear() == DateTimeConstants.DECEMBER)
                                || (holiday.getDate().getDayOfMonth() == 31
                                    && holiday.getDate().getMonthOfYear() == DateTimeConstants.DECEMBER)) {
                            feiertage = feiertage + 0.5;
                        } else {
                            // ansonsten wird ganzer feiertag aufaddiert
                            feiertage = feiertage + 1.0;
                        }
                    }
                }

                date = date.plusDays(1);
            }

            for (Holiday holiday : feiertageAnWerktagen) {
                if ((endDate.toLocalDate()).equals(holiday.getDate())) {
                    if ((holiday.getDate().getDayOfMonth() == 24
                                && holiday.getDate().getMonthOfYear() == DateTimeConstants.DECEMBER)
                            || (holiday.getDate().getDayOfMonth() == 31
                                && holiday.getDate().getMonthOfYear() == DateTimeConstants.DECEMBER)) {
                        feiertage = feiertage + 0.5;
                    } else {
                        // ansonsten wird ganzer feiertag aufaddiert
                        feiertage = feiertage + 1.0;
                    }
                }
            }
        }

        return feiertage;
    }
}
