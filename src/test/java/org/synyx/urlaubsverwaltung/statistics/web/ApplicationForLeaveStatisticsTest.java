package org.synyx.urlaubsverwaltung.statistics.web;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link ApplicationForLeaveStatistics}.
 */
public class ApplicationForLeaveStatisticsTest {

    // Initialization --------------------------------------------------------------------------------------------------

    private VacationTypeService vacationTypeService;

    private List<VacationType> vacationTypes;

    @Before
    public void setUp() {

        vacationTypeService = mock(VacationTypeService.class);
        vacationTypes = TestDataCreator.createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithNull() {

        new ApplicationForLeaveStatistics(null, null);
    }


    @Test
    public void ensureHasDefaultValues() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

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
        Assert.assertEquals("Wrong number of elements", TestDataCreator.createVacationTypes().size(),
            statistics.getWaitingVacationDays().size());
        Assert.assertEquals("Wrong number of elements", TestDataCreator.createVacationTypes().size(),
            statistics.getAllowedVacationDays().size());

        for (VacationType type : TestDataCreator.createVacationTypes()) {
            Assert.assertEquals("Waiting vacation days for " + type.getCategory() + " should be zero", BigDecimal.ZERO,
                statistics.getWaitingVacationDays().get(type));
            Assert.assertEquals("Allowed vacation days for " + type.getCategory() + " should be zero", BigDecimal.ZERO,
                statistics.getAllowedVacationDays().get(type));
        }
    }


    // Total left vacation days ----------------------------------------------------------------------------------------

    @Test
    public void ensureCanSetTotalLeftVacationDays() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.setLeftVacationDays(BigDecimal.ONE);

        Assert.assertEquals("Wrong number of days", BigDecimal.ONE, statistics.getLeftVacationDays());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingTotalLeftVacationDaysToNull() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.setLeftVacationDays(null);
    }


    // Adding vacation days --------------------------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfAddingWaitingVacationDaysWithNullVacationType() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.addWaitingVacationDays(null, BigDecimal.ONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfAddingWaitingVacationDaysWithNullDays() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.addWaitingVacationDays(TestDataCreator.createVacationType(VacationCategory.HOLIDAY), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfAddingAllowedVacationDaysWithNullVacationType() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.addAllowedVacationDays(null, BigDecimal.ONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfAddingAllowedVacationDaysWithNullDays() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.addAllowedVacationDays(TestDataCreator.createVacationType(VacationCategory.HOLIDAY), null);
    }


    @Test
    public void ensureCanAddWaitingVacationDays() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.addWaitingVacationDays(vacationTypes.get(0), BigDecimal.ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(0), BigDecimal.ONE);

        statistics.addWaitingVacationDays(vacationTypes.get(1), BigDecimal.ONE);

        Assert.assertEquals("Wrong number of days", new BigDecimal("2"),
            statistics.getWaitingVacationDays().get(vacationTypes.get(0)));
        Assert.assertEquals("Wrong number of days", BigDecimal.ONE,
            statistics.getWaitingVacationDays().get(vacationTypes.get(1)));
        Assert.assertEquals("Wrong number of days", BigDecimal.ZERO,
            statistics.getWaitingVacationDays().get(vacationTypes.get(2)));
        Assert.assertEquals("Wrong number of days", BigDecimal.ZERO,
            statistics.getWaitingVacationDays().get(vacationTypes.get(3)));
    }


    @Test
    public void ensureCanAddAllowedVacationDays() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.addAllowedVacationDays(vacationTypes.get(2), BigDecimal.ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(2), BigDecimal.ONE);

        statistics.addAllowedVacationDays(vacationTypes.get(3), BigDecimal.ONE);

        Assert.assertEquals("Wrong number of days", BigDecimal.ZERO,
            statistics.getAllowedVacationDays().get(vacationTypes.get(0)));
        Assert.assertEquals("Wrong number of days", BigDecimal.ZERO,
            statistics.getAllowedVacationDays().get(vacationTypes.get(1)));
        Assert.assertEquals("Wrong number of days", new BigDecimal("2"),
            statistics.getAllowedVacationDays().get(vacationTypes.get(2)));
        Assert.assertEquals("Wrong number of days", BigDecimal.ONE,
            statistics.getAllowedVacationDays().get(vacationTypes.get(3)));
    }


    // Total waiting vacation days -------------------------------------------------------------------------------------

    @Test
    public void ensureCanCalculateTotalWaitingVacationDays() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.addWaitingVacationDays(vacationTypes.get(0), BigDecimal.ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(0), BigDecimal.ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(0), BigDecimal.ONE);

        statistics.addWaitingVacationDays(vacationTypes.get(1), BigDecimal.ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(1), BigDecimal.ONE);

        statistics.addWaitingVacationDays(vacationTypes.get(2), BigDecimal.ONE);

        statistics.addWaitingVacationDays(vacationTypes.get(3), BigDecimal.TEN);

        Assert.assertEquals("Wrong total waiting vacation days", new BigDecimal("16"),
            statistics.getTotalWaitingVacationDays());
    }


    // Total allowed vacation days -------------------------------------------------------------------------------------

    @Test
    public void ensureCanCalculateTotalAllowedVacationDays() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.addAllowedVacationDays(vacationTypes.get(0), BigDecimal.ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(0), BigDecimal.ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(0), BigDecimal.ONE);

        statistics.addAllowedVacationDays(vacationTypes.get(1), BigDecimal.ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(1), BigDecimal.ONE);

        statistics.addAllowedVacationDays(vacationTypes.get(2), BigDecimal.ONE);

        statistics.addAllowedVacationDays(vacationTypes.get(3), BigDecimal.TEN);

        Assert.assertEquals("Wrong total allowed vacation days", new BigDecimal("16"),
            statistics.getTotalAllowedVacationDays());
    }


    // Total left overtime ---------------------------------------------------------------------------------------------

    @Test
    public void ensureCanSetTotalLeftOvertime() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.setLeftOvertime(BigDecimal.ONE);

        Assert.assertEquals("Wrong number of hours", BigDecimal.ONE, statistics.getLeftOvertime());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingTotalLeftOvertimeToNull() {

        Person person = mock(Person.class);

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        statistics.setLeftOvertime(null);
    }
}
