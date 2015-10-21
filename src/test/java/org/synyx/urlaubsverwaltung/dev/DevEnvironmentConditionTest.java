package org.synyx.urlaubsverwaltung.dev;

import org.junit.Assert;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.startup.Environment;

import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DevEnvironmentConditionTest {

    @Test
    public void ensureConditionDoesNotMatchIfNoEnvironmentIsSet() {

        System.getProperties().remove(Environment.PROPERTY_KEY);

        DevEnvironmentCondition condition = new DevEnvironmentCondition();

        Assert.assertFalse("Condition should not match for " + Environment.PROPERTY_KEY + "=<null>",
            condition.matches(null, null));
    }


    @Test
    public void ensureConditionMatchesIfEnvironmentIsSetToDev() {

        Consumer<String> assertDoesMatch = (env) -> {
            System.getProperties().put(Environment.PROPERTY_KEY, env);

            DevEnvironmentCondition condition = new DevEnvironmentCondition();

            Assert.assertTrue("Condition should match for " + Environment.PROPERTY_KEY + "=" + env,
                condition.matches(null, null));
        };

        assertDoesMatch.accept(Environment.Type.DEV.getName());
        assertDoesMatch.accept(Environment.Type.DEV.getName().toUpperCase());
    }


    @Test
    public void ensureConditionDoesNotMatchForOtherEnvironment() {

        Consumer<String> assertDoesNotMatch = (env) -> {
            System.getProperties().put(Environment.PROPERTY_KEY, env);

            DevEnvironmentCondition condition = new DevEnvironmentCondition();

            Assert.assertFalse("Condition should not match for " + Environment.PROPERTY_KEY + "=" + env,
                condition.matches(null, null));
        };

        assertDoesNotMatch.accept(Environment.Type.TEST.getName());
        assertDoesNotMatch.accept(Environment.Type.PROD.getName());
        assertDoesNotMatch.accept("foo");
    }
}
