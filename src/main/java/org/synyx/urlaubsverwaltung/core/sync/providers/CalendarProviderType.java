package org.synyx.urlaubsverwaltung.core.sync.providers;

/**
 * Represent the supported calendar providers to sync the absences.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public enum CalendarProviderType {

    EWS("Exchange"),
    GOOGLE("Google");

    private String name;

    CalendarProviderType(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }
}
