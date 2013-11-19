package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service for handling {@link org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteComment}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Transactional
public class SickNoteCommentService {

    private SickNoteCommentDAO dao;

    @Autowired
    public SickNoteCommentService(SickNoteCommentDAO dao) {

        this.dao = dao;
    }


    public SickNoteCommentService() {
    }

    public void save(SickNoteComment comment) {

        dao.save(comment);
    }


    public List<SickNoteComment> getAll() {

        return dao.findAll();
    }


    public SickNoteComment getById(Integer id) {

        return dao.findOne(id);
    }
}
