package org.synyx.urlaubsverwaltung.web.statistics;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationForLeaveStatisticsTest {

    // Initialization --------------------------------------------------------------------------------------------------

    private VacationTypeService vacationTypeService;

    private VacationTypeService getVacationTypeService() {

        if (vacationTypeService == null) {
            vacationTypeService = Mockito.mock(VacationTypeService.class);
            Mockito.when(vacationTypeService.getVacationTypes()).thenReturn(TestDataCreator.getVacationTypes());
        }

        return vacationTypeService;
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithNull() {

        new ApplicationForLeaveStatistics(null, null);
    }


    @Test
    public void ensureHasDefaultValues() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        Assert.assertNotNull("Person should not be null", statistics.getPerson());
        Assert.assertNotNull("Total waiting vacation days should not be null",
            statistics.getTotalWaitingVacationDays());
        Assert.assertNotNull("Total allowed vacation days should not be null",
            statistics.getTotalAllowedVacationDays());
        Assert.assertNotNull("Left vacation days should not be null", statistics.getLeftVacationDays());
        Assert.assertNotNull("Left overtime should not be null", statistics.getLeftOvertime());
        Assert.assertNotNull("Waiting vacation days per type should not be null", statistics.getWaitingVacationDays());
        Assert.assertNotNull("Allowed vacation days per type should not be null", statistics.getAllowedVacationDays());

        // Total
        Assert.assertEquals("Total waiting vacation days should have default value", BigDecimal.ZERO,
            statistics.getTotalWaitingVacationDays());
        Assert.assertEquals("Total allowed vacation days should have default value", BigDecimal.ZERO,
            statistics.getTotalAllowedVacationDays());

        // Left
        Assert.assertEquals("Left vacation days should have default value", BigDecimal.ZERO,
            statistics.getLeftVacationDays());
        Assert.assertEquals("Left overtime should have default value", BigDecimal.ZERO, statistics.getLeftOvertime());

        // Per vacation type
        Assert.assertEquals("Wrong number of elements", TestDataCreator.getVacationTypes().size(),
            statistics.getWaitingVacationDays().size());
        Assert.assertEquals("Wrong number of elements", TestDataCreator.getVacationTypes().size(),
            statistics.getAllowedVacationDays().size());

        for (VacationType type : TestDataCreator.getVacationTypes()) {
            Assert.assertEquals("Waiting vacation days for " + type.getTypeName() + " should be zero", BigDecimal.ZERO,
                statistics.getWaitingVacationDays().get(type));
            Assert.assertEquals("Allowed vacation days for " + type.getTypeName() + " should be zero", BigDecimal.ZERO,
                statistics.getAllowedVacationDays().get(type));
        }
    }


    // Total left vacation days ----------------------------------------------------------------------------------------

    @Test
    public void ensureCanSetTotalLeftVacationDays() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.setLeftVacationDays(BigDecimal.ONE);

        Assert.assertEquals("Wrong number of days", BigDecimal.ONE, statistics.getLeftVacationDays());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingTotalLeftVacationDaysToNull() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.setLeftVacationDays(null);
    }


    // Adding vacation days --------------------------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfAddingWaitingVacationDaysWithNullVacationType() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.addWaitingVacationDays(null, BigDecimal.ONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfAddingWaitingVacationDaysWithNullDays() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.addWaitingVacationDays(TestDataCreator.getVacationType(VacationType.HOLIDAY), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfAddingAllowedVacationDaysWithNullVacationType() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.addAllowedVacationDays(null, BigDecimal.ONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfAddingAllowedVacationDaysWithNullDays() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.addAllowedVacationDays(TestDataCreator.getVacationType(VacationType.HOLIDAY), null);
    }


    @Test
    public void ensureCanAddWaitingVacationDays() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.addWaitingVacationDays(TestDataCreator.getVacationType(VacationType.HOLIDAY), BigDecimal.ONE);
        statistics.addWaitingVacationDays(TestDataCreator.getVacationType(VacationType.HOLIDAY), BigDecimal.ONE);

        statistics.addWaitingVacationDays(TestDataCreator.getVacationType(VacationType.OVERTIME), BigDecimal.ONE);

        Assert.assertEquals("Wrong number of days", new BigDecimal("2"),
            statistics.getWaitingVacationDays().get(TestDataCreator.getVacationType(VacationType.HOLIDAY)));
        Assert.assertEquals("Wrong number of days", BigDecimal.ONE,
            statistics.getWaitingVacationDays().get(TestDataCreator.getVacationType(VacationType.OVERTIME)));
        Assert.assertEquals("Wrong number of days", BigDecimal.ZERO,
            statistics.getWaitingVacationDays().get(TestDataCreator.getVacationType(VacationType.SPECIALLEAVE)));
        Assert.assertEquals("Wrong number of days", BigDecimal.ZERO,
            statistics.getWaitingVacationDays().get(TestDataCreator.getVacationType(VacationType.UNPAIDLEAVE)));
    }


    @Test
    public void ensureCanAddAllowedVacationDays() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.addAllowedVacationDays(TestDataCreator.getVacationType(VacationType.SPECIALLEAVE), BigDecimal.ONE);
        statistics.addAllowedVacationDays(TestDataCreator.getVacationType(VacationType.SPECIALLEAVE), BigDecimal.ONE);

        statistics.addAllowedVacationDays(TestDataCreator.getVacationType(VacationType.UNPAIDLEAVE), BigDecimal.ONE);

        Assert.assertEquals("Wrong number of days", BigDecimal.ZERO,
            statistics.getAllowedVacationDays().get(TestDataCreator.getVacationType(VacationType.HOLIDAY)));
        Assert.assertEquals("Wrong number of days", BigDecimal.ZERO,
            statistics.getAllowedVacationDays().get(TestDataCreator.getVacationType(VacationType.OVERTIME)));
        Assert.assertEquals("Wrong number of days", new BigDecimal("2"),
            statistics.getAllowedVacationDays().get(TestDataCreator.getVacationType(VacationType.SPECIALLEAVE)));
        Assert.assertEquals("Wrong number of days", BigDecimal.ONE,
            statistics.getAllowedVacationDays().get(TestDataCreator.getVacationType(VacationType.UNPAIDLEAVE)));
    }


    // Total waiting vacation days -------------------------------------------------------------------------------------

    @Test
    public void ensureCanCalculateTotalWaitingVacationDays() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.addWaitingVacationDays(TestDataCreator.getVacationType(VacationType.HOLIDAY), BigDecimal.ONE);
        statistics.addWaitingVacationDays(TestDataCreator.getVacationType(VacationType.HOLIDAY), BigDecimal.ONE);
        statistics.addWaitingVacationDays(TestDataCreator.getVacationType(VacationType.HOLIDAY), BigDecimal.ONE);

        statistics.addWaitingVacationDays(TestDataCreator.getVacationType(VacationType.OVERTIME), BigDecimal.ONE);
        statistics.addWaitingVacationDays(TestDataCreator.getVacationType(VacationType.OVERTIME), BigDecimal.ONE);

        statistics.addWaitingVacationDays(TestDataCreator.getVacationType(VacationType.SPECIALLEAVE), BigDecimal.ONE);

        statistics.addWaitingVacationDays(TestDataCreator.getVacationType(VacationType.UNPAIDLEAVE), BigDecimal.TEN);

        Assert.assertEquals("Wrong total waiting vacation days", new BigDecimal("16"),
            statistics.getTotalWaitingVacationDays());
    }


    // Total allowed vacation days -------------------------------------------------------------------------------------

    @Test
    public void ensureCanCalculateTotalAllowedVacationDays() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.addAllowedVacationDays(TestDataCreator.getVacationType(VacationType.HOLIDAY), BigDecimal.ONE);
        statistics.addAllowedVacationDays(TestDataCreator.getVacationType(VacationType.HOLIDAY), BigDecimal.ONE);
        statistics.addAllowedVacationDays(TestDataCreator.getVacationType(VacationType.HOLIDAY), BigDecimal.ONE);

        statistics.addAllowedVacationDays(TestDataCreator.getVacationType(VacationType.OVERTIME), BigDecimal.ONE);
        statistics.addAllowedVacationDays(TestDataCreator.getVacationType(VacationType.OVERTIME), BigDecimal.ONE);

        statistics.addAllowedVacationDays(TestDataCreator.getVacationType(VacationType.SPECIALLEAVE), BigDecimal.ONE);

        statistics.addAllowedVacationDays(TestDataCreator.getVacationType(VacationType.UNPAIDLEAVE), BigDecimal.TEN);

        Assert.assertEquals("Wrong total allowed vacation days", new BigDecimal("16"),
            statistics.getTotalAllowedVacationDays());
    }


    // Total left overtime ---------------------------------------------------------------------------------------------

    @Test
    public void ensureCanSetTotalLeftOvertime() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.setLeftOvertime(BigDecimal.ONE);

        Assert.assertEquals("Wrong number of hours", BigDecimal.ONE, statistics.getLeftOvertime());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingTotalLeftOvertimeToNull() {

        Person person = Mockito.mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, getVacationTypeService());

        statistics.setLeftOvertime(null);
    }
}
