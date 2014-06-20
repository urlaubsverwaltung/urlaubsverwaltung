package org.synyx.urlaubsverwaltung.core.calendar;

import org.joda.time.DateTimeConstants;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public enum Day {

    MONDAY(DateTimeConstants.MONDAY, "monday"),
    TUESDAY(DateTimeConstants.TUESDAY, "tuesday"),
    WEDNESDAY(DateTimeConstants.WEDNESDAY, "wednesday"),
    THURSDAY(DateTimeConstants.THURSDAY, "thursday"),
    FRIDAY(DateTimeConstants.FRIDAY, "friday"),
    SATURDAY(DateTimeConstants.SATURDAY, "saturday"),
    SUNDAY(DateTimeConstants.SUNDAY, "sunday");

    private Integer dayOfWeek;
    private String name;

    private Day(Integer dayOfWeek, String name) {

        this.dayOfWeek = dayOfWeek;
        this.name = name;
    }

    public Integer getDayOfWeek() {

        return dayOfWeek;
    }


    public String getName() {

        return name;
    }
}
