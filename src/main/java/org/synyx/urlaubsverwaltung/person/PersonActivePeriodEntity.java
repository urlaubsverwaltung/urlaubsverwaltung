package org.synyx.urlaubsverwaltung.person;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.Instant;
import java.util.Objects;

import static jakarta.persistence.GenerationType.SEQUENCE;

/**
 * Entity representing a historical period during which a person was active in the system.
 *
 * <p>
 * An entry with a defined {@link PersonActivePeriodEntity#getValidFrom()} but with
 * {@link PersonActivePeriodEntity#getValidTo()} eq {@code null} means, that the person is currently active.
 */
@Entity(name = "person_active_period")
public class PersonActivePeriodEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "person_active_period_generator")
    @SequenceGenerator(name = "person_active_period_generator", sequenceName = "person_active_period_id_seq", allocationSize = 5)
    private Long id;

    @NotNull
    private Long personId;

    @NotNull
    private Instant validFrom;

    /**
     * The end of the validity period for the person's activity.<br/>
     * If {@code null}, the person is considered active indefinitely.
     */
    @Nullable
    private Instant validTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    @Nullable
    public Instant getValidTo() {
        return validTo;
    }

    public void setValidTo(@Nullable Instant validTo) {
        this.validTo = validTo;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PersonActivePeriodEntity that = (PersonActivePeriodEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PersonActivePeriodEntity{" +
            "id=" + id +
            ", personId=" + personId +
            ", validFrom=" + validFrom +
            ", validTo=" + validTo +
            "} " + super.toString();
    }
}
