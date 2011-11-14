/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;

import java.util.List;


/**
 * @author  aljona
 */
public interface UrlaubskontoDAO extends JpaRepository<Urlaubskonto, Integer> {

    @Query("select x from Urlaubskonto x where x.year = ? and x.person = ?")
    Urlaubskonto getUrlaubskontoForDateAndPerson(Integer year, Person person);


    @Query("select x from Urlaubskonto x where x.year = ?")
    List<Urlaubskonto> getUrlaubskontoForYear(Integer year);


    @Query("select x from Urlaubskonto x where x.person = ?")
    List<Urlaubskonto> getAllUrlaubskontoByPerson(Person person);
}
