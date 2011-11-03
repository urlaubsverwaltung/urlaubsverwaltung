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


/**
 * @author  aljona
 */
public interface UrlaubsanspruchDAO extends JpaRepository<Urlaubsanspruch, Integer> {

    @Query("select x from Urlaubsanspruch x where x.year = ? and x.person = ?")
    Urlaubsanspruch getUrlaubsanspruchByDate(Integer year, Person person);


    @Query("select x from Urlaubsanspruch x where x.person = ?")
    List<Urlaubsanspruch> getUrlaubsanspruchByDate(Person person);
}
