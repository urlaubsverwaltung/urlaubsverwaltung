package org.synyx.urlaubsverwaltung.department;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static java.time.ZoneOffset.UTC;

@Entity(name = "department")
public class DepartmentEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "department_generator")
    @SequenceGenerator(name = "department_generator", sequenceName = "department_id_seq")
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @NotNull
    private LocalDate createdAt;

    private LocalDate lastModification;

    // flag for two stage approval process
    private boolean twoStageApproval;

    @CollectionTable(name = "department_member", joinColumns = @JoinColumn(name = "department_id"))
    @ElementCollection(fetch = EAGER)
    private List<DepartmentMemberEmbeddable> members = new ArrayList<>();

    @OneToMany(fetch = EAGER)
    @CollectionTable(name = "department_department_head")
    private List<Person> departmentHeads = new ArrayList<>();

    @OneToMany(fetch = EAGER)
    @CollectionTable(name = "department_second_stage_authority")
    private List<Person> secondStageAuthorities = new ArrayList<>();

    public DepartmentEntity() {
        this.lastModification = LocalDate.now(UTC);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getLastModification() {
        return this.lastModification;
    }

    public void setLastModification(LocalDate lastModification) {
        this.lastModification = lastModification;
    }

    public boolean isTwoStageApproval() {
        return twoStageApproval;
    }

    public void setTwoStageApproval(boolean twoStageApproval) {
        this.twoStageApproval = twoStageApproval;
    }

    public List<DepartmentMemberEmbeddable> getMembers() {
        return members;
    }

    public void setMembers(List<DepartmentMemberEmbeddable> members) {
        this.members = members;
    }

    public List<Person> getDepartmentHeads() {
        return departmentHeads;
    }

    public void setDepartmentHeads(List<Person> departmentHeads) {
        this.departmentHeads = departmentHeads;
    }

    public List<Person> getSecondStageAuthorities() {
        return secondStageAuthorities;
    }

    public void setSecondStageAuthorities(List<Person> secondStageAuthorities) {
        this.secondStageAuthorities = secondStageAuthorities;
    }

    @Override
    public String toString() {
        return "DepartmentEntity{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", lastModification=" + lastModification +
            ", twoStageApproval=" + twoStageApproval +
            ", members=" + members +
            ", departmentHeads=" + departmentHeads +
            ", secondStageAuthorities=" + secondStageAuthorities +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DepartmentEntity that = (DepartmentEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
