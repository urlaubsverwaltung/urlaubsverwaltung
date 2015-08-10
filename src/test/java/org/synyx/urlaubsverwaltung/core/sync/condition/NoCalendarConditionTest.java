package org.synyx.urlaubsverwaltung.core.sync.condition;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit test for {@link NoCalendarCondition}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class NoCalendarConditionTest {

    @Before
    public void setUp() {

        System.clearProperty("calendar");
    }


    @Test
    public void ensureConditionMatchesIfNoCalendarIsSpecified() {

        NoCalendarCondition condition = new NoCalendarCondition();

        boolean matches = condition.matches(null, null);

        Assert.assertTrue("Condition must match if no calendar is specified", matches);
    }


    @Test
    public void ensureConditionMatchesIfAnInvalidCalendarIsSpecified() {

        System.setProperty("calendar", "foo");

        NoCalendarCondition condition = new NoCalendarCondition();

        boolean matches = condition.matches(null, null);

        Assert.assertTrue("Condition must match if an invalid calendar is specified", matches);
    }


    @Test
    public void ensureConditionDoesNotMatchForEwsCalendar() {

        System.setProperty("calendar", "ews");

        NoCalendarCondition condition = new NoCalendarCondition();

        boolean matches = condition.matches(null, null);

        Assert.assertFalse("Condition must not match for calendar 'ews'", matches);
    }


    @Test
    public void ensureConditionDoesNotMatchForGoogleCalendar() {

        System.setProperty("calendar", "google");

        NoCalendarCondition condition = new NoCalendarCondition();

        boolean matches = condition.matches(null, null);

        Assert.assertFalse("Condition must not match for calendar 'google'", matches);
    }
}
