package org.synyx.urlaubsverwaltung.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.util.Locale;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "user_settings")
public class UserSettingsEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "person_id")
    private Long personId;

    @OneToOne(fetch = LAZY)
    @PrimaryKeyJoinColumn(name = "person_id", referencedColumnName = "id")
    private Person person;

    @NotNull
    @Enumerated(STRING)
    private Theme theme;

    private Locale locale;

    private Locale localeBrowserSpecific;

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Person getPerson() {
        return person;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocaleBrowserSpecific() {
        return localeBrowserSpecific;
    }

    public void setLocaleBrowserSpecific(Locale localeBrowserSpecific) {
        this.localeBrowserSpecific = localeBrowserSpecific;
    }

    @Override
    public String toString() {
        return "UserSettingsEntity{" +
            "personId=" + personId +
            ", theme=" + theme +
            ", locale=" + locale +
            ", localeBrowserSpecific=" + localeBrowserSpecific +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSettingsEntity that = (UserSettingsEntity) o;
        return Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId);
    }
}
