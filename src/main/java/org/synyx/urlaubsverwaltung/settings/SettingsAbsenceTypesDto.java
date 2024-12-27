package org.synyx.urlaubsverwaltung.settings;

import jakarta.validation.Valid;

import java.util.Objects;

public class SettingsAbsenceTypesDto {

    private Long id;
    private AbsenceTypeSettingsDto absenceTypeSettings;
    @Valid
    private SpecialLeaveSettingsDto specialLeaveSettings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AbsenceTypeSettingsDto getAbsenceTypeSettings() {
        return absenceTypeSettings;
    }

    public void setAbsenceTypeSettings(AbsenceTypeSettingsDto absenceTypeSettings) {
        this.absenceTypeSettings = absenceTypeSettings;
    }

    public SpecialLeaveSettingsDto getSpecialLeaveSettings() {
        return specialLeaveSettings;
    }

    public void setSpecialLeaveSettings(SpecialLeaveSettingsDto specialLeaveSettings) {
        this.specialLeaveSettings = specialLeaveSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsAbsenceTypesDto that = (SettingsAbsenceTypesDto) o;
        return Objects.equals(id, that.id)
            && Objects.equals(absenceTypeSettings, that.absenceTypeSettings)
            && Objects.equals(specialLeaveSettings, that.specialLeaveSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, absenceTypeSettings, specialLeaveSettings);
    }

    @Override
    public String toString() {
        return "SettingsAbsenceTypesDto{" +
            "id=" + id +
            ", absenceTypeSettings=" + absenceTypeSettings +
            ", specialLeaveSettings=" + specialLeaveSettings +
            '}';
    }
}
