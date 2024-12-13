package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;

import java.util.Locale;
import java.util.Map;

/**
 * @param id
 * @param active
 * @param vacationTypeCategory
 * @param requiresApprovalToApply
 * @param requiresApprovalToCancel
 * @param color
 * @param visibleToEveryone
 * @param customType
 * @param messageKey               only available when customType is false
 * @param labels                   translation of the vacation type for all supported locales
 */
public record VacationTypeDTO(Long id, boolean active, VacationTypeCategoryDTO vacationTypeCategory,
                              boolean requiresApprovalToApply, boolean requiresApprovalToCancel,
                              VacationTypeColorDTO color, boolean visibleToEveryone, boolean customType,
                              String messageKey, Map<Locale, String> labels) {

    public VacationTypeEntity toVacationType() {
        VacationTypeEntity entity = new VacationTypeEntity();
        entity.setActive(this.active);
        entity.setCategory(this.vacationTypeCategory.toVacationCategory());
        entity.setRequiresApprovalToApply(this.requiresApprovalToApply);
        entity.setRequiresApprovalToCancel(this.requiresApprovalToCancel);
        entity.setColor(this.color.toVacationTypeColor());
        entity.setVisibleToEveryone(this.visibleToEveryone);
        entity.setCustom(this.customType);
        entity.setMessageKey(this.messageKey);
        entity.setLabelByLocale(this.labels);
        return entity;
    }
}
