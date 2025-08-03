package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.format.annotation.DateTimeFormat;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;

/**
 * A statistic containing information about sick notes of a year.
 */
public class SickNoteStatistics {

    @DateTimeFormat(pattern = DD_MM_YYYY)
    private final LocalDate asOfDate;
    private final int year;
    private final int totalNumberOfAllSickNotes;
    private final BigDecimal totalNumberOfSickDaysAllCategories;
    private final Map<SickNoteCategory, BigDecimal> totalNumberOfSickNotesByCategory;
    private final Map<SickNoteCategory, BigDecimal> totalNumberOfSickDaysByCategory;
    private final Map<SickNoteCategory, BigDecimal> averageDurationOfSickNoteByCategory;
    private final Long numberOfPersonsWithMinimumOneSickNote;
    private final Long numberOfPersonsWithoutSickNote;
    private final List<BigDecimal> numberOfSickDaysByMonth;
    private final List<BigDecimal> numberOfChildSickDaysByMonth;

    SickNoteStatistics(Year year, LocalDate asOfDate, List<SickNote> sickNotes, List<Person> visibleActivePersonsForPerson, WorkDaysCountService workDaysCountService) {
        this.year = year.getValue();
        this.asOfDate = asOfDate;

        this.numberOfPersonsWithMinimumOneSickNote = sickNotes.stream().map(SickNote::getPerson).distinct().count();
        this.numberOfPersonsWithoutSickNote = calculateNumberOfPersonWithoutSickNote(visibleActivePersonsForPerson, sickNotes);

        this.totalNumberOfAllSickNotes = sickNotes.size();
        this.totalNumberOfSickNotesByCategory = calculateTotalNumberOfSickNotesByCategory(sickNotes);

        this.totalNumberOfSickDaysByCategory = calculateTotalNumberOfSickDaysByCategory(workDaysCountService, sickNotes);
        this.totalNumberOfSickDaysAllCategories = calculateTotalNumberOfSickDaysAllCategories(totalNumberOfSickDaysByCategory);
        this.averageDurationOfSickNoteByCategory = calculateAverageDurationOfSickNoteByCategory(totalNumberOfSickDaysByCategory, sickNotes);

        this.numberOfSickDaysByMonth = calculateTotalNumberOfSickDaysAllCategories(year, workDaysCountService, sickNotes, SICK_NOTE);
        this.numberOfChildSickDaysByMonth = calculateTotalNumberOfSickDaysAllCategories(year, workDaysCountService, sickNotes, SICK_NOTE_CHILD);
    }

    public List<BigDecimal> getNumberOfSickDaysByMonth() {
        return numberOfSickDaysByMonth;
    }

    public List<BigDecimal> getNumberOfChildSickDaysByMonth() {
        return numberOfChildSickDaysByMonth;
    }

    public int getTotalNumberOfAllSickNotes() {
        return totalNumberOfAllSickNotes;
    }

    public BigDecimal getTotalNumberOfSickNotes() {
        return totalNumberOfSickNotesByCategory.getOrDefault(SICK_NOTE, ZERO);
    }

    public BigDecimal getTotalNumberOfChildSickNotes() {
        return totalNumberOfSickNotesByCategory.getOrDefault(SICK_NOTE_CHILD, ZERO);
    }

    public BigDecimal getAtLeastOneSickNotePercent() {
        return (valueOf(numberOfPersonsWithMinimumOneSickNote)
            .divide(valueOf(numberOfPersonsWithMinimumOneSickNote).add(valueOf(numberOfPersonsWithoutSickNote)), 3, HALF_UP))
            .multiply(valueOf(100));
    }

    public Long getNumberOfPersonsWithMinimumOneSickNote() {
        return numberOfPersonsWithMinimumOneSickNote;
    }

    public Long getNumberOfPersonsWithoutSickNote() {
        return numberOfPersonsWithoutSickNote;
    }

    public BigDecimal getTotalNumberOfSickDaysAllCategories() {
        return totalNumberOfSickDaysAllCategories;
    }

    public BigDecimal getTotalNumberOfSickDays() {
        return totalNumberOfSickDaysByCategory.getOrDefault(SICK_NOTE, ZERO);
    }

    public BigDecimal getTotalNumberOfChildSickDays() {
        return totalNumberOfSickDaysByCategory.getOrDefault(SICK_NOTE_CHILD, ZERO);
    }

    public BigDecimal getAverageDurationOfAllSickNotes() {
        final BigDecimal totalNumberOfSickNotes = getTotalNumberOfSickNotes();
        if (Objects.equals(totalNumberOfSickNotes, ZERO)) {
            return ZERO;
        }

        return getTotalNumberOfSickDays().divide(getTotalNumberOfSickNotes(), 2, HALF_UP);
    }

    public BigDecimal getAverageDurationOfSickNote() {
        return averageDurationOfSickNoteByCategory.getOrDefault(SICK_NOTE, ZERO);
    }

    public BigDecimal getAverageDurationOfChildSickNote() {
        return averageDurationOfSickNoteByCategory.getOrDefault(SICK_NOTE_CHILD, ZERO);
    }

