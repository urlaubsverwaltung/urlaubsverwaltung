package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;


/**
 * A statistic containing information about sick notes of a year.
 */
public class SickNoteStatistics {

    private final Instant created;
    private final int year;
    private final int totalNumberOfSickNotes;
    private final BigDecimal totalNumberOfSickDays;
    private final Long numberOfPersonsWithMinimumOneSickNote;

    public SickNoteStatistics(Clock clock, SickNoteService sickNoteService, WorkDaysService calendarService) {

        this.year = Year.now(clock).getValue();
        this.numberOfPersonsWithMinimumOneSickNote = sickNoteService.getNumberOfPersonsWithMinimumOneSickNote(year);
        this.created = Instant.now(clock);

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
            Instant sickNoteStartDate = sickNote.getStartDate();
            Instant sickNoteEndDate = sickNote.getEndDate();

            Instant startDate;
            Instant endDate;

            Assert.isTrue(Year.from(sickNoteStartDate).getValue() == this.year || Year.from(sickNoteEndDate).getValue() == this.year,
                "Start date OR end date of the sick note must be in the year " + this.year);

            if (Year.from(sickNoteStartDate).getValue() == this.year) {
                startDate = sickNoteStartDate;
            } else {
                startDate = sickNoteEndDate.with(firstDayOfYear());
            }

            if (Year.from(sickNoteEndDate).getValue() == this.year) {
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


    public Instant getCreated() {

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
