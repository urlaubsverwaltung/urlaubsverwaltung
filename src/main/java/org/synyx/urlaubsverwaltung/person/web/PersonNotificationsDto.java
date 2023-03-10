package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.MailNotification;

import java.util.ArrayList;
import java.util.List;

public class PersonNotificationsDto {

    private Integer id;
    private String name;
    private List<MailNotification> emailNotifications = new ArrayList<>();
    private List<PersonPermissionsRoleDto> permissions = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MailNotification> getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(List<MailNotification> emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public List<PersonPermissionsRoleDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PersonPermissionsRoleDto> permissions) {
        this.permissions = permissions;
    }
}
