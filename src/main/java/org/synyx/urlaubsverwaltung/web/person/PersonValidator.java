
package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.web.MailAddressValidationUtil;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;


/**
 * This class validate if a {@link PersonForm} is filled correctly by the user, else it saves error messages in errors
 * object.
 *
 * @author  Aljona Murygina
 */
@Component
public class PersonValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_ENTRY = "error.entry.invalid";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";
    private static final String ERROR_EMAIL = "error.entry.mail";
    private static final String ERROR_LOGIN_UNIQUE = "person.form.data.login.error";

    private static final String ATTRIBUTE_LOGIN_NAME = "loginName";
    private static final String ATTRIBUTE_FIRST_NAME = "firstName";
    private static final String ATTRIBUTE_LAST_NAME = "lastName";
    private static final String ATTRIBUTE_ANNUAL_VACATION_DAYS = "annualVacationDays";
    private static final String ATTRIBUTE_REMAINING_VACATION_DAYS = "remainingVacationDays";
    private static final String ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING = "remainingVacationDaysNotExpiring";
    private static final String ATTRIBUTE_EMAIL = "email";
    private static final String ATTRIBUTE_PERMISSIONS = "permissions";

    private static final int MAX_CHARS = 50;

    private final PersonService personService;
    private final SettingsService settingsService;

    @Autowired
    public PersonValidator(PersonService personService, SettingsService settingsService) {

        this.personService = personService;
        this.settingsService = settingsService;
    }

    @Override
    public boolean supports(Class<?> clazz) {

        return PersonForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        PersonForm form = (PersonForm) target;

        validateName(form.getFirstName(), ATTRIBUTE_FIRST_NAME, errors);

        validateName(form.getLastName(), ATTRIBUTE_LAST_NAME, errors);

        validateEmail(form.getEmail(), errors);

        validatePeriod(form, errors);

        validateValidFrom(form, errors);

        validateAnnualVacation(form, errors);

        validateRemainingVacationDays(form, errors);

        validatePermissions(form, errors);

        validateNotifications(form, errors);

        validateWorkingTimes(form, errors);
    }


    /**
     * This method ensures that the field firstName and the field lastName are not {@code null} or empty and not too
     * long.
     *
     * @param  name  (may be the field firstName or lastName)
     * @param  field
     * @param  errors
     */
    protected void validateName(String name, String field, Errors errors) {

        // is the name field null/empty?
        if (!StringUtils.hasText(name)) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        } else {
            // is String length alright?
            if (!validateStringLength(name)) {
                errors.rejectValue(field, ERROR_LENGTH);
            }
        }
    }


    public void validateLogin(String login, Errors errors) {

        validateName(login, ATTRIBUTE_LOGIN_NAME, errors);

        if (!errors.hasFieldErrors(ATTRIBUTE_LOGIN_NAME)) {
            // validate unique login name
            Optional<Person> person = personService.getPersonByLogin(login);

            if (person.isPresent()) {
                errors.rejectValue(ATTRIBUTE_LOGIN_NAME, ERROR_LOGIN_UNIQUE);
            }
        }
    }


    /**
     * This method checks if the field email is filled and if it is filled, it validates the entry with a regex.
     *
     * @param  email
     * @param  errors
     */
    protected void validateEmail(String email, Errors errors) {

        // is email field null or empty
        if (!StringUtils.hasText(email)) {
            errors.rejectValue(ATTRIBUTE_EMAIL, ERROR_MANDATORY_FIELD);
        } else {
            // String length alright?
            if (!validateStringLength(email)) {
                errors.rejectValue(ATTRIBUTE_EMAIL, ERROR_LENGTH);
            }

            if (!MailAddressValidationUtil.hasValidFormat(email)) {
                errors.rejectValue(ATTRIBUTE_EMAIL, ERROR_EMAIL);
            }
        }
    }


    protected void validatePeriod(PersonForm form, Errors errors) {

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
                errors.reject("error.entry.invalidPeriod");
            }
        }
    }


    private void validateDateNotNull(DateMidnight date, String field, Errors errors) {

        // may be that date field is null because of cast exception, than there is already a field error
        if (date == null && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }


    private void validateValidFrom(PersonForm form, Errors errors) {

        validateDateNotNull(form.getValidFrom(), "validFrom", errors);
    }


    protected void validateAnnualVacation(PersonForm form, Errors errors) {

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


    protected void validateRemainingVacationDays(PersonForm form, Errors errors) {

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


    /**
     * This method validates if the holiday entitlement's fields remaining vacation days and vacation days are filled
     * and if they are filled, it checks if the number of days is realistic.
     *
     * @param  days
     * @param  field
     * @param  maximumDays
     * @param  errors
     */
    private void validateNumberOfDays(BigDecimal days, String field, BigDecimal maximumDays, Errors errors) {

        // is number of days < 0 ?
        if (days.compareTo(BigDecimal.ZERO) == -1) {
            errors.rejectValue(field, ERROR_ENTRY);
        }

        // is number of days unrealistic?
        if (days.compareTo(maximumDays) == 1) {
            errors.rejectValue(field, ERROR_ENTRY);
        }
    }


    /**
     * Checks if a String has a valid length.
     *
     * @param  string
     *
     * @return
     */
    protected boolean validateStringLength(String string) {

        return string.length() <= MAX_CHARS;
    }


    protected void validatePermissions(PersonForm personForm, Errors errors) {

        List<Role> roles = personForm.getPermissions();

        if (roles == null || roles.isEmpty()) {
            errors.rejectValue(ATTRIBUTE_PERMISSIONS, "person.form.permissions.error.mandatory");
        } else {
            // if role inactive set, then only this role may be selected
            if (roles.contains(Role.INACTIVE) && roles.size() != 1) {
                errors.rejectValue(ATTRIBUTE_PERMISSIONS, "person.form.permissions.error.inactive");
            }

            if (roles.contains(Role.DEPARTMENT_HEAD) && roles.contains(Role.BOSS)) {
                errors.rejectValue(ATTRIBUTE_PERMISSIONS, "person.form.permissions.error.departmentHead");
            }

            if (roles.contains(Role.SECOND_STAGE_AUTHORITY) && roles.contains(Role.BOSS)) {
                errors.rejectValue(ATTRIBUTE_PERMISSIONS, "person.form.permissions.error.secondStage");
            }
        }
    }


    protected void validateNotifications(PersonForm personForm, Errors errors) {

        List<Role> roles = personForm.getPermissions();
        List<MailNotification> notifications = personForm.getNotifications();

        if (roles != null) {
            boolean departmentHeadNotificationsSelectedButNotDepartmentHeadRole = notifications.contains(
                    MailNotification.NOTIFICATION_DEPARTMENT_HEAD) && !roles.contains(Role.DEPARTMENT_HEAD);
            boolean secondStageNotificationsSelectedButNotSecondStageRole = notifications.contains(
                    MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY)
                && !roles.contains(Role.SECOND_STAGE_AUTHORITY);
            boolean bossNotificationsSelectedButNotBossRole = notifications.contains(MailNotification.NOTIFICATION_BOSS)
                && !roles.contains(Role.BOSS);
            boolean officeNotificationsSelectedButNotOfficeRole = notifications.contains(
                    MailNotification.NOTIFICATION_OFFICE) && !roles.contains(Role.OFFICE);

            if (departmentHeadNotificationsSelectedButNotDepartmentHeadRole
                    || secondStageNotificationsSelectedButNotSecondStageRole || bossNotificationsSelectedButNotBossRole
                    || officeNotificationsSelectedButNotOfficeRole) {
                errors.rejectValue("notifications", "person.form.notifications.error.combination");
            }
        }
    }


    protected void validateWorkingTimes(PersonForm personForm, Errors errors) {

        if (personForm.getWorkingDays() == null || personForm.getWorkingDays().isEmpty()) {
            errors.rejectValue("workingDays", "person.form.workingTime.error.mandatory");
        }
    }
}
