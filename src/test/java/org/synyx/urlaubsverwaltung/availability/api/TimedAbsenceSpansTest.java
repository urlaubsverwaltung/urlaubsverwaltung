package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.availability.api.TimedAbsence;
import org.synyx.urlaubsverwaltung.availability.api.TimedAbsenceSpans;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class TimedAbsenceSpansTest {

    @Test
    public void ensurePresenceRatioCalculatedCorrectly() {

        TimedAbsence timedAbsence1 = new TimedAbsence(DayLength.MORNING, TimedAbsence.Type.FREETIME);
        TimedAbsence timedAbsence2 = new TimedAbsence(DayLength.NOON, TimedAbsence.Type.VACATION);

        List<TimedAbsence> absenceList = Arrays.asList(timedAbsence1, timedAbsence2);

        TimedAbsenceSpans timedAbsenceSpans = new TimedAbsenceSpans(absenceList);

        BigDecimal presenceRatio = timedAbsenceSpans.calculatePresenceRatio();
        Assert.assertTrue(BigDecimal.ZERO.compareTo(presenceRatio) == 0);
    }


    @Test
    public void ensurePresenceRatioIsNotNegative() {

        TimedAbsence timedAbsence1 = new TimedAbsence(DayLength.MORNING, TimedAbsence.Type.FREETIME);
        TimedAbsence timedAbsence2 = new TimedAbsence(DayLength.FULL, TimedAbsence.Type.SICK_NOTE);

        List<TimedAbsence> absenceList = Arrays.asList(timedAbsence1, timedAbsence2);

        TimedAbsenceSpans timedAbsenceSpans = new TimedAbsenceSpans(absenceList);

        BigDecimal presenceRatio = timedAbsenceSpans.calculatePresenceRatio();
        Assert.assertTrue(BigDecimal.ZERO.compareTo(presenceRatio) == 0);
    }


    @Test
    public void ensurePresenceRatioIsCalculatedCorrectlyForEmptyList() {

        BigDecimal presenceRatio = new TimedAbsenceSpans(new ArrayList<>()).calculatePresenceRatio();
        Assert.assertTrue(BigDecimal.ONE.compareTo(presenceRatio) == 0);
    }


    @Test
    public void ensureCalculationIsNullSave() {

        final BigDecimal bigDecimal = new TimedAbsenceSpans(null).calculatePresenceRatio();
        assertThat(bigDecimal).isSameAs(BigDecimal.ONE);
    }
}
