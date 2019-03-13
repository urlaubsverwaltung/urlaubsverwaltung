package org.synyx.urlaubsverwaltung.core.overtime;

import org.springframework.data.repository.CrudRepository;

import java.util.List;


/**
 * Allows access to overtime comments.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
public interface OvertimeCommentDAO extends CrudRepository<OvertimeComment, Integer> {

    List<OvertimeComment> findByOvertime(Overtime overtime);
}
