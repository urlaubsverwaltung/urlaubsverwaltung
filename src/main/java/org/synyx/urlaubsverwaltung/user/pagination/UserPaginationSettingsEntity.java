package org.synyx.urlaubsverwaltung.user.pagination;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "user_pagination_settings")
public class UserPaginationSettingsEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "person_id")
    private Long personId;

    @OneToOne(fetch = LAZY)
    @PrimaryKeyJoinColumn(name = "person_id", referencedColumnName = "id")
    private Person person;

    @NotNull
    @Positive
    private int defaultPageSize;

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    @Override
    public String toString() {
        return "UserPaginationSettingsEntity{" +
            "personId=" + personId +
            ", defaultPageSize=" + defaultPageSize +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPaginationSettingsEntity that = (UserPaginationSettingsEntity) o;
        return Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId);
    }
}
