package org.synyx.urlaubsverwaltung.overtime.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Validates overtime record.
 */
@Component
public class OvertimeFormValidator implements Validator {

    private static final String ERROR_MANDATORY = "error.entry.mandatory";
    private static final String ERROR_INVALID_PERIOD = "error.entry.invalidPeriod";
    private static final String ERROR_MAX_OVERTIME = "overtime.data.numberOfHours.error.maxOvertime";
    private static final String ERROR_MIN_OVERTIME = "overtime.data.numberOfHours.error.minOvertime";
    private static final String ERROR_OVERTIME_DEACTIVATED = "overtime.record.error.deactivated";

    private static final String ATTRIBUTE_START_DATE = "startDate";
    private static final String ATTRIBUTE_END_DATE = "endDate";

    private final OvertimeService overtimeService;
    private final SettingsService settingsService;

    @Autowired
    OvertimeFormValidator(OvertimeService overtimeService, SettingsService settingsService) {
        this.overtimeService = overtimeService;
        this.settingsService = settingsService;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return OvertimeForm.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final OvertimeForm overtimeForm = (OvertimeForm) target;
        final OvertimeSettings overtimeSettings = settingsService.getSettings().getOvertimeSettings();

        if (!overtimeSettings.isOvertimeActive()) {
            errors.reject(ERROR_OVERTIME_DEACTIVATED);

            // if overtime management is deactivated, no need to execute further validation
            return;
        }

        validatePeriod(overtimeForm, errors);
        validateNumberOfHours(overtimeSettings, overtimeForm, errors);
        validateMaximumOvertimeNotReached(overtimeSettings, overtimeForm, errors);
    }

    private void validatePeriod(OvertimeForm overtimeForm, Errors errors) {

        final LocalDate startDate = overtimeForm.getStartDate();
        final LocalDate endDate = overtimeForm.getEndDate();

        validateDateNotNull(startDate, ATTRIBUTE_START_DATE, errors);
        validateDateNotNull(endDate, ATTRIBUTE_END_DATE, errors);

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            errors.rejectValue(ATTRIBUTE_END_DATE, ERROR_INVALID_PERIOD);
        }
    }

    private void validateDateNotNull(LocalDate date, String field, Errors errors) {
        // may be that date field is null because of cast exception, than there is already a field error
        if (date == null && !errors.hasFieldErrors(field)) {
            errors.rejectValue(field, ERROR_MANDATORY);
        }
    }

    private void validateNumberOfHours(OvertimeSettings overtimeSettings, OvertimeForm overtimeForm, Errors errors) {

        final BigDecimal hours = overtimeForm.getHours();
        final Integer minutes = overtimeForm.getMinutes();

        final boolean overtimeReductionEnabled = overtimeSettings.isOvertimeReductionWithoutApplicationActive();

        if (hours == null && minutes == null) {
            errors.rejectValue("hours", "overtime.error.hoursOrMinutesRequired");
            errors.rejectValue("minutes", "overtime.error.hoursOrMinutesRequired");
        } else if (!overtimeReductionEnabled && overtimeForm.isReduce()) {
            errors.rejectValue("reduce", "overtime.error.overtimeReductionNotAllowed");
        }
    }


    private void validateMaximumOvertimeNotReached(OvertimeSettings overtimeSettings, OvertimeForm overtimeForm, Errors errors) {

        final Duration numberOfHours = overtimeForm.getDuration();
        if (numberOfHours != null) {
            final Duration maximumOvertime = Duration.ofHours(overtimeSettings.getMaximumOvertime());
            final Duration minimumOvertime = Duration.ofHours(overtimeSettings.getMinimumOvertime());

            if (maximumOvertime.isZero()) {
                errors.reject(ERROR_OVERTIME_DEACTIVATED);
                return;
            }

            Duration leftOvertime = overtimeService.getLeftOvertimeForPerson(overtimeForm.getPerson());

            final Long overtimeRecordId = overtimeForm.getId();
            if (overtimeRecordId != null) {
                final Optional<Overtime> overtimeRecordOptional = overtimeService.getOvertimeById(overtimeRecordId);
                if (overtimeRecordOptional.isPresent()) {
                    leftOvertime = leftOvertime.minus(overtimeRecordOptional.get().getDuration());
                }
            }

            // left overtime + overtime record must not be greater than maximum overtime
            if (leftOvertime.plus(numberOfHours).compareTo(maximumOvertime) > 0) {
                // maxOvertime is an Integer -> save to extract hours here
                errors.reject(ERROR_MAX_OVERTIME, new Object[]{maximumOvertime.toHours()}, null);
            }

            // left overtime + overtime record must be greater than minimum overtime
            // are missing hours (means negative)
            if (leftOvertime.plus(numberOfHours).compareTo(minimumOvertime.negated()) < 0) {
                // minimumOvertime is an Integer -> save to extract hours here
                errors.reject(ERROR_MIN_OVERTIME, new Object[]{minimumOvertime.toHours()}, null);
            }
        }
    }
}
