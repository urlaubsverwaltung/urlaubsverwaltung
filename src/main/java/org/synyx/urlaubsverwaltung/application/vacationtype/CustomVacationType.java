package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * {@linkplain VacationType} created by the user.
 */
public final class CustomVacationType extends VacationType<CustomVacationType> {

    private final List<VacationTypeLabel> labels;

    private CustomVacationType(CustomVacationType.Builder builder) {
        super(builder);
        this.labels = builder.labels;
    }

    public List<VacationTypeLabel> labels() {
        return labels;
    }

    public Map<Locale, VacationTypeLabel> labelsByLocale() {
        return labels.stream().collect(toMap(VacationTypeLabel::locale, identity()));
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
            (vacationType, locale) -> Optional.ofNullable(vacationType.labelsByLocale().get(locale))
                .map(VacationTypeLabel::label)
                .orElse(null);

        return new Builder(labelResolver);
    }

    public static Builder builder(CustomVacationType customVacationType) {
        return new Builder(customVacationType);
    }

    public static final class Builder extends VacationType.Builder<CustomVacationType, Builder> {

        private List<VacationTypeLabel> labels;

        Builder(VacationTypeLabelResolver<CustomVacationType> labelResolver) {
            super(labelResolver);
        }

        Builder(CustomVacationType vacationType) {
            super(vacationType);
        }

        public Builder labels(List<VacationTypeLabel> labels) {
            this.labels = labels;
            return this;
        }

        @Override
        public CustomVacationType build() {
            return new CustomVacationType(this);
        }
    }
}
