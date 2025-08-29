package org.synyx.urlaubsverwaltung.department;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.Instant;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;

/**
 * Entity representing a historical department membership.
 *
 * <p>
 * An entry with a defined {@link DepartmentMembershipEntity#getValidFrom()} but with
 * {@link DepartmentMembershipEntity#getValidTo()} eq {@code null} means, that the membership is currently valid.
 */
@Entity(name = "department_membership")
public class DepartmentMembershipEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "department_membership_generator")
    @SequenceGenerator(name = "department_membership_generator", sequenceName = "department_membership_id_seq")
    private Long id;

    @NotNull
    private Long departmentId;

    @NotNull
    private Long personId;

    @NotNull
    @Enumerated(STRING)
    private DepartmentMembershipKind membershipKind;

    @NotNull
    private Instant validFrom;

    /**
     * The end of the validity period for the membership.<br/>
     * If {@code null}, the membership is considered valid indefinitely.
     */
    @Nullable
    private Instant validTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public DepartmentMembershipKind getMembershipKind() {
        return membershipKind;
    }

    public void setMembershipKind(DepartmentMembershipKind kind) {
        this.membershipKind = kind;
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
        if (o == null || getClass() != o.getClass()) return false;
        DepartmentMembershipEntity that = (DepartmentMembershipEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "DepartmentMembershipEntity{" +
            "id=" + id +
            ", departmentId=" + departmentId +
            ", personId=" + personId +
            ", kind=" + membershipKind +
            ", validFrom=" + validFrom +
            ", validTo=" + validTo +
            "} " + super.toString();
    }
}
