package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DecimalConverter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.math.RoundingMode.HALF_EVEN;
import static java.time.Duration.ZERO;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.util.DecimalConverter.toFormattedDecimal;

/**
 * This class describes an application for leave.
 */

public class Application {

    private Long id;

    /**
     * Person that will be on vacation if this application for leave is allowed.
     */
    private Person person;

    /**
     * Person that made the application - can be different to the person that will be on vacation.
     */
    private Person applier;

    /**
     * Person that allowed or rejected the application for leave.
     */
    private Person boss;

    /**
     * Person that cancelled the application.
     */
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
    private VacationType<?> vacationType;

    /**
     * Day length of the vacation period, e.g. full day, morning, noon.
     */
    private DayLength dayLength;

    /**
     * Reason for the vacation, is required for some types of vacation, e.g. for special leave.
     */
    private String reason;

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

    public VacationType<?> getVacationType() {
        return vacationType;
    }

    public void setVacationType(VacationType<?> vacationType) {
        this.vacationType = vacationType;
    }

    public boolean isFormerlyAllowed() {
        return hasStatus(CANCELLED);
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

    /**
     * Partition the overtime reduction duration of this application over all involved days.
     * The sum of these mapped durations equals the duration of the application.
     * This partitioning weights all days evenly  and doesn't account for half days, weekends etc.
     *
     * @return a mapping of durations to the days involved in this application, never {@code null}
     */
    public Map<LocalDate, Duration> getOvertimeReductionShares() {
        return new DateRange(startDate, endDate).stream()
            .collect(toMap(Function.identity(), this::getOvertimeReductionShareFor));
    }

    public Duration getOvertimeReductionShareFor(LocalDate date) {
        return getOvertimeReductionShareFor(new DateRange(date, date));
    }

    public Duration getOvertimeReductionShareFor(DateRange dateRange) {

        if (vacationType == null || !OVERTIME.equals(vacationType.getCategory())) {
            return ZERO;
        }

        final DateRange overtimeDateRange = new DateRange(startDate, endDate);
        final Duration durationOfOverlap = overtimeDateRange.overlap(dateRange).map(DateRange::duration).orElse(ZERO);

        final Duration overtimeReductionHours = Optional.ofNullable(hours).orElse(ZERO);
        final Duration overtimeDateRangeDuration = overtimeDateRange.duration();
        final BigDecimal secondsProRata = toFormattedDecimal(overtimeReductionHours)
            .divide(toFormattedDecimal(overtimeDateRangeDuration), HALF_EVEN)
            .multiply(toFormattedDecimal(durationOfOverlap))
            .setScale(0, HALF_EVEN);

        return DecimalConverter.toDuration(secondsProRata);
    }

    private List<DateRange> splitByYear() {
        List<DateRange> dateRangesByYear = new ArrayList<>();

        LocalDate currentStartDate = startDate;
        LocalDate currentEndDate = startDate.withDayOfYear(1).plusYears(1).minusDays(1);

        while (currentEndDate.isBefore(endDate) || currentEndDate.isEqual(endDate)) {
            dateRangesByYear.add(new DateRange(currentStartDate, currentEndDate));

            currentStartDate = currentEndDate.plusDays(1);
            currentEndDate = currentStartDate.withDayOfYear(1).plusYears(1).minusDays(1);
        }

        // Add the remaining date range if endDate is not on a year boundary
        if (!currentStartDate.isAfter(endDate)) {
            dateRangesByYear.add(new DateRange(currentStartDate, endDate));
        }

        return dateRangesByYear;
    }

    public Map<Integer, Duration> getHoursByYear() {
        return this.splitByYear().stream()
            .collect(toMap(
                dateRangeForYear -> dateRangeForYear.startDate().getYear(),
                this::getOvertimeReductionShareFor
            ));
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

    /**
     * Checks if the application for leave has the given status.
     *
     * @param status to be checked
     * @return {@code true} if the application for leave has the given status, else {@code false}
     */
    public boolean hasStatus(ApplicationStatus status) {
        return getStatus() == status;
    }

    /**
     * Return period of time of the application for leave.
     *
     * @return period of time, never {@code null}
     */
    public Period getPeriod() {
        return new Period(getStartDate(), getEndDate(), getDayLength());
    }

    /**
     * Get start of application for leave as date with time.
     *
     * @return start date with time or {@code null} if start date or start time is missing
     */
    public ZonedDateTime getStartDateWithTime() {

        final LocalDate date = getStartDate();
        final LocalTime time = getStartTime();

        if (date != null && time != null) {
            return ZonedDateTime.of(date, time, UTC);
        }

        return null;
    }

    /**
     * Get end of application for leave as date with time.
     *
     * @return end date with time or {@code null} if end date or end time is missing
     */
    public ZonedDateTime getEndDateWithTime() {

        final LocalDate date = getEndDate();
        final LocalTime time = getEndTime();

        if (date != null && time != null) {
            return ZonedDateTime.of(date, time, UTC);
        }

        return null;
    }

    public List<HolidayReplacementEntity> getHolidayReplacements() {
        return holidayReplacements;
    }

    public void setHolidayReplacements(List<HolidayReplacementEntity> holidayReplacements) {
        this.holidayReplacements = holidayReplacements;
    }

    @Override
    public String toString() {
        return "Application{" +
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
        final Application that = (Application) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
