package org.synyx.urlaubsverwaltung.core.application.domain;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.application.domain.VacationDaysLeft}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class VacationDaysLeftTest {

    private VacationDaysLeft.Builder builder;

    @Before
    public void setUp() {

        builder = VacationDaysLeft.builder().withAnnualVacation(new BigDecimal("28")).withRemainingVacation(
                new BigDecimal("5")).notExpiring(new BigDecimal("2"));
    }


    @Test
    public void ensureBuildsCorrectVacationDaysLeftObject() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5")).forUsedDaysAfterApril(
                new BigDecimal("10")).get();

        Assert.assertNotNull("Should not be null", vacationDaysLeft);
    }


    @Test
    public void ensureCorrectVacationDaysLeft() {

        VacationDaysLeft vacationDaysLeft = builder.forUsedDaysBeforeApril(new BigDecimal("5")).forUsedDaysAfterApril(
                new BigDecimal("10")).get();

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
}
