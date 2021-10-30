package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;

import java.math.BigDecimal;

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

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(new BigDecimal("10"))
            .build();

        assertThat(vacationDaysLeft).isNotNull();
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysBeforeAndAfterApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(new BigDecimal("10"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("18"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ZERO);
    }


    @Test
    void ensureCorrectVacationDaysLeftWithOnlyUsedDaysAfterApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(BigDecimal.ZERO)
            .forUsedDaysAfterApril(new BigDecimal("10"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("20"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(new BigDecimal("3"));
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ZERO);
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysBeforeAprilGreaterThanRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("10"))
            .forUsedDaysAfterApril(new BigDecimal("5"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("18"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ZERO);
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysLessThanRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("2"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ONE);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ZERO);
    }


    @Test
    void ensureCorrectVacationDaysLeftWithOnlyUsedDaysBeforeAprilLessThanRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("3"))
            .forUsedDaysAfterApril(BigDecimal.ZERO)
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(new BigDecimal("2"));
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(new BigDecimal("2"));
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsingDifferenceOfNotExpiringRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(BigDecimal.ZERO)
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ONE);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ONE);
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsingNotExpiringRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("27"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ZERO);
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysBeforeApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("26"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ZERO);
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysBeforeAprilWithoutUsedDaysAfterApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(BigDecimal.ZERO)
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ZERO);
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysExpiringAndNotExpiring() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(new BigDecimal("1"))
            .build();

        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("28"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(BigDecimal.ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ZERO);
    }
}
