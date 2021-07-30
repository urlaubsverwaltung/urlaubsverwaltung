package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.period.DayLength;

public class ApplicationForLeaveDto {

    private final int id;
    private final Person person;
    private final VacationType vacationType;
    private final String duration;
    private final DayLength dayLength;
    private final String workDays;
    private final String durationOfAbsenceDescription;
    private final boolean statusWaiting;
    private final boolean editAllowed;
    private final boolean approveAllowed;
    private final boolean temporaryApproveAllowed;
    private final boolean rejectAllowed;

    @SuppressWarnings("java:S107") // "Methods should not have too many parameters" - Builder is used for construction
    private ApplicationForLeaveDto(int id, Person person, VacationType vacationType, String duration,
                                   DayLength dayLength, String workDays, String durationOfAbsenceDescription,
                                   boolean statusWaiting, boolean editAllowed, boolean approveAllowed,
                                   boolean temporaryApproveAllowed, boolean rejectAllowed) {
        this.id = id;
        this.person = person;
        this.vacationType = vacationType;
        this.duration = duration;
        this.dayLength = dayLength;
        this.workDays = workDays;
        this.durationOfAbsenceDescription = durationOfAbsenceDescription;
        this.statusWaiting = statusWaiting;
        this.editAllowed = editAllowed;
        this.approveAllowed = approveAllowed;
        this.temporaryApproveAllowed = temporaryApproveAllowed;
        this.rejectAllowed = rejectAllowed;
    }

    public int getId() {
        return id;
    }

    public Person getPerson() {
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

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private int id;
        private Person person;
        private VacationType vacationType;
        private String duration;
        private DayLength dayLength;
        private String workDays;
        private String durationOfAbsenceDescription;
        private boolean statusWaiting;
        private boolean editAllowed;
        private boolean approveAllowed;
        private boolean temporaryApproveAllowed;
        private boolean rejectAllowed;

        Builder id(int id) {
            this.id = id;
            return this;
        }

        Builder person(Person person) {
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
                editAllowed,
                approveAllowed,
                temporaryApproveAllowed,
                rejectAllowed
            );
        }
    }

    public static class Person {
        private final String name;
        private final String avatarUrl;

        public Person(String name, String avatarUrl) {
            this.name = name;
            this.avatarUrl = avatarUrl;
        }

        public String getName() {
            return name;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }
    }

    public static class VacationType {
        private final String category;
        private final String messageKey;

        public VacationType(String category, String messageKey) {
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
