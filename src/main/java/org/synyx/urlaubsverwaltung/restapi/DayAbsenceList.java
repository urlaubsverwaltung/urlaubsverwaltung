package org.synyx.urlaubsverwaltung.restapi;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
class DayAbsenceList {

    private final List<DayAbsence> absences;

    DayAbsenceList(List<DayAbsence> absences) {

        this.absences = absences;
    }

    public List<DayAbsence> getAbsences() {

        return absences;
    }
}
