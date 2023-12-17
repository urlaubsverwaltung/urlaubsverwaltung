package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.math.BigDecimal.TEN;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

class AccountTest {

    static Stream<Arguments> GloballyLocallyExpiring() {
        return Stream.of(
            Arguments.of(true, null, true),
            Arguments.of(false, null, false),
            Arguments.of(true, true, true),
            Arguments.of(true, false, false),
            Arguments.of(false, true, true),
            Arguments.of(false, false, false)
        );
    }

    @ParameterizedTest
    @MethodSource("GloballyLocallyExpiring")
    void ensureSpecificRemainingVacationDaysExpireOverridesGlobal(boolean globally, Boolean locally, boolean result) {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, locally, expiryDate, TEN, TEN, TEN, "Comment");
        account.setDoRemainingVacationDaysExpireGlobally(globally);
        assertThat(account.doRemainingVacationDaysExpire()).isEqualTo(result);
    }

    @Test
    void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10L);
        person.setPermissions(List.of(USER));

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "Comment");

        final String accountToString = account.toString();
        assertThat(accountToString).isEqualTo("Account{id=null, person=Person{id='10'}, validFrom=2014-01-01, " +
            "validTo=2014-12-31, doRemainingVacationDaysExpireLocally=true, doRemainingVacationDaysExpireGlobally=false, expiryDateLocally=2014-04-01, expiryDateGlobally=null, " +
            "expiryNotificationSentDate=null, annualVacationDays=10, actualVacationDays=null, " +
            "remainingVacationDays=10, remainingVacationDaysNotExpiring=10}");
    }

    @Test
    void equals() {
        final Account accountOne = new Account();
        accountOne.setId(1L);

        final Account accountOneOne = new Account();
        accountOneOne.setId(1L);

        final Account accountTwo = new Account();
        accountTwo.setId(2L);

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
        accountOne.setId(1L);

        assertThat(accountOne.hashCode()).isEqualTo(32);
    }
}
