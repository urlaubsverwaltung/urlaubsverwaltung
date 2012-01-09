/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface HolidaysAccountDAO extends JpaRepository<HolidaysAccount, Integer> {

    @Query("select x from HolidaysAccount x where x.year = ?1 and x.person = ?2 and x.active = true")
    HolidaysAccount getHolidaysAccountByYearAndPerson(int year, Person person);


    @Query("select x from HolidaysAccount x where x.person = ?1")
    List<HolidaysAccount> getAllHolidaysAccountsByPerson(Person person);
}
