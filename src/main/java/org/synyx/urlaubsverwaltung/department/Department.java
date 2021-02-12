package org.synyx.urlaubsverwaltung.department;

import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;

/**
 * Department represents an organisation unit of a company.
 */
public class Department {

    private Integer id;

    private String name;

    private String description;

    private LocalDate lastModification;

    // flag for two stage approval process
    private boolean twoStageApproval;

    private List<Person> members = new ArrayList<>();

    private List<Person> departmentHeads = new ArrayList<>();

    private List<Person> secondStageAuthorities = new ArrayList<>();

    public Department() {
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
        return "Department{" +
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return isTwoStageApproval() == that.isTwoStageApproval()
            && Objects.equals(getId(), that.getId())
            && Objects.equals(getName(), that.getName())
            && Objects.equals(getDescription(), that.getDescription())
            && Objects.equals(getLastModification(), that.getLastModification())
            && Objects.equals(getMembers(), that.getMembers())
            && Objects.equals(getDepartmentHeads(), that.getDepartmentHeads())
            && Objects.equals(getSecondStageAuthorities(), that.getSecondStageAuthorities());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getLastModification(), isTwoStageApproval(),
            getMembers(), getDepartmentHeads(), getSecondStageAuthorities());
    }
}
