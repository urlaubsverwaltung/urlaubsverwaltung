package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.time.Month.APRIL;
import static org.assertj.core.api.Assertions.assertThat;

class ExpiredRemainingVacationDaysCsvRowTest {

    @Test
    void ofTakesTheValuesOfTheExpiredRemainingVacationDays() {

        final ExpiredRemainingVacationDays expired = expired();
        final PersonBasedata basedata = new PersonBasedata(new PersonId(1L), "42", "");

        assertThat(ExpiredRemainingVacationDaysCsvRow.of(expired, basedata, List.of("Entwicklung")))
            .isEqualTo(new ExpiredRemainingVacationDaysCsvRow("42", "Franka", "Potente", List.of("Entwicklung"),
                LocalDate.of(2027, APRIL, 1), new BigDecimal("3"), ONE, new BigDecimal("12")));
    }

    @Test
    void ofWritesEmptyPersonnelNumberAndNoDepartmentsWhenUnknown() {

        final ExpiredRemainingVacationDaysCsvRow row = ExpiredRemainingVacationDaysCsvRow.of(expired(), null, null);

        assertThat(row.personnelNumber()).isEmpty();
        assertThat(row.departments()).isEmpty();
    }

    @Test
    void ofWritesEmptyPersonnelNumberWhenBasedataHasNone() {

        final PersonBasedata basedata = new PersonBasedata(new PersonId(1L), null, "");

        assertThat(ExpiredRemainingVacationDaysCsvRow.of(expired(), basedata, List.of()).personnelNumber()).isEmpty();
    }

    private static ExpiredRemainingVacationDays expired() {
        final Person person = new Person("franka", "Potente", "Franka", "franka@example.org");
        final Account account = new Account();
        account.setPerson(person);
        account.setExpiryDateLocally(LocalDate.of(2027, APRIL, 1));
        return new ExpiredRemainingVacationDays(account, new BigDecimal("3"), ONE, new BigDecimal("12"));
    }
}
