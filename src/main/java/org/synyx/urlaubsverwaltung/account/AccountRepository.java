package org.synyx.urlaubsverwaltung.account;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.person.Person;


/**
 * Repository for {@link Account} entities.
 */
public interface AccountRepository extends CrudRepository<Account, Integer> {

    @Query("select x from Account x where YEAR(x.validFrom) = ?1 and x.person = ?2")
    Account getHolidaysAccountByYearAndPerson(int year, Person person);
}
