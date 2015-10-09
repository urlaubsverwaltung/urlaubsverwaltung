package org.synyx.urlaubsverwaltung.core.calendar.workingtime;

import org.joda.time.DateTimeConstants;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public enum Day {

    MONDAY(DateTimeConstants.MONDAY),
    TUESDAY(DateTimeConstants.TUESDAY),
    WEDNESDAY(DateTimeConstants.WEDNESDAY),
    THURSDAY(DateTimeConstants.THURSDAY),
    FRIDAY(DateTimeConstants.FRIDAY),
    SATURDAY(DateTimeConstants.SATURDAY),
    SUNDAY(DateTimeConstants.SUNDAY);

    private Integer dayOfWeek;

    Day(Integer dayOfWeek) {

        this.dayOfWeek = dayOfWeek;
    }

    public Integer getDayOfWeek() {

        return dayOfWeek;
    }
}
