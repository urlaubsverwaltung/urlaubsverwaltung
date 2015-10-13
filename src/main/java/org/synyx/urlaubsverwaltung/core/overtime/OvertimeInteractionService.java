package org.synyx.urlaubsverwaltung.core.overtime;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Optional;


/**
 * Provides possibility to create {@link Overtime}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
public interface OvertimeInteractionService {

    /**
     * Saves an overtime record.
     *
     * @param  overtime  to be saved
     * @param  comment  contains further information to the overtime record, is optional
     * @param  recorder  identifies the person that recorded the overtime
     *
     * @return  the created overtime record
     */
    Overtime record(Overtime overtime, Optional<String> comment, Person recorder);
}
