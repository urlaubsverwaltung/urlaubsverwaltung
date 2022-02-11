package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.period.DayLength;

public class ApplicationForLeaveDto {

    private final int id;
    private final ApplicationPersonDto person;
    private final VacationType vacationType;
    private final String duration;
    private final DayLength dayLength;
    private final String workDays;
    private final String durationOfAbsenceDescription;
    private final boolean statusWaiting;
    private final boolean cancelAllowed;
    private final boolean editAllowed;
    private final boolean approveAllowed;
    private final boolean temporaryApproveAllowed;
    private final boolean rejectAllowed;
    private final boolean cancellationRequested;

    @SuppressWarnings("java:S107") // "Methods should not have too many parameters" - Builder is used for construction
    private ApplicationForLeaveDto(int id, ApplicationPersonDto person, VacationType vacationType, String duration,
                                   DayLength dayLength, String workDays, String durationOfAbsenceDescription,
                                   boolean statusWaiting, boolean cancelAllowed, boolean editAllowed, boolean approveAllowed,
                                   boolean temporaryApproveAllowed, boolean rejectAllowed, boolean cancellationRequested) {
        this.id = id;
        this.person = person;
        this.vacationType = vacationType;
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

    public int getId() {
        return id;
    }

    public ApplicationPersonDto getPerson() {
        return person;
    }

    public VacationType getVacationType() {
        return vacationType;
    }

    public String getDuration() {
        return duration;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public String getWorkDays() {
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
        private int id;
        private ApplicationPersonDto person;
        private VacationType vacationType;
        private String duration;
        private DayLength dayLength;
        private String workDays;
        private String durationOfAbsenceDescription;
        private boolean statusWaiting;
        private boolean cancelAllowed;
        private boolean editAllowed;
        private boolean approveAllowed;
        private boolean temporaryApproveAllowed;
        private boolean rejectAllowed;
        private boolean cancellationRequested;

        Builder id(int id) {
            this.id = id;
            return this;
        }

        Builder person(ApplicationPersonDto person) {
            this.person = person;
            return this;
        }

        Builder vacationType(VacationType vacationType) {
            this.vacationType = vacationType;
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

        Builder workDays(String workDays) {
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

    public static class VacationType {
        private final String category;
        private final String messageKey;

        VacationType(String category, String messageKey) {
            this.category = category;
            this.messageKey = messageKey;
        }

        public String getCategory() {
            return category;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }
}
