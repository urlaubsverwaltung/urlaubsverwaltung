package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.WITH_AUB;

class SickDaysTest {

    @Test
    void ensureDaysMapIsInitialized() {

        final SickDays sickDays = new SickDays();

        assertThat(sickDays.getDays())
            .hasSize(2)
            .containsEntry("WITH_AUB", ZERO)
            .containsEntry("TOTAL", ZERO);
    }

    @Test
    void ensureCanAddDays() {

        final SickDays sickDays = new SickDays();

        sickDays.addDays(TOTAL, ONE);
        sickDays.addDays(TOTAL, ONE);

        sickDays.addDays(WITH_AUB, ONE);
        sickDays.addDays(WITH_AUB, ONE);
        sickDays.addDays(WITH_AUB, ONE);

        assertThat(sickDays.getDays())
            .hasSize(2)
            .containsEntry("WITH_AUB", BigDecimal.valueOf(3))
            .containsEntry("TOTAL", BigDecimal.valueOf(2));
    }
}