    public BigDecimal getAverageDurationOfDiseasePerPerson() {
        final Long numberOfPersons = numberOfPersonsWithMinimumOneSickNote;
        if (numberOfPersons == 0) {
            return ZERO;
        }

        return totalNumberOfSickDaysAllCategories.divide(valueOf(numberOfPersons), 2, HALF_UP);
    }

    public BigDecimal getAverageDurationOfDiseasePerPersonAndSick() {
        final Long numberOfPersons = numberOfPersonsWithMinimumOneSickNote;
        if (numberOfPersons == 0) {
            return ZERO;
        }

        return getTotalNumberOfSickDays().divide(valueOf(numberOfPersons), 2, HALF_UP);
    }

    public BigDecimal getAverageDurationOfDiseasePerPersonAndChildSick() {
        final Long numberOfPersons = numberOfPersonsWithMinimumOneSickNote;
        if (numberOfPersons == 0) {
            return ZERO;
        }

        return getTotalNumberOfChildSickDays().divide(valueOf(numberOfPersons), 2, HALF_UP);
    }

    public LocalDate getAsOfDate() {
        return asOfDate;
    }

    public int getYear() {
        return year;
    }

    private Long calculateNumberOfPersonWithoutSickNote(List<Person> visibleActivePersonsForPerson, List<SickNote> sickNotes) {
        final Set<Person> personsWithSickNotes = sickNotes.stream()
            .map(SickNote::getPerson)
            .collect(toSet());

        return visibleActivePersonsForPerson.stream()
            .filter(person -> !personsWithSickNotes.contains(person))
            .count();
    }

    private Map<SickNoteCategory, BigDecimal> calculateTotalNumberOfSickNotesByCategory(List<SickNote> sickNotes) {
        return sickNotes.stream()
            .map(SickNote::getSickNoteType)
            .collect(groupingBy(SickNoteType::getCategory, collectingAndThen(counting(), BigDecimal::valueOf)));
    }

    private Map<SickNoteCategory, BigDecimal> calculateTotalNumberOfSickDaysByCategory(WorkDaysCountService workDaysCountService, List<SickNote> sickNotes) {
        final LocalDate firstDayOfYear = Year.of(year).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Map<SickNoteCategory, BigDecimal> result = new EnumMap<>(SickNoteCategory.class);

        sickNotes.forEach(sickNote -> {
            final BigDecimal workDays = getWorkDays(workDaysCountService, sickNote, firstDayOfYear, lastDayOfYear);
            final BigDecimal newCount = result.getOrDefault(sickNote.getSickNoteType().getCategory(), ZERO).add(workDays);
            result.put(sickNote.getSickNoteType().getCategory(), newCount);
        });
        return result;
    }

    private Map<SickNoteCategory, BigDecimal> calculateAverageDurationOfSickNoteByCategory(Map<SickNoteCategory, BigDecimal> totalNumberOfSickDaysByCategory, List<SickNote> sickNotes) {
        final Map<SickNoteCategory, BigDecimal> result = new EnumMap<>(SickNoteCategory.class);

        final Map<SickNoteCategory, Long> countByCategory = sickNotes.stream()
            .collect(groupingBy(sickNote -> sickNote.getSickNoteType().getCategory(), counting()));

        Arrays.stream(SickNoteCategory.values())
            .forEach(type -> {
                final BigDecimal averageNumberOfDaysPerSickNote = totalNumberOfSickDaysByCategory.getOrDefault(type, ZERO)
                    .divide(valueOf(countByCategory.getOrDefault(type, 1L)), 2, HALF_UP);
                result.put(type, averageNumberOfDaysPerSickNote);
            });

        return result;
    }

    private BigDecimal calculateTotalNumberOfSickDaysAllCategories(Map<SickNoteCategory, BigDecimal> totalNumberOfSickDaysByCategory) {
        return totalNumberOfSickDaysByCategory.values().stream().reduce(ZERO, BigDecimal::add);
    }

    private List<BigDecimal> calculateTotalNumberOfSickDaysAllCategories(Year year, WorkDaysCountService workDaysCountService, List<SickNote> sickNotes, SickNoteCategory category) {

        final List<BigDecimal> values = new ArrayList<>();

        for (final Month month : Month.values()) {

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
            "created=" + asOfDate +
            ", year=" + year +
            ", totalNumberOfSickNotes=" + totalNumberOfAllSickNotes +
            ", totalNumberOfSickNotesByCategory=" + totalNumberOfSickNotesByCategory +
            ", totalNumberOfSickDaysAllCategories=" + totalNumberOfSickDaysAllCategories +
            ", totalNumberOfSickDaysByCategory=" + totalNumberOfSickDaysByCategory +
            ", averageDurationOfSickNoteByCategory=" + averageDurationOfSickNoteByCategory +
            ", numberOfPersonsWithMinimumOneSickNote=" + numberOfPersonsWithMinimumOneSickNote +
            ", numberOfPersonsWithoutSickNote=" + numberOfPersonsWithoutSickNote +
            ", numberOfSickDaysByMonth=" + numberOfSickDaysByMonth +
            ", numberOfChildSickDaysByMonth=" + numberOfChildSickDaysByMonth +
            '}';
    }
}
