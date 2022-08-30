package org.synyx.urlaubsverwaltung.account;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;

import static java.math.BigDecimal.TEN;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AccountRepositoryIT extends TestContainersBase {

    @Autowired
    private AccountRepository sut;

    @Autowired
    private PersonService personService;

    @Test
    void ensureUniqueConstraintOfPersonAndValidFrom() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson = personService.create(person);

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        Account account = new Account(savedPerson, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        sut.save(account);

        final LocalDate validFrom2 = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo2 = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate2 = LocalDate.of(2014, APRIL, 1);
        Account account2 = new Account(savedPerson, validFrom2, validTo2, true, expiryDate2, TEN, TEN, TEN, "comment 2");
        assertThatThrownBy(() -> sut.save(account2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
