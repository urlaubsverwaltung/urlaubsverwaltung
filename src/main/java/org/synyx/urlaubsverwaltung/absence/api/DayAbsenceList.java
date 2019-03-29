package org.synyx.urlaubsverwaltung.absence.api;

import java.util.List;


public class DayAbsenceList {

    private final List<DayAbsence> absences;

    public DayAbsenceList(List<DayAbsence> absences) {

        this.absences = absences;
    }

    public List<DayAbsence> getAbsences() {

        return absences;
    }
}
