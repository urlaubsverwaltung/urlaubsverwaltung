package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service for handling {@link SickNoteComment}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
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
