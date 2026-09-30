package org.synyx.urlaubsverwaltung.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


/**
 * Repository for {@link Account} entities.
 */
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    @Query("select x from account x where YEAR(x.validFrom) = ?1 and x.person = ?2")
    Optional<AccountEntity> getHolidaysAccountByYearAndPerson(int year, Person person);

    @Query("select a from account a where YEAR(a.validFrom) = :year and a.person in :persons")
    List<AccountEntity> findAccountByYearAndPersons(@Param("year") int year, @Param("persons") List<Person> persons);

    @Modifying
    void deleteByPerson(Person person);

    @Transactional
    @Modifying
    @Query("update account a set a.expiryNotificationSentDate = :expiryNotificationSentDate where a.id in :ids")
    void updateExpiryNotificationSentDate(@Param("ids") List<Long> ids, @Param("expiryNotificationSentDate") LocalDate expiryNotificationSentDate);

    List<AccountEntity> findAllByPersonId(Long personId);
}
