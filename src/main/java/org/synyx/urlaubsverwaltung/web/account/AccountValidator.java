package org.synyx.urlaubsverwaltung.web.account;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.core.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;

import java.math.BigDecimal;

/**
 * Validates {@link AccountForm}.
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
@Component
class AccountValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_ENTRY = "error.entry.invalid";
    private static final String ERROR_PERIOD = "error.entry.invalidPeriod";
    private static final String ERROR_COMMENT_TO_LONG = "error.entry.commentTooLong";

    private static final String ATTRIBUTE_ANNUAL_VACATION_DAYS = "annualVacationDays";
    private static final String ATTRIBUTE_ACTUAL_VACATION_DAYS = "actualVacationDays";
    private static final String ATTRIBUTE_REMAINING_VACATION_DAYS = "remainingVacationDays";
    private static final String ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING = "remainingVacationDaysNotExpiring";
    private static final String ATTRIBUTE_COMMENT = "comment";

    private final SettingsService settingsService;

    @Autowired
    AccountValidator(SettingsService settingsService) {

        this.settingsService = settingsService;
    }

    @Override
    public boolean supports(Class<?> clazz) {

        return AccountForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        AccountForm form = (AccountForm) target;

        validatePeriod(form, errors);

        validateAnnualVacation(form, errors);

        validateActualVacation(form, errors);

        validateRemainingVacationDays(form, errors);

        validateComment(form, errors);
    }

    void validateComment(AccountForm form, Errors errors) {

        String comment = form.getComment();

        if (comment != null && comment.length() > 200) {
            errors.rejectValue(ATTRIBUTE_COMMENT, ERROR_COMMENT_TO_LONG);
        }
    }

    void validatePeriod(AccountForm form, Errors errors) {

        DateMidnight holidaysAccountValidFrom = form.getHolidaysAccountValidFrom();
        DateMidnight holidaysAccountValidTo = form.getHolidaysAccountValidTo();

        validateDateNotNull(holidaysAccountValidFrom, "holidaysAccountValidFrom", errors);
        validateDateNotNull(holidaysAccountValidTo, "holidaysAccountValidTo", errors);

        if (holidaysAccountValidFrom != null && holidaysAccountValidTo != null) {
            boolean periodIsNotWithinOneYear = holidaysAccountValidFrom.getYear() != form.getHolidaysAccountYear()
                || holidaysAccountValidTo.getYear() != form.getHolidaysAccountYear();
            boolean periodIsOnlyOneDay = holidaysAccountValidFrom.equals(holidaysAccountValidTo);
            boolean beginOfPeriodIsAfterEndOfPeriod = holidaysAccountValidFrom.isAfter(holidaysAccountValidTo);

            if (periodIsNotWithinOneYear || periodIsOnlyOneDay || beginOfPeriodIsAfterEndOfPeriod) {
                errors.reject(ERROR_PERIOD);
            }
        }
    }

    private void validateDateNotNull(DateMidnight date, String field, Errors errors) {

        // may be that date field is null because of cast exception, than there is already a field error
        if (date == null && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }

    void validateAnnualVacation(AccountForm form, Errors errors) {

        BigDecimal annualVacationDays = form.getAnnualVacationDays();
        Settings settings = settingsService.getSettings();
        AbsenceSettings absenceSettings = settings.getAbsenceSettings();
        BigDecimal maxDays = BigDecimal.valueOf(absenceSettings.getMaximumAnnualVacationDays());

        validateNumberNotNull(annualVacationDays, ATTRIBUTE_ANNUAL_VACATION_DAYS, errors);

        if (annualVacationDays != null) {
            validateNumberOfDays(annualVacationDays, ATTRIBUTE_ANNUAL_VACATION_DAYS, maxDays, errors);
        }
    }

    private void validateNumberNotNull(BigDecimal number, String field, Errors errors) {

        // may be that number field is null because of cast exception, than there is already a field error
        if (number == null && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }

    private void validateNumberOfDays(BigDecimal days, String field, BigDecimal maximumDays, Errors errors) {

        // is number of days < 0 ?
        if (days.compareTo(BigDecimal.ZERO) < 0) {
            errors.rejectValue(field, ERROR_ENTRY);
        }

        // is number of days unrealistic?
        if (days.compareTo(maximumDays) > 0) {
            errors.rejectValue(field, ERROR_ENTRY);
        }
    }

    void validateActualVacation(AccountForm form, Errors errors) {

        BigDecimal actualVacationDays = form.getActualVacationDays();

        validateNumberNotNull(actualVacationDays, ATTRIBUTE_ACTUAL_VACATION_DAYS, errors);

        if (actualVacationDays != null) {
            BigDecimal annualVacationDays = form.getAnnualVacationDays();

            validateNumberOfDays(actualVacationDays, ATTRIBUTE_ACTUAL_VACATION_DAYS, annualVacationDays, errors);
        }
    }

    void validateRemainingVacationDays(AccountForm form, Errors errors) {

        Settings settings = settingsService.getSettings();
        AbsenceSettings absenceSettings = settings.getAbsenceSettings();
        BigDecimal maxDays = BigDecimal.valueOf(absenceSettings.getMaximumAnnualVacationDays());

        BigDecimal remainingVacationDays = form.getRemainingVacationDays();
        BigDecimal remainingVacationDaysNotExpiring = form.getRemainingVacationDaysNotExpiring();

        validateNumberNotNull(remainingVacationDays, ATTRIBUTE_REMAINING_VACATION_DAYS, errors);
        validateNumberNotNull(remainingVacationDaysNotExpiring, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, errors);

        if (remainingVacationDays != null) {
            // field entitlement's remaining vacation days
            validateNumberOfDays(remainingVacationDays, ATTRIBUTE_REMAINING_VACATION_DAYS, maxDays, errors);

            if (remainingVacationDaysNotExpiring != null) {
                validateNumberOfDays(remainingVacationDaysNotExpiring, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING,
                    remainingVacationDays, errors);
            }
        }
    }
}
