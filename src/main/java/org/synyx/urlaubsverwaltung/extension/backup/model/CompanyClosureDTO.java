package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntity;

import java.util.List;

public record CompanyClosureDTO(int month, int dayOfMonth, String name, List<CompanyClosureVersionDTO> versions) {

    public CompanyClosureEntity toEntity() {
        final CompanyClosureEntity entity = new CompanyClosureEntity();
        entity.setMonth(month());
        entity.setDayOfMonth(dayOfMonth());
        entity.setName(name());
        return entity;
    }
}
