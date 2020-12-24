package org.synyx.urlaubsverwaltung.application.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.application.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.CalculationService;
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
import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.FULLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.PARTLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.util.DateUtil.isChristmasEve;
import static org.synyx.urlaubsverwaltung.util.DateUtil.isNewYearsEve;

/**
 * This class validate if an {@link org.synyx.urlaubsverwaltung.application.web.ApplicationForLeaveForm} is filled
 * correctly by the user, else it saves error messages in errors object.
 */
@Component
public class ApplicationForLeaveFormValidator implements Validator {

    private static final int MAX_CHARS = 200;

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";
    private static final String ERROR_PERIOD = "error.entry.invalidPeriod";
    private static final String ERROR_HALF_DAY_PERIOD = "application.error.halfDayPeriod";
    private static final String ERROR_MISSING_REASON = "application.error.missingReasonForSpecialLeave";
    private static final String ERROR_PAST = "application.error.tooFarInThePast";
    private static final String ERROR_TOO_LONG = "application.error.tooFarInTheFuture";
    private static final String ERROR_ZERO_DAYS = "application.error.zeroDays";
    private static final String ERROR_OVERLAP = "application.error.overlap";
    private static final String ERROR_WORKING_TIME = "application.error.noValidWorkingTime";
    private static final String ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_MORNING = "application.error.alreadyAbsentOn.christmasEve.morning";
    private static final String ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_NOON = "application.error.alreadyAbsentOn.christmasEve.noon";
    private static final String ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_FULL = "application.error.alreadyAbsentOn.christmasEve.full";
    private static final String ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_MORNING = "application.error.alreadyAbsentOn.newYearsEve.morning";
    private static final String ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_NOON = "application.error.alreadyAbsentOn.newYearsEve.noon";
    private static final String ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_FULL = "application.error.alreadyAbsentOn.newYearsEve.full";

    private static final String ERROR_NOT_ENOUGH_DAYS = "application.error.notEnoughVacationDays";
    private static final String ERROR_NOT_ENOUGH_OVERTIME = "application.error.notEnoughOvertime";
    private static final String ERROR_MISSING_HOURS = "application.error.missingHoursForOvertime";
    private static final String ERROR_INVALID_HOURS = "application.error.invalidHoursForOvertime";

    private static final String ATTRIBUTE_START_DATE = "startDate";
    private static final String ATTRIBUTE_END_DATE = "endDate";
    private static final String ATTRIBUTE_REASON = "reason";
    private static final String ATTRIBUTE_ADDRESS = "address";
    private static final String ATTRIBUTE_COMMENT = "comment";
    private static final String ATTRIBUTE_HOURS = "hours";
    private static final String DAY_LENGTH = "dayLength";

    private final WorkingTimeService workingTimeService;
    private final WorkDaysCountService workDaysCountService;
    private final OverlapService overlapService;
    private final CalculationService calculationService;
    private final SettingsService settingsService;
    private final OvertimeService overtimeService;
    private final Clock clock;

    @Autowired
    public ApplicationForLeaveFormValidator(WorkingTimeService workingTimeService, WorkDaysCountService workDaysCountService,
                                            OverlapService overlapService, CalculationService calculationService, SettingsService settingsService,
                                            OvertimeService overtimeService, Clock clock) {

        this.workingTimeService = workingTimeService;
        this.workDaysCountService = workDaysCountService;
        this.overlapService = overlapService;
        this.calculationService = calculationService;
        this.settingsService = settingsService;
        this.overtimeService = overtimeService;
        this.clock = clock;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ApplicationForLeaveForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        final ApplicationForLeaveForm applicationForm = (ApplicationForLeaveForm) target;
        final Settings settings = settingsService.getSettings();

        // check if date fields are valid
        validateDateFields(applicationForm, settings, errors);

        // check hours
        validateHours(applicationForm, settings, errors);

        validateDayLength(applicationForm, settings, errors);

        // check if reason is not filled
        if (SPECIALLEAVE.equals(applicationForm.getVacationType().getCategory()) && !hasText(applicationForm.getReason())) {
            errors.rejectValue(ATTRIBUTE_REASON, ERROR_MISSING_REASON);
        }

        // validate length of texts
        validateStringLength(applicationForm.getReason(), ATTRIBUTE_REASON, errors);
        validateStringLength(applicationForm.getAddress(), ATTRIBUTE_ADDRESS, errors);
        validateStringLength(applicationForm.getComment(), ATTRIBUTE_COMMENT, errors);

        if (!errors.hasErrors()) {
            // validate if applying for leave is possible
            // (check overlapping applications for leave, vacation days of the person etc.)
            validateIfApplyingForLeaveIsPossible(applicationForm, settings, errors);
        }
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
    }

