package org.synyx.urlaubsverwaltung.core.startup;

/**
 * Represents the supported environments.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class Environment {

    public static final String PROPERTY_KEY = "env";

    public enum Type {

        DEV("dev"),
        TEST("test"),
        PROD("prod");

        private String name;

        Type(String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }
    }
}
