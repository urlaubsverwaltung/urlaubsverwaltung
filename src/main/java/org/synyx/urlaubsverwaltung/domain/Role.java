
package org.synyx.urlaubsverwaltung.domain;

/**
 * @author  aljona
 */
public enum Role {

    USER("User"),
    CHEF("Chef"),
    OFFICE("Office");

    private String roleName;

    private Role(String roleName) {

        this.roleName = roleName;
    }

    public String getRoleName() {

        return this.roleName;
    }
}
