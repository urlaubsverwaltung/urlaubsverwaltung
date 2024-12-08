package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AccountRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private AccountRepository sut;

    @Autowired
    private PersonService personService;

    @Test
    void ensureUniqueConstraintOfPersonAndValidFrom() {

        final Person person = personService.create("muster", "Marlene", "Muster", "muster@example.org");

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final AccountEntity accountEntity = new AccountEntity(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        sut.save(accountEntity);

        final LocalDate validTo2 = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate2 = LocalDate.of(2014, APRIL, 1);
        final AccountEntity accountEntity2 = new AccountEntity(person, validFrom, validTo2, true, expiryDate2, TEN, TEN, TEN, "comment 2");
        assertThatThrownBy(() -> sut.saveAndFlush(accountEntity2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }


    @Test
    void ensureFindAccountByYearAndPersons() {

        final Person savedPerson = personService.create("muster", "Marlene", "Muster", "muster@example.org");

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);

        final AccountEntity accountToFind = new AccountEntity(savedPerson, validFrom, validTo, null, expiryDate, TEN, TEN, TEN, "comment");
        final AccountEntity savedAccountToFind = sut.save(accountToFind);

        final Person savedOtherPerson = personService.create("otherPerson", "person", "other", "other@example.org");
        final AccountEntity otherAccountToFind = new AccountEntity(savedOtherPerson, validFrom, validTo, null, expiryDate, TEN, TEN, TEN, "comment");
        final AccountEntity savedOtherAccountToFind = sut.save(otherAccountToFind);

        /* Do not find these accounts */
        final Person savedPersonNotInSearch = personService.create("personNotInSearch", "person", "notInSearch", "notInSearch@example.org");
        final AccountEntity accountWrongPerson = new AccountEntity(savedPersonNotInSearch, validFrom, validTo, null, expiryDate, TEN, TEN, TEN, "comment");
        sut.save(accountWrongPerson);

        final LocalDate validFrom2015 = LocalDate.of(2015, JANUARY, 1);
        final LocalDate validTo2015 = LocalDate.of(2015, DECEMBER, 31);
        final LocalDate expiryDate2015 = LocalDate.of(2015, APRIL, 1);
        final AccountEntity accountWrongYear = new AccountEntity(savedPerson, validFrom2015, validTo2015, null, expiryDate2015, TEN, TEN, TEN, "comment");
        sut.save(accountWrongYear);

        assertThat(sut.findAccountByYearAndPersons(2014, List.of(savedPerson, savedOtherPerson)))
            .containsExactly(savedAccountToFind, savedOtherAccountToFind);
    }
}
