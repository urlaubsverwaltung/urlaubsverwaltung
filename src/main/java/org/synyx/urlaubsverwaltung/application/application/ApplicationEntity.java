package org.synyx.urlaubsverwaltung.application.application;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.DurationConverter;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity(name = "application")
public class ApplicationEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "application_generator")
    @SequenceGenerator(name = "application_generator", sequenceName = "application_id_seq")
    private Long id;

    /**
     * Person that will be on vacation if this application for leave is allowed.
     */
    @ManyToOne
    private Person person;

    /**
     * Person that made the application - can be different to the person that will be on vacation.
     */
    @ManyToOne
    private Person applier;

    /**
     * Person that allowed or rejected the application for leave.
     */
    @ManyToOne
    private Person boss;

    /**
     * Person that cancelled the application.
     */
    @ManyToOne
    private Person canceller;

    /**
     * Flag for two stage approval process.
     *
     * @since 2.15.0
     */
    private boolean twoStageApproval;

    /**
     * Start date of the application for leave.
     */
    private LocalDate startDate;

    /**
     * Start time of the application for leave.
     *
     * @since 2.15.0
     */
    private LocalTime startTime;

    /**
     * End date of the application for leave.
     */
    private LocalDate endDate;

    /**
     * End time of the application for leave.
     *
     * @since 2.15.0
     */
    private LocalTime endTime;

    /**
     * Type of vacation, e.g. holiday, special leave etc.
     */
    @ManyToOne
    private VacationTypeEntity vacationType;

    /**
     * Day length of the vacation period, e.g. full day, morning, noon.
     */
    @Enumerated(STRING)
    private DayLength dayLength;

    /**
     * Reason for the vacation, is required for some types of vacation, e.g. for special leave.
     */
    private String reason;

    @CollectionTable(name = "holiday_replacements", joinColumns = @JoinColumn(name = "application_id"))
    @ElementCollection(fetch = EAGER)
    private List<HolidayReplacementEntity> holidayReplacements = new ArrayList<>();

    /**
     * Further information: address, phone number etc.
     */
    private String address;

    /**
     * Date of application for leave creation.
     */
    private LocalDate applicationDate;

    /**
     * Date of application for leave cancellation.
     */
    private LocalDate cancelDate;

    /**
     * Date of application for leave processing (allow or reject).
     */
    private LocalDate editedDate;

    /**
     * Last date of sending a remind notification that application for leave has to be processed.
     */
    private LocalDate remindDate;

    /**
     * Describes the current status of the application for leave (e.g. allowed, rejected etc.)
     */
    @Enumerated(STRING)
    private ApplicationStatus status;

    /**
     * Flag if team is informed about vacation or not.
     */
    private boolean teamInformed;

    /**
     * The number of overtime hours that are used for this application for leave.
     *
     * @since 2.11.0
     */
    @Convert(converter = DurationConverter.class)
    private Duration hours;

    private LocalDate upcomingHolidayReplacementNotificationSend;

    private LocalDate upcomingApplicationsReminderSend;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getApplicationDate() {
        return this.applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LocalDate getCancelDate() {
        return this.cancelDate;
    }

    public void setCancelDate(LocalDate cancelDate) {
        this.cancelDate = cancelDate;
    }

    public LocalDate getEditedDate() {
        return this.editedDate;
    }

    public void setEditedDate(LocalDate editedDate) {
        this.editedDate = editedDate;
    }

    public Person getApplier() {
        return applier;
    }

    public void setApplier(Person applier) {
        this.applier = applier;
    }

    public Person getBoss() {
        return boss;
    }

    public void setBoss(Person boss) {
        this.boss = boss;
    }

    public Person getCanceller() {
        return canceller;
    }

    public void setCanceller(Person canceller) {
        this.canceller = canceller;
    }

    public boolean isTwoStageApproval() {
        return twoStageApproval;
    }

    public void setTwoStageApproval(boolean twoStageApproval) {
        this.twoStageApproval = twoStageApproval;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public void setDayLength(DayLength dayLength) {
        this.dayLength = dayLength;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public VacationTypeEntity getVacationType() {
        return vacationType;
    }

    public void setVacationType(VacationTypeEntity vacationType) {
        this.vacationType = vacationType;
    }

    public LocalDate getRemindDate() {
        return this.remindDate;
    }

    public void setRemindDate(LocalDate remindDate) {
        this.remindDate = remindDate;
    }

    public boolean isTeamInformed() {
        return teamInformed;
    }

    public void setTeamInformed(boolean teamInformed) {
        this.teamInformed = teamInformed;
    }

    public Duration getHours() {
        return hours;
    }

    public void setHours(Duration hours) {
        this.hours = hours;
    }

    public LocalDate getUpcomingHolidayReplacementNotificationSend() {
        return upcomingHolidayReplacementNotificationSend;
    }

    public void setUpcomingHolidayReplacementNotificationSend(LocalDate upcomingHolidayReplacementNotificationSend) {
        this.upcomingHolidayReplacementNotificationSend = upcomingHolidayReplacementNotificationSend;
    }

    public LocalDate getUpcomingApplicationsReminderSend() {
        return upcomingApplicationsReminderSend;
    }

    public void setUpcomingApplicationsReminderSend(LocalDate upcomingApplicationsReminderSend) {
        this.upcomingApplicationsReminderSend = upcomingApplicationsReminderSend;
    }

    public List<HolidayReplacementEntity> getHolidayReplacements() {
        return holidayReplacements;
    }

    public void setHolidayReplacements(List<HolidayReplacementEntity> holidayReplacements) {
        this.holidayReplacements = holidayReplacements;
    }

    @Override
    public String toString() {
        return "ApplicationEntity{" +
            "person=" + person +
            ", applier=" + applier +
            ", boss=" + boss +
            ", canceller=" + canceller +
            ", twoStageApproval=" + twoStageApproval +
            ", startDate=" + startDate +
            ", startTime=" + startTime +
            ", endDate=" + endDate +
            ", endTime=" + endTime +
            ", vacationType=" + vacationType +
            ", dayLength=" + dayLength +
            ", holidayReplacements=" + holidayReplacements +
            ", applicationDate=" + applicationDate +
            ", cancelDate=" + cancelDate +
            ", editedDate=" + editedDate +
            ", remindDate=" + remindDate +
            ", status=" + status +
            ", teamInformed=" + teamInformed +
            ", hours=" + hours +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ApplicationEntity that = (ApplicationEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
