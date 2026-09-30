package org.synyx.urlaubsverwaltung.account;

import org.jspecify.annotations.Nullable;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * One row of the remaining vacation days of a person that expired.
 */
record ExpiredRemainingVacationDaysCsvRow(
    String personnelNumber,
    String firstName,
    String lastName,
    List<String> departments,
    LocalDate expiryDate,
    BigDecimal expiredRemainingVacationDays,
    BigDecimal remainingVacationDaysNotExpiring,
    BigDecimal vacationDaysLeft
) {

    static ExpiredRemainingVacationDaysCsvRow of(ExpiredRemainingVacationDays expired, @Nullable PersonBasedata basedata, @Nullable List<String> departments) {

        final Account account = expired.account();
        final Person person = account.getPerson();

        return new ExpiredRemainingVacationDaysCsvRow(
            basedata == null || basedata.personnelNumber() == null ? "" : basedata.personnelNumber(),
            person.getFirstName(),
            person.getLastName(),
            departments == null ? List.of() : departments,
            account.getExpiryDate(),
            expired.expiredRemainingVacationDays(),
            expired.remainingVacationDaysNotExpiring(),
            expired.totalLeftVacationDays()
        );
    }
}
