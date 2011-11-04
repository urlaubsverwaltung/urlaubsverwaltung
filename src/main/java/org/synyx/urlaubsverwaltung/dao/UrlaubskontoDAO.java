/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;

import java.util.List;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;


/**
 * @author  aljona
 */
public interface UrlaubskontoDAO extends JpaRepository<Urlaubskonto, Integer> {

    @Query("select x from Urlaubskonto x where x.year = ? and x.person = ?")
    Urlaubskonto getUrlaubskontoForDateAndPerson(Integer year, Person person);

}
