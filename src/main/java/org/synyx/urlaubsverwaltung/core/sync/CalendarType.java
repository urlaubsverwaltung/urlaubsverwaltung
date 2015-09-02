package org.synyx.urlaubsverwaltung.core.sync;

/**
 * Represent the supported calendar providers to sync the absences.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public enum CalendarType {

    EWS("Exchange"),
    GOOGLE("Google");

    private String name;

    CalendarType(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }
}
