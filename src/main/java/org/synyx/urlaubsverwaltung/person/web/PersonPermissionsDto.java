package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.Objects;

public class PersonPermissionsDto {

    private Integer id;
    private String niceName;
    private String gravatarURL;
    private String email;

    private List<Role> permissions;
    private List<MailNotification> notifications;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNiceName() {
        return niceName;
    }

    public void setNiceName(String niceName) {
        this.niceName = niceName;
    }

    public String getGravatarURL() {
        return gravatarURL;
    }

    public PersonPermissionsDto setGravatarURL(String gravatarURL) {
        this.gravatarURL = gravatarURL;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public PersonPermissionsDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public List<Role> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Role> permissions) {
        this.permissions = permissions;
    }

    public List<MailNotification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<MailNotification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonPermissionsDto that = (PersonPermissionsDto) o;
        return id.equals(that.id) && permissions.equals(that.permissions) && notifications.equals(that.notifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, permissions, notifications);
    }
}
