package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
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
    void ensureBuildsCorrectVacationDaysLeftObject() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(new BigDecimal("10"))
            .build();

        assertThat(vacationDaysLeft).isNotNull();
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysBeforeAndAfterApril() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(new BigDecimal("10"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("18"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithOnlyUsedDaysAfterApril() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(ZERO)
            .forUsedDaysAfterApril(new BigDecimal("10"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("20"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(new BigDecimal("3"));
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysBeforeAprilGreaterThanRemainingVacationDays() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("10"))
            .forUsedDaysAfterApril(new BigDecimal("5"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("18"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysLessThanRemainingVacationDays() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("2"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ONE);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithOnlyUsedDaysBeforeAprilLessThanRemainingVacationDays() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("3"))
            .forUsedDaysAfterApril(ZERO)
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(new BigDecimal("2"));
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(new BigDecimal("2"));
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsingDifferenceOfNotExpiringRemainingVacationDays() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(ZERO)
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ONE);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsingNotExpiringRemainingVacationDays() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("27"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysBeforeApril() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("26"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysBeforeAprilWithoutUsedDaysAfterApril() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(ZERO)
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysExpiringAndNotExpiring() {

        final VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(new BigDecimal("1"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }
}
