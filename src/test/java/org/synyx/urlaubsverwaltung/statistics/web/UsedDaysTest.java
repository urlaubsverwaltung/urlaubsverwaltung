package org.synyx.urlaubsverwaltung.statistics.web;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;

import java.math.BigDecimal;
import java.util.Map;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.statistics.web.UsedDays}.
 */
public class UsedDaysTest {

    @Test
    public void ensureDaysMapIsInitialized() {

        UsedDays usedDays = new UsedDays(ApplicationStatus.ALLOWED);

        Map<String, BigDecimal> daysMap = usedDays.getDays();

        Assert.assertEquals("Number of map elements should match the number of the given application states", 1,
            daysMap.size());

        Assert.assertEquals("Days should be initialized with 0", BigDecimal.ZERO, daysMap.get("ALLOWED"));
    }


    @Test(expected = UnsupportedOperationException.class)
    public void ensureThrowsIfTryingToAddDaysForAnApplicationStateThatHasNotBeenSet() {

        UsedDays usedDays = new UsedDays(ApplicationStatus.ALLOWED);

        usedDays.addDays(ApplicationStatus.WAITING, BigDecimal.ONE);
    }


    @Test
    public void ensureCanAddDaysPerApplicationStatusCorrectly() {

        UsedDays usedDays = new UsedDays(ApplicationStatus.WAITING, ApplicationStatus.ALLOWED);

        usedDays.addDays(ApplicationStatus.ALLOWED, BigDecimal.ONE);
        usedDays.addDays(ApplicationStatus.ALLOWED, BigDecimal.ONE);

        usedDays.addDays(ApplicationStatus.WAITING, BigDecimal.ONE);
        usedDays.addDays(ApplicationStatus.WAITING, BigDecimal.ONE);
        usedDays.addDays(ApplicationStatus.WAITING, BigDecimal.ONE);

        Assert.assertEquals("Allowed state should have correct number of days", BigDecimal.valueOf(2),
            usedDays.getDays().get("ALLOWED"));
        Assert.assertEquals("Waiting state should have correct number of days", BigDecimal.valueOf(3),
            usedDays.getDays().get("WAITING"));
    }
}
