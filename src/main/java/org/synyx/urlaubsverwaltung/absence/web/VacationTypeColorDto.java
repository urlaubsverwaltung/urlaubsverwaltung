package org.synyx.urlaubsverwaltung.absence.web;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

public class VacationTypeColorDto {

    private final String label;
    private final VacationTypeColor color;

    VacationTypeColorDto(String label, VacationTypeColor color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getMessageAbbreviationKey() {
        return label + ".abbr";
    }

    public VacationTypeColor getColor() {
        return color;
    }
}
