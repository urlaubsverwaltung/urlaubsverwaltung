
package org.synyx.urlaubsverwaltung.web.validator;

import org.joda.time.DateMidnight;
import org.joda.time.IllegalFieldValueException;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.util.NumberUtil;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.math.BigDecimal;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class validate if a {@link PersonForm} is filled correctly by the user, else it saves error messages in errors
 * object.
 *
 * @author  Aljona Murygina
 */
@Component
public class PersonValidator implements Validator {

    private static final String MANDATORY_FIELD = "error.mandatory.field";
    private static final String ERROR_ENTRY = "error.entry";
    private static final String ERROR_EMAIL = "error.email";
    private static final String ERROR_LENGTH = "error.length";
    private static final String ERROR_LOGIN_UNIQUE = "error.login.unique";

    private static final String LOGIN_NAME = "loginName";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String ANNUAL_VACATION_DAYS = "annualVacationDays";
    private static final String REMAINING_VACATION_DAYS = "remainingVacationDays";
    private static final String YEAR = "year";
    private static final String EMAIL = "email";
    private static final String PERMISSIONS = "permissions";

    // a regex for email addresses that are valid, but may be "strange looking" (e.g. tomr$2@example.com)
    // original from: http://www.markussipila.info/pub/emailvalidator.php?action=validate
    // modified by adding following characters: äöüß
    private static final String EMAIL_PATTERN =
        "^[a-zäöüß0-9,!#\\$%&'\\*\\+/=\\?\\^_`\\{\\|}~-]+(\\.[a-zäöüß0-9,!#\\$%&'\\*\\+/=\\?\\^_`\\{\\|}~-]+)*@"
        + "[a-zäöüß0-9-]+(\\.[a-zäöüß0-9-]+)*\\.([a-z]{2,})$";

    private static final int MAX_LIMIT_OF_YEARS = 10;
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

        validateName(form.getFirstName(), FIRST_NAME, errors);

        validateName(form.getLastName(), LAST_NAME, errors);

        validateEmail(form.getEmail(), errors);

        validateYear(form.getYear(), errors);

        validatePeriod(form, errors);

        if (form.getValidFrom() == null) {
            errors.rejectValue("validFrom", MANDATORY_FIELD);
        }

        validateAnnualVacation(form, errors, form.getLocale());

        validateRemainingVacationDays(form, errors, form.getLocale());

        validatePermissions(form, errors);

