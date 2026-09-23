package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.department.Department;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;

/**
 * Represents a blackout period ("Urlaubssperre") during which vacation applications for leave are blocked.
 */
public class BlackoutPeriod {

    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate createdAt;
    private LocalDate lastModification;
    private boolean companyWide;
    private boolean allVacationTypes;
    private List<Department> departments = new ArrayList<>();
    private List<VacationType<?>> vacationTypes = new ArrayList<>();

    public BlackoutPeriod() {
        this.lastModification = LocalDate.now(UTC);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getLastModification() {
        return lastModification;
    }

    public void setLastModification(LocalDate lastModification) {
        this.lastModification = lastModification;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<VacationType<?>> getVacationTypes() {
        return vacationTypes;
    }

    public void setVacationTypes(List<VacationType<?>> vacationTypes) {
        this.vacationTypes = vacationTypes;
    }

    /**
     * @return {@code true} if this blackout period applies company-wide. A blackout period that is not company-wide
     * applies to the members of its departments only - and to nobody if none of its departments exists anymore.
     */
    public boolean isCompanyWide() {
        return companyWide;
    }

    public void setCompanyWide(boolean companyWide) {
        this.companyWide = companyWide;
    }

    /**
     * @return {@code true} if this blackout period applies to every vacation type, including types created later
     */
    public boolean appliesToAllVacationTypes() {
        return allVacationTypes;
    }

    public void setAllVacationTypes(boolean allVacationTypes) {
        this.allVacationTypes = allVacationTypes;
    }

    /**
     * @return {@code true} if this blackout period is scoped to departments but none of them exists anymore,
     * i.e. it applies to nobody
     */
    public boolean hasNoRemainingDepartments() {
        return !companyWide && departments.isEmpty();
    }

    public boolean overlaps(LocalDate otherStartDate, LocalDate otherEndDate) {
        return !startDate.isAfter(otherEndDate) && !endDate.isBefore(otherStartDate);
    }

    @Override
    public String toString() {
        return "BlackoutPeriod{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
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
        final BlackoutPeriod that = (BlackoutPeriod) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
