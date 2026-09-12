package org.synyx.urlaubsverwaltung.blackoutperiod;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.GenerationType.SEQUENCE;
import static java.time.ZoneOffset.UTC;

/**
 * A blackout period ("Urlaubssperre") during which vacation applications are blocked, either company-wide or
 * scoped to specific departments, and either for all vacation types or a restricted set of them.
 */
@Entity(name = "blackout_period")
public class BlackoutPeriodEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "blackout_period_generator")
    @SequenceGenerator(name = "blackout_period_generator", sequenceName = "blackout_period_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "last_modification")
    private LocalDate lastModification;

    @ElementCollection
    @CollectionTable(name = "blackout_period_department", joinColumns = @JoinColumn(name = "blackout_period_id"))
    @Column(name = "department_id")
    private Set<Long> departmentIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "blackout_period_vacation_type", joinColumns = @JoinColumn(name = "blackout_period_id"))
    @Column(name = "vacation_type_id")
    private Set<Long> vacationTypeIds = new HashSet<>();

    public BlackoutPeriodEntity() {
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

    public Set<Long> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(Set<Long> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public Set<Long> getVacationTypeIds() {
        return vacationTypeIds;
    }

    public void setVacationTypeIds(Set<Long> vacationTypeIds) {
        this.vacationTypeIds = vacationTypeIds;
    }

    @Override
    public String toString() {
        return "BlackoutPeriodEntity{" +
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
        final BlackoutPeriodEntity that = (BlackoutPeriodEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
