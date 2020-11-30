package org.synyx.urlaubsverwaltung.overview;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;


/**
 * Unit test for {@link UsedDays}.
 */
class UsedDaysTest {

    @Test
    void ensureDaysMapIsInitialized() {

        UsedDays usedDays = new UsedDays(ApplicationStatus.ALLOWED);

        Map<String, BigDecimal> daysMap = usedDays.getDays();

        assertThat(daysMap)
            .hasSize(1)
            .containsEntry("ALLOWED", BigDecimal.ZERO);
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

        assertThat(usedDays.getDays())
            .containsEntry("ALLOWED", BigDecimal.valueOf(2))
            .containsEntry("WAITING", BigDecimal.valueOf(3));
    }
}
