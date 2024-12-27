package org.synyx.urlaubsverwaltung.settings;

import java.util.Locale;
import java.util.Objects;

public class AbsenceTypeSettingsItemLabelDto {

    private Locale locale;
    private String label;

    public AbsenceTypeSettingsItemLabelDto() {
        //
    }

    public AbsenceTypeSettingsItemLabelDto(Locale locale, String label) {
        this.locale = locale;
        this.label = label;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsenceTypeSettingsItemLabelDto that = (AbsenceTypeSettingsItemLabelDto) o;
        return Objects.equals(locale, that.locale) && Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, label);
    }

    @Override
    public String toString() {
        return "AbsenceTypeSettingsItemLabelDto{" +
            "locale=" + locale +
            ", label='" + label + '\'' +
            '}';
    }
}
