package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

import java.util.List;
import java.util.Objects;

public class AbsenceTypeSettingsItemDto {

    private Long id;
    private boolean active;
    private String label;
    // messageKey is required until there is no distinction between Provided- and CustomVacationType anymore
    private String messageKey;
    private VacationCategory category;
    private boolean requiresApprovalToApply;
    private boolean requiresApprovalToCancel;
    private VacationTypeColor color;
    private boolean visibleToEveryone;
    private List<AbsenceTypeSettingsItemLabelDto> labels;

    public AbsenceTypeSettingsItemDto() {
        //
    }

    private AbsenceTypeSettingsItemDto(Builder builder) {
        this.id = builder.id;
        this.active = builder.active;
        this.label = builder.label;
        this.messageKey = builder.messageKey;
        this.category = builder.category;
        this.requiresApprovalToApply = builder.requiresApprovalToApply;
        this.requiresApprovalToCancel = builder.requiresApprovalToCancel;
        this.color = builder.color;
        this.visibleToEveryone = builder.visibleToEveryone;
        this.labels = builder.labels;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public VacationCategory getCategory() {
        return category;
    }

    public void setCategory(VacationCategory category) {
        this.category = category;
    }

    public boolean isRequiresApprovalToApply() {
        return requiresApprovalToApply;
    }

    public void setRequiresApprovalToApply(boolean requiresApprovalToApply) {
        this.requiresApprovalToApply = requiresApprovalToApply;
    }

    public boolean isRequiresApprovalToCancel() {
        return requiresApprovalToCancel;
    }

    public void setRequiresApprovalToCancel(boolean requiresApprovalToCancel) {
        this.requiresApprovalToCancel = requiresApprovalToCancel;
    }

    public VacationTypeColor getColor() {
        return color;
    }

    public void setColor(VacationTypeColor color) {
        this.color = color;
    }

    public boolean isVisibleToEveryone() {
        return visibleToEveryone;
    }

    public void setVisibleToEveryone(boolean visibleToEveryone) {
        this.visibleToEveryone = visibleToEveryone;
    }

    public List<AbsenceTypeSettingsItemLabelDto> getLabels() {
        return labels;
    }

    public void setLabels(List<AbsenceTypeSettingsItemLabelDto> labels) {
        this.labels = labels;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "AbsenceTypeItemSettingDto{" +
            "id=" + id +
            ", active=" + active +
            ", label='" + label + '\'' +
            ", messageKey='" + messageKey + '\'' +
            ", category='" + category + '\'' +
            ", requiresApprovalToApply=" + requiresApprovalToApply +
            ", requiresApprovalToCancel=" + requiresApprovalToCancel +
            ", color=" + color +
            ", visibleToEveryone=" + visibleToEveryone +
            ", labels=" + labels +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsenceTypeSettingsItemDto dto = (AbsenceTypeSettingsItemDto) o;
        return active == dto.active
            && requiresApprovalToApply == dto.requiresApprovalToApply
            && requiresApprovalToCancel == dto.requiresApprovalToCancel
            && visibleToEveryone == dto.visibleToEveryone
            && Objects.equals(id, dto.id)
            && Objects.equals(label, dto.label)
            && Objects.equals(messageKey, dto.messageKey)
            && category == dto.category
            && color == dto.color
            && Objects.equals(labels, dto.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, active, label, messageKey, category, requiresApprovalToApply, requiresApprovalToCancel,
            color, visibleToEveryone, labels);
    }

    public static class Builder {
        private Long id;
        private boolean active;
        private String label;
        private String messageKey;
        private VacationCategory category;
        private boolean requiresApprovalToApply;
        private boolean requiresApprovalToCancel;
        private VacationTypeColor color;
        private boolean visibleToEveryone;
        private List<AbsenceTypeSettingsItemLabelDto> labels = List.of();

        Builder setId(Long id) {
            this.id = id;
            return this;
        }

        Builder setActive(boolean active) {
            this.active = active;
            return this;
        }

        Builder setLabel(String label) {
            this.label = label;
            return this;
        }

        Builder setMessageKey(String messageKey) {
            this.messageKey = messageKey;
            return this;
        }

        Builder setCategory(VacationCategory category) {
            this.category = category;
            return this;
        }

        Builder setRequiresApprovalToApply(boolean requiresApprovalToApply) {
            this.requiresApprovalToApply = requiresApprovalToApply;
            return this;
        }

        Builder setRequiresApprovalToCancel(boolean requiresApprovalToCancel) {
            this.requiresApprovalToCancel = requiresApprovalToCancel;
            return this;
        }

        Builder setColor(VacationTypeColor color) {
            this.color = color;
            return this;
        }

        Builder setVisibleToEveryone(boolean visibleToEveryone) {
            this.visibleToEveryone = visibleToEveryone;
            return this;
        }

        Builder setLabels(List<AbsenceTypeSettingsItemLabelDto> labels) {
            this.labels = labels;
            return this;
        }

        AbsenceTypeSettingsItemDto build() {
            return new AbsenceTypeSettingsItemDto(this);
        }
    }
}
