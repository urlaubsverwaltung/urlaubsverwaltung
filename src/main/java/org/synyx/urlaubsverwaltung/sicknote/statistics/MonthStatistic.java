package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.synyx.urlaubsverwaltung.sicknote.SickNote;

import java.math.BigDecimal;

import java.util.List;


/**
 * Sick note statistic for a month.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class MonthStatistic {

    private Month month;

    private int numberOfSickNotes;

    private BigDecimal numberOfSickDays;

    public MonthStatistic(Month month, List<SickNote> sickNotes) {

        this.month = month;
        this.numberOfSickNotes = sickNotes.size();

        BigDecimal days = BigDecimal.ZERO;

        for (SickNote sickNote : sickNotes) {
            days = days.add(sickNote.getWorkDays());
        }

        this.numberOfSickDays = days;
    }

    public Month getMonth() {

        return month;
    }


    public int getNumberOfSickNotes() {

        return numberOfSickNotes;
    }


    public BigDecimal getNumberOfSickDays() {

        return numberOfSickDays;
    }
}
