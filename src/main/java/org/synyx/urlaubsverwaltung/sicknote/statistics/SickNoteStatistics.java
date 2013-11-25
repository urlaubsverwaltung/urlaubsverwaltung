package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.joda.time.DateMidnight;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;

import java.math.BigDecimal;

import java.util.List;


/**
 * A statistic containing information about sick notes of a year.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteStatistics {

    private DateMidnight created;

    private int year;

    private OwnCalendarService calendarService;

    private List<SickNote> sickNotes;

    public SickNoteStatistics(int year, List<SickNote> sickNotes, OwnCalendarService calendarService) {

        this.year = year;
        this.sickNotes = sickNotes;
        this.calendarService = calendarService;

        this.created = DateMidnight.now();
    }

    public int getTotalNumberOfSickNotes() {

        return this.sickNotes.size();
    }


    public BigDecimal getTotalNumberOfSickDays() {

        BigDecimal numberOfSickDays = BigDecimal.ZERO;

        for (SickNote sickNote : this.sickNotes) {
            BigDecimal workDays = getWorkDaysOfSickNote(sickNote);
            numberOfSickDays = numberOfSickDays.add(workDays);
        }

        return numberOfSickDays;
    }


    private BigDecimal getWorkDaysOfSickNote(SickNote sickNote) {

        DateMidnight sickNoteStartDate = sickNote.getStartDate();
        DateMidnight sickNoteEndDate = sickNote.getEndDate();

        DateMidnight startDate;
        DateMidnight endDate;

        Assert.isTrue(sickNoteStartDate.getYear() == this.year || sickNoteEndDate.getYear() == this.year,
            "Start date OR end date of the sick note must be in the year " + this.year);

        if (sickNoteStartDate.getYear() == this.year) {
            startDate = sickNoteStartDate;
        } else {
            startDate = sickNoteEndDate.dayOfYear().withMinimumValue();
        }

        if (sickNoteEndDate.getYear() == this.year) {
            endDate = sickNoteEndDate;
        } else {
            endDate = sickNoteStartDate.dayOfYear().withMaximumValue();
        }

        return calendarService.getWorkDays(DayLength.FULL, startDate, endDate);
    }


    public DateMidnight getCreated() {

        return this.created;
    }


    public int getYear() {

        return this.year;
    }


    public BigDecimal getAverageDurationOfDisease() {

        double averageDuration = getTotalNumberOfSickDays().doubleValue() / getTotalNumberOfSickNotes();

        return BigDecimal.valueOf(averageDuration);
    }
}
