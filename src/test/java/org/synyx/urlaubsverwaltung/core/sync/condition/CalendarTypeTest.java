package org.synyx.urlaubsverwaltung.core.sync.condition;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test for {@link CalendarType}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CalendarTypeTest {

    @Test
    public void ensureReturnsTrueIfValidNameGiven() {

        boolean contains = CalendarType.contains("ews");

        Assert.assertTrue("Valid name should return true", contains);
    }


    @Test
    public void ensureReturnsFalseIfInvalidNameGiven() {

        boolean contains = CalendarType.contains("foo");

        Assert.assertFalse("Invalid name should return false", contains);
    }


    @Test
    public void ensureReturnsFalseIfNullGiven() {

        boolean contains = CalendarType.contains(null);

        Assert.assertFalse("Null should return false", contains);
    }
}
