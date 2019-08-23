package org.synyx.urlaubsverwaltung.account.domain;

import org.junit.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

public class AccountTest {

    @Test
    public void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPassword("Theo");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));
        final Account account = new Account(person, LocalDate.MIN, LocalDate.MAX, TEN, TEN, TEN, "Comment");

        final String accountToString = account.toString();
        assertThat(accountToString).isEqualTo("Account[" +
            "person=10," +
            "validFrom=01.01.+1000000000," +
            "validTo=31.12.+999999999," +
            "annualVacationDays=10," +
            "vacationDays=<null>," +
            "remainingVacationDays=10," +
            "remainingVacationDaysNotExpiring=10," +
            "comment=Comment]");
        assertThat(accountToString).doesNotContain("Theo", "USER", "NOTIFICATION_USER");
    }
}
