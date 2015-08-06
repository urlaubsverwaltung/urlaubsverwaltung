package org.synyx.urlaubsverwaltung.core.sync.condition;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit test for {@link ExchangeCalendarCondition}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ExchangeCalendarConditionTest {

    @Before
    public void setUp() {

        System.clearProperty("calendar");
    }


    @Test
    public void ensureConditionMatchesForEwsCalendar() {

        System.setProperty("calendar", "ews");

        ExchangeCalendarCondition condition = new ExchangeCalendarCondition();

        boolean matches = condition.matches(null, null);

        Assert.assertTrue("Condition must match for calendar 'ews'", matches);
    }


    @Test
    public void ensureConditionDoesNotMatchForOtherValidCalendar() {

        System.setProperty("calendar", "google");

        ExchangeCalendarCondition condition = new ExchangeCalendarCondition();

        boolean matches = condition.matches(null, null);

        Assert.assertFalse("Condition must not match for other valid calendar", matches);
    }


    @Test
    public void ensureConditionDoesNotMatchIfNoCalendarSpecified() {

        ExchangeCalendarCondition condition = new ExchangeCalendarCondition();

        boolean matches = condition.matches(null, null);

        Assert.assertFalse("Condition must not match if no calendar is specified", matches);
    }


    @Test
    public void ensureConditionDoesNotMatchIfInvalidCalendarSpecified() {

        System.setProperty("calendar", "foo");

        ExchangeCalendarCondition condition = new ExchangeCalendarCondition();

        boolean matches = condition.matches(null, null);

        Assert.assertFalse("Condition must not match for invalid calendar", matches);
    }
}
