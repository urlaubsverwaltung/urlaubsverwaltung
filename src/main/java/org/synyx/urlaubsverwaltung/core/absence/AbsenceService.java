package org.synyx.urlaubsverwaltung.core.absence;

import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;

import java.util.List;


public interface AbsenceService {

    List<Absence> getOpenAbsences();
}
