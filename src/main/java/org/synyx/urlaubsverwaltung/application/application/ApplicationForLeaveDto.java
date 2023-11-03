package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;

public class ApplicationForLeaveDto {

    private final long id;
    private final ApplicationPersonDto person;
    private final VacationTypeDto vacationType;
    private final ApplicationStatus status;
    private final String duration;
    private final DayLength dayLength;
    private final BigDecimal workDays;
    private final String durationOfAbsenceDescription;
    private final boolean statusWaiting;
    private final boolean cancelAllowed;
    private final boolean editAllowed;
    private final boolean approveAllowed;
    private final boolean temporaryApproveAllowed;
    private final boolean rejectAllowed;
    private final boolean cancellationRequested;

    @SuppressWarnings("java:S107") // "Methods should not have too many parameters" - Builder is used for construction
    private ApplicationForLeaveDto(long id, ApplicationPersonDto person, VacationTypeDto vacationType, ApplicationStatus status,
                                   String duration, DayLength dayLength, BigDecimal workDays, String durationOfAbsenceDescription,
                                   boolean statusWaiting, boolean cancelAllowed, boolean editAllowed, boolean approveAllowed,
                                   boolean temporaryApproveAllowed, boolean rejectAllowed, boolean cancellationRequested) {
        this.id = id;
        this.person = person;
        this.vacationType = vacationType;
        this.status = status;
        this.duration = duration;
        this.dayLength = dayLength;
        this.workDays = workDays;
        this.durationOfAbsenceDescription = durationOfAbsenceDescription;
        this.statusWaiting = statusWaiting;
        this.cancelAllowed = cancelAllowed;
        this.editAllowed = editAllowed;
        this.approveAllowed = approveAllowed;
        this.temporaryApproveAllowed = temporaryApproveAllowed;
        this.rejectAllowed = rejectAllowed;
        this.cancellationRequested = cancellationRequested;
    }

    public long getId() {
        return id;
    }

    public ApplicationPersonDto getPerson() {
        return person;
    }

    public VacationTypeDto getVacationType() {
        return vacationType;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getDuration() {
        return duration;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public BigDecimal getWorkDays() {
        return workDays;
    }

    public String getDurationOfAbsenceDescription() {
        return durationOfAbsenceDescription;
    }

    public boolean isStatusWaiting() {
        return statusWaiting;
    }

    public boolean isCancelAllowed() {
        return cancelAllowed;
    }

    public boolean isEditAllowed() {
        return editAllowed;
    }

    public boolean isApproveAllowed() {
        return approveAllowed;
    }

    public boolean isTemporaryApproveAllowed() {
        return temporaryApproveAllowed;
    }

    public boolean isRejectAllowed() {
        return rejectAllowed;
    }

    public boolean isCancellationRequested() {
        return cancellationRequested;
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private long id;
        private ApplicationPersonDto person;
        private VacationTypeDto vacationType;
        private ApplicationStatus status;
        private String duration;
        private DayLength dayLength;
        private BigDecimal workDays;
        private String durationOfAbsenceDescription;
        private boolean statusWaiting;
        private boolean cancelAllowed;
        private boolean editAllowed;
        private boolean approveAllowed;
        private boolean temporaryApproveAllowed;
        private boolean rejectAllowed;
        private boolean cancellationRequested;

        Builder id(long id) {
            this.id = id;
            return this;
        }

        Builder person(ApplicationPersonDto person) {
            this.person = person;
            return this;
        }

        Builder vacationType(VacationTypeDto vacationType) {
            this.vacationType = vacationType;
            return this;
        }

        Builder status(ApplicationStatus status) {
            this.status = status;
            return this;
        }

        Builder duration(String duration) {
            this.duration = duration;
            return this;
        }

        Builder dayLength(DayLength dayLength) {
            this.dayLength = dayLength;
            return this;
        }

        Builder workDays(BigDecimal workDays) {
            this.workDays = workDays;
            return this;
        }

        Builder durationOfAbsenceDescription(String durationOfAbsenceDescription) {
            this.durationOfAbsenceDescription = durationOfAbsenceDescription;
            return this;
        }

        Builder statusWaiting(boolean statusWaiting) {
            this.statusWaiting = statusWaiting;
            return this;
        }

        Builder cancelAllowed(boolean cancelAllowed) {
            this.cancelAllowed = cancelAllowed;
            return this;
        }

        Builder editAllowed(boolean editAllowed) {
            this.editAllowed = editAllowed;
            return this;
        }

        Builder approveAllowed(boolean approveAllowed) {
            this.approveAllowed = approveAllowed;
            return this;
        }

        Builder temporaryApproveAllowed(boolean temporaryApproveAllowed) {
            this.temporaryApproveAllowed = temporaryApproveAllowed;
            return this;
        }

        Builder rejectAllowed(boolean rejectAllowed) {
            this.rejectAllowed = rejectAllowed;
            return this;
        }

        Builder cancellationRequested(boolean cancellationRequested) {
            this.cancellationRequested = cancellationRequested;
            return this;
        }

        ApplicationForLeaveDto build() {
            return new ApplicationForLeaveDto(
                id,
                person,
                vacationType,
                status,
                duration,
                dayLength,
                workDays,
                durationOfAbsenceDescription,
                statusWaiting,
                cancelAllowed,
                editAllowed,
                approveAllowed,
                temporaryApproveAllowed,
                rejectAllowed,
                cancellationRequested
            );
        }
    }

    public static class VacationTypeDto {
        private final String category;
        private final String label;
        private final VacationTypeColor color;

        VacationTypeDto(String category, String label, VacationTypeColor color) {
            this.category = category;
            this.label = label;
            this.color = color;
        }

        public String getCategory() {
            return category;
        }

        public String getLabel() {
            return label;
        }

        public VacationTypeColor getColor() {
            return color;
        }
    }
}
