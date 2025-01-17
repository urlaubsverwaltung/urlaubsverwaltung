package org.synyx.urlaubsverwaltung.extension.backup.model;


import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;

public record OverTimeSettingsDTO(
    boolean overtimeActive,
    boolean overtimeReductionWithoutApplicationActive,
    boolean overtimeWritePrivilegedOnly,
    Integer maximumOvertime,
    Integer minimumOvertime,
    Integer minimumOvertimeReduction) {


    public static OverTimeSettingsDTO of(OvertimeSettings overtimeSettings) {
        return new OverTimeSettingsDTO(
            overtimeSettings.isOvertimeActive(),
            overtimeSettings.isOvertimeReductionWithoutApplicationActive(),
            overtimeSettings.isOvertimeWritePrivilegedOnly(),
            overtimeSettings.getMaximumOvertime(),
            overtimeSettings.getMinimumOvertime(),
            overtimeSettings.getMinimumOvertimeReduction()
        );
    }

    public OvertimeSettings toOverTimeSettings() {
        OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(overtimeActive);
        overtimeSettings.setOvertimeReductionWithoutApplicationActive(overtimeReductionWithoutApplicationActive);
        overtimeSettings.setOvertimeWritePrivilegedOnly(overtimeWritePrivilegedOnly);
        overtimeSettings.setMaximumOvertime(maximumOvertime);
        overtimeSettings.setMinimumOvertime(minimumOvertime);
        overtimeSettings.setMinimumOvertimeReduction(minimumOvertimeReduction);
        return overtimeSettings;
    }
}
