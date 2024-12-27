package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.util.Locale;
import java.util.Objects;

/**
 * Describes a type of vacation.
 */
public abstract class VacationType<T extends VacationType<T>> {

    protected final Long id;
    protected final boolean active;
    protected final VacationCategory category;
    protected final boolean requiresApprovalToApply;
    protected final boolean requiresApprovalToCancel;
    protected final VacationTypeColor color;
    protected final boolean visibleToEveryone;
    protected final VacationTypeLabelResolver<T> labelResolver;

    protected VacationType(Builder<T, ?> builder) {
        this.id = builder.id;
        this.active = builder.active;
        this.category = builder.category;
        this.requiresApprovalToApply = builder.requiresApprovalToApply;
        this.requiresApprovalToCancel = builder.requiresApprovalToCancel;
        this.color = builder.color;
        this.visibleToEveryone = builder.visibleToEveryone;
        this.labelResolver = builder.labelResolver;
    }

    public Long getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public VacationCategory getCategory() {
        return category;
    }

    public boolean isRequiresApprovalToApply() {
        return requiresApprovalToApply;
    }

    public boolean isRequiresApprovalToCancel() {
        return requiresApprovalToCancel;
    }

    public VacationTypeColor getColor() {
        return color;
    }

    public boolean isVisibleToEveryone() {
        return visibleToEveryone;
    }

    public boolean isOfCategory(VacationCategory category) {
        return this.category.equals(category);
    }

    @SuppressWarnings("unchecked")
    public String getLabel(Locale locale) {
        return labelResolver.getLabel((T) this, locale);
    }

    protected VacationTypeLabelResolver<T> getLabelResolver() {
        return labelResolver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacationType<?> that = (VacationType<?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @SuppressWarnings("unchecked")
    public abstract static class Builder<T extends VacationType<T>, B extends Builder<T, B>> {

        protected Long id;
        protected boolean active;
        protected VacationCategory category;
        protected boolean requiresApprovalToApply;
        protected boolean requiresApprovalToCancel;
        protected VacationTypeColor color;
        protected boolean visibleToEveryone;
        protected final VacationTypeLabelResolver<T> labelResolver;

        protected Builder(VacationTypeLabelResolver<T> labelResolver) {
            this.labelResolver = labelResolver;
        }

        protected Builder(T vacationType) {
            this(vacationType.getLabelResolver());
            this.id = vacationType.getId();
            this.active = vacationType.isActive();
            this.category = vacationType.getCategory();
            this.requiresApprovalToApply = vacationType.isRequiresApprovalToApply();
            this.requiresApprovalToCancel = vacationType.isRequiresApprovalToCancel();
            this.color = vacationType.getColor();
            this.visibleToEveryone = vacationType.isVisibleToEveryone();
        }

        public B id(Long id) {
            this.id = id;
            return (B) this;
        }

        public B active(boolean active) {
            this.active = active;
            return (B) this;
        }

        public B category(VacationCategory category) {
            this.category = category;
            return (B) this;
        }

        public B requiresApprovalToApply(boolean requiresApprovalToApply) {
            this.requiresApprovalToApply = requiresApprovalToApply;
            return (B) this;
        }

        public B requiresApprovalToCancel(boolean requiresApprovalToCancel) {
            this.requiresApprovalToCancel = requiresApprovalToCancel;
            return (B) this;
        }

        public B color(VacationTypeColor color) {
            this.color = color;
            return (B) this;
        }

        public B visibleToEveryone(boolean visibleToEveryone) {
            this.visibleToEveryone = visibleToEveryone;
            return (B) this;
        }

        public abstract T build();
    }
}
