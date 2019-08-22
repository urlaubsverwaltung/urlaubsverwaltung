package org.synyx.urlaubsverwaltung.statistics.web;

import org.junit.Test;
import org.synyx.urlaubsverwaltung.sickdays.web.SickDays;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.sickdays.web.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sickdays.web.SickDays.SickDayType.WITH_AUB;


public class SickDaysTest {

    @Test
    public void ensureDaysMapIsInitialized() {

        SickDays sickDays = new SickDays();
        Map<String, BigDecimal> daysMap = sickDays.getDays();

        assertThat(daysMap).hasSize(2);
        assertThat(daysMap.get("WITH_AUB")).isEqualTo(ZERO);
        assertThat(daysMap.get("TOTAL")).isEqualTo(ZERO);
    }


    @Test
    public void ensureCanAddDays() {

        SickDays sickDays = new SickDays();

        sickDays.addDays(TOTAL, ONE);
        sickDays.addDays(TOTAL, ONE);

        sickDays.addDays(WITH_AUB, ONE);
        sickDays.addDays(WITH_AUB, ONE);
        sickDays.addDays(WITH_AUB, ONE);

        assertThat(sickDays.getDays().get("WITH_AUB")).isEqualTo(BigDecimal.valueOf(3));
        assertThat(sickDays.getDays().get("TOTAL")).isEqualTo(BigDecimal.valueOf(2));
    }
}
