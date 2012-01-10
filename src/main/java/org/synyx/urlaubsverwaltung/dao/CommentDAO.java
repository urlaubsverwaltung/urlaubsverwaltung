/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import org.synyx.urlaubsverwaltung.domain.Comment;


/**
 * @author  Aljona Murygina
 */
public interface CommentDAO extends JpaRepository<Comment, Integer> {
}
