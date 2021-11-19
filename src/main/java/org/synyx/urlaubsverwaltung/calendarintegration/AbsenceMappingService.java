package org.synyx.urlaubsverwaltung.calendarintegration;

import java.util.Optional;

/**
 * Service provides access to {@link AbsenceMapping}.
 */
@Deprecated(since = "4.26.0", forRemoval = true)
public interface AbsenceMappingService {

    /**
     * Creates mapping between absence and calendar sync event.
     *
     * @param id                 of the absence, may be either an application for leave or a sick note
     * @param absenceMappingType describes the reason of the absence, either vacation or sick day
     * @param eventId            identifies the calendar event
     * @return created absence mapping
     */
    AbsenceMapping create(Integer id, AbsenceMappingType absenceMappingType, String eventId);

    /**
     * Deletes an absence mapping.
     *
     * @param absenceMapping to be deleted
     */
    void delete(AbsenceMapping absenceMapping);

    /**
     * Returns an absence mapping between application id or sick note id and event.
     *
     * @param id                 of application for leave or id of sick note
     * @param absenceMappingType type ob absence
     * @return mapping between absence and event
     */
    Optional<AbsenceMapping> getAbsenceByIdAndType(Integer id, AbsenceMappingType absenceMappingType);
}
