package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;


/**
 * A statistic containing information about sick notes of a year.
 */
public class SickNoteStatistics {

    private final LocalDate created;
    private final int year;
    private final int totalNumberOfSickNotes;
    private final BigDecimal totalNumberOfSickDays;
    private final Long numberOfPersonsWithMinimumOneSickNote;

    public SickNoteStatistics(int year, SickNoteService sickNoteService, WorkDaysService calendarService) {

        this.year = year;
        this.numberOfPersonsWithMinimumOneSickNote = sickNoteService.getNumberOfPersonsWithMinimumOneSickNote(year);
        this.created = LocalDate.now(UTC);

        List<SickNote> sickNotes = sickNoteService.getAllActiveByYear(year);

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
            LocalDate sickNoteStartDate = sickNote.getStartDate();
            LocalDate sickNoteEndDate = sickNote.getEndDate();

            LocalDate startDate;
            LocalDate endDate;

            Assert.isTrue(sickNoteStartDate.getYear() == this.year || sickNoteEndDate.getYear() == this.year,
                "Start date OR end date of the sick note must be in the year " + this.year);

            if (sickNoteStartDate.getYear() == this.year) {
                startDate = sickNoteStartDate;
            } else {
                startDate = sickNoteEndDate.with(firstDayOfYear());
            }

            if (sickNoteEndDate.getYear() == this.year) {
                endDate = sickNoteEndDate;
            } else {
                endDate = sickNoteStartDate.with(lastDayOfYear());
            }

            BigDecimal workDays = calendarService.getWorkDays(sickNote.getDayLength(), startDate, endDate,
                sickNote.getPerson());

            numberOfSickDays = numberOfSickDays.add(workDays);
        }

        return numberOfSickDays;
    }


    public LocalDate getCreated() {

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

    @Override
    public String toString() {
        return "SickNoteStatistics{" +
            "created=" + created +
            ", year=" + year +
            ", totalNumberOfSickNotes=" + totalNumberOfSickNotes +
            ", totalNumberOfSickDays=" + totalNumberOfSickDays +
            ", numberOfPersonsWithMinimumOneSickNote=" + numberOfPersonsWithMinimumOneSickNote +
            '}';
    }
}
