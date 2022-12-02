package org.synyx.urlaubsverwaltung.account;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;
import java.util.Optional;


/**
 * Repository for {@link Account} entities.
 */
public interface AccountRepository extends CrudRepository<AccountEntity, Integer> {

    @Query("select x from account x where YEAR(x.validFrom) = ?1 and x.person = ?2")
    Optional<AccountEntity> getHolidaysAccountByYearAndPerson(int year, Person person);

    @Query("select a from account a where YEAR(a.validFrom) = :year and a.person in :persons")
    List<AccountEntity> findAccountByYearAndPersons(@Param("year") int year, @Param("persons") List<Person> persons);

    @Modifying
    void deleteByPerson(Person person);
}
