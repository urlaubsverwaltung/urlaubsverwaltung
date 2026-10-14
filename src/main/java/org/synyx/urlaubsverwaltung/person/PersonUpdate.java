package org.synyx.urlaubsverwaltung.person;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Describes the data of a {@linkplain Person} to change. Every aspect that is not given stays untouched, which
 * makes it impossible to change data unintentionally - especially data that is not meant to be changed at all,
 * like the creation timestamp of a {@linkplain Person}.
 */
public final class PersonUpdate {

    private final PersonalData personalData;
    private final Collection<Role> permissions;
    private final Collection<MailNotification> notifications;
    private final EmailVerification emailVerification;

    private PersonUpdate(PersonalData personalData, Collection<Role> permissions, Collection<MailNotification> notifications,
                         EmailVerification emailVerification) {
        this.personalData = personalData;
        this.permissions = permissions;
        this.notifications = notifications;
        this.emailVerification = emailVerification;
    }

    public static PersonUpdate ofPersonalData(String username, String firstName, String lastName, String email) {
        return new PersonUpdate(new PersonalData(username, firstName, lastName, email), null, null, null);
    }

    public static PersonUpdate ofPermissions(Collection<Role> permissions) {
        return new PersonUpdate(null, permissions, null, null);
    }

    public static PersonUpdate ofNotifications(Collection<MailNotification> notifications) {
        return new PersonUpdate(null, null, notifications, null);
    }

    public PersonUpdate withPersonalData(String username, String firstName, String lastName, String email) {
        return new PersonUpdate(new PersonalData(username, firstName, lastName, email), permissions, notifications, emailVerification);
    }

    public PersonUpdate withPermissions(Collection<Role> permissions) {
        return new PersonUpdate(personalData, permissions, notifications, emailVerification);
    }

    public PersonUpdate withNotifications(Collection<MailNotification> notifications) {
        return new PersonUpdate(personalData, permissions, notifications, emailVerification);
    }

    /**
     * @param emailVerified whether the identity provider has verified the email address, {@code null} if unknown
     */
    public PersonUpdate withEmailVerified(Boolean emailVerified) {
        return new PersonUpdate(personalData, permissions, notifications, new EmailVerification(emailVerified));
    }

    public Optional<PersonalData> personalData() {
        return ofNullable(personalData);
    }

    public Optional<Collection<Role>> permissions() {
        return ofNullable(permissions);
    }

    public Optional<Collection<MailNotification>> notifications() {
        return ofNullable(notifications);
    }

    public Optional<EmailVerification> emailVerification() {
        return ofNullable(emailVerification);
    }

    public record PersonalData(String username, String firstName, String lastName, String email) {
    }

    /**
     * @param verified whether the identity provider has verified the email address, {@code null} if unknown
     */
    public record EmailVerification(Boolean verified) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PersonUpdate that = (PersonUpdate) o;
        return Objects.equals(personalData, that.personalData)
            && Objects.equals(permissions, that.permissions)
            && Objects.equals(notifications, that.notifications)
            && Objects.equals(emailVerification, that.emailVerification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personalData, permissions, notifications, emailVerification);
    }

    @Override
    public String toString() {
        return "PersonUpdate{" +
            "personalData=" + personalData +
            ", permissions=" + permissions +
            ", notifications=" + notifications +
            ", emailVerification=" + emailVerification +
            '}';
    }
}