    private static void validateChristmasEve(DayLength applicationDayLength, WorkingTimeSettings workingTimeSettings, Errors errors) {

        final DayLength workingDurationForChristmasEve = workingTimeSettings.getWorkingDurationForChristmasEve();

        switch (workingDurationForChristmasEve) {
            case ZERO:
                if (applicationDayLength != DayLength.ZERO) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_FULL);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_FULL);
                }
                return;
            case MORNING:
                if (applicationDayLength == NOON) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_NOON);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_NOON);
                }
                return;
            case NOON:
                if (applicationDayLength == MORNING) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_MORNING);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_CHRISTMAS_EVE_MORNING);
                }
                return;
            default:
                // nothing to do here
        }
    }

    private static void validateNewYearsEve(DayLength applicationDayLength, WorkingTimeSettings workingTimeSettings, Errors errors) {

        final DayLength workingDurationForNewYearsEve = workingTimeSettings.getWorkingDurationForNewYearsEve();

        switch (workingDurationForNewYearsEve) {
            case ZERO:
                if (applicationDayLength != DayLength.ZERO) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_FULL);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_FULL);
                }
                return;
            case MORNING:
                if (applicationDayLength == NOON) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_NOON);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_NOON);
                }
                return;
            case NOON:
                if (applicationDayLength == MORNING) {
                    errors.rejectValue(DAY_LENGTH, ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_MORNING);
                    errors.reject(ERROR_ALREADY_ABSENT_ON_NEWYEARS_EVE_MORNING);
                }
                return;
            default:
                // nothing to do here
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
            final  ApplicationSettings applicationSettings = settings.getApplicationSettings();

            validateNotTooFarInTheFuture(endDate, applicationSettings, errors);
            validateNotTooFarInThePast(startDate, applicationSettings, errors);
            validateSameDayIfHalfDayPeriod(startDate, endDate, dayLength, errors);
        }
    }

    private void validateNotTooFarInTheFuture(LocalDate date, ApplicationSettings settings, Errors errors) {

        final Integer maximumMonths = settings.getMaximumMonthsToApplyForLeaveInAdvance();
        final LocalDate future = ZonedDateTime.now(clock).plusMonths(maximumMonths).toLocalDate();

        if (date.isAfter(future)) {
            errors.reject(ERROR_TOO_LONG, new Object[]{settings.getMaximumMonthsToApplyForLeaveInAdvance()}, null);
        }
    }

    private void validateNotTooFarInThePast(LocalDate date, ApplicationSettings settings, Errors errors) {

        final Integer maximumMonths = settings.getMaximumMonthsToApplyForLeaveInAdvance();
        final LocalDate past = ZonedDateTime.now(clock).minusMonths(maximumMonths).toLocalDate();

        if (date.isBefore(past)) {
            errors.reject(ERROR_PAST);
        }
    }

    private void validateSameDayIfHalfDayPeriod(LocalDate startDate, LocalDate endDate, DayLength dayLength, Errors errors) {

        boolean isHalfDay = dayLength == MORNING || dayLength == NOON;

        if (isHalfDay && !startDate.isEqual(endDate)) {
            errors.reject(ERROR_HALF_DAY_PERIOD);
        }
    }

    private void validateTime(Time startTime, Time endTime, Errors errors) {

        final boolean startTimeIsProvided = startTime != null;
        final boolean endTimeIsProvided = endTime != null;

        final boolean onlyStartTimeProvided = startTimeIsProvided && !endTimeIsProvided;
        final boolean onlyEndTimeProvided = !startTimeIsProvided && endTimeIsProvided;

        if (onlyStartTimeProvided || onlyEndTimeProvided) {
            errors.reject(ERROR_PERIOD);
        }

        if (startTimeIsProvided && endTimeIsProvided) {
            final long timeDifference = endTime.getTime() - startTime.getTime();

            if (timeDifference <= 0) {
                errors.reject(ERROR_PERIOD);
            }
        }
    }

    private void validateHours(ApplicationForLeaveForm applicationForLeave, Settings settings, Errors errors) {

        final BigDecimal hours = applicationForLeave.getHours();
        final boolean isOvertime = OVERTIME.equals(applicationForLeave.getVacationType().getCategory());
        final boolean overtimeFunctionIsActive = settings.getOvertimeSettings().isOvertimeActive();
        final boolean hoursRequiredButNotProvided = isOvertime && overtimeFunctionIsActive && hours == null;

        if (hoursRequiredButNotProvided && !errors.hasFieldErrors(ATTRIBUTE_HOURS)) {
            errors.rejectValue(ATTRIBUTE_HOURS, ERROR_MISSING_HOURS);
        }

        if (hours != null && !CalcUtil.isPositive(hours)) {
            errors.rejectValue(ATTRIBUTE_HOURS, ERROR_INVALID_HOURS);
        }
    }

    private void validateStringLength(String text, String field, Errors errors) {

        if (hasText(text) && text.length() > MAX_CHARS) {
            errors.rejectValue(field, ERROR_LENGTH);
        }
    }

    private void validateIfApplyingForLeaveIsPossible(ApplicationForLeaveForm applicationForm, Settings settings, Errors errors) {

        final Application application = applicationForm.generateApplicationForLeave();

        /*
         * Ensure the person has a working time for the period of the application for leave
         */
        if (!personHasWorkingTime(application)) {
            errors.reject(ERROR_WORKING_TIME);

            return;
        }

        /*
         * Ensure that no one applies for leave for a vacation of 0 days
         */
        if (vacationOfZeroDays(application)) {
            errors.reject(ERROR_ZERO_DAYS);

            return;
        }

        /*
         * Ensure that there is no application for leave and no sick note in the same period
         */
        if (vacationIsOverlapping(application)) {
            errors.reject(ERROR_OVERLAP);

            return;
        }

        /*
         * Ensure that the person has enough vacation days left if the vacation type is
         * {@link org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY}
         */
        if (!enoughVacationDaysLeft(application)) {
            errors.reject(ERROR_NOT_ENOUGH_DAYS);
        }

        /*
         * Ensure that the person has enough overtime hours left if the vacation type is
         * {@link org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME}
         */
        if (!enoughOvertimeHoursLeft(application, settings)) {
            errors.reject(ERROR_NOT_ENOUGH_OVERTIME);
        }
    }

    private boolean personHasWorkingTime(Application application) {

        final Optional<WorkingTime> workingTime = workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(
            application.getPerson(), application.getStartDate());

        return workingTime.isPresent();
    }

    private boolean vacationOfZeroDays(Application application) {

        final BigDecimal days = workDaysCountService.getWorkDaysCount(application.getDayLength(), application.getStartDate(),
            application.getEndDate(), application.getPerson());

        return CalcUtil.isZero(days);
    }

    private boolean vacationIsOverlapping(Application application) {

        final OverlapCase overlap = overlapService.checkOverlap(application);

        return overlap == FULLY_OVERLAPPING || overlap == PARTLY_OVERLAPPING;
    }

    private boolean enoughVacationDaysLeft(Application application) {

        final  boolean isHoliday = HOLIDAY.equals(application.getVacationType().getCategory());

        if (isHoliday) {
            return calculationService.checkApplication(application);
        }

        return true;
    }

    private boolean enoughOvertimeHoursLeft(Application application, Settings settings) {

        final boolean isOvertime = OVERTIME.equals(application.getVacationType().getCategory());

        if (isOvertime) {
            final OvertimeSettings overtimeSettings = settings.getOvertimeSettings();
            final boolean overtimeActive = overtimeSettings.isOvertimeActive();

            if (overtimeActive && application.getHours() != null) {
                return checkOvertimeHours(application, overtimeSettings);
            }
        }

        return true;
    }

    private boolean checkOvertimeHours(Application application, OvertimeSettings settings) {

        final BigDecimal minimumOvertime = new BigDecimal(settings.getMinimumOvertime());
        final BigDecimal leftOvertimeForPerson = overtimeService.getLeftOvertimeForPerson(application.getPerson());
        final BigDecimal temporaryOvertimeForPerson = leftOvertimeForPerson.subtract(application.getHours());

        return temporaryOvertimeForPerson.compareTo(minimumOvertime.negate()) >= 0;
    }
}
