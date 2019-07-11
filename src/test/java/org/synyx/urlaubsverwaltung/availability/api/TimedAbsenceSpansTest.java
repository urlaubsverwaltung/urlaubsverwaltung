package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.availability.api.TimedAbsence.Type.FREETIME;
import static org.synyx.urlaubsverwaltung.availability.api.TimedAbsence.Type.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.availability.api.TimedAbsence.Type.VACATION;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;


public class TimedAbsenceSpansTest {

    @Test
    public void ensurePresenceRatioCalculatedCorrectly() {

        final TimedAbsence timedAbsence1 = new TimedAbsence(MORNING, FREETIME);
        final TimedAbsence timedAbsence2 = new TimedAbsence(NOON, VACATION);
        final TimedAbsenceSpans timedAbsenceSpans = new TimedAbsenceSpans(List.of(timedAbsence1, timedAbsence2));

        final BigDecimal presenceRatio = timedAbsenceSpans.calculatePresenceRatio();
        assertThat(presenceRatio).isEqualTo(BigDecimal.valueOf(0.0));
    }


    @Test
    public void ensurePresenceRatioIsNotNegative() {

        final TimedAbsence timedAbsence1 = new TimedAbsence(MORNING, FREETIME);
        final TimedAbsence timedAbsence2 = new TimedAbsence(FULL, SICK_NOTE);
        final TimedAbsenceSpans timedAbsenceSpans = new TimedAbsenceSpans(List.of(timedAbsence1, timedAbsence2));

        final BigDecimal presenceRatio = timedAbsenceSpans.calculatePresenceRatio();
        assertThat(presenceRatio).isEqualTo(BigDecimal.ZERO);
    }


    @Test
    public void ensurePresenceRatioIsCalculatedCorrectlyForEmptyList() {

        final BigDecimal presenceRatio = new TimedAbsenceSpans(emptyList()).calculatePresenceRatio();
        assertThat(presenceRatio).isEqualTo(BigDecimal.ONE);
    }


    @Test
    public void ensureCalculationIsNullSave() {

        final BigDecimal bigDecimal = new TimedAbsenceSpans(null).calculatePresenceRatio();
        assertThat(bigDecimal).isSameAs(BigDecimal.ONE);
    }
}
