package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

class HolidayAccountVacationDaysTest {

    @Test
    void ensureEqualsIsTrueWhenAccountIsEqual() {

        final Account accountOne = new Account();
        accountOne.setId(1L);

        final Account accountTwo = new Account();
        accountTwo.setId(2L);

        final HolidayAccountVacationDays one = new HolidayAccountVacationDays(accountOne, null, null);
        final HolidayAccountVacationDays two = new HolidayAccountVacationDays(accountOne, null, null);

        assertThat(one.equals(two)).isTrue();
    }

    @Test
    void ensureEqualsIgnoresVacationDaysLeftYear() {

        final Account accountOne = new Account();
        accountOne.setId(1L);

        final Account accountTwo = new Account();
        accountTwo.setId(2L);

        final VacationDaysLeft daysLeftYear = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .withVacationDaysUsedNextYear(ZERO)
            .build();

        final HolidayAccountVacationDays one = new HolidayAccountVacationDays(accountOne, daysLeftYear, null);
        final HolidayAccountVacationDays two = new HolidayAccountVacationDays(accountOne, null, null);

        assertThat(one.equals(two)).isTrue();
    }

    @Test
    void ensureEqualsIgnoresVacationDaysLeftDateRange() {

        final Account accountOne = new Account();
        accountOne.setId(1L);

        final Account accountTwo = new Account();
        accountTwo.setId(2L);

        final VacationDaysLeft daysLeftDateRange = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .withVacationDaysUsedNextYear(ZERO)
            .build();

        final HolidayAccountVacationDays one = new HolidayAccountVacationDays(accountOne, null, daysLeftDateRange);
        final HolidayAccountVacationDays two = new HolidayAccountVacationDays(accountOne, null, null);

        assertThat(one.equals(two)).isTrue();
    }
}
