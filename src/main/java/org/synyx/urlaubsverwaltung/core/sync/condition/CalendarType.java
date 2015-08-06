package org.synyx.urlaubsverwaltung.core.sync.condition;

/**
 * Represent the supported calendar providers to sync the absences.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public enum CalendarType {

    EWS("ews"),
    GOOGLE("google");

    private String name;

    CalendarType(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }


    public static boolean contains(String name) {

        for (CalendarType calendarType : CalendarType.values()) {
            if (calendarType.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }
}
