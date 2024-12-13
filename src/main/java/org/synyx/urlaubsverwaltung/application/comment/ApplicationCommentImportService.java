package org.synyx.urlaubsverwaltung.application.comment;

import org.springframework.stereotype.Service;

@Service
public class ApplicationCommentImportService {

    private final ApplicationCommentRepository applicationCommentRepository;

    ApplicationCommentImportService(ApplicationCommentRepository applicationCommentRepository) {
        this.applicationCommentRepository = applicationCommentRepository;
    }

    public void deleteAll() {
        applicationCommentRepository.deleteAll();
    }

    public void importApplicationComment(ApplicationCommentEntity entity) {
        applicationCommentRepository.save(entity);
    }
}
