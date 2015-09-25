package org.synyx.urlaubsverwaltung.core.sicknote.statistics;

import org.joda.time.DateMidnight;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteDAO;

import java.math.BigDecimal;

import java.util.List;


/**
 * A statistic containing information about sick notes of a year.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteStatistics {

    private final DateMidnight created;

    private final int year;

    private final int totalNumberOfSickNotes;

    private final BigDecimal totalNumberOfSickDays;

    private final Long numberOfPersonsWithMinimumOneSickNote;

    public SickNoteStatistics(int year, SickNoteDAO sickNoteDAO, WorkDaysService calendarService) {

        this.year = year;
        this.numberOfPersonsWithMinimumOneSickNote = sickNoteDAO.findNumberOfPersonsWithMinimumOneSickNote(year);
        this.created = DateMidnight.now();

        List<SickNote> sickNotes = sickNoteDAO.findAllActiveByYear(year);

        this.totalNumberOfSickNotes = sickNotes.size();
        this.totalNumberOfSickDays = calculateTotalNumberOfSickDays(calendarService, sickNotes);
    }

    public int getTotalNumberOfSickNotes() {

        return this.totalNumberOfSickNotes;
    }


    public BigDecimal getTotalNumberOfSickDays() {

        return this.totalNumberOfSickDays;
    }


    private BigDecimal calculateTotalNumberOfSickDays(WorkDaysService calendarService, List<SickNote> sickNotes) {

        BigDecimal numberOfSickDays = BigDecimal.ZERO;

        for (SickNote sickNote : sickNotes) {
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

            BigDecimal workDays = calendarService.getWorkDays(sickNote.getDayLength(), startDate, endDate,
                    sickNote.getPerson());

            numberOfSickDays = numberOfSickDays.add(workDays);
        }

        return numberOfSickDays;
    }


    public DateMidnight getCreated() {

        return this.created;
    }


    public int getYear() {

        return this.year;
    }


    public Long getNumberOfPersonsWithMinimumOneSickNote() {

        return this.numberOfPersonsWithMinimumOneSickNote;
    }


    public BigDecimal getAverageDurationOfDiseasePerPerson() {

        Long numberOfPersons = getNumberOfPersonsWithMinimumOneSickNote();

        if (numberOfPersons == 0) {
            return BigDecimal.ZERO;
        } else {
            double averageDuration = getTotalNumberOfSickDays().doubleValue() / numberOfPersons;

            return BigDecimal.valueOf(averageDuration);
        }
    }
}
