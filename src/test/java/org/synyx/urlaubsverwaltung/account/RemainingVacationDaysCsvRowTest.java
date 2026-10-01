package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.TWO;
import static java.math.BigDecimal.ZERO;
import static java.time.Month.APRIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createHolidaysAccount;

class RemainingVacationDaysCsvRowTest {

    @Test
    void ofExpiringAccount() {
        final Person person = new Person("franka", "Potente", "Franka", "franka@example.org");
        person.setId(1L);

        final Account account = createHolidaysAccount(person, 2027, new BigDecimal("30"), TEN, TWO, "comment");

        final RemainingVacationDaysCsvRow row = RemainingVacationDaysCsvRow.of(account, new PersonBasedata(new PersonId(1L), "42", "info"), List.of("Entwicklung", "Marketing"));

        assertThat(row).isEqualTo(new RemainingVacationDaysCsvRow("42", "Franka", "Potente", List.of("Entwicklung", "Marketing"), TEN, TWO, LocalDate.of(2027, APRIL, 1)));
    }

    @Test
    void ofAccountWithRemainingDaysNotExpiring() {
        final Person person = new Person("franka", "Potente", "Franka", "franka@example.org");
        person.setId(1L);

        final Account account = createHolidaysAccount(person, 2027, new BigDecimal("30"), TEN, TWO, "comment");
        account.setDoRemainingVacationDaysExpireLocally(false);

        final RemainingVacationDaysCsvRow row = RemainingVacationDaysCsvRow.of(account, null, null);

        assertThat(row).isEqualTo(new RemainingVacationDaysCsvRow("", "Franka", "Potente", List.of(), TEN, TEN, null));
    }

    @Test
    void ofExpiringAccountWithoutNotExpiringDays() {
        final Person person = new Person("franka", "Potente", "Franka", "franka@example.org");
        person.setId(1L);

        final Account account = createHolidaysAccount(person, 2027, new BigDecimal("30"), TEN, null, "comment");

        final RemainingVacationDaysCsvRow row = RemainingVacationDaysCsvRow.of(account, null, List.of());

        assertThat(row.remainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }
}
