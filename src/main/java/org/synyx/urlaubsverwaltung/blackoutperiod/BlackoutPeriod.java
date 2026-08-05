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
     * @return {@code true} if this blackout period applies company-wide, i.e. is not restricted to specific departments
     */
    public boolean isCompanyWide() {
        return departments.isEmpty();
    }

    /**
     * @return {@code true} if this blackout period applies to every vacation type, i.e. is not restricted to specific ones
     */
    public boolean appliesToAllVacationTypes() {
        return vacationTypes.isEmpty();
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
