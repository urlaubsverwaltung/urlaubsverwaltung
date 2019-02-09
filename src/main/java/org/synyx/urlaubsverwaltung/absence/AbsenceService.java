package org.synyx.urlaubsverwaltung.absence;


import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;


public interface AbsenceService {

    List<Absence> getOpenAbsences(Person person);
}
