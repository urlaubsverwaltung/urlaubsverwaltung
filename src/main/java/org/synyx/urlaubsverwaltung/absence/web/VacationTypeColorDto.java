package org.synyx.urlaubsverwaltung.absence.web;

class VacationTypeColorDto {

    private final String messageKey;
    private final String color;

    public VacationTypeColorDto(String messageKey, String color) {
        this.messageKey = messageKey;
        this.color = color;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getMessageAbbreviationKey() {
        return messageKey + ".abbr";
    }

    public String getColor() {
        return color;
    }
}
