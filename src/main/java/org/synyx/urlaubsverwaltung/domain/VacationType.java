package org.synyx.urlaubsverwaltung.domain;

/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public enum VacationType {

    HOLIDAY("vac.erholung"),
    SPECIALLEAVE("vac.sonder"),
    UNPAIDLEAVE("vac.unbezahlt"),
    OVERTIME("vac.ueberstunden");

    private String vacationTypeName;

    private VacationType(String vacationTypeName) {

        this.vacationTypeName = vacationTypeName;
    }

    public String getVacationTypeName() {

        return this.vacationTypeName;
    }
}
