package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.MailNotification;

import java.util.ArrayList;
import java.util.List;

public class PersonNotificationsDto {

    private Integer id;
    private String name;
    private List<MailNotification> notifications = new ArrayList<>();
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

    public List<MailNotification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<MailNotification> notifications) {
        this.notifications = notifications;
    }

    public List<PersonPermissionsRoleDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PersonPermissionsRoleDto> permissions) {
        this.permissions = permissions;
    }
}
