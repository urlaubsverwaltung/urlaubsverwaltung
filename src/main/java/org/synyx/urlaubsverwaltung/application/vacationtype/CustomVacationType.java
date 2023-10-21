package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.util.Locale;
import java.util.Map;

/**
 * {@linkplain VacationType} created by the user.
 */
public final class CustomVacationType extends VacationType<CustomVacationType> {

    private final Map<Locale, String> labelByLocale;

    private CustomVacationType(CustomVacationType.Builder builder) {
        super(builder);
        this.labelByLocale = builder.labelByLocale;
    }

    public Map<Locale, String> getLabelByLocale() {
        return labelByLocale;
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

        final VacationTypeLabelResolver<CustomVacationType> labelResolver =
            (vacationType, locale) -> vacationType.labelByLocale.get(locale);

        return new Builder(labelResolver);
    }

    public static Builder builder(CustomVacationType customVacationType) {
        return new Builder(customVacationType);
    }

    public static final class Builder extends VacationType.Builder<CustomVacationType, Builder> {

        private Map<Locale, String> labelByLocale;

        Builder(VacationTypeLabelResolver<CustomVacationType> labelResolver) {
            super(labelResolver);
        }

        Builder(CustomVacationType vacationType) {
            super(vacationType);
        }

        public Builder labelByLocale(Map<Locale, String> labelByLocale) {
            this.labelByLocale = labelByLocale;
            return this;
        }

        @Override
        public CustomVacationType build() {
            return new CustomVacationType(this);
        }
    }
}
