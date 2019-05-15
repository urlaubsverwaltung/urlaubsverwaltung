package org.synyx.urlaubsverwaltung.absence.api;

import java.util.List;


public class DayAbsenceList {

    private final List<DayAbsence> absences;

    DayAbsenceList(List<DayAbsence> absences) {

        this.absences = absences;
    }

    public List<DayAbsence> getAbsences() {

        return absences;
    }
}
