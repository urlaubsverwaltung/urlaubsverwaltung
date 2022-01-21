package org.synyx.urlaubsverwaltung.user;

import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "user_settings")
class UserSettingsEntity {

    @Id
    @Column(name = "person_id")
    private Integer personId;

    @OneToOne(fetch = LAZY)
    @PrimaryKeyJoinColumn(name = "person_id", referencedColumnName = "id")
    private Person person;

    @NotNull
    @Enumerated(STRING)
    private Theme theme;

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    @Override
    public String toString() {
        return "UserSettingsEntity{" +
            "personId=" + personId +
            ", theme=" + theme +
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
