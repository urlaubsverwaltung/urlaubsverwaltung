package org.synyx.urlaubsverwaltung.account;

import org.jspecify.annotations.Nullable;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;

/**
 * One row of the remaining vacation days of a person carried over into a new year.
 *
 * @param expiryDate {@code null} if the remaining vacation days do not expire
 */
record RemainingVacationDaysCsvRow(
    String personnelNumber,
    String firstName,
    String lastName,
    List<String> departments,
    BigDecimal remainingVacationDays,
    BigDecimal remainingVacationDaysNotExpiring,
    @Nullable LocalDate expiryDate
) {

    static RemainingVacationDaysCsvRow of(Account account, @Nullable PersonBasedata basedata, @Nullable List<String> departments) {

        final Person person = account.getPerson();
        final BigDecimal remainingVacationDays = Objects.requireNonNullElse(account.getRemainingVacationDays(), ZERO);
        final boolean remainingVacationDaysExpire = account.doRemainingVacationDaysExpire();

        return new RemainingVacationDaysCsvRow(
            basedata == null || basedata.personnelNumber() == null ? "" : basedata.personnelNumber(),
            person.getFirstName(),
            person.getLastName(),
            departments == null ? List.of() : departments,
            remainingVacationDays,
            remainingVacationDaysExpire ? Objects.requireNonNullElse(account.getRemainingVacationDaysNotExpiring(), ZERO) : remainingVacationDays,
            remainingVacationDaysExpire ? account.getExpiryDate() : null
        );
    }
}
