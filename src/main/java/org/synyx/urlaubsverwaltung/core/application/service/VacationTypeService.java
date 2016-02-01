package org.synyx.urlaubsverwaltung.core.application.service;

import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;

import java.util.List;


public interface VacationTypeService {

    List<VacationType> getVacationTypes();
}
