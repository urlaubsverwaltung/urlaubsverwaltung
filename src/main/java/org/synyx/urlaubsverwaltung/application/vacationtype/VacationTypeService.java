package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VacationTypeService {

    Optional<VacationType<?>> getById(Long id);

    /**
     * Returns all created vacation types
     *
     * @return list of all created vacation types
     */
    List<VacationType<?>> getAllVacationTypes();

    /**
     * Returns all active vacation type
     *
     * @return list of all active vacation types
     */
    List<VacationType<?>> getActiveVacationTypes();

    /**
     * Returns the vacation types filter by the given vacationCategory parameter
     *
     * @param vacationCategory to filter out
     * @return a filtered list of @{VacationCategory}
     */
    List<VacationType<?>> getActiveVacationTypesWithoutCategory(VacationCategory vacationCategory);

    /**
     * Updates the given vacation types
     *
     * @param vacationTypeUpdates the vacation types to update
     */
    void updateVacationTypes(List<VacationTypeUpdate> vacationTypeUpdates);

    /**
     * Creates new vacation types. Ids of every element must be null.
     *
     * @param vacationTypes to create
     */
    void createVacationTypes(Collection<VacationType<?>> vacationTypes);

    void insertDefaultVacationTypes();
}
