package org.synyx.urlaubsverwaltung.statistics.web;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.statistics.web.UsedDays}.
 */
class UsedDaysTest {

    @Test
    void ensureDaysMapIsInitialized() {

        UsedDays usedDays = new UsedDays(ApplicationStatus.ALLOWED);

        Map<String, BigDecimal> daysMap = usedDays.getDays();

        Assert.assertEquals("Number of map elements should match the number of the given application states", 1,
            daysMap.size());

        Assert.assertEquals("Days should be initialized with 0", BigDecimal.ZERO, daysMap.get("ALLOWED"));
    }

    @Test
    void ensureThrowsIfTryingToAddDaysForAnApplicationStateThatHasNotBeenSet() {
        UsedDays usedDays = new UsedDays(ApplicationStatus.ALLOWED);
        assertThatThrownBy(() -> usedDays.addDays(WAITING, ONE)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void ensureCanAddDaysPerApplicationStatusCorrectly() {

        UsedDays usedDays = new UsedDays(WAITING, ApplicationStatus.ALLOWED);

        usedDays.addDays(ApplicationStatus.ALLOWED, ONE);
        usedDays.addDays(ApplicationStatus.ALLOWED, ONE);

        usedDays.addDays(WAITING, ONE);
        usedDays.addDays(WAITING, ONE);
        usedDays.addDays(WAITING, ONE);

        Assert.assertEquals("Allowed state should have correct number of days", BigDecimal.valueOf(2),
            usedDays.getDays().get("ALLOWED"));
        Assert.assertEquals("Waiting state should have correct number of days", BigDecimal.valueOf(3),
            usedDays.getDays().get("WAITING"));
    }
}
