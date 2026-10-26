package org.synyx.urlaubsverwaltung.extension.backup.model;

import jakarta.annotation.Nullable;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodEntity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

/**
 * @param departmentIds   ids of the departments in the backup, only relevant if not companyWide
 * @param vacationTypeIds ids of the vacation types in the backup, only relevant if not allVacationTypes
 */
public record BlackoutPeriodDTO(
    String title,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate createdAt,
    @Nullable LocalDate lastModification,
    boolean companyWide,
    boolean allVacationTypes,
    List<Long> departmentIds,
    List<Long> vacationTypeIds
) {

    public BlackoutPeriodEntity toEntity(List<Long> restoredDepartmentIds, List<Long> restoredVacationTypeIds) {
        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setTitle(this.title());
        entity.setStartDate(this.startDate());
        entity.setEndDate(this.endDate());
        entity.setCreatedAt(this.createdAt());
        entity.setLastModification(this.lastModification());
        entity.setCompanyWide(this.companyWide());
        entity.setAllVacationTypes(this.allVacationTypes());
        entity.setDepartmentIds(new HashSet<>(restoredDepartmentIds));
        entity.setVacationTypeIds(new HashSet<>(restoredVacationTypeIds));
        return entity;
    }
}
