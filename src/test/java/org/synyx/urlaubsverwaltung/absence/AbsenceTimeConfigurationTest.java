package org.synyx.urlaubsverwaltung.absence;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;


/**
 * Unit test for {@link AbsenceTimeConfiguration}.
 */
class AbsenceTimeConfigurationTest {

    private CalendarSettings calendarSettings;

    @BeforeEach
    void setUp() {

        calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(8);
        calendarSettings.setWorkDayEndHour(16);
    }


    @Test
    void ensureCorrectMorningStart() {

        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);

        Assert.assertNotNull("Morning start should not be null", timeConfiguration.getMorningStart());
        Assert.assertEquals("Wrong morning start", (Integer) 8, timeConfiguration.getMorningStart());
    }


    @Test
    void ensureCorrectMorningEnd() {

        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);

        Assert.assertNotNull("Morning end should not be null", timeConfiguration.getMorningEnd());
        Assert.assertEquals("Wrong morning end", (Integer) 12, timeConfiguration.getMorningEnd());
    }


    @Test
    void ensureCorrectNoonStart() {

        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);

        Assert.assertNotNull("Noon start should not be null", timeConfiguration.getNoonStart());
        Assert.assertEquals("Wrong noon start", (Integer) 12, timeConfiguration.getNoonStart());
    }


    @Test
    void ensureCorrectNoonEnd() {

        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);

        Assert.assertNotNull("Noon end should not be null", timeConfiguration.getNoonEnd());
        Assert.assertEquals("Wrong noon end", (Integer) 16, timeConfiguration.getNoonEnd());
    }
}
