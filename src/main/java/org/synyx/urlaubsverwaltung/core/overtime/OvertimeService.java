package org.synyx.urlaubsverwaltung.core.overtime;

/**
 * Provides access to {@link Overtime} records. Business interactions are found in
 * {@link org.synyx.urlaubsverwaltung.core.overtime.OvertimeInteractionService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
public interface OvertimeService {

    /**
     * Saves an overtime record.
     *
     * @param  overtime  to be saved
     */
    void save(Overtime overtime);
}
