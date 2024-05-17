package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

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

    public static Builder builder(MessageSource messageSource) {

        final VacationTypeLabelResolver<CustomVacationType> labelResolver =
            (vacationType, locale) -> {
                final Map<Locale, VacationTypeLabel> labelByLocale = vacationType.labelsByLocale();

                return Optional.ofNullable(labelByLocale.get(locale))
                    .map(VacationTypeLabel::label)
                    .filter(StringUtils::hasText)
                    // fallback to base locale (e.g. "de" for incoming "de_DE")
                    .or(() -> Optional.of(labelByLocale.get(Locale.forLanguageTag(locale.getLanguage()))).map(VacationTypeLabel::label).filter(StringUtils::hasText))
                    .orElseGet(() -> messageSource.getMessage("vacationtype.label.fallback", new Object[]{}, locale));
            };

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
