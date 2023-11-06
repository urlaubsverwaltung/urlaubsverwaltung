package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.springframework.context.MessageSource;

/**
 * {@linkplain VacationType} that has been hard coded in Urlaubsverwaltung some time.
 */
public final class ProvidedVacationType extends VacationType<ProvidedVacationType> {

    private final String messageKey;

    private ProvidedVacationType(Builder builder) {
        super(builder);
        this.messageKey = builder.messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public String toString() {
        return "ProvidedVacationType{" +
            "messageKey='" + messageKey + '\'' +
            ", id=" + id +
            ", active=" + active +
            ", category=" + category +
            ", requiresApprovalToApply=" + requiresApprovalToApply +
            ", requiresApprovalToCancel=" + requiresApprovalToCancel +
            ", color=" + color +
            ", visibleToEveryone=" + visibleToEveryone +
            '}';
    }

    public static Builder builder(MessageSource messageSource) {

        final VacationTypeLabelResolver<ProvidedVacationType> labelResolver =
            (vacationType, locale) -> messageSource.getMessage(vacationType.messageKey, new Object[]{}, locale);

        return new Builder(labelResolver);
    }

    public static Builder builder(ProvidedVacationType providedVacationType) {
        return new Builder(providedVacationType);
    }

    public static final class Builder extends VacationType.Builder<ProvidedVacationType, Builder> {

        private String messageKey;

        Builder(VacationTypeLabelResolver<ProvidedVacationType> labelResolver) {
            super(labelResolver);
        }

        Builder(ProvidedVacationType vacationType) {
            super(vacationType);
            this.messageKey = vacationType.messageKey;
        }

        public Builder messageKey(String messageKey) {
            this.messageKey = messageKey;
            return this;
        }

        @Override
        public ProvidedVacationType build() {
            return new ProvidedVacationType(this);
        }
    }
}
