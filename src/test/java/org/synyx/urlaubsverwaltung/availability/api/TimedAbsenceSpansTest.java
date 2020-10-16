package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;


class TimedAbsenceSpansTest {

    @Test
    void ensurePresenceRatioCalculatedCorrectly() {

        final TimedAbsence timedAbsence1 = new TimedAbsence(MORNING);
        final TimedAbsence timedAbsence2 = new TimedAbsence(NOON);
        final TimedAbsenceSpans timedAbsenceSpans = new TimedAbsenceSpans(List.of(timedAbsence1, timedAbsence2));

        final BigDecimal presenceRatio = timedAbsenceSpans.calculatePresenceRatio();
        assertThat(presenceRatio).isEqualTo(BigDecimal.valueOf(0.0));
    }


    @Test
    void ensurePresenceRatioIsNotNegative() {

        final TimedAbsence timedAbsence1 = new TimedAbsence(MORNING);
        final TimedAbsence timedAbsence2 = new TimedAbsence(FULL);
        final TimedAbsenceSpans timedAbsenceSpans = new TimedAbsenceSpans(List.of(timedAbsence1, timedAbsence2));

        final BigDecimal presenceRatio = timedAbsenceSpans.calculatePresenceRatio();
        assertThat(presenceRatio).isEqualTo(BigDecimal.ZERO);
    }


    @Test
    void ensurePresenceRatioIsCalculatedCorrectlyForEmptyList() {

        final BigDecimal presenceRatio = new TimedAbsenceSpans(emptyList()).calculatePresenceRatio();
        assertThat(presenceRatio).isEqualTo(BigDecimal.ONE);
    }


    @Test
    void ensureCalculationIsNullSave() {

        final BigDecimal bigDecimal = new TimedAbsenceSpans(null).calculatePresenceRatio();
        assertThat(bigDecimal).isSameAs(BigDecimal.ONE);
    }
}
