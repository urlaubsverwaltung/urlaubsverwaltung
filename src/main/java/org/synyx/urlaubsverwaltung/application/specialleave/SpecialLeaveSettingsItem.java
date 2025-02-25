package org.synyx.urlaubsverwaltung.application.specialleave;

public record SpecialLeaveSettingsItem(
    Long id,
    Boolean active,
    String messageKey,
    Integer days
) {
}
