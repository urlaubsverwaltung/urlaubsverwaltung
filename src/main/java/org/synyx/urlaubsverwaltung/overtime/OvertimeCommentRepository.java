package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Allows access to overtime comments.
 *
 * @since 2.11.0
 */
interface OvertimeCommentRepository extends CrudRepository<OvertimeCommentEntity, Long> {

    List<OvertimeCommentEntity> findByOvertimeIdOrderByIdDesc(Long overtimeId);
}
