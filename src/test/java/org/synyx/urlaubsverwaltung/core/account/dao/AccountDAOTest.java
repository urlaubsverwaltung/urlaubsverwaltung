package org.synyx.urlaubsverwaltung.core.account.dao;


import org.joda.time.DateMidnight;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonDAO;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import static java.math.BigDecimal.TEN;
import static org.joda.time.DateTimeConstants.DECEMBER;
import static org.joda.time.DateTimeConstants.JANUARY;

@RunWith(SpringRunner.class)
@DataJpaTest
public class AccountDAOTest {

    @Autowired
    private AccountDAO sut;

    @Autowired
    private PersonDAO personDAO;

    @Test(expected = DataIntegrityViolationException.class)
    public void ensureUniqueConstraintOfPersonAndValidFrom() {

        final Person person = TestDataCreator.createPerson("test user");
        person.setId(1);
        personDAO.save(person);

        final DateMidnight validFrom = new DateMidnight(2014, JANUARY, 1);
        final DateMidnight validTo = new DateMidnight(2014, DECEMBER, 31);
        Account account = new Account(person, validFrom.toDate(), validTo.toDate(), TEN, TEN, TEN, "comment");
        sut.save(account);

        final DateMidnight validFrom2 = new DateMidnight(2014, JANUARY, 1);
        final DateMidnight validTo2 = new DateMidnight(2014, DECEMBER, 31);
        Account account2 = new Account(person, validFrom2.toDate(), validTo2.toDate(), TEN, TEN, TEN, "comment 2");
        sut.save(account2);
    }
}
