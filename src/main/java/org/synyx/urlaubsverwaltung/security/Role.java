
package org.synyx.urlaubsverwaltung.security;

/**
 * Enum describing possible types of rights/roles a user may have.
 *
 * @author  Aljona Murygina
 */
public enum Role {

    USER("role.user"),
    BOSS("role.boss"),
    OFFICE("role.office"),
    INACTIVE("role.inactive");

    private String propertyKey;

    private Role(String propertyKey) {

        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {

        return propertyKey;
    }
}
