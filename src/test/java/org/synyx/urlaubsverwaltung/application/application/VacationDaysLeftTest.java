package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link VacationDaysLeft}.
 */
class VacationDaysLeftTest {

    private VacationDaysLeft.Builder builder;

    @BeforeEach
    void setUp() {
        builder = VacationDaysLeft.builder().withAnnualVacation(new BigDecimal("28"))
            .withRemainingVacation(new BigDecimal("5"))
            .notExpiring(new BigDecimal("2"));
    }

    @Test
    void ensureEmptyVacationDaysLeft() {
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder().build();

        final LocalDate now = LocalDate.now();
        assertThat(vacationDaysLeft.getLeftVacationDays(now, true, now)).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getVacationDays()).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysLeft(now, true, now)).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getVacationDaysUsedNextYear()).isEqualByComparingTo(ZERO);
    }

    @Test
    void ensureBuildsCorrectVacationDaysLeftObject() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedVacationDaysBeforeExpiry(new BigDecimal("5"))
            .forUsedVacationDaysAfterExpiry(new BigDecimal("10"))
            .build();

        assertThat(vacationDaysLeft).isNotNull();
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysBeforeAndAfterApril() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedVacationDaysBeforeExpiry(new BigDecimal("5"))
            .forUsedVacationDaysAfterExpiry(new BigDecimal("10"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("18"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithOnlyUsedDaysAfterApril() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(new BigDecimal("10"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("20"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(new BigDecimal("3"));
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysbeforeExpiryDateGreaterThanRemainingVacationDays() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedVacationDaysBeforeExpiry(new BigDecimal("10"))
            .forUsedVacationDaysAfterExpiry(new BigDecimal("5"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("18"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysLessThanRemainingVacationDays() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedVacationDaysBeforeExpiry(new BigDecimal("2"))
            .forUsedVacationDaysAfterExpiry(new BigDecimal("2"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ONE);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithOnlyUsedDaysbeforeExpiryDateLessThanRemainingVacationDays() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedVacationDaysBeforeExpiry(new BigDecimal("3"))
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(new BigDecimal("2"));
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(new BigDecimal("2"));
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsingDifferenceOfNotExpiringRemainingVacationDays() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedVacationDaysBeforeExpiry(new BigDecimal("4"))
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ONE);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsingNotExpiringRemainingVacationDays() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedVacationDaysBeforeExpiry(new BigDecimal("4"))
            .forUsedVacationDaysAfterExpiry(new BigDecimal("2"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("27"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysbeforeExpiryDate() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedVacationDaysBeforeExpiry(new BigDecimal("5"))
            .forUsedVacationDaysAfterExpiry(new BigDecimal("2"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("26"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysbeforeExpiryDateWithoutUsedDaysAfterApril() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedVacationDaysBeforeExpiry(new BigDecimal("5"))
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysExpiringAndNotExpiring() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedVacationDaysBeforeExpiry(new BigDecimal("4"))
            .forUsedVacationDaysAfterExpiry(new BigDecimal("1"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void getRemainingVacationDaysLeftForThisYearAndBeforeExpiryDate() {
        final VacationDaysLeft vacationDaysLeft = builder
            .withAnnualVacation(new BigDecimal("10"))
            .withRemainingVacation(new BigDecimal("5"))
            .notExpiring(new BigDecimal("0"))
            .forUsedVacationDaysBeforeExpiry(ONE)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();

        final LocalDate someDayBeforeExpiryDate = LocalDate.now().with(firstDayOfYear());
        final LocalDate expiryDate = LocalDate.now().withMonth(Month.APRIL.getValue()).with(firstDayOfMonth());
        assertThat(vacationDaysLeft.getRemainingVacationDaysLeft(someDayBeforeExpiryDate, true, expiryDate)).isEqualByComparingTo("4");
    }

    @Test
    void getRemainingVacationDaysLeftIfDaysDoesNotExpire() {
        final VacationDaysLeft vacationDaysLeft = builder
            .withAnnualVacation(new BigDecimal("10"))
            .withRemainingVacation(new BigDecimal("4"))
            .notExpiring(new BigDecimal("0"))
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();

        final LocalDate someDayBeforeExpiryDate = LocalDate.now().with(firstDayOfYear());
        final LocalDate expiryDate = LocalDate.now().withMonth(Month.APRIL.getValue()).with(firstDayOfMonth());
        assertThat(vacationDaysLeft.getRemainingVacationDaysLeft(someDayBeforeExpiryDate, false, expiryDate)).isEqualByComparingTo("4");
    }

    @Test
    void getRemainingVacationDaysLeftForThisYearAndAfterApril() {
        final VacationDaysLeft vacationDaysLeft = builder
            .withAnnualVacation(new BigDecimal("10"))
            .withRemainingVacation(new BigDecimal("5"))
            .notExpiring(new BigDecimal("5"))
            .forUsedVacationDaysBeforeExpiry(ONE)
            .forUsedVacationDaysAfterExpiry(ONE)
            .build();

        final LocalDate someDayBeforeExpiryDate = LocalDate.now().with(lastDayOfYear());
        final LocalDate expiryDate = LocalDate.now().withMonth(Month.APRIL.getValue()).with(firstDayOfMonth());
        assertThat(vacationDaysLeft.getRemainingVacationDaysLeft(someDayBeforeExpiryDate, true, expiryDate)).isEqualByComparingTo("3");
    }
}
