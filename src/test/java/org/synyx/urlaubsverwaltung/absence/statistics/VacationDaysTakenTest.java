package org.synyx.urlaubsverwaltung.absence.statistics;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;

class VacationDaysTakenTest {

    private static Person person(long id) {
        final Person person = new Person();
        person.setId(id);
        return person;
    }

    private static Account account(Person person, String actualVacationDays, String remainingVacationDays,
                                    String remainingVacationDaysNotExpiring, boolean doExpire, LocalDate expiryDate) {
        final Account account = new Account(person, LocalDate.of(2024, JANUARY, 1), LocalDate.of(2024, DECEMBER, 31),
            doExpire, expiryDate, ZERO, new BigDecimal(remainingVacationDays), new BigDecimal(remainingVacationDaysNotExpiring), "");
        account.setActualVacationDays(new BigDecimal(actualVacationDays));
        return account;
    }

    private static VacationDaysLeft vacationDaysLeft(Account account, String usedBeforeExpiry, String usedAfterExpiry) {
        return VacationDaysLeft.builder()
            .withAnnualVacation(account.getActualVacationDays())
            .withRemainingVacation(account.getRemainingVacationDays())
            .notExpiring(account.getRemainingVacationDaysNotExpiring())
            .forUsedVacationDaysBeforeExpiry(new BigDecimal(usedBeforeExpiry))
            .forUsedVacationDaysAfterExpiry(new BigDecimal(usedAfterExpiry))
            .build();
    }

