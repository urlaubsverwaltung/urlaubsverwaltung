package org.synyx.urlaubsverwaltung.overview;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;

class UsedDaysTest {

    @Test
    void ensureDaysMapIsInitialized() {
        final UsedDays usedDays = new UsedDays(ALLOWED);
        final Map<String, BigDecimal> daysMap = usedDays.getDays();
        assertThat(daysMap)
            .hasSize(1)
            .containsEntry("ALLOWED", ZERO);
    }

    @Test
    void ensureThrowsIfTryingToAddDaysForAnApplicationStateThatHasNotBeenSet() {
        UsedDays usedDays = new UsedDays(ALLOWED);
        assertThatThrownBy(() -> usedDays.addDays(WAITING, ONE)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void ensureCanAddDaysPerApplicationStatusCorrectly() {

        final UsedDays usedDays = new UsedDays(WAITING, ALLOWED);
        usedDays.addDays(ALLOWED, ONE);
        usedDays.addDays(ALLOWED, ONE);
        usedDays.addDays(WAITING, ONE);
        usedDays.addDays(WAITING, ONE);
        usedDays.addDays(WAITING, ONE);
        assertThat(usedDays.getDays())
            .containsEntry("ALLOWED", BigDecimal.valueOf(2))
            .containsEntry("WAITING", BigDecimal.valueOf(3));
    }

    @Test
    void ensureSumIsCalculatedCorrectly() {
        final UsedDays usedDays = new UsedDays(WAITING, ALLOWED);
        usedDays.addDays(ALLOWED, ONE);
        usedDays.addDays(ALLOWED, TEN);
        usedDays.addDays(WAITING, ONE);
        usedDays.addDays(WAITING, ONE);
        usedDays.addDays(WAITING, ONE);
        assertThat(usedDays.getSum()).isEqualByComparingTo(BigDecimal.valueOf(14));
    }
}
