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


    void delete(AbsenceMapping absenceMapping);


    Optional<AbsenceMapping> getAbsenceByIdAndType(Integer id, AbsenceType absenceType);
}
