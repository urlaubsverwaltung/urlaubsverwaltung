package org.synyx.urlaubsverwaltung.calendar;

import com.google.common.collect.Lists;

import com.google.gdata.util.common.base.Objects;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.ArrayList;
import java.util.List;


/**
 * dies ist ein vorläufiger versuch den google calendar einzubinden die methoden wurden dem googlecalendarserviceimpl
 * des ressourcenplanungstools von Otto Allmendinger - allmendinger@synyx.de entnommen für das urlaubsverwaltungstool
 * nicht nötige methoden und attribute wurden auskommentiert
 */

public class YearWeek implements Comparable<YearWeek> {

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendWeekyear(4, 4)
        .appendLiteral("-W").appendWeekOfWeekyear(2).toFormatter();

    private final int year;

    private final int week;

    public YearWeek() {

        this(new LocalDate());
    }


    public YearWeek(String yearWeekString) {

//        this(LocalDate.parse(yearWeekString, formatter)); // works in joda time 2.0
        this(new LocalDate(formatter.parseDateTime(yearWeekString)));
    }


    public YearWeek(int year, int week) {

        this(new LocalDate().withWeekyear(year).withWeekOfWeekyear(week));
    }


    public YearWeek(YearWeek yearWeek) {

        this(yearWeek.getYear(), yearWeek.getWeek());
    }


    public YearWeek(LocalDate date) {

        this.year = date.getWeekyear();
        this.week = date.getWeekOfWeekyear();
    }

    public int getYear() {

        return year;
    }


    public int getWeek() {

        return week;
    }


    public String getId() {

        return getFirstDay().toString(formatter);
    }


    public LocalDate getFirstDay() {

        return new LocalDate().withWeekyear(getYear()).withWeekOfWeekyear(getWeek()).withDayOfWeek(1);
    }


    public YearWeek getNextWeek() {

        return new YearWeek(getFirstDay().plusWeeks(1));
    }


    public YearWeek getPreviousWeek() {

        return new YearWeek(getFirstDay().minusWeeks(1));
    }


    @Override
    public String toString() {

        return getId();
    }


    @Override
    public boolean equals(Object o) {

        if (!(o instanceof YearWeek)) {
            return false;
        }

        YearWeek otherYearWeek = (YearWeek) o;

        return (otherYearWeek.getYear() == getYear()) && (otherYearWeek.getWeek() == getWeek());
    }


    public boolean isAfter(YearWeek yearWeek) {

        return getFirstDay().isAfter(yearWeek.getFirstDay());
    }


    public List<LocalDate> getDays() {

        // XXX
        // do this with guava maybe
        // could also be iterator() ?
        List<LocalDate> days = new ArrayList<LocalDate>();
        LocalDate day = getFirstDay();

        while (contains(day)) {
            days.add(day);
            day = day.plusDays(1);
        }

        return days;
    }


    public int compareTo(YearWeek yearWeek) {

        // TODO: Code Quality: Replace this guava Ordering

        return equals(yearWeek) ? 0 : (isAfter(yearWeek) ? 1 : -1);
    }


    @Override
    public int hashCode() {

        return Objects.hashCode(year, week);
    }


    public boolean contains(LocalDate date) {

        return equals(new YearWeek(date));
    }


    public static Iterable<YearWeek> getRange(YearWeek start, int count) {

        List<YearWeek> yearWeeks = Lists.newArrayList();
        YearWeek yearWeek = new YearWeek(start);

        for (int i = 0; i < count; i++) {
            yearWeeks.add(yearWeek);
            yearWeek = yearWeek.getNextWeek();
        }

        return yearWeeks;
    }
}
