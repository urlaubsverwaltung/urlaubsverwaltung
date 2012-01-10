/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.domain.Comment;


/**
 * @author  Aljona Murygina
 */
public interface CommentService {

    void saveComment(Comment comment);
}
