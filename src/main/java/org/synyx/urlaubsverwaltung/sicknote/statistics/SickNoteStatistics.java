package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.format.annotation.DateTimeFormat;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;

/**
 * A statistic containing information about sick notes of a year.
 */
public class SickNoteStatistics {

    @DateTimeFormat(pattern = DD_MM_YYYY)
    private final LocalDate created;
    private final int year;
    private final int totalNumberOfSickNotes;
    private final BigDecimal totalNumberOfSickDays;
    private final Long numberOfPersonsWithMinimumOneSickNote;
    private final List<BigDecimal> numberOfSickDaysByMonth;
    private final List<BigDecimal> numberOfChildSickDaysByMonth;

    SickNoteStatistics(Clock clock, List<SickNote> sickNotes, WorkDaysCountService workDaysCountService) {

        final Year year = Year.now(clock);

        this.year = year.getValue();
        this.numberOfPersonsWithMinimumOneSickNote = sickNotes.stream().map(SickNote::getPerson).distinct().count();
        this.created = LocalDate.now(clock);

        this.totalNumberOfSickNotes = sickNotes.size();
        this.totalNumberOfSickDays = calculateTotalNumberOfSickDays(workDaysCountService, sickNotes);
        this.numberOfSickDaysByMonth = calculateTotalNumberOfSickDays(year, workDaysCountService, sickNotes, SICK_NOTE);
        this.numberOfChildSickDaysByMonth = calculateTotalNumberOfSickDays(year, workDaysCountService, sickNotes, SICK_NOTE_CHILD);
    }

    public int getTotalNumberOfSickNotes() {
        return totalNumberOfSickNotes;
    }

    public BigDecimal getTotalNumberOfSickDays() {
        return totalNumberOfSickDays;
    }

    public LocalDate getCreated() {
        return created;
    }

    public int getYear() {
        return year;
    }

    public Long getNumberOfPersonsWithMinimumOneSickNote() {
        return numberOfPersonsWithMinimumOneSickNote;
    }

    public BigDecimal getAverageDurationOfDiseasePerPerson() {
        final Long numberOfPersons = numberOfPersonsWithMinimumOneSickNote;
        if (numberOfPersons == 0) {
            return ZERO;
        }

        double averageDuration = totalNumberOfSickDays.doubleValue() / numberOfPersons;
        return BigDecimal.valueOf(averageDuration);
    }

    public List<BigDecimal> getNumberOfSickDaysByMonth() {
        return numberOfSickDaysByMonth;
    }

    public List<BigDecimal> getNumberOfChildSickDaysByMonth() {
        return numberOfChildSickDaysByMonth;
    }

    private BigDecimal calculateTotalNumberOfSickDays(WorkDaysCountService workDaysCountService, List<SickNote> sickNotes) {

        BigDecimal numberOfSickDays = ZERO;
        for (final SickNote sickNote : sickNotes) {

            final LocalDate firstDayOfYear = Year.of(year).atDay(1);
            final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

            final BigDecimal workDays = getWorkDays(workDaysCountService, sickNote, firstDayOfYear, lastDayOfYear);
            numberOfSickDays = numberOfSickDays.add(workDays);
        }

        return numberOfSickDays;
    }

    private List<BigDecimal> calculateTotalNumberOfSickDays(Year year, WorkDaysCountService workDaysCountService, List<SickNote> sickNotes, SickNoteCategory category) {

        final List<BigDecimal> values = new ArrayList<>();

        for (Month month : Month.values()) {

            final LocalDate firstDateOfMonth = year.atMonth(month).atDay(1);
            final LocalDate lastDateOfMonth = year.atMonth(month).atEndOfMonth();
            final DateRange monthDateRange = new DateRange(firstDateOfMonth, lastDateOfMonth);

            BigDecimal sumOfSickDaysInMonth = ZERO;

            for (SickNote sickNote : sickNotes) {
                final boolean matchesCategory = sickNote.getSickNoteType().getCategory().equals(category);
                final boolean touchesMonth = sickNote.getDateRange().isOverlapping(monthDateRange);
                if (matchesCategory && touchesMonth) {
                    final BigDecimal workDays = getWorkDays(workDaysCountService, sickNote, firstDateOfMonth, lastDateOfMonth);
                    sumOfSickDaysInMonth = sumOfSickDaysInMonth.add(workDays);
                }
            }

            values.add(sumOfSickDaysInMonth);
        }

        return values;
    }

    private BigDecimal getWorkDays(WorkDaysCountService workDaysCountService, SickNote sickNote, LocalDate rangeMin, LocalDate rangeMax) {
        final LocalDate startDate = sickNote.getStartDate().isBefore(rangeMin) ? rangeMin : sickNote.getStartDate();
        final LocalDate endDate = sickNote.getEndDate().isAfter(rangeMax) ? rangeMax : sickNote.getEndDate();
        return workDaysCountService.getWorkDaysCount(sickNote.getDayLength(), startDate, endDate, sickNote.getPerson());
    }

    @Override
    public String toString() {
        return "SickNoteStatistics{" +
            "created=" + created +
            ", year=" + year +
            ", totalNumberOfSickNotes=" + totalNumberOfSickNotes +
            ", totalNumberOfSickDays=" + totalNumberOfSickDays +
            ", numberOfPersonsWithMinimumOneSickNote=" + numberOfPersonsWithMinimumOneSickNote +
            ", numberOfSickDaysByMonth=" + numberOfSickDaysByMonth +
            ", numberOfChildSickDaysByMonth=" + numberOfChildSickDaysByMonth +
            '}';
    }
}
