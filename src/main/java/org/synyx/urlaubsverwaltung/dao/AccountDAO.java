/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Person;

/**
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
public interface AccountDAO extends JpaRepository<Account, Integer> {
    
    @Query("select x from Account x where x.year = ?1 and x.person = ?2")
    List<Account> getHolidaysAccountsByYearAndPerson(int year, Person person);
    
}
