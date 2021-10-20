package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

class AccountTest {

    @Test
    void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));
        final Account account = new Account(person, LocalDate.MIN, LocalDate.MAX, TEN, TEN, TEN, "Comment");

        final String accountToString = account.toString();
        assertThat(accountToString).isEqualTo("Account{person=Person{id='10'}, validFrom=-999999999-01-01," +
            " validTo=+999999999-12-31, annualVacationDays=10, vacationDays=null, remainingVacationDays=10," +
            " remainingVacationDaysNotExpiring=10}");
    }

    @Test
    void equals() {
        final Account accountOne = new Account();
        accountOne.setId(1);

        final Account accountOneOne = new Account();
        accountOneOne.setId(1);

        final Account accountTwo = new Account();
        accountTwo.setId(2);

        assertThat(accountOne)
            .isEqualTo(accountOne)
            .isEqualTo(accountOneOne)
            .isNotEqualTo(accountTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final Account accountOne = new Account();
        accountOne.setId(1);

        assertThat(accountOne.hashCode()).isEqualTo(32);
    }
}
