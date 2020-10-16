package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;

import java.math.BigDecimal;


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

        Assert.assertNotNull("Should not be null", vacationDaysLeft);
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysBeforeAndAfterApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(new BigDecimal("10"))
            .build();

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of left vacation days", new BigDecimal("18"),
            vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days that do not expire", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }


    @Test
    void ensureCorrectVacationDaysLeftWithOnlyUsedDaysAfterApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(BigDecimal.ZERO)
            .forUsedDaysAfterApril(new BigDecimal("10"))
            .build();

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of left vacation days", new BigDecimal("20"),
            vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days", new BigDecimal("3"),
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days that do not expire", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysBeforeAprilGreaterThanRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("10"))
            .forUsedDaysAfterApril(new BigDecimal("5"))
            .build();

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of left vacation days", new BigDecimal("18"),
            vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days that do not expire", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsedDaysLessThanRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("2"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .build();

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of left vacation days", new BigDecimal("28"),
            vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days", BigDecimal.ONE,
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days that do not expire", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }


    @Test
    void ensureCorrectVacationDaysLeftWithOnlyUsedDaysBeforeAprilLessThanRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("3"))
            .forUsedDaysAfterApril(BigDecimal.ZERO)
            .build();

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of left vacation days", new BigDecimal("28"),
            vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days", new BigDecimal("2"),
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days that do not expire", new BigDecimal("2"),
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsingDifferenceOfNotExpiringRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(BigDecimal.ZERO)
            .build();

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of left vacation days", new BigDecimal("28"),
            vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days", BigDecimal.ONE,
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days that do not expire", BigDecimal.ONE,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsingNotExpiringRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .build();

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of left vacation days", new BigDecimal("27"),
            vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days that do not expire", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysBeforeApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .build();

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of left vacation days", new BigDecimal("26"),
            vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days that do not expire", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysBeforeAprilWithoutUsedDaysAfterApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(BigDecimal.ZERO)
            .build();

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of left vacation days", new BigDecimal("28"),
            vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days that do not expire", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }


    @Test
    void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysExpiringAndNotExpiring() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(new BigDecimal("1"))
            .build();

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of left vacation days", new BigDecimal("28"),
            vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of left remaining vacation days that do not expire", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }
}
