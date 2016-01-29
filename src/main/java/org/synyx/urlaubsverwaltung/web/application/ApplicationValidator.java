package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.calendar.OverlapCase;
import org.synyx.urlaubsverwaltung.core.calendar.OverlapService;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.util.CalcUtil;

import java.math.BigDecimal;

import java.sql.Time;

import java.util.Optional;


/**
 * This class validate if an {@link org.synyx.urlaubsverwaltung.web.application.ApplicationForLeaveForm} is filled
 * correctly by the user, else it saves error messages in errors object.
 *
 * @author  Aljona Murygina
 */
@Component
public class ApplicationValidator implements Validator {

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

    private final WorkingTimeService workingTimeService;
    private final WorkDaysService calendarService;
    private final OverlapService overlapService;
    private final CalculationService calculationService;
    private final SettingsService settingsService;
    private final OvertimeService overtimeService;

    @Autowired
    public ApplicationValidator(WorkingTimeService workingTimeService, WorkDaysService calendarService,
        OverlapService overlapService, CalculationService calculationService, SettingsService settingsService,
                                OvertimeService overtimeService) {

        this.workingTimeService = workingTimeService;
        this.calendarService = calendarService;
        this.overlapService = overlapService;
        this.calculationService = calculationService;
        this.settingsService = settingsService;
        this.overtimeService = overtimeService;
    }

    @Override
    public boolean supports(Class<?> clazz) {

        return ApplicationForLeaveForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        ApplicationForLeaveForm applicationForm = (ApplicationForLeaveForm) target;

        // check if date fields are valid
        validateDateFields(applicationForm, errors);

        // check hours
        validateHours(applicationForm, errors);

        // check if reason is not filled
        if (VacationType.SPECIALLEAVE.equals(applicationForm.getVacationType().getTypeName())
                && !StringUtils.hasText(applicationForm.getReason())) {
            errors.rejectValue(ATTRIBUTE_REASON, ERROR_MISSING_REASON);
        }

        // validate length of texts
        validateStringLength(applicationForm.getReason(), ATTRIBUTE_REASON, errors);
        validateStringLength(applicationForm.getAddress(), ATTRIBUTE_ADDRESS, errors);
        validateStringLength(applicationForm.getComment(), ATTRIBUTE_COMMENT, errors);

        if (!errors.hasErrors()) {
            // validate if applying for leave is possible
            // (check overlapping applications for leave, vacation days of the person etc.)
            validateIfApplyingForLeaveIsPossible(applicationForm, errors);
        }
    }


    private void validateDateFields(ApplicationForLeaveForm applicationForLeave, Errors errors) {

        DateMidnight startDate = applicationForLeave.getStartDate();
        DateMidnight endDate = applicationForLeave.getEndDate();

        validateNotNull(startDate, ATTRIBUTE_START_DATE, errors);
        validateNotNull(endDate, ATTRIBUTE_END_DATE, errors);

        if (startDate != null && endDate != null) {
            validatePeriod(startDate, endDate, applicationForLeave.getDayLength(), errors);
            validateTime(applicationForLeave.getStartTime(), applicationForLeave.getEndTime(), errors);
        }
    }


