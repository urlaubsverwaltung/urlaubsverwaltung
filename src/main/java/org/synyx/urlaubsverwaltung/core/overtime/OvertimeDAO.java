package org.synyx.urlaubsverwaltung.core.overtime;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Allows access to overtime records.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
public interface OvertimeDAO extends JpaRepository<Overtime, Integer> {

    // OK
}
