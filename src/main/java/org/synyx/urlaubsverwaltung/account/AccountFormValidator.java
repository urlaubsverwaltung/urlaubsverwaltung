package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.CachedSupplier;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * Validates {@link AccountForm}.
 */
@Component
class AccountFormValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INTEGER_FIELD = "error.entry.integer";
    private static final String ERROR_FULL_OR_HALF_NUMBER = "error.entry.fullOrHalfNumber";

    private static final String ATTRIBUTE_ANNUAL_VACATION_DAYS = "annualVacationDays";
    private static final String ATTRIBUTE_ACTUAL_VACATION_DAYS = "actualVacationDays";
    private static final String ATTRIBUTE_REMAINING_VACATION_DAYS = "remainingVacationDays";
    private static final String ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING = "remainingVacationDaysNotExpiring";
    private static final String ATTR_HOLIDAYS_ACCOUNT_VALID_FROM = "holidaysAccountValidFrom";
    private static final String ATTR_HOLIDAYS_ACCOUNT_VALID_TO = "holidaysAccountValidTo";
    private static final String ATTR_HOLIDAYS_ACCOUNT_EXPIRY_DATE_LOCALLY = "expiryDateLocally";
    private static final String ERROR_ENTRY_MIN = "error.entry.min";

    private final SettingsService settingsService;

    @Autowired
    AccountFormValidator(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return AccountForm.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final Supplier<Settings> settingsSupplier = new CachedSupplier<>(this::getSettings);
        final BigDecimal maxAnnualVacationDays = getMaximumAnnualVacationDays(settingsSupplier);

        final AccountForm form = (AccountForm) target;
        validatePeriod(form, errors);
        validateAnnualVacation(form, errors, maxAnnualVacationDays, settingsSupplier);
        validateActualVacation(form, errors);
        validateExpiryDateLocally(form, errors, settingsSupplier);
        validateRemainingVacationDays(form, errors);
        validateRemainingVacationDaysNotExpiring(form, errors);
    }

    void validatePeriod(AccountForm form, Errors errors) {

        final LocalDate holidaysAccountValidFrom = form.getHolidaysAccountValidFrom();
        final LocalDate holidaysAccountValidTo = form.getHolidaysAccountValidTo();

        validateDateNotNull(holidaysAccountValidFrom, ATTR_HOLIDAYS_ACCOUNT_VALID_FROM, errors);
        validateDateNotNull(holidaysAccountValidTo, ATTR_HOLIDAYS_ACCOUNT_VALID_TO, errors);

        if (holidaysAccountValidFrom != null && holidaysAccountValidTo != null) {
            final int year = form.getHolidaysAccountYear();
            final int fromYear = holidaysAccountValidFrom.getYear();
            final int toYear = holidaysAccountValidTo.getYear();
            final boolean invalidYear = fromYear != year || toYear != year;

            if (invalidYear) {
                if (fromYear != year) {
                    reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_FROM, msg("holidaysAccountValidFrom.invalidYear"), String.valueOf(year));
                }
                if (toYear != year) {
                    reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_TO, msg("holidaysAccountValidTo.invalidYear"), String.valueOf(year));
                }
            }

            boolean periodIsOnlyOneDay = holidaysAccountValidFrom.equals(holidaysAccountValidTo);
            if (periodIsOnlyOneDay) {
                reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_FROM, msg("holidaysAccountValidFrom.invalidRange"));
                reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_TO, msg("holidaysAccountValidTo.invalidRange"));
            }

            boolean beginOfPeriodIsAfterEndOfPeriod = holidaysAccountValidFrom.isAfter(holidaysAccountValidTo);
            if (beginOfPeriodIsAfterEndOfPeriod) {
                reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_FROM, msg("holidaysAccountValidFrom.invalidRangeReversed"));
                reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_TO, msg("holidaysAccountValidTo.invalidRangeReversed"));
            }
        }
    }

    void validateAnnualVacation(AccountForm form, Errors errors, BigDecimal maxAnnualVacationDays, Supplier<Settings> settingsSupplier) {

        final BigDecimal annualVacationDays = form.getAnnualVacationDays();
        validateNumberNotNull(annualVacationDays, ATTRIBUTE_ANNUAL_VACATION_DAYS, errors);

        if (annualVacationDays != null) {

            if (isAllowHalfDaysActive(settingsSupplier)) {
                validateFullOrHalfDay(annualVacationDays, ATTRIBUTE_ANNUAL_VACATION_DAYS, errors);
            } else {
                validateIsInteger(annualVacationDays, ATTRIBUTE_ANNUAL_VACATION_DAYS, errors);
            }

            if (isNegative(annualVacationDays)) {
                reject(errors, ATTRIBUTE_ANNUAL_VACATION_DAYS, ERROR_ENTRY_MIN, "0");
            } else if (isGreater(annualVacationDays, maxAnnualVacationDays)) {
                reject(errors, ATTRIBUTE_ANNUAL_VACATION_DAYS, "error.entry.max", maxAnnualVacationDays);
            }
        }
    }

    void validateActualVacation(AccountForm form, Errors errors) {

        final BigDecimal actualVacationDays = form.getActualVacationDays();
        validateNumberNotNull(actualVacationDays, ATTRIBUTE_ACTUAL_VACATION_DAYS, errors);

        if (actualVacationDays != null) {

            validateFullOrHalfDay(actualVacationDays, ATTRIBUTE_ACTUAL_VACATION_DAYS, errors);

            final BigDecimal annualVacationDays = form.getAnnualVacationDays();

            if (isNegative(actualVacationDays)) {
                reject(errors, ATTRIBUTE_ACTUAL_VACATION_DAYS, ERROR_ENTRY_MIN, "0");
            } else if (isGreater(actualVacationDays, annualVacationDays)) {
                reject(errors, ATTRIBUTE_ACTUAL_VACATION_DAYS, "error.entry.max", annualVacationDays);
            }
        }
    }

    void validateExpiryDateLocally(AccountForm form, Errors errors, Supplier<Settings> settingsSupplier) {
        if (!form.doRemainingVacationDaysExpire()) {
            // feature disabled. nothing has to be validated.
            return;
        }

        final LocalDate expiryDateLocally = form.getExpiryDateLocally();
        final boolean expireGlobally = settingsSupplier.get().getAccountSettings().isDoRemainingVacationDaysExpireGlobally();

        if (!expireGlobally) {
            // must not be null when expiryDate overrides global one
            validateDateNotNull(expiryDateLocally, ATTR_HOLIDAYS_ACCOUNT_EXPIRY_DATE_LOCALLY, errors);
        }

        if (expiryDateLocally != null) {
            final int year = form.getHolidaysAccountYear();
            final int expiryYear = expiryDateLocally.getYear();

            if (expiryYear != year) {
                reject(errors, ATTR_HOLIDAYS_ACCOUNT_EXPIRY_DATE_LOCALLY, msg("expiryDateLocally.invalidYear"), String.valueOf(year));
            }
        }
    }

    void validateRemainingVacationDays(AccountForm form, Errors errors) {

        final BigDecimal remainingVacationDays = form.getRemainingVacationDays();

        validateNumberNotNull(remainingVacationDays, ATTRIBUTE_REMAINING_VACATION_DAYS, errors);

        if (remainingVacationDays != null) {
            validateFullOrHalfDay(remainingVacationDays, ATTRIBUTE_REMAINING_VACATION_DAYS, errors);
            if (isNegative(remainingVacationDays)) {
                reject(errors, ATTRIBUTE_REMAINING_VACATION_DAYS, ERROR_ENTRY_MIN, "0");
            }
        }
    }

    void validateRemainingVacationDaysNotExpiring(AccountForm form, Errors errors) {
        if (!form.doRemainingVacationDaysExpire()) {
            // feature disabled. nothing has to be validated.
            return;
        }

        final BigDecimal remainingVacationDays = form.getRemainingVacationDays();
        final BigDecimal remainingVacationDaysNotExpiring = form.getRemainingVacationDaysNotExpiring();

        if (remainingVacationDays == null && remainingVacationDaysNotExpiring == null) {
            reject(errors, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, ERROR_MANDATORY_FIELD);
        }

        if (remainingVacationDays != null) {
            if (remainingVacationDaysNotExpiring == null) {
                reject(errors, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, ERROR_MANDATORY_FIELD);
            } else {
                validateFullOrHalfDay(remainingVacationDaysNotExpiring, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, errors);
                if (isNegative(remainingVacationDaysNotExpiring)) {
                    reject(errors, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, ERROR_ENTRY_MIN, "0");
                } else if (isGreater(remainingVacationDaysNotExpiring, remainingVacationDays)) {
                    reject(errors, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, msg("remainingVacationDaysNotExpiring.tooBig"), remainingVacationDays);
                }
            }
        }
    }

    private void validateFullOrHalfDay(BigDecimal days, String field, Errors errors) {

        final int scale = days.stripTrailingZeros().scale();
        final String decimal = days.subtract(new BigDecimal(days.intValue())).toPlainString();
        final boolean isFullOrHalfAnHour = scale <= 1 && (decimal.equals("0") || decimal.startsWith("0.0") || decimal.startsWith("0.5"));

        if (!isFullOrHalfAnHour && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_FULL_OR_HALF_NUMBER);
        }
    }

    private void validateDateNotNull(LocalDate date, String field, Errors errors) {

        // may be that date field is null because of cast exception, than there is already a field error
        if (date == null && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }

    private void validateIsInteger(BigDecimal days, String field, Errors errors) {

        final boolean isValid = days.stripTrailingZeros().scale() <= 0;

        if (!isValid && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_INTEGER_FIELD);
        }
    }

    private void validateNumberNotNull(BigDecimal number, String field, Errors errors) {

        // may be that number field is null because of cast exception, than there is already a field error
        final boolean isValid = number != null;

        if (!isValid && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }

    private boolean isNegative(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) < 0;
    }

    private boolean isGreater(BigDecimal first, BigDecimal second) {
        return first.compareTo(second) > 0;
    }

    private void reject(Errors errors, String field, String errorCode, Object... messageAttributes) {
        if (messageAttributes.length == 0) {
            errors.rejectValue(field, errorCode);
        } else {
            errors.rejectValue(field, errorCode, messageAttributes, "");
        }
    }

    private static String msg(String key) {
        return "person.form.annualVacation.error." + key;
    }

    private static BigDecimal getMaximumAnnualVacationDays(Supplier<Settings> settingsSupplier) {
        final AccountSettings accountSettings = settingsSupplier.get().getAccountSettings();
        return BigDecimal.valueOf(accountSettings.getMaximumAnnualVacationDays());
    }

    private static boolean isAllowHalfDaysActive(Supplier<Settings> settingsSupplier) {
        final ApplicationSettings applicationSettings = settingsSupplier.get().getApplicationSettings();
        return applicationSettings.isAllowHalfDays();
    }

    private Settings getSettings() {
        return settingsService.getSettings();
    }
}
