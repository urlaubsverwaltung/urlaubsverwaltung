package org.synyx.urlaubsverwaltung.statistics.web;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;


public class SickDaysTest {

    @Test
    public void ensureDaysMapIsInitialized() {

        SickDays sickDays = new SickDays();

        Map<String, BigDecimal> daysMap = sickDays.getDays();

        Assert.assertEquals("Wrong number of elements", 2, daysMap.size());

        Assert.assertEquals("Should have been initialized with 0", BigDecimal.ZERO, daysMap.get("WITH_AUB"));
        Assert.assertEquals("Should have been initialized with 0", BigDecimal.ZERO, daysMap.get("TOTAL"));
    }


    @Test
    public void ensureCanAddDays() {

        SickDays sickDays = new SickDays();

        sickDays.addDays(SickDays.SickDayType.TOTAL, BigDecimal.ONE);
        sickDays.addDays(SickDays.SickDayType.TOTAL, BigDecimal.ONE);

        sickDays.addDays(SickDays.SickDayType.WITH_AUB, BigDecimal.ONE);
        sickDays.addDays(SickDays.SickDayType.WITH_AUB, BigDecimal.ONE);
        sickDays.addDays(SickDays.SickDayType.WITH_AUB, BigDecimal.ONE);

        Assert.assertEquals("Sick days without AUB should have correct number of days", BigDecimal.valueOf(2),
            sickDays.getDays().get("TOTAL"));

        Assert.assertEquals("Sick days with AUB should have correct number of days", BigDecimal.valueOf(3),
            sickDays.getDays().get("WITH_AUB"));
    }
}
