package org.synyx.urlaubsverwaltung.security;

/**
 * Represents the supported kinds of authentication.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class Authentication {

    public static final String PROPERTY_KEY = "auth";

    public enum Type {

        DEFAULT("default"),
        LDAP("ldap"),
        ACTIVE_DIRECTORY("activeDirectory");

        private String name;

        Type(String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }
    }
}
