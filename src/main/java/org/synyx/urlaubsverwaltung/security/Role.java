
package org.synyx.urlaubsverwaltung.security;

/**
 * @author  Aljona Murygina
 */
public enum Role {

    USER("role.user", 0),
    BOSS("role.boss", 1),
    OFFICE("role.office", 2),
    INACTIVE("role.inactive", 3),
    ADMIN("role.admin", 4);

    private String roleName;
    
    private int number;

    private Role(String roleName, int number) {

        this.roleName = roleName;
        this.number = number;
    }

    public String getRoleName() {

        return this.roleName;
    }

    public int getNumber() {
        return number;
    }
}
