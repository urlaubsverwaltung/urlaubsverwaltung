package org.synyx.urlaubsverwaltung.user;

public class ThemeDto {

    private String value;
    private String label;

    public String getValue() {
        return value;
    }

    public ThemeDto setValue(String value) {
        this.value = value;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public ThemeDto setLabel(String label) {
        this.label = label;
        return this;
    }
}
