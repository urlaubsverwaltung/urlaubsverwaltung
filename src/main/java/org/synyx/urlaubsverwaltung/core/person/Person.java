package org.synyx.urlaubsverwaltung.core.person;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


/**
 * This class describes a person.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */

@Entity
public class Person extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 765672310978437L;

    private String loginName;

    private String password;

    private String lastName;

    private String firstName;

    private String email;

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @Enumerated(EnumType.STRING)
    private Collection<Role> permissions;

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @Enumerated(EnumType.STRING)
    private Collection<MailNotification> notifications;

    public Person() {

        /* OK */
    }


    public Person(String loginName, String lastName, String firstName, String email) {

        this.loginName = loginName;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
    }

    public String getEmail() {

        return email;
    }


    public void setEmail(String email) {

        this.email = email;
    }


    public String getFirstName() {

        return firstName;
    }


    public void setFirstName(String firstName) {

        this.firstName = firstName;
    }


    public String getLastName() {

        return lastName;
    }


    public void setLastName(String lastName) {

        this.lastName = lastName;
    }


    public String getLoginName() {

        return loginName;
    }


    public void setLoginName(String loginName) {

        this.loginName = loginName;
    }


    public String getPassword() {

        return password;
    }


    public void setPassword(String password) {

        this.password = password;
    }

    public void setPermissions(Collection<Role> permissions) {

        this.permissions = permissions;
    }


    public Collection<Role> getPermissions() {

        if (permissions == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(permissions);
    }


    public boolean hasRole(final Role role) {

        return getPermissions().stream().filter(permission -> permission.equals(role)).findFirst().isPresent();
    }


    public Collection<MailNotification> getNotifications() {

        if (notifications == null) {
            notifications = Collections.emptyList();
        }

        return Collections.unmodifiableCollection(notifications);
    }


    public void setNotifications(Collection<MailNotification> notifications) {

        this.notifications = notifications;
    }


    public boolean hasNotificationType(final MailNotification notification) {

        return getNotifications().stream().filter(element -> element.equals(notification)).findFirst().isPresent();
    }


    public String getNiceName() {

        if (StringUtils.hasText(this.firstName) && StringUtils.hasText(this.lastName)) {
            return this.firstName + " " + this.lastName;
        }

        return this.loginName;
    }


    public String getGravatarURL() {

        if (StringUtils.hasText(this.email)) {
            return GravatarUtil.createImgURL(this.email);
        }

        return "";
    }


    @Override
    public void setId(Integer id) { // NOSONAR - needed for setting ID in form

        super.setId(id);
    }


    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId())
            .append("loginName", getLoginName())
            .append("lastName", getLastName())
            .append("firstName", getFirstName())
            .append("email", getEmail())
            .append("permissions", getPermissions())
            .toString();
    }
}
