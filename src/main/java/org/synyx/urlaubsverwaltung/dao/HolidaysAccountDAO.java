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

    @Query("select from HolidaysAccount where year = ?1 and person = ?2")
    HolidaysAccount getHolidaysAccountByYearAndPerson(Integer year, Person person);


    @Query("select from HolidaysAccount where year = ?1")
    List<HolidaysAccount> getAllHolidaysAccountsByYear(Integer year);


    @Query("select from HolidaysAccount where person = ?1")
    List<HolidaysAccount> getAllHolidaysAccountsByPerson(Person person);
}
