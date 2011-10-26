package org.synyx.urlaubsverwaltung.domain;

/**
 * @author  johannes
 */
public enum VacationType {

    ERHOLUNGSURLAUB("vac.erholung"),
    SONDERURLAUB("vac.sonder"),
    UNBEZAHLTERURLAUB("vac.unbezahlt"),
    UEBERSTUNDENABBUMMELN("vac.ueberstunden");

    private String vacationTypeName;

    private VacationType(String vacationTypeName) {

        this.vacationTypeName = vacationTypeName;
    }

    public String getStateName() {

        return this.vacationTypeName;
    }
}
