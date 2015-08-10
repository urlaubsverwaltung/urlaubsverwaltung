package org.synyx.urlaubsverwaltung.core.sync.absence;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.Optional;


/**
 * Service provides access to {@link AbsenceMapping}.
 *
 * <p>Daniel Hammann - <hammann@synyx.de>.</p>
 */
public interface AbsenceMappingService {

    /**
     * Creates mapping between application for leave and calendar sync event.
     *
     * @param  application
     * @param  eventId
     *
     * @return  created absence mapping
     */
    AbsenceMapping create(Application application, String eventId);


    /**
     * Creates mapping between sick note and calendar sync event.
     *
     * @param  sickNote
     * @param  eventId
     *
     * @return  created absence mapping
     */
    AbsenceMapping create(SickNote sickNote, String eventId);


    /**
     * Deletes an absence mapping.
     *
     * @param  absenceMapping
     */
    void delete(AbsenceMapping absenceMapping);


    /**
     * Returns an absence mapping between application id or sicknote id and event.
     *
     * @param  id  of application or id of sicknote
     * @param  absenceType  type ob absence
     *
     * @return  mapping between absence and event
     */
    Optional<AbsenceMapping> getAbsenceByIdAndType(Integer id, AbsenceType absenceType);
}
