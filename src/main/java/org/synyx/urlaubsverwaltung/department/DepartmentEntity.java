package org.synyx.urlaubsverwaltung.department;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;
import static javax.persistence.FetchType.EAGER;
import static org.hibernate.annotations.LazyCollectionOption.FALSE;

@Entity(name = "Department")
class DepartmentEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    @NotNull
    private LocalDate createdAt;

    private LocalDate lastModification;

    // flag for two stage approval process
    private boolean twoStageApproval;

    @OneToMany
    @LazyCollection(FALSE)
    @CollectionTable(name = "Department_Member")
    private List<Person> members = new ArrayList<>();

    @OneToMany
    @LazyCollection(FALSE)
    @CollectionTable(name = "Department_DepartmentHead")
    private List<Person> departmentHeads = new ArrayList<>();

    @OneToMany
    @LazyCollection(FALSE)
    @CollectionTable(name = "Department_SecondStageAuthority")
    private List<Person> secondStageAuthorities = new ArrayList<>();

    public DepartmentEntity() {
        this.lastModification = LocalDate.now(UTC);
    }

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

    public List<Person> getMembers() {
        if (members == null) {
            members = Collections.emptyList();
        }

        return Collections.unmodifiableList(members);
    }

    public void setMembers(List<Person> members) {
        this.members = members;
    }

    public List<Person> getDepartmentHeads() {
        if (departmentHeads == null) {
            departmentHeads = Collections.emptyList();
        }

        return Collections.unmodifiableList(departmentHeads);
    }

    public void setDepartmentHeads(List<Person> departmentHeads) {
        this.departmentHeads = departmentHeads;
    }

    public List<Person> getSecondStageAuthorities() {
        if (secondStageAuthorities == null) {
            secondStageAuthorities = Collections.emptyList();
        }

        return Collections.unmodifiableList(secondStageAuthorities);
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
