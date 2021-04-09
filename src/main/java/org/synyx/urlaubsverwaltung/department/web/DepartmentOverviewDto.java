package org.synyx.urlaubsverwaltung.department.web;

import java.time.LocalDate;
import java.util.Objects;

public class DepartmentOverviewDto {

    private Integer id;
    private String name;
    private String description;
    private LocalDate lastModification;
    private boolean twoStageApproval;
    private int activeMembersCount;
    private int inactiveMembersCount;

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

    public int getActiveMembersCount() {
        return activeMembersCount;
    }

    public void setActiveMembersCount(int activeMembersCount) {
        this.activeMembersCount = activeMembersCount;
    }

    public int getInactiveMembersCount() {
        return inactiveMembersCount;
    }

    public void setInactiveMembersCount(int inactiveMembersCount) {
        this.inactiveMembersCount = inactiveMembersCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepartmentOverviewDto that = (DepartmentOverviewDto) o;
        return twoStageApproval == that.twoStageApproval
            && activeMembersCount == that.activeMembersCount
            && inactiveMembersCount == that.inactiveMembersCount
            && Objects.equals(id, that.id)
            && Objects.equals(name, that.name)
            && Objects.equals(description, that.description)
            && Objects.equals(lastModification, that.lastModification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, lastModification, twoStageApproval, activeMembersCount, inactiveMembersCount);
    }

    @Override
    public String toString() {
        return "DepartmentOverviewDto{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", lastModification=" + lastModification +
            ", twoStageApproval=" + twoStageApproval +
            ", activeMembersCount=" + activeMembersCount +
            ", inactiveMembersCount=" + inactiveMembersCount +
            '}';
    }
}
