package org.synyx.urlaubsverwaltung.restapi.absence;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DayAbsenceList {

    private final List<DayAbsence> absences;

    public DayAbsenceList(List<DayAbsence> absences) {

        this.absences = absences;
    }

    public List<DayAbsence> getAbsences() {

        return absences;
    }
}
