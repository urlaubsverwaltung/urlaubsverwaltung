package org.synyx.urlaubsverwaltung.web.validator;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.calendar.OverlapCase;
import org.synyx.urlaubsverwaltung.core.calendar.OverlapService;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.util.CalcUtil;
import org.synyx.urlaubsverwaltung.web.application.ApplicationForLeaveForm;

import java.math.BigDecimal;


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
    private static final String ERROR_MISSING_REASON = "application.error.missingReasonForSpecialLeave";
    private static final String ERROR_PAST = "application.error.tooFarInThePast";
    private static final String ERROR_TOO_LONG = "application.error.tooFarInTheFuture";
    private static final String ERROR_ZERO_DAYS = "application.error.zeroDays";
    private static final String ERROR_OVERLAP = "application.error.overlap";
    private static final String ERROR_NOT_ENOUGH_DAYS = "application.error.notEnoughVacationDays";

    private static final String ATTRIBUTE_START_DATE = "startDate";
    private static final String ATTRIBUTE_END_DATE = "endDate";
    private static final String ATTRIBUTE_START_DATE_HALF = "startDateHalf";
    private static final String ATTRIBUTE_REASON = "reason";
    private static final String ATTRIBUTE_ADDRESS = "address";
    private static final String ATTRIBUTE_COMMENT = "comment";

    private final WorkDaysService calendarService;
    private final OverlapService overlapService;
    private final CalculationService calculationService;
    private final SettingsService settingsService;

    @Autowired
    public ApplicationValidator(WorkDaysService calendarService, OverlapService overlapService,
        CalculationService calculationService, SettingsService settingsService) {

        this.calendarService = calendarService;
        this.overlapService = overlapService;
        this.calculationService = calculationService;
        this.settingsService = settingsService;
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

        // check if reason is not filled
        if (applicationForm.getVacationType() == VacationType.SPECIALLEAVE
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

        if (applicationForLeave.getHowLong() == DayLength.FULL) {
            DateMidnight startDate = applicationForLeave.getStartDate();
            DateMidnight endDate = applicationForLeave.getEndDate();

            validateNotNull(startDate, ATTRIBUTE_START_DATE, errors);
            validateNotNull(endDate, ATTRIBUTE_END_DATE, errors);

            if (startDate != null && endDate != null) {
                validatePeriod(startDate, endDate, errors);
            }
        } else {
            DateMidnight date = applicationForLeave.getStartDateHalf();

            validateNotNull(date, ATTRIBUTE_START_DATE_HALF, errors);

            if (date != null) {
                validatePeriod(date, date, errors);
            }
        }
    }


    private void validateNotNull(DateMidnight date, String field, Errors errors) {

        // may be that date field is null because of cast exception, than there is already a field error
        if (date == null && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }


    private void validatePeriod(DateMidnight startDate, DateMidnight endDate, Errors errors) {

        // ensure that startDate < endDate
        if (startDate.isAfter(endDate)) {
            errors.reject(ERROR_PERIOD);
        } else {
            validateNotTooFarInTheFuture(endDate, errors);
            validateNotTooFarInThePast(startDate, errors);
        }
    }


    private void validateNotTooFarInTheFuture(DateMidnight date, Errors errors) {

        Settings settings = settingsService.getSettings();

        Integer maximumMonths = settings.getMaximumMonthsToApplyForLeaveInAdvance();
        DateMidnight future = DateMidnight.now().plusMonths(maximumMonths);

        if (date.isAfter(future)) {
            errors.reject(ERROR_TOO_LONG,
                new Object[] { settings.getMaximumMonthsToApplyForLeaveInAdvance().toString() }, null);
        }
    }


    private void validateNotTooFarInThePast(DateMidnight date, Errors errors) {

        Settings settings = settingsService.getSettings();

        Integer maximumMonths = settings.getMaximumMonthsToApplyForLeaveInAdvance();
        DateMidnight past = DateMidnight.now().minusMonths(maximumMonths);

        if (date.isBefore(past)) {
            errors.reject(ERROR_PAST);
        }
    }


    private void validateStringLength(String text, String field, Errors errors) {

        if (StringUtils.hasText(text) && text.length() > MAX_CHARS) {
            errors.rejectValue(field, ERROR_LENGTH);
        }
    }


    private void validateIfApplyingForLeaveIsPossible(ApplicationForLeaveForm applicationForm, Errors errors) {

        DayLength dayLength = applicationForm.getHowLong();
        Person person = applicationForm.getPerson();

        BigDecimal days;

        if (dayLength == DayLength.FULL) {
            days = calendarService.getWorkDays(dayLength, applicationForm.getStartDate(), applicationForm.getEndDate(),
                    person);
        } else {
            days = calendarService.getWorkDays(dayLength, applicationForm.getStartDateHalf(),
                    applicationForm.getStartDateHalf(), person);
        }

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
        Application application = applicationForm.generateApplicationForLeave();
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

        boolean isHoliday = applicationForm.getVacationType() == VacationType.HOLIDAY;

        if (isHoliday) {
            boolean enoughVacationDaysLeft = calculationService.checkApplication(
                    applicationForm.generateApplicationForLeave());

            if (!enoughVacationDaysLeft) {
                errors.reject(ERROR_NOT_ENOUGH_DAYS);
            }
        }
    }
}
