package org.synyx.urlaubsverwaltung.application.comment;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface ApplicationCommentRepository extends CrudRepository<ApplicationCommentEntity, Long> {

    List<ApplicationCommentEntity> findByApplicationIdOrderByIdDesc(Long applicationId);
}
