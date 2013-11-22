package org.synyx.urlaubsverwaltung.sicknote.statistics;

/**
 * Enum representing the months of a year.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public enum Month {

    JANUARY("month.january"),
    FEBRUARY("month.february"),
    MARCH("month.march"),
    APRIL("month.april"),
    MAY("month.may"),
    JUNE("month.june"),
    JULY("month.july"),
    AUGUST("month.august"),
    SEPTEMBER("month.september"),
    OCTOBER("month.october"),
    NOVEMBER("month.november"),
    DECEMBER("month.december");

    private String messageKey;

    private Month(String messageKey) {

        this.messageKey = messageKey;
    }

    public String getMessageKey() {

        return messageKey;
    }
}
