package org.synyx.urlaubsverwaltung.application.domain;

/**
 * Defines the category of vacation, describes for example if vacation days are used for this kind of vacation or not.
 */
public enum VacationCategory {

    HOLIDAY("application.data.vacationType.holiday"),
    SPECIALLEAVE("application.data.vacationType.specialleave"),
    UNPAIDLEAVE("application.data.vacationType.unpaidleave"),
    OVERTIME("application.data.vacationType.overtime"),
    OTHER("application.data.vacationType.other");

    private final String messageKey;

    VacationCategory(String messageKey) {

        this.messageKey = messageKey;
    }

    public String getMessageKey() {

        return messageKey;
    }
}
