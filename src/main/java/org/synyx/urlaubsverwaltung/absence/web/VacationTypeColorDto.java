package org.synyx.urlaubsverwaltung.absence.web;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

class VacationTypeColorDto {

    private final String messageKey;
    private final VacationTypeColor color;

    public VacationTypeColorDto(String messageKey, VacationTypeColor color) {
        this.messageKey = messageKey;
        this.color = color;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getMessageAbbreviationKey() {
        return messageKey + ".abbr";
    }

    public VacationTypeColor getColor() {
        return color;
    }
}