    @Test
    void sumAndPercentageAcrossMultiplePersons() {

        // 30 annual + 10 remaining, 3 used before expiry -> taken 3, validEntitlement 40
        final Person personA = person(1);
        final Account accountA = account(personA, "30", "10", "2", true, LocalDate.of(2024, MARCH, 31));
        final VacationDaysLeft leftA = vacationDaysLeft(accountA, "3", "0");

        // 20 annual + 0 remaining, 5 used before expiry -> taken 5, validEntitlement 20
        final Person personB = person(2);
        final Account accountB = account(personB, "20", "0", "0", true, LocalDate.of(2024, MARCH, 31));
        final VacationDaysLeft leftB = vacationDaysLeft(accountB, "5", "0");

        final Map<Account, VacationDaysLeft> input = new LinkedHashMap<>();
        input.put(accountA, leftA);
        input.put(accountB, leftB);

        final VacationDaysTakenResult actual = VacationDaysTaken.calculate(LocalDate.of(2024, JANUARY, 15), input);

        assertThat(actual.vacationDaysTaken()).isEqualByComparingTo("8");
        assertThat(actual.validEntitlement()).isEqualByComparingTo("60");
        assertThat(actual.percentage()).isEqualByComparingTo(new BigDecimal("8").divide(new BigDecimal("60"), 3, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
    }

    @Test
    void asOfDateBeforeExpiryDateUsesFullRemainingVacationInNumeratorAndDenominator() {

        final Person person = person(1);
        final Account account = account(person, "30", "10", "2", true, LocalDate.of(2024, MARCH, 31));
        final VacationDaysLeft left = vacationDaysLeft(account, "3", "0");

        final VacationDaysTakenResult actual = VacationDaysTaken.calculate(LocalDate.of(2024, JANUARY, 15), Map.of(account, left));

        // before expiry: the full 10 remaining days count, not just the 2 non-expiring ones
        assertThat(actual.validEntitlement()).isEqualByComparingTo("40"); // 30 + 10
        assertThat(actual.vacationDaysTaken()).isEqualByComparingTo("3");
    }

    @Test
    void asOfDateAfterExpiryDatePercentageUnchangedWhenNothingWasTaken() {

        final Person person = person(1);
        final Account account = account(person, "30", "10", "2", true, LocalDate.of(2024, MARCH, 31));
        final VacationDaysLeft left = vacationDaysLeft(account, "0", "0");

        final VacationDaysTakenResult before = VacationDaysTaken.calculate(LocalDate.of(2024, JANUARY, 15), Map.of(account, left));
        final VacationDaysTakenResult after = VacationDaysTaken.calculate(LocalDate.of(2024, APRIL, 1), Map.of(account, left));

        assertThat(before.percentage()).isEqualByComparingTo(ZERO);
        assertThat(after.percentage()).isEqualByComparingTo(ZERO);
        assertThat(before.vacationDaysTaken()).isEqualByComparingTo(ZERO);
        assertThat(after.vacationDaysTaken()).isEqualByComparingTo(ZERO);
    }

    @Test
    void expiredDaysAreSummedAcrossPersons() {

        final Person personA = person(1);
        final Account accountA = account(personA, "30", "10", "2", true, LocalDate.of(2024, MARCH, 31));
        final VacationDaysLeft leftA = vacationDaysLeft(accountA, "0", "0");

        final Person personB = person(2);
        final Account accountB = account(personB, "25", "4", "1", true, LocalDate.of(2024, MARCH, 31));
        final VacationDaysLeft leftB = vacationDaysLeft(accountB, "0", "0");

        final Map<Account, VacationDaysLeft> input = new LinkedHashMap<>();
        input.put(accountA, leftA);
        input.put(accountB, leftB);

        final VacationDaysTakenResult actual = VacationDaysTaken.calculate(LocalDate.of(2024, APRIL, 1), input);

        // A: 10 - 2 = 8 expired, B: 4 - 1 = 3 expired
        assertThat(actual.expiredRemainingVacationDays()).isEqualByComparingTo("11");
    }

    @Test
    void sumOnlyReflectsPersonsPresentInTheMap() {

        // a person without a vacation account for the year simply never becomes an entry here -
        // there is no other state this class could pick such a person up from.
        final Person personA = person(1);
        final Account accountA = account(personA, "30", "0", "0", true, LocalDate.of(2024, MARCH, 31));
        final VacationDaysLeft leftA = vacationDaysLeft(accountA, "10", "0");

        final Person personB = person(2);
        final Account accountB = account(personB, "30", "0", "0", true, LocalDate.of(2024, MARCH, 31));
        final VacationDaysLeft leftB = vacationDaysLeft(accountB, "20", "0");

        final Map<Account, VacationDaysLeft> input = new LinkedHashMap<>();
        input.put(accountA, leftA);
        input.put(accountB, leftB);

        final VacationDaysTakenResult actual = VacationDaysTaken.calculate(LocalDate.of(2024, JANUARY, 15), input);

        assertThat(actual.vacationDaysTaken()).isEqualByComparingTo("30"); // 10 + 20
    }

    @Test
    void personWithDifferentExpiryDateIsHandledIndividually() {

        final LocalDate asOfDate = LocalDate.of(2024, MAY, 1);

        // expiry already passed at the as-of date
        final Person personA = person(1);
        final Account accountA = account(personA, "30", "10", "2", true, LocalDate.of(2024, MARCH, 31));
        final VacationDaysLeft leftA = vacationDaysLeft(accountA, "0", "0");

        // expiry not yet reached at the same as-of date
        final Person personB = person(2);
        final Account accountB = account(personB, "30", "10", "2", true, LocalDate.of(2024, JUNE, 30));
        final VacationDaysLeft leftB = vacationDaysLeft(accountB, "0", "0");

        final Map<Account, VacationDaysLeft> input = new LinkedHashMap<>();
        input.put(accountA, leftA);
        input.put(accountB, leftB);

        final VacationDaysTakenResult actual = VacationDaysTaken.calculate(asOfDate, input);

        // A already lost the 8 expired days (10 - 2), B has not yet -> 8 expired in total, not 16
        assertThat(actual.expiredRemainingVacationDays()).isEqualByComparingTo("8");
        // A: validEntitlement 30+2=32, B: validEntitlement 30+10=40 -> 72 total
        assertThat(actual.validEntitlement()).isEqualByComparingTo("72");
    }

    @Test
    void zeroEntitlementSumYieldsZeroPercentageWithoutException() {

        final Person person = person(1);
        final Account account = account(person, "0", "0", "0", true, LocalDate.of(2024, MARCH, 31));
        final VacationDaysLeft left = vacationDaysLeft(account, "0", "0");

        final VacationDaysTakenResult actual = VacationDaysTaken.calculate(LocalDate.of(2024, JANUARY, 15), Map.of(account, left));

        assertThat(actual.validEntitlement()).isEqualByComparingTo(ZERO);
        assertThat(actual.percentage()).isEqualByComparingTo(ZERO);
    }

    @Test
    void emptyInputYieldsAnEmptyResultWithoutException() {

        final VacationDaysTakenResult actual = VacationDaysTaken.calculate(LocalDate.of(2024, JANUARY, 15), Map.of());

        assertThat(actual.vacationDaysTaken()).isEqualByComparingTo(ZERO);
        assertThat(actual.validEntitlement()).isEqualByComparingTo(ZERO);
        assertThat(actual.percentage()).isEqualByComparingTo(ZERO);
        assertThat(actual.expiredRemainingVacationDays()).isEqualByComparingTo(ZERO);
    }
}
