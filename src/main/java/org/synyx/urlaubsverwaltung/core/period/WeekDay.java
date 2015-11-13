package org.synyx.urlaubsverwaltung.core.period;

import org.joda.time.DateTimeConstants;


/**
 * Represents a day of week.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public enum WeekDay {

    MONDAY(DateTimeConstants.MONDAY),
    TUESDAY(DateTimeConstants.TUESDAY),
    WEDNESDAY(DateTimeConstants.WEDNESDAY),
    THURSDAY(DateTimeConstants.THURSDAY),
    FRIDAY(DateTimeConstants.FRIDAY),
    SATURDAY(DateTimeConstants.SATURDAY),
    SUNDAY(DateTimeConstants.SUNDAY);

    private Integer dayOfWeek;

    WeekDay(Integer dayOfWeek) {

        this.dayOfWeek = dayOfWeek;
    }

    public Integer getDayOfWeek() {

        return dayOfWeek;
    }
}
