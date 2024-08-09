package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.overlap.OverlapCase;
import org.synyx.urlaubsverwaltung.overlap.OverlapService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.util.CalcUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.FULLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.PARTLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.util.DateUtil.isChristmasEve;
import static org.synyx.urlaubsverwaltung.util.DateUtil.isNewYearsEve;

/**
 * This class validate if an {@link ApplicationForLeaveForm} is filled
 * correctly by the user, else it saves error messages in errors object.
 */
@Component
class ApplicationForLeaveFormValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_PERIOD = "error.entry.invalidPeriod";
    private static final String ERROR_HALF_DAY_PERIOD = "application.error.halfDayPeriod";
    private static final String ERROR_MISSING_REASON = "application.error.missingReasonForSpecialLeave";
    private static final String ERROR_TOO_FAR_IN_PAST = "application.error.tooFarInThePast";
    private static final String ERROR_TOO_FAR_IN_FUTURE = "application.error.tooFarInTheFuture";
    private static final String ERROR_ZERO_DAYS = "application.error.zeroDays";
    private static final String ERROR_OVERLAP = "application.error.overlap";
    private static final String ERROR_WORKING_TIME = "application.error.noValidWorkingTime";
    private static final String ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_MORNING = "application.error.alreadyAbsentOn.christmasEve.morning";
    private static final String ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_NOON = "application.error.alreadyAbsentOn.christmasEve.noon";
    private static final String ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_FULL = "application.error.alreadyAbsentOn.christmasEve.full";
    private static final String ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_MORNING = "application.error.alreadyAbsentOn.newYearsEve.morning";
    private static final String ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_NOON = "application.error.alreadyAbsentOn.newYearsEve.noon";
    private static final String ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_FULL = "application.error.alreadyAbsentOn.newYearsEve.full";
    private static final String ERROR_HALF_DAYS_NOT_ALLOWED = "application.error.halfDayPeriod.notAllowed";

    private static final String ERROR_NOT_ENOUGH_DAYS = "application.error.notEnoughVacationDays";
    private static final String ERROR_NOT_ENOUGH_OVERTIME = "application.error.notEnoughOvertime";
    private static final String ERROR_MISSING_HOURS = "application.error.missingHoursForOvertime";
    private static final String ERROR_INVALID_HOURS = "application.error.negativeOvertimeReduction";

    private static final String ATTRIBUTE_START_DATE = "startDate";
    private static final String ATTRIBUTE_END_DATE = "endDate";
    private static final String ATTRIBUTE_REASON = "reason";
    private static final String ATTRIBUTE_HOURS = "hours";
    private static final String ATTRIBUTE_MINUTES = "minutes";
    private static final String DAY_LENGTH = "dayLength";

    private final WorkingTimeService workingTimeService;
    private final WorkDaysCountService workDaysCountService;
    private final OverlapService overlapService;
    private final CalculationService calculationService;
    private final SettingsService settingsService;
    private final OvertimeService overtimeService;
    private final VacationTypeService vacationTypeService;
    private final ApplicationMapper applicationMapper;
    private final Clock clock;

    @Autowired
    ApplicationForLeaveFormValidator(
        WorkingTimeService workingTimeService,
        WorkDaysCountService workDaysCountService,
        OverlapService overlapService,
        CalculationService calculationService,
        SettingsService settingsService,
        OvertimeService overtimeService,
        VacationTypeService vacationTypeService,
        ApplicationMapper applicationMapper,
        Clock clock
    ) {
        this.workingTimeService = workingTimeService;
        this.workDaysCountService = workDaysCountService;
        this.overlapService = overlapService;
        this.calculationService = calculationService;
        this.settingsService = settingsService;
        this.overtimeService = overtimeService;
        this.vacationTypeService = vacationTypeService;
        this.applicationMapper = applicationMapper;
        this.clock = clock;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return ApplicationForLeaveForm.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final ApplicationForLeaveForm applicationForm = (ApplicationForLeaveForm) target;
        final Settings settings = settingsService.getSettings();
        final VacationType<?> vacationType = getVacationType(applicationForm.getVacationType().getId());

        // check if date fields are valid
        validateDateFields(applicationForm, settings, errors);

        // check overtime reduction
        validateOvertimeReduction(applicationForm, settings, vacationType, errors);

        validateDayLength(applicationForm, settings, errors);

        // check if reason is not filled
        if (SPECIALLEAVE.equals(vacationType.getCategory()) && !hasText(applicationForm.getReason())) {
            errors.rejectValue(ATTRIBUTE_REASON, ERROR_MISSING_REASON);
        }

        if (!errors.hasErrors()) {
            // validate if applying for leave is possible
            // (check overlapping applications for leave, vacation days of the person etc.)
            validateIfApplyingForLeaveIsPossible(applicationForm, settings, vacationType, errors);
        }
    }

    private VacationType<?> getVacationType(Long vacationTypeId) {
        return vacationTypeService.getById(vacationTypeId)
            .orElseThrow(() -> new IllegalStateException("could not find vacationType with id=" + vacationTypeId));
    }

    private static void validateDayLength(ApplicationForLeaveForm applicationForm, Settings settings, Errors errors) {

        final LocalDate startDate = applicationForm.getStartDate();
        final LocalDate endDate = applicationForm.getEndDate();
        final DayLength dayLength = applicationForm.getDayLength();

        if (startDate == null) {
            return;
        }

        if (endDate == null || startDate.isEqual(endDate)) {
            if (isChristmasEve(startDate)) {
                validateChristmasEve(dayLength, settings.getWorkingTimeSettings(), errors);
            } else if (isNewYearsEve(startDate)) {
                validateNewYearsEve(dayLength, settings.getWorkingTimeSettings(), errors);
            }
        }

        if (applicationForm.getDayLength() != DayLength.FULL && !settings.getApplicationSettings().isAllowHalfDays()) {
            errors.rejectValue(DAY_LENGTH, ERROR_HALF_DAYS_NOT_ALLOWED);
        }
    }

    private static void validateChristmasEve(DayLength applicationDayLength, WorkingTimeSettings workingTimeSettings, Errors errors) {
        final DayLength workingDurationForChristmasEve = workingTimeSettings.getWorkingDurationForChristmasEve();
        switch (workingDurationForChristmasEve) {
            case ZERO -> {
                if (applicationDayLength != DayLength.ZERO) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_FULL);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_FULL);
                }
            }
            case MORNING -> {
                if (applicationDayLength == NOON) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_NOON);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_NOON);
                }
            }
            case NOON -> {
                if (applicationDayLength == MORNING) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_MORNING);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_MORNING);
                }
            }
        }
    }

    private static void validateNewYearsEve(DayLength applicationDayLength, WorkingTimeSettings workingTimeSettings, Errors errors) {
        final DayLength workingDurationForNewYearsEve = workingTimeSettings.getWorkingDurationForNewYearsEve();
        switch (workingDurationForNewYearsEve) {
            case ZERO -> {
                if (applicationDayLength != DayLength.ZERO) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_FULL);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_FULL);
                }
            }
            case MORNING -> {
                if (applicationDayLength == NOON) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_NOON);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_NOON);
                }
            }
            case NOON -> {
                if (applicationDayLength == MORNING) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_MORNING);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_MORNING);
                }
            }
        }
    }

    private void validateDateFields(ApplicationForLeaveForm applicationForLeave, Settings settings, Errors errors) {

        final LocalDate startDate = applicationForLeave.getStartDate();
        final LocalDate endDate = applicationForLeave.getEndDate();

        validateNotNull(startDate, ATTRIBUTE_START_DATE, errors);
        validateNotNull(endDate, ATTRIBUTE_END_DATE, errors);

        if (startDate != null && endDate != null) {
            validatePeriod(startDate, endDate, applicationForLeave.getDayLength(), settings, errors);
            validateTime(applicationForLeave.getStartTime(), applicationForLeave.getEndTime(), errors);
        }
    }

    private void validateNotNull(LocalDate date, String field, Errors errors) {

        // may be that date field is null because of cast exception, than there is already a field error
        if (date == null && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate, DayLength dayLength, Settings settings, Errors errors) {

        // ensure that startDate < endDate
        if (startDate.isAfter(endDate)) {
            errors.reject(ERROR_PERIOD);
        } else {
            final ApplicationSettings applicationSettings = settings.getApplicationSettings();

            validateNotTooFarInTheFuture(endDate, applicationSettings, errors);
            validateNotTooFarInThePast(startDate, applicationSettings, errors);
            validateSameDayIfHalfDayPeriod(startDate, endDate, dayLength, errors);
        }
    }

    private void validateNotTooFarInTheFuture(LocalDate date, ApplicationSettings settings, Errors errors) {

        final Integer maximumMonthsInAdvance = settings.getMaximumMonthsToApplyForLeaveInAdvance();
        final LocalDate future = ZonedDateTime.now(clock).plusMonths(maximumMonthsInAdvance).toLocalDate();

        if (date.isAfter(future)) {
            errors.reject(ERROR_TOO_FAR_IN_FUTURE, new Object[]{maximumMonthsInAdvance}, null);
        }
    }

    private void validateNotTooFarInThePast(LocalDate date, ApplicationSettings settings, Errors errors) {

        final Integer maximumMonthAfterwards = settings.getMaximumMonthsToApplyForLeaveAfterwards();
        final LocalDate past = ZonedDateTime.now(clock).minusMonths(maximumMonthAfterwards).toLocalDate();

        if (date.isBefore(past)) {
            errors.reject(ERROR_TOO_FAR_IN_PAST, new Object[]{maximumMonthAfterwards}, null);
        }
    }

    private void validateSameDayIfHalfDayPeriod(LocalDate startDate, LocalDate endDate, DayLength dayLength, Errors errors) {

        boolean isHalfDay = dayLength == MORNING || dayLength == NOON;

        if (isHalfDay && !startDate.isEqual(endDate)) {
            errors.reject(ERROR_HALF_DAY_PERIOD);
        }
    }

    private void validateTime(LocalTime startTime, LocalTime endTime, Errors errors) {

        final boolean startTimeIsProvided = startTime != null;
        final boolean endTimeIsProvided = endTime != null;

        final boolean onlyStartTimeProvided = startTimeIsProvided && !endTimeIsProvided;
        final boolean onlyEndTimeProvided = !startTimeIsProvided && endTimeIsProvided;

        if (onlyStartTimeProvided || onlyEndTimeProvided) {
            errors.reject(ERROR_PERIOD);
        }

        if ((startTimeIsProvided && endTimeIsProvided) &&
            (endTime.isBefore(startTime) || endTime.equals(startTime))) {
            errors.reject(ERROR_PERIOD);
        }
    }

    private void validateOvertimeReduction(ApplicationForLeaveForm applicationForLeave, Settings settings, VacationType<?> vacationType, Errors errors) {

        final boolean isOvertime = OVERTIME.equals(vacationType.getCategory());
        if (!isOvertime) {
            return;
        }

        final BigDecimal hours = applicationForLeave.getHours();
        final Integer minutes = applicationForLeave.getMinutes();
        final boolean overtimeFunctionIsActive = settings.getOvertimeSettings().isOvertimeActive();
        final boolean overtimeReductionInputRequiredButNotProvided = overtimeFunctionIsActive && (hours == null && minutes == null);

        if (overtimeReductionInputRequiredButNotProvided && !errors.hasFieldErrors(ATTRIBUTE_HOURS)) {
            errors.rejectValue(ATTRIBUTE_HOURS, ERROR_MISSING_HOURS);
        }

        final boolean hoursNullOrZero = hours == null || CalcUtil.isZero(hours);
        final boolean onlyMinutesAreSet = hoursNullOrZero && minutes != null && minutes > 0;

        if (hours != null && !CalcUtil.isPositive(hours) && !onlyMinutesAreSet) {
            errors.rejectValue(ATTRIBUTE_HOURS, ERROR_INVALID_HOURS);
        }

        if (minutes != null && (minutes < 0 || (minutes == 0 && hoursNullOrZero))) {
            errors.rejectValue(ATTRIBUTE_MINUTES, ERROR_INVALID_HOURS);
        }

        final int minimumOvertimeReduction = settings.getOvertimeSettings().getMinimumOvertimeReduction();
        final Duration minimumDuration = Duration.ofHours(minimumOvertimeReduction);
        final Duration overtimeReduction = applicationForLeave.getOvertimeReduction();
        if (overtimeReduction != null && overtimeReduction.compareTo(minimumDuration) < 0) {
            errors.rejectValue("overtimeReduction", "overtime.error.minimumReductionRequired", new Object[]{minimumOvertimeReduction}, null);
        }
    }

    private void validateIfApplyingForLeaveIsPossible(ApplicationForLeaveForm applicationForm, Settings settings, VacationType<?> vacationType, Errors errors) {

        /*
         * Ensure the person has a working time for the period of the application for leave
         */
        if (!personHasWorkingTime(applicationForm)) {
            errors.reject(ERROR_WORKING_TIME);

            return;
        }

        /*
         * Ensure that no one applies for leave for a vacation of 0 days
         */
        if (vacationOfZeroDays(applicationForm)) {
            errors.reject(ERROR_ZERO_DAYS);

            return;
        }

        /*
         * Ensure that there is no application for leave and no sick note in the same period
         */
        if (vacationIsOverlapping(applicationForm)) {
            errors.reject(ERROR_OVERLAP);

            return;
        }

        /*
         * Ensure that the person has enough vacation days left if the vacation type is
         * {@link org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY}
         */
        if (!enoughVacationDaysLeft(applicationForm, vacationType)) {
            errors.reject(ERROR_NOT_ENOUGH_DAYS);
        }

        /*
         * Ensure that the person has enough overtime hours left if the vacation type is
         * {@link org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME}
         */
        if (!enoughOvertimeHoursLeft(applicationForm, settings, vacationType)) {
            errors.reject(ERROR_NOT_ENOUGH_OVERTIME);
        }
    }

    private boolean personHasWorkingTime(ApplicationForLeaveForm applicationForLeaveForm) {

        final Optional<WorkingTime> workingTime = workingTimeService.getWorkingTime(applicationForLeaveForm.getPerson(), applicationForLeaveForm.getStartDate());
        return workingTime.isPresent();
    }

    private boolean vacationOfZeroDays(ApplicationForLeaveForm applicationForLeaveForm) {

        final BigDecimal days = workDaysCountService.getWorkDaysCount(applicationForLeaveForm.getDayLength(), applicationForLeaveForm.getStartDate(),
            applicationForLeaveForm.getEndDate(), applicationForLeaveForm.getPerson());

        return CalcUtil.isZero(days);
    }

    private boolean vacationIsOverlapping(ApplicationForLeaveForm applicationForLeaveForm) {

        final Application application = applicationMapper.mapToApplication(applicationForLeaveForm);
        final OverlapCase overlap = overlapService.checkOverlap(application);

        return overlap == FULLY_OVERLAPPING || overlap == PARTLY_OVERLAPPING;
    }

    private boolean enoughVacationDaysLeft(ApplicationForLeaveForm applicationForLeaveForm, VacationType<?> vacationType) {

        final boolean isHoliday = HOLIDAY.equals(vacationType.getCategory());

        if (isHoliday) {
            final Application application = applicationMapper.mapToApplication(applicationForLeaveForm);
            return calculationService.checkApplication(application);
        }

        return true;
    }

    private boolean enoughOvertimeHoursLeft(ApplicationForLeaveForm applicationForLeaveForm, Settings settings, VacationType<?> vacationType) {

        final boolean isOvertime = OVERTIME.equals(vacationType.getCategory());

        if (isOvertime) {
            final OvertimeSettings overtimeSettings = settings.getOvertimeSettings();
            final boolean overtimeActive = overtimeSettings.isOvertimeActive();

            if (overtimeActive && applicationForLeaveForm.getHours() != null) {
                return checkOvertimeHours(applicationForLeaveForm, overtimeSettings);
            }
        }

        return true;
    }

    private boolean checkOvertimeHours(ApplicationForLeaveForm applicationForLeaveForm, OvertimeSettings settings) {

        final Duration minimumOvertime = Duration.ofHours(settings.getMinimumOvertime());

        final Duration leftOvertimeForPerson;
        if (applicationForLeaveForm.getId() != null) {
            leftOvertimeForPerson = overtimeService.getLeftOvertimeForPerson(applicationForLeaveForm.getPerson(), List.of(applicationForLeaveForm.getId()));
        } else {
            leftOvertimeForPerson = overtimeService.getLeftOvertimeForPerson(applicationForLeaveForm.getPerson());
        }

        final Duration temporaryOvertimeForPerson = leftOvertimeForPerson.minus(Duration.ofHours(applicationForLeaveForm.getHours().longValue()));

        return temporaryOvertimeForPerson.compareTo(minimumOvertime.negated()) >= 0;
    }
}
