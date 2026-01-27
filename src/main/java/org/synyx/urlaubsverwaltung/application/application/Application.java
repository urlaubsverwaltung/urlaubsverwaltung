package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarSupplier;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.time.ZoneOffset.UTC;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;

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
        return hours == null && (getVacationType() != null && getVacationType().isOfCategory(OVERTIME)) ? Duration.ZERO : hours;
    }

    public DateRange getDateRange() {
        return new DateRange(startDate, endDate);
    }

    /**
     * Partition the overtime reduction duration of this application over all involved days.
     * The sum of these mapped durations equals the duration of the application.
     *
     * @param workingTimeCalendarSupplier workingTimeCalendar to consider working-days and no-working-days
     * @return a mapping of durations to the days involved in this application, never {@code null}
     */
    public Map<LocalDate, Duration> getOvertimeReductionShares(WorkingTimeCalendarSupplier workingTimeCalendarSupplier) {

        final DateRange dateRange = getDateRange();
        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarSupplier.getWorkingTimeCalendar(person, dateRange);

        // e.g. one day and a half
        final BigDecimal workingTimeDays = workingTimeCalendar.workingTime(this);

        if (Objects.equals(workingTimeDays, ZERO)) {
            return dateRange.stream().collect(toMap(identity(), localDate -> Duration.ZERO));
        }

        // e.g. three
        final int numberOfHalfDays = workingTimeDays.divide(BigDecimal.valueOf(0.5), HALF_EVEN).intValue();
        // e.g. 12 hours
        final Duration overtimeReduction = getHours();
        // e.g. 12 hours / 3 half days = 4 hours
        final Duration halfDayReduction = overtimeReduction.dividedBy(numberOfHalfDays);

        // date 1 | FULL_DAY --> 8 hours
        // date 2 | MORNING  --> 4 hours
        return dateRange.stream().collect(toMap(identity(), date -> {
            final DayLength dayLengthAtDate = workingTimeCalendar.workingTimeDayLength(date).orElse(DayLength.ZERO);
            return switch (dayLengthAtDate) {
                case ZERO -> Duration.ZERO;
                case FULL -> dayLength.isFull() ? halfDayReduction.multipliedBy(2) : halfDayReduction;
                case MORNING ->
                    (dayLength.isFull() || (dayLength.isMorning() && dayLengthAtDate.isMorning())) ? halfDayReduction : Duration.ZERO;
                case NOON ->
                    (dayLength.isFull() || (dayLength.isNoon() && dayLengthAtDate.isNoon())) ? halfDayReduction : Duration.ZERO;
            };
        }));
    }

    /**
     * Calculates the overtime reduction duration for the given date range.
     *
     * @param dateRange                   date range to calculate overtime reduction
     * @param workingTimeCalendarSupplier supplies working time calendar for this application
     * @return overtime reduction duration for the given date range
     */
    public Duration getOvertimeReductionShareFor(DateRange dateRange, WorkingTimeCalendarSupplier workingTimeCalendarSupplier) {

        if (vacationType == null || !OVERTIME.equals(vacationType.getCategory())) {
            return Duration.ZERO;
        }

        final Map<LocalDate, Duration> durationByDate = getOvertimeReductionShares(workingTimeCalendarSupplier);

        return dateRange.stream()
            .map(date -> durationByDate.getOrDefault(date, Duration.ZERO))
            .reduce(Duration.ZERO, Duration::plus);
    }

    private List<DateRange> dateRangesSplitByYear() {
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

    public Map<Integer, Duration> getHoursByYear(WorkingTimeCalendarSupplier workingTimeSupplier) {

        final DateRange dateRange = getDateRange();
        final WorkingTimeCalendar workingTimeCalendar = workingTimeSupplier.getWorkingTimeCalendar(person, dateRange);

        final List<DateRange> dateRanges = dateRangesSplitByYear();

        return dateRanges.stream().collect(toMap(
            dateRangeForYear -> dateRangeForYear.startDate().getYear(),
            dateRangeForYear -> getOvertimeReductionShareFor(dateRangeForYear, (person, range) -> workingTimeCalendar)
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
            "id=" + id +
            ", person=" + person +
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