    private void validateNotNull(DateMidnight date, String field, Errors errors) {

        // may be that date field is null because of cast exception, than there is already a field error
        if (date == null && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }


    private void validatePeriod(DateMidnight startDate, DateMidnight endDate, DayLength dayLength, Errors errors) {

        // ensure that startDate < endDate
        if (startDate.isAfter(endDate)) {
            errors.reject(ERROR_PERIOD);
        } else {
            Settings settings = settingsService.getSettings();
            AbsenceSettings absenceSettings = settings.getAbsenceSettings();

            validateNotTooFarInTheFuture(endDate, absenceSettings, errors);
            validateNotTooFarInThePast(startDate, absenceSettings, errors);
            validateSameDayIfHalfDayPeriod(startDate, endDate, dayLength, errors);
        }
    }


    private void validateNotTooFarInTheFuture(DateMidnight date, AbsenceSettings settings, Errors errors) {

        Integer maximumMonths = settings.getMaximumMonthsToApplyForLeaveInAdvance();
        DateMidnight future = DateMidnight.now().plusMonths(maximumMonths);

        if (date.isAfter(future)) {
            errors.reject(ERROR_TOO_LONG,
                new Object[] { settings.getMaximumMonthsToApplyForLeaveInAdvance().toString() }, null);
        }
    }


    private void validateNotTooFarInThePast(DateMidnight date, AbsenceSettings settings, Errors errors) {

        Integer maximumMonths = settings.getMaximumMonthsToApplyForLeaveInAdvance();
        DateMidnight past = DateMidnight.now().minusMonths(maximumMonths);

        if (date.isBefore(past)) {
            errors.reject(ERROR_PAST);
        }
    }


    private void validateSameDayIfHalfDayPeriod(DateMidnight startDate, DateMidnight endDate, DayLength dayLength,
        Errors errors) {

        boolean isHalfDay = dayLength == DayLength.MORNING || dayLength == DayLength.NOON;

        if (isHalfDay && !startDate.isEqual(endDate)) {
            errors.reject(ERROR_HALF_DAY_PERIOD);
        }
    }


    private void validateTime(Time startTime, Time endTime, Errors errors) {

        boolean startTimeIsProvided = startTime != null;
        boolean endTimeIsProvided = endTime != null;

        if ((startTimeIsProvided && !endTimeIsProvided) || (!startTimeIsProvided && endTimeIsProvided)) {
            errors.reject(ERROR_PERIOD);
        }

        if (startTimeIsProvided && endTimeIsProvided) {
            long timeDifference = endTime.getTime() - startTime.getTime();

            if (timeDifference <= 0) {
                errors.reject(ERROR_PERIOD);
            }
        }
    }


    private void validateHours(ApplicationForLeaveForm applicationForLeave, Errors errors) {

        BigDecimal hours = applicationForLeave.getHours();
        boolean isOvertime = VacationType.OVERTIME.equals(applicationForLeave.getVacationType().getTypeName());

        if (isOvertime && hours == null && !errors.hasFieldErrors(ATTRIBUTE_HOURS)) {
            errors.rejectValue(ATTRIBUTE_HOURS, ERROR_MISSING_HOURS);
        }

        if (hours != null && !CalcUtil.isPositive(hours)) {
            errors.rejectValue(ATTRIBUTE_HOURS, ERROR_INVALID_HOURS);
        }
    }


    private void validateStringLength(String text, String field, Errors errors) {

        if (StringUtils.hasText(text) && text.length() > MAX_CHARS) {
            errors.rejectValue(field, ERROR_LENGTH);
        }
    }


    private void validateIfApplyingForLeaveIsPossible(ApplicationForLeaveForm applicationForm, Errors errors) {

        Application application = applicationForm.generateApplicationForLeave();
        Person person = application.getPerson();

        /**
         * Ensure the person has a working time for the period of the application for leave
         */
        Optional<WorkingTime> workingTime = workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(person,
                application.getStartDate());

        if (!workingTime.isPresent()) {
            errors.reject(ERROR_WORKING_TIME);

            return;
        }

        /**
         * Calculate the work days
         */
        BigDecimal days = calendarService.getWorkDays(application.getDayLength(), application.getStartDate(),
                application.getEndDate(), person);

        /**
         * Ensure that no one applies for leave for a vacation of 0 days
         */
        if (CalcUtil.isZero(days)) {
            errors.reject(ERROR_ZERO_DAYS);

            return;
        }

        /**
         * Ensure that there is no application for leave and no sick note in the same period
         */
        OverlapCase overlap = overlapService.checkOverlap(application);

        boolean isOverlapping = overlap == OverlapCase.FULLY_OVERLAPPING || overlap == OverlapCase.PARTLY_OVERLAPPING;

        if (isOverlapping) {
            errors.reject(ERROR_OVERLAP);

            return;
        }

        /**
         * Ensure that the person has enough vacation days left if the vacation type is
         * {@link org.synyx.urlaubsverwaltung.core.application.domain.VacationType.HOLIDAY}
         */

        boolean isHoliday = VacationType.HOLIDAY.equals(application.getVacationType().getTypeName());

        if (isHoliday) {
            boolean enoughVacationDaysLeft = calculationService.checkApplication(application);

            if (!enoughVacationDaysLeft) {
                errors.reject(ERROR_NOT_ENOUGH_DAYS);
            }
        }


        /**
         * Check overtime of given user
         */
        Boolean overtimeActive = settingsService.getSettings().getWorkingTimeSettings().getOvertimeActive();
        Boolean isOvertime = VacationType.OVERTIME.equals(application.getVacationType());

        if (isOvertime && overtimeActive) {
            boolean enoughOvertimeHours = checkOvertimeHours(application);

            if (!enoughOvertimeHours) {
                errors.reject(ERROR_NOT_ENOUGH_OVERTIME);
            }
        }


    }

    private boolean checkOvertimeHours(Application application) {

        Settings settings = settingsService.getSettings();
        BigDecimal minimumOvertime = new BigDecimal(settings.getWorkingTimeSettings().getMinimumOvertime());
        BigDecimal leftOvertimeForPerson = overtimeService.getLeftOvertimeForPerson(application.getPerson());

        BigDecimal temporaryOvertimeForPerson = leftOvertimeForPerson.subtract(application.getHours());

        return (temporaryOvertimeForPerson.compareTo(minimumOvertime.negate()) >= 0);
    }
}
