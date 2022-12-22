package org.synyx.urlaubsverwaltung.department.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

public class DepartmentForm {

    private Long id;
    private String name;
    private String description;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate lastModification;
    private boolean twoStageApproval;
    private List<Person> members = new ArrayList<>();
    private List<Person> departmentHeads = new ArrayList<>();
    private List<Person> secondStageAuthorities = new ArrayList<>();

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

    public LocalDate getLastModification() {
        return lastModification;
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
        return members;
    }

    public void setMembers(List<Person> members) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DepartmentForm that = (DepartmentForm) o;
        return twoStageApproval == that.twoStageApproval &&
            Objects.equals(name, that.name) &&
            Objects.equals(description, that.description) &&
            Objects.equals(lastModification, that.lastModification) &&
            Objects.equals(members, that.members) &&
            Objects.equals(departmentHeads, that.departmentHeads) &&
            Objects.equals(secondStageAuthorities, that.secondStageAuthorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, lastModification, twoStageApproval, members, departmentHeads, secondStageAuthorities);
    }

    @Override
    public String toString() {
        return "DepartmentForm{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", lastModification=" + lastModification +
            ", twoStageApproval=" + twoStageApproval +
            ", members=" + members +
            ", departmentHeads=" + departmentHeads +
            ", secondStageAuthorities=" + secondStageAuthorities +
            '}';
    }
}
