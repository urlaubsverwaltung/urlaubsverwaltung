package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SystemPropertyConditionTest {

    @Test
    public void ensureThrowsIfPropertyKeyIsEmpty() {

        Consumer<String> assertThrows = (key) -> {
            try {
                new SystemPropertyCondition(key, "value");
                Assert.fail("Should throw on empty property key!");
            } catch (IllegalArgumentException ex) {
                // Expected
            }
        };

        assertThrows.accept(null);
        assertThrows.accept("");
        assertThrows.accept(" ");
    }


    @Test
    public void ensureThrowsIfPropertyValueIsEmpty() {

        Consumer<String> assertThrows = (value) -> {
            try {
                new SystemPropertyCondition("key", value);
                Assert.fail("Should throw on empty property value!");
            } catch (IllegalArgumentException ex) {
                // Expected
            }
        };

        assertThrows.accept(null);
        assertThrows.accept("");
        assertThrows.accept(" ");
    }


    @Test
    public void ensureDoesNotMatchIfSystemPropertyIsNotSet() {

        String propertyKey = "key";

        System.getProperties().remove(propertyKey);

        SystemPropertyCondition condition = new SystemPropertyCondition(propertyKey, "bar");

        Assert.assertFalse("Should not match if system property is set to other value than given",
            condition.matches(null, null));
    }


    @Test
    public void ensureDoesNotMatchIfSystemPropertyIsSetToOtherValue() {

        String propertyKey = "key";

        System.getProperties().put(propertyKey, "foo");

        SystemPropertyCondition condition = new SystemPropertyCondition(propertyKey, "bar");

        Assert.assertFalse("Should not match if system property is set to other value than given",
            condition.matches(null, null));
    }


    @Test
    public void ensureMatchesIfSystemPropertyIsSetToGivenValue() {

        String propertyKey = "key";
        String propertyValue = "value";

        System.getProperties().put(propertyKey, propertyValue);

        SystemPropertyCondition condition = new SystemPropertyCondition(propertyKey, propertyValue);

        Assert.assertTrue("Should match if system property is set", condition.matches(null, null));
    }


    @Test
    public void ensureMatchingSystemPropertyIsCaseInsensitive() {

        String propertyKey = "key";

        System.getProperties().put(propertyKey, "value");

        SystemPropertyCondition condition = new SystemPropertyCondition(propertyKey, "vAlUe");

        Assert.assertTrue("Should match if system property is set", condition.matches(null, null));
    }
}
