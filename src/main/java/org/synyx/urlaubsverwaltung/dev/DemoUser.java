package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.person.Role;

/**
 * Demo users that can be used to sign in with when demo data is created.
 */
enum DemoUser {

    // Special entitled users
    USER("3d07ae0d-90a1-4dfd-9393-172c0c9e7c10", "user", "Klaus", "Müller", "user@example.org", Role.USER),
    DEPARTMENT_HEAD("65c9e89f-7c92-45e8-b39f-2cfedf1883bc", "departmentHead", "Thorsten", "Krüger", "departmentHead@example.org", Role.USER, Role.DEPARTMENT_HEAD),
    SECOND_STAGE_AUTHORITY("f45e3262-d738-4f1e-8a9a-e142c1633731", "secondStageAuthority", "Peter", "Huber", "secondStageAuthority@example.org", Role.USER, Role.SECOND_STAGE_AUTHORITY),
    BOSS("9e7f6ab0-7075-4a17-853b-9a76fd063381", "boss", "Max", "Mustermann", "boss@example.org", Role.USER, Role.BOSS),
    OFFICE("58400ef7-1cc9-48cb-93a8-f45c7af186ad", "office", "Marlene", "Muster", "office@example.org", Role.USER, Role.BOSS, Role.OFFICE),
    ADMIN("1e04fb76-3d43-4adb-afcf-d0f7a93beb83", "admin", "Senor", "Operation", "admin@example.org", Role.USER, Role.ADMIN),

    // Simple users
    HANS("893e54e2-a8ad-45bf-ae40-c14e7392dda6", "hdampf", "Hans", "Dampf", "dampf@example.org", Role.USER),
    GUENTHER("244d5644-246c-4e2e-a0bf-ef4fabc95acd", "gbaier", "Günther", "Baier", "baier@example.org", Role.USER),
    ELENA("b50b27cc-5768-4ba9-a4c6-bbdba9796342", "eschneider", "Elena", "Schneider", "schneider@example.org", Role.USER),
    BRIGITTE("4cffef90-1068-42be-b5fa-ba0b8edbac81", "bhaendel", "Brigitte", "Händel", "haendel@example.org", Role.USER),
    NIKO("8682331e-5555-48dd-989c-0bc589d9da0d", "nschmidt", "Niko", "Schmidt", "schmidt@example.org", Role.USER),
    HORST("9a11eb12-2b0f-4ba1-9943-3aaf3dde3de6", "horst", "Horst", "Dieter", "hdieter@example.org", Role.INACTIVE);

    private final String uuid;
    private final String username;
    private final Role[] roles;
    private final String firstName;
    private final String lastName;
    private final String email;

    DemoUser(String uuid, String username, String firstName, String lastName, String email, Role... roles) {
        this.uuid = uuid;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
    }

    String getUsername() {
        return username;
    }

    Role[] getRoles() {
        return roles;
    }

    String getFirstName() {
        return firstName;
    }

    String getLastName() {
        return lastName;
    }

    String getEmail() {
        return email;
    }

    public String getUuid() {
        return uuid;
    }
}
