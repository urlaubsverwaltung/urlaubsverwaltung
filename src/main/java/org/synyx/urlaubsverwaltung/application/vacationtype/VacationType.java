package org.synyx.urlaubsverwaltung.application.vacationtype;

/**
 * Describes a type of vacation.
 */
public interface VacationType {

    Long getId();
    boolean isActive();
    VacationCategory getCategory();
    String getMessageKey();
    boolean isRequiresApprovalToApply();
    boolean isRequiresApprovalToCancel();
    VacationTypeColor getColor();
    boolean isVisibleToEveryone();

    default boolean isOfCategory(VacationCategory category) {
        return this.getCategory().equals(category);
    }
}
