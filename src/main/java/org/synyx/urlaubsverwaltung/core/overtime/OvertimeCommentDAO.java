package org.synyx.urlaubsverwaltung.core.overtime;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Allows access to overtime comments.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
public interface OvertimeCommentDAO extends JpaRepository<OvertimeComment, Integer> {

    // OK
}
