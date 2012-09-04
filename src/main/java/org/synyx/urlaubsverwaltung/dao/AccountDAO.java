
package org.synyx.urlaubsverwaltung.dao;

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
    Account getHolidaysAccountByYearAndPerson(int year, Person person);
    
}
