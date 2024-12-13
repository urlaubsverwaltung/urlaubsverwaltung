package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.springframework.stereotype.Service;

@Service
public class SickNoteCommentImportService {


    private final SickNoteCommentEntityRepository sickNoteCommentEntityRepository;

    SickNoteCommentImportService(SickNoteCommentEntityRepository sickNoteCommentEntityRepository) {
        this.sickNoteCommentEntityRepository = sickNoteCommentEntityRepository;
    }

    public void deleteAll() {
        sickNoteCommentEntityRepository.deleteAll();
    }

    public void importSickNoteComment(SickNoteCommentEntity entity) {
        sickNoteCommentEntityRepository.save(entity);
    }
}
