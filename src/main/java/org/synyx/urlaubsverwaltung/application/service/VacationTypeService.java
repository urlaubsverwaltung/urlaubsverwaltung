package org.synyx.urlaubsverwaltung.application.service;

import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;

import java.util.List;


public interface VacationTypeService {

    /**
     * Returns all created vacation types
     *
     * @return list of all created vacation types
     */
    List<VacationType> getVacationTypes();

    /**
     * Returns the vacation types filter by the given vacationCategory parameter
     *
     * @param vacationCategory to filter out
     * @return a filtered list of @{VacationCategory}
     */
    List<VacationType> getVacationTypesFilteredBy(VacationCategory vacationCategory);
}
