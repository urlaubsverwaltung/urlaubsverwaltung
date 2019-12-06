package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.account.domain.VacationDaysLeft;

import java.math.BigDecimal;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.account.domain.VacationDaysLeft}.
 */
public class VacationDaysLeftTest {

    private VacationDaysLeft.Builder builder;

    @Before
    public void setUp() {

        builder = VacationDaysLeft.builder().withAnnualVacation(new BigDecimal("28"))
            .withRemainingVacation(new BigDecimal("5"))
            .notExpiring(new BigDecimal("2"));
    }


    @Test
    public void ensureBuildsCorrectVacationDaysLeftObject() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(new BigDecimal("10"))
            .get();

        Assert.assertNotNull("Should not be null", vacationDaysLeft);
    }


    @Test
    public void ensureCorrectVacationDaysLeftWithUsedDaysBeforeAndAfterApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(new BigDecimal("10"))
            .get();

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
    public void ensureCorrectVacationDaysLeftWithOnlyUsedDaysAfterApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(BigDecimal.ZERO)
            .forUsedDaysAfterApril(new BigDecimal("10"))
            .get();

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
    public void ensureCorrectVacationDaysLeftWithUsedDaysBeforeAprilGreaterThanRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("10"))
            .forUsedDaysAfterApril(new BigDecimal("5"))
            .get();

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
    public void ensureCorrectVacationDaysLeftWithUsedDaysLessThanRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("2"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .get();

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
    public void ensureCorrectVacationDaysLeftWithOnlyUsedDaysBeforeAprilLessThanRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("3"))
            .forUsedDaysAfterApril(BigDecimal.ZERO)
            .get();

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
    public void ensureCorrectVacationDaysLeftWithUsingDifferenceOfNotExpiringRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(BigDecimal.ZERO)
            .get();

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
    public void ensureCorrectVacationDaysLeftWithUsingNotExpiringRemainingVacationDays() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .get();

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
    public void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysBeforeApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(new BigDecimal("2"))
            .get();

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
    public void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysBeforeAprilWithoutUsedDaysAfterApril() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5"))
            .forUsedDaysAfterApril(BigDecimal.ZERO)
            .get();

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
    public void ensureCorrectVacationDaysLeftWithUsingAllRemainingVacationDaysExpiringAndNotExpiring() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("4"))
            .forUsedDaysAfterApril(new BigDecimal("1"))
            .get();

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
