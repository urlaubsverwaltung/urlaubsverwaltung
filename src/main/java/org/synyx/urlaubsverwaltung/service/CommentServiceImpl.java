/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.dao.CommentDAO;
import org.synyx.urlaubsverwaltung.domain.Comment;


/**
 * @author  Aljona Murygina
 */
@Transactional
public class CommentServiceImpl implements CommentService {

    private CommentDAO commentDAO;

    @Autowired
    public CommentServiceImpl(CommentDAO commentDAO) {

        this.commentDAO = commentDAO;
    }

    @Override
    public void saveComment(Comment comment) {

        commentDAO.save(comment);
    }
}
