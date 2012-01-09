
package org.synyx.urlaubsverwaltung.domain;

/**
 * @author  Aljona Murygina
 */
public enum Role {

    USER("role.user"),
    BOSS("role.boss"),
    OFFICE("role.office");

    private String roleName;

    private Role(String roleName) {

        this.roleName = roleName;
    }

    public String getRoleName() {

        return this.roleName;
    }
}
