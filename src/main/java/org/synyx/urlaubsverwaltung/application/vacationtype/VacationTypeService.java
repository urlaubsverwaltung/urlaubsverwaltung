package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.util.List;
import java.util.Optional;

public interface VacationTypeService {

    Optional<VacationType> getById(Integer id);

    /**
     * Returns all created vacation types
     *
     * @return list of all created vacation types
     */
    List<VacationType> getAllVacationTypes();

    /**
     * Returns all active vacation type
     *
     * @return list of all active vacation types
     */
    List<VacationType> getActiveVacationTypes();

    /**
     * Returns the vacation types filter by the given vacationCategory parameter
     *
     * @param vacationCategory to filter out
     * @return a filtered list of @{VacationCategory}
     */
    List<VacationType> getActiveVacationTypesWithoutCategory(VacationCategory vacationCategory);
}
