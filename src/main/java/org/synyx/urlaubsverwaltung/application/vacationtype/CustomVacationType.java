package org.synyx.urlaubsverwaltung.application.vacationtype;

/**
 * {@linkplain VacationType} created by the user.
 */
public final class CustomVacationType extends VacationType<CustomVacationType> {

    private CustomVacationType(CustomVacationType.Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "CustomVacationType{" +
            "id=" + id +
            ", active=" + active +
            ", category=" + category +
            ", requiresApprovalToApply=" + requiresApprovalToApply +
            ", requiresApprovalToCancel=" + requiresApprovalToCancel +
            ", color=" + color +
            ", visibleToEveryone=" + visibleToEveryone +
            '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(CustomVacationType customVacationType) {
        return new Builder(customVacationType);
    }

    public static final class Builder extends VacationType.Builder<CustomVacationType, Builder> {

        Builder() {
            // TODO allow labels for custom vacation type
            super((vacationType, locale) -> "");
        }

        Builder(CustomVacationType vacationType) {
            super(vacationType);
        }

        @Override
        public CustomVacationType build() {
            return new CustomVacationType(this);
        }
    }
}
