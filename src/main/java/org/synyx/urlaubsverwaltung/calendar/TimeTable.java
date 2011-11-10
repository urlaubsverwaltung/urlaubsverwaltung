package org.synyx.urlaubsverwaltung.calendar;

import com.google.common.collect.Maps;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Minutes;
import org.joda.time.Period;

import java.util.Collection;
import java.util.Map;


/**
 * dies ist ein vorläufiger versuch den google calendar einzubinden die methoden wurden dem googlecalendarserviceimpl
 * des ressourcenplanungstools von Otto Allmendinger - allmendinger@synyx.de entnommen für das urlaubsverwaltungstool
 * nicht nötige methoden und attribute wurden auskommentiert
 */

class TimeTable {

    private final Map<LocalDate, Minutes> timeTable = Maps.newHashMap();

    public void addEventSpan(DateTime startTime, DateTime endTime) {

        // does NOT consider overlapping events time spent in overlapping events will be simply added as if they were
        // separate

        LocalDate lastDay = new LocalDate(endTime);

        DateTime dt = new DateTime(startTime);

        while (dt.isBefore(endTime)) {
            LocalDate day = new LocalDate(dt);
            DateTime endOfDayTime = new DateMidnight(dt.plusDays(1)).toDateTime();
            DateTime periodEnd = day.equals(lastDay) ? endTime : endOfDayTime;
            Minutes newMinutes = new Period(dt, periodEnd).toStandardMinutes();
            addTime(day, newMinutes);
            dt = endOfDayTime;
        }
    }


    /**
     * add 24-hour periods between first day and last day, excluding last day
     *
     * @param  firstDay
     * @param  lastDay
     */
    public void addEventSpan(LocalDate firstDay, LocalDate lastDay) {

        LocalDate day = new LocalDate(firstDay);

        while (day.isBefore(lastDay)) {
            addTime(day, Days.ONE.toStandardMinutes());
            day = day.plusDays(1);
        }
    }


    private void addTime(LocalDate day, Minutes minutes) {

        timeTable.put(day, getTime(day).plus(minutes));
    }


    public Minutes getTime(LocalDate date) {

        Minutes minutes = timeTable.get(date);

        return (minutes == null) ? Minutes.ZERO : minutes;
    }


    public Map<LocalDate, Minutes> getTimeTableMap() {

        return timeTable;
    }


    public int[] getWeekMinutes(YearWeek yearWeek) {

        Collection<LocalDate> days = yearWeek.getDays();
        int[] weekMinutes = new int[days.size()];
        int i = 0;

        for (LocalDate day : days) {
            weekMinutes[i] = getTime(day).getMinutes();
            i++;
        }

        return weekMinutes;
    }
}
