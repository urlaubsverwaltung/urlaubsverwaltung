package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.joda.time.DateMidnight;

import java.math.BigDecimal;

import java.util.List;


/**
 * A statistic containing information about sick notes of a year.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteStatistics {

    private int totalNumberOfSickNotes;

    private BigDecimal totalNumberOfSickDays;

    List<MonthStatistic> monthStatistics;

    private DateMidnight created;

    private int year;

    public SickNoteStatistics(int year, List<MonthStatistic> monthStatistics) {

        this.created = DateMidnight.now();
        this.year = year;
        this.monthStatistics = monthStatistics;

        int numberOfSickNotes = 0;
        BigDecimal numberOfSickDays = BigDecimal.ZERO;

        for (MonthStatistic statistic : this.monthStatistics) {
            numberOfSickNotes += statistic.getNumberOfSickNotes();
            numberOfSickDays = numberOfSickDays.add(statistic.getNumberOfSickDays());
        }

        this.totalNumberOfSickNotes = numberOfSickNotes;
        this.totalNumberOfSickDays = numberOfSickDays;
    }

    public int getTotalNumberOfSickNotes() {

        return totalNumberOfSickNotes;
    }


    public BigDecimal getTotalNumberOfSickDays() {

        return totalNumberOfSickDays;
    }


    public List<MonthStatistic> getMonthStatistics() {

        return monthStatistics;
    }


    public DateMidnight getCreated() {

        return created;
    }


    public int getYear() {

        return year;
    }
}
