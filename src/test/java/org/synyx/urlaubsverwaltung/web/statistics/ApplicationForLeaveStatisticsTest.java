package org.synyx.urlaubsverwaltung.web.statistics;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationForLeaveStatisticsTest {

    @Test
    public void ensureHasDefaultValues() {

        Person person = Mockito.mock(Person.class);
        Mockito.when(person.getEmail()).thenReturn("muster@muster.de");

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);

        Assert.assertNotNull("Person should not be null", statistics.getPerson());
        Assert.assertNotNull("Waiting vacation days should not be null", statistics.getWaitingVacationDays());
        Assert.assertNotNull("Allowed vacation days should not be null", statistics.getAllowedVacationDays());
        Assert.assertNotNull("Left vacation days should not be null", statistics.getLeftVacationDays());

        Assert.assertEquals("Waiting vacation days should have default value", BigDecimal.ZERO,
            statistics.getWaitingVacationDays());
        Assert.assertEquals("Allowed vacation days should have default value", BigDecimal.ZERO,
            statistics.getAllowedVacationDays());
        Assert.assertEquals("Left vacation days should have default value", BigDecimal.ZERO,
            statistics.getLeftVacationDays());
    }
}
