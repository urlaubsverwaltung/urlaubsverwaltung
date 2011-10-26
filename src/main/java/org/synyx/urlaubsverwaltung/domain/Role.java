
package org.synyx.urlaubsverwaltung.domain;

/**
 * @author  aljona
 */
public enum Role {

    USER("role.user"),
    CHEF("role.chef"),
    OFFICE("role.office");

    private String roleName;

    private Role(String roleName) {

        this.roleName = roleName;
    }

    public String getRoleName() {

        return this.roleName;
    }
}