        validateNotifications(form, errors);
    }


    private boolean matchPattern(String nameOfPattern, String matchSequence) {

        Pattern pattern = Pattern.compile(nameOfPattern);
        Matcher matcher = pattern.matcher(matchSequence);

        return matcher.matches();
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
            errors.rejectValue(field, MANDATORY_FIELD);
        } else {
            // is String length alright?
            if (!validateStringLength(name)) {
                errors.rejectValue(field, ERROR_LENGTH);
            }
        }
    }


    public void validateLogin(String login, Errors errors) {

        validateName(login, LOGIN_NAME, errors);

        if (!errors.hasFieldErrors(LOGIN_NAME)) {
            // validate unique login name
            if (personService.getPersonByLogin(login) != null) {
                errors.rejectValue(LOGIN_NAME, ERROR_LOGIN_UNIQUE);
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
            errors.rejectValue(EMAIL, MANDATORY_FIELD);
        } else {
            // String length alright?
            if (!validateStringLength(email)) {
                errors.rejectValue(EMAIL, ERROR_LENGTH);
            }

            // validation with regex
            String normalizedEmail = email.trim().toLowerCase();

            if (!matchPattern(EMAIL_PATTERN, normalizedEmail)) {
                errors.rejectValue(EMAIL, ERROR_EMAIL);
            }
        }
    }


    /**
     * This method checks if the field year is filled and if it is filled, it checks if the year entry makes sense (at
     * the moment: from 2010 - 2030 alright)
     *
     * @param  yearForm
     * @param  errors
     */
    protected void validateYear(String yearForm, Errors errors) {

        // is year field filled?
        if (!StringUtils.hasText(yearForm)) {
            errors.rejectValue(YEAR, MANDATORY_FIELD);
        } else {
            try {
                int year = Integer.parseInt(yearForm);

                int now = DateMidnight.now().getYear();

                if (year < (now - MAX_LIMIT_OF_YEARS + 1) || year > (now + MAX_LIMIT_OF_YEARS)) {
                    errors.rejectValue(YEAR, ERROR_ENTRY);
                }
            } catch (NumberFormatException ex) {
                errors.rejectValue(YEAR, ERROR_ENTRY);
            }
        }
    }


    /**
     * Validates that from date is before to date, i.e. is a valid period.
     *
     * @param  form  PersonForm
     * @param  errors  Errors
     */
    protected void validatePeriod(PersonForm form, Errors errors) {

        try {
            DateMidnight from = new DateMidnight(Integer.parseInt(form.getYear()),
                    Integer.parseInt(form.getMonthFrom()), Integer.parseInt(form.getDayFrom()));

            DateMidnight to = new DateMidnight(Integer.parseInt(form.getYear()), Integer.parseInt(form.getMonthTo()),
                    Integer.parseInt(form.getDayTo()));

            if (!from.isBefore(to)) {
                errors.reject("error.period");
            }
        } catch (IllegalFieldValueException ex) {
            errors.reject("error.period");
        }
    }


    /**
     * This method validates if mandatory field annual vacation days is filled and if the entry is valid.
     *
     * @param  form
     * @param  errors
     * @param  locale
     */
    protected void validateAnnualVacation(PersonForm form, Errors errors, Locale locale) {

        Settings settings = settingsService.getSettings();

        if (StringUtils.hasText(form.getAnnualVacationDays())) {
            try {
                validateNumberOfDays(NumberUtil.parseNumber(form.getAnnualVacationDays(), locale), ANNUAL_VACATION_DAYS,
                    BigDecimal.valueOf(settings.getMaximumAnnualVacationDays()), errors);
            } catch (NumberFormatException ex) {
                errors.rejectValue(ANNUAL_VACATION_DAYS, ERROR_ENTRY);
            }
        } else {
            errors.rejectValue(ANNUAL_VACATION_DAYS, MANDATORY_FIELD);
        }
    }


    /**
     * This method gets the property value for maximal number of days and notifies Tool-Manager if necessary (false
     * property value) or validate the number of entitlement's remaining vacation days with method validateNumberOfDays.
     *
     * @param  form
     * @param  errors
     * @param  locale
     */
    protected void validateRemainingVacationDays(PersonForm form, Errors errors, Locale locale) {

        Settings settings = settingsService.getSettings();

        if (StringUtils.hasText(form.getRemainingVacationDays())) {
            try {
                // field entitlement's remaining vacation days
                validateNumberOfDays(NumberUtil.parseNumber(form.getRemainingVacationDays(), locale),
                    REMAINING_VACATION_DAYS, BigDecimal.valueOf(settings.getMaximumAnnualVacationDays()), errors);
            } catch (NumberFormatException ex) {
                errors.rejectValue(REMAINING_VACATION_DAYS, ERROR_ENTRY);
            }
        } else {
            errors.rejectValue(REMAINING_VACATION_DAYS, MANDATORY_FIELD);
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

        // is field filled?
        if (days == null) {
            if (errors.getFieldErrors(field).isEmpty()) {
                errors.rejectValue(field, MANDATORY_FIELD);
            }
        } else {
            // is number of days < 0 ?
            if (days.compareTo(BigDecimal.ZERO) == -1) {
                errors.rejectValue(field, ERROR_ENTRY);
            }

            // is number of days unrealistic?
            if (days.compareTo(maximumDays) == 1) {
                errors.rejectValue(field, ERROR_ENTRY);
            }
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
            errors.rejectValue(PERMISSIONS, "role.error.least");
        } else {
            // if role inactive set, then only this role may be selected
            // else this is an error

            boolean roleInactiveSet = false;

            for (Role r : roles) {
                if (r == Role.INACTIVE) {
                    roleInactiveSet = true;
                }
            }

            if (roleInactiveSet) {
                // validate that there is only role inactive set
                // this means size of role collection must have size 1
                if (roles.size() != 1) {
                    errors.rejectValue(PERMISSIONS, "role.error.inactive");
                }
            }
        }
    }


    protected void validateNotifications(PersonForm personForm, Errors errors) {

        List<Role> roles = personForm.getPermissions();
        List<MailNotification> notifications = personForm.getNotifications();

        if (roles != null) {
            boolean bossNotificationsSelectedButNotBossRole = notifications.contains(MailNotification.NOTIFICATION_BOSS)
                && !roles.contains(Role.BOSS);
            boolean officeNotificationsSelectedButNotOfficeRole = notifications.contains(
                    MailNotification.NOTIFICATION_OFFICE) && !roles.contains(Role.OFFICE);

            if (bossNotificationsSelectedButNotBossRole || officeNotificationsSelectedButNotOfficeRole) {
                errors.rejectValue("notifications", "notification.error");
            }
        }
    }
}
